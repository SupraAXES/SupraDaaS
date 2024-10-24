package com.supra.daas.util;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtil {;

    private static String executeRequest(HttpUriRequest request) {
        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(
                null, (chain, authType) -> true).build();

            CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
            CloseableHttpResponse response = httpClient.execute(request);

            int code = response.getStatusLine().getStatusCode();
            log.info("VM Request success: " + code);
            if (code == 200 || code == 201) {
                HttpEntity entity = response.getEntity();
                String body = EntityUtils.toString(entity);
                log.info("VM Response body: " + body);

                return body;
            }
        } catch (Exception e) {
            log.info("VM Request failed: " + e.getMessage());
        }

        return null;
    }
    
    public static String postJsonParams(String url, String param) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json");
        // RequestConfig requestConfig = RequestConfig.custom()
        //         .setConnectTimeout(5000)
        //         .setConnectionRequestTimeout(5000)
        //         .setSocketTimeout(5000)
        //         .build();
        // httpPost.setConfig(requestConfig);

        try {
            httpPost.setEntity(new StringEntity(param));   
        } catch (Exception e) {
        }

        return executeRequest(httpPost);
    }

}
