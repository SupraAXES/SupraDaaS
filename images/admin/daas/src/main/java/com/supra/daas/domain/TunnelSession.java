package com.supra.daas.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.supra.daas.util.EmptyUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class TunnelSession {
    
    private String id;

    private String host;

    private int port;

    private String protocol;

    private String username;

    private String password;

    private String priKey;

    private Map<String, String> query;

    public TunnelSession(SessionInfo session) {
        this.id = session.getUuid();
       
        String access = session.getAccess();
        ResourceInfo resource = session.getResource();

        this.host = "dvm-" + resource.getId();
        if (EmptyUtils.isEmpty(access)) {
            List<AccessInfo> accessList = resource.getAccess();

            if (accessList != null && accessList.size() > 0) {
                for (AccessInfo item : accessList) {
                    if (EmptyUtils.isNotEmpty(item.getDef())) {
                        this.protocol = item.getPl();
                        if (EmptyUtils.isNotEmpty(item.getPort())) {
                            this.port = Integer.parseInt(item.getPort());
                        }
                        break;
                    }
                }
            }
        } else {
            String[] arr = access.split(":");
            this.protocol = arr[0];
            if (arr.length > 1) {
                this.port = Integer.parseInt(arr[1]);
            }
        }

        AccountInfo account = session.getAutoAccount();
        if (account != null) {
            this.username = account.getUser();
            this.password = account.getPass();
            this.priKey = account.getPri();
        } else {
            List<AccountInfo> accouts = resource.getAutoAccounts();
            if (EmptyUtils.isNotEmpty(accouts)) {
                for (AccountInfo item : accouts) {
                    if (EmptyUtils.isNotEmpty(item.getDef())) {
                        this.username = item.getUser();
                        this.password = item.getPass();
                        break;
                    }
                }
            }
        }

        if ("guest_vnc".equals(protocol)) {
            this.protocol = "vnc";
            this.port = 5999;
            this.username = "supra";
            this.password = "supra";
        } else if (EmptyUtils.isNotEmpty(protocol) && this.port <= 0) {
            if ("ssh".equals(protocol)) {
                this.port = 22;
            } else if ("vnc".equals(protocol)) {
                this.port = 5900;
            } else {
                this.port = 3389;
            }
        }

        Map<String, String> query = new HashMap<>();

        query.put("drive-name", "MyFiles");
        if (EmptyUtils.isNotEmpty(session.getPath())) {
            query.put("drive-path", session.getPath().replace(
                "/opt/supra/data/files", "/opt/supra/data/user_data"));
        }

        log.info("guaca username = " + this.username);
        log.info("guaca password = " + this.password);
        if (EmptyUtils.isNotEmpty(this.username)) {
            query.put("username", this.username);
        }
        if (EmptyUtils.isNotEmpty(this.priKey)) {
            query.put("private-key", this.priKey);
            if (EmptyUtils.isNotEmpty(this.password)) {
                query.put("passphrase", this.password);
            }
        }
        else if (EmptyUtils.isNotEmpty(this.password)) {
            query.put("password", this.password);
        }

        if ("vnc".equals(protocol)) {
            query.put("audio-servername", this.host);
        }

        ResourceRule rule = session.getRule();
        if (rule != null) {
            if (EmptyUtils.isNotEmpty(rule.getAudioOut())) {
                if ("rdp".equals(protocol)) {
                    query.put("disable-audio", "false");
                } else if ("ssh".equals(protocol)) {
                } else {
                    query.put("enable-audio", "true");
                }
            }
            if (EmptyUtils.isNotEmpty(rule.getAudioIn())) {
                query.put("enable-audio-input", "true");
            }
            if (EmptyUtils.isEmpty(rule.getCopy())) {
                query.put("disable-copy", "true");
            }
            if (EmptyUtils.isEmpty(rule.getPaste())) {
                query.put("disable-paste", "true");
            }
            if (EmptyUtils.isEmpty(rule.getUpload())) {
                query.put("disable-upload", "true");
            }
            if (EmptyUtils.isEmpty(rule.getDownload())) {
                query.put("disable-download", "true");
            }
        }

        this.query = query;
    }
}
