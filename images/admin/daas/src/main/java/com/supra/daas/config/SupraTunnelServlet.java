package com.supra.daas.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.supra.daas.domain.SessionInfo;
import com.supra.daas.domain.TunnelSession;
import com.supra.daas.service.impl.SessionServiceImpl;
import com.supra.daas.util.EmptyUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleClientInformation;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.websocket.GuacamoleWebSocketTunnelEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/tunnel", subprotocols = "guacamole")
@Component
public class SupraTunnelServlet extends GuacamoleWebSocketTunnelEndpoint {

    private static DateFormat dateFormat = null;

    private final Logger logger = LoggerFactory.getLogger("SessionLogger");
 
    private static final String DPI_PARAMETER = "DPI"; // GUAC_DPI
    private static final String WIDTH_PARAMETER = "WIDTH"; // GUAC_WIDTH
    private static final String HEIGHT_PARAMETER = "HEIGHT"; // GUAC_HEIGHT
    private static final String TIMEZONE_PARAMETER = "TIMEZONE"; // GUAC_TIMEZONE

    private static Map<String, Set<GuacamoleTunnel>> tunnelMap = new ConcurrentHashMap<>();

    public static void closeSession(String session) {
        if (session == null || session.length() == 0) {
            return;
        }

        Set<GuacamoleTunnel> set = tunnelMap.get(session);
        if (set != null) {
            Set<GuacamoleTunnel> tmp = new HashSet<>(set);
            for (GuacamoleTunnel tunnel : tmp) {
                try {
                    tunnel.close();
                } catch (Exception e) {
                }
            }
            tunnelMap.remove(session);
        }
    }

    private static void addSessionTunnel(String session, GuacamoleTunnel tunnel) {
        if (session == null || session.length() == 0) {
            return;
        }
        Set<GuacamoleTunnel> set = tunnelMap.get(session);
        if (set == null) {
            set = new HashSet<>();
            tunnelMap.put(session, set);
        }
        set.add(tunnel);
    }

    private static void removeSessionTunnel(String session, GuacamoleTunnel tunnel) {
        if (session == null || session.length() == 0) {
            return;
        }

        Set<GuacamoleTunnel> set = tunnelMap.get(session);
        if (set != null) {
            set.remove(tunnel);

            if (set.size() == 0) {
                tunnelMap.remove(session);
            }
        }
    }

    @Override
    protected GuacamoleTunnel createTunnel(Session session, EndpointConfig endpointConfig) throws GuacamoleException {
        log.info("Create tunnel: " + getTime());

        Map<String, List<String>> params = session.getRequestParameterMap();

        GuacamoleConfiguration config = new GuacamoleConfiguration();
        GuacamoleClientInformation info = new GuacamoleClientInformation();

        Map<String, String> configParams = new HashMap<>();

        info.getAudioMimetypes().add("audio/L8");
        info.getAudioMimetypes().add("audio/L16");
        info.getImageMimetypes().add("mage/jpeg");
        info.getImageMimetypes().add("mage/png");
        info.getImageMimetypes().add("mage/webp");
        info.setOptimalResolution(getIntParameter(params, DPI_PARAMETER));
        info.setOptimalScreenWidth(getIntParameter(params, WIDTH_PARAMETER));
        info.setOptimalScreenHeight(getIntParameter(params, HEIGHT_PARAMETER));

        List<String> timezone = params.get(TIMEZONE_PARAMETER);
        if (timezone != null && timezone.size() > 0) {
            info.setTimezone(timezone.get(0));
        } else {
            info.setTimezone("Asia/Shanghai");
        }

        final String sessionId = params.get("id").get(0);
        SessionInfo sessionInfo = SessionServiceImpl.getInstance().getSession(sessionId);
        if (sessionInfo == null) {
            log.info("Tunnel Session not found: " + sessionId);
            throw new GuacamoleException("Session not found");
        }

        log.info("Tunnel Session found: " + JSON.toJSONString(sessionInfo));
        TunnelSession tunnelSession = new TunnelSession(sessionInfo);

        int port = tunnelSession.getPort();
        String hostname = tunnelSession.getHost();

        config.setProtocol(tunnelSession.getProtocol());

        addParameter(config, configParams, "hostname", hostname);
        if (port > 0) {
            addParameter(config, configParams, "port", String.valueOf(port));
        }

        String rescAgent = System.getenv().get("GUACD_HOSTNAME");
        if (EmptyUtils.isEmpty(rescAgent)) {
            rescAgent = "supra-agent";
        }

        Map<String, String> query = tunnelSession.getQuery();
        if (query != null && query.size() > 0) {
            for (Map.Entry<String, String> param : query.entrySet()) {
                
                String key = param.getKey();
                String value = param.getValue();

                addParameter(config, configParams, key, value);
            }
        }

        JSONObject localParam = readLocalParams(tunnelSession.getProtocol());
        if (localParam != null) {
            for (String item : localParam.keySet()) {
                if (! configParams.containsKey(item)) {
                    String value = localParam.getString(item);
                    config.setParameter(item, value);
                }
            }
        }

        // Connect to guacd, proxying a connection to the VNC server above
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket(rescAgent, 4822),
                config, info
        );

        // Create tunnel from now-configured socket
        GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket) {
            @Override
            public void close() throws GuacamoleException {
                super.close();
                removeSessionTunnel(sessionId, this);

                log.info("Close tunnel: " + getTime());
            }
        };

        addSessionTunnel(sessionId, tunnel);
        log.info("Add tunnel: " + getTime());

        return tunnel;
    }

    private int getIntParameter(Map<String, List<String>> params, String key) {
        List<String> values = params.get(key);
        if (values != null && values.size() > 0) {
            try {
                String value = values.get(0);
                if (value.endsWith("?undefined")) {
                    value = value.substring(0, value.length() - "?undefined".length());
                }
                return Integer.parseInt(value);
            } catch (Exception e) {
            }
        }

        return 0;
    }

    private void addParameter(GuacamoleConfiguration config, Map<String, String> params,
                              String key, String value) {
        log.info("tunnel Add parameter: " + key + " = " + value + "-END");
        
        params.put(key, value);
        config.setParameter(key, value);
    }

    private JSONObject readLocalParams(String scheme) {
        String content = "";
        File file = new File("/opt/supra/config/guacd_settings.json");
        if (!file.exists()) {
            try {
                Resource resource = new ClassPathResource("default_settings.json");
                InputStream is = resource.getInputStream();
                byte[] data = readContent(is); 
                content = new String(data, "utf-8");   
            } catch (Exception e) {
            }            
        } else {
            try {
                byte[] data = readContent(new FileInputStream(file));
                content = new String(data, "utf-8");
            } catch (Exception e) {
            }
        }
        try {
            return JSONObject.parseObject(content)
                    .getJSONObject("default-settings")
                    .getJSONObject(scheme);
        } catch (Exception e) {
        }

        return null;
    }

    private static byte[] readContent(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] data = new byte[1024];
        int count = 0;
        while ((count = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, count);
        }

        is.close();
        bos.close();

        return bos.toByteArray();
    }

    private static String getTime() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        return dateFormat.format(Calendar.getInstance().getTime());
    }

}
