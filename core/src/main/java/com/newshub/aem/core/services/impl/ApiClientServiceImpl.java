package com.newshub.aem.core.services.impl;

import com.newshub.aem.core.configurations.ApiClientConfig;
import com.newshub.aem.core.services.ApiClientService;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component(service = ApiClientService.class)
@Designate(ocd = ApiClientConfig.class)
public class ApiClientServiceImpl implements ApiClientService {

    private static final Logger log = LoggerFactory.getLogger(ApiClientServiceImpl.class);
    private CloseableHttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;

    @Activate
    @Modified
    protected void activate(ApiClientConfig config) {

        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(config.maxTotalConnections());
        connectionManager.setDefaultMaxPerRoute(config.maxConnectionsPerRoute());

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.connectionTimeout())
                .setSocketTimeout(config.socketTimeout())
                .setConnectionRequestTimeout(config.connectionRequestTimeout())
                .build();

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
    @Deactivate
    protected  void deactivate(){
        // Clean up resources
        if(httpClient !=null){
            try {
                httpClient.close();
                log.info("HTTP client closed");
            } catch (IOException e) {
                log.error("Error Closing HTTP client",e);
            }
        }
        if(connectionManager != null){
            connectionManager.close();
            log.info("Connection manager closed");
        }
    }

    @Override
    public String get(String url, Map<String, String> headers) {
        HttpGet request = new HttpGet(url);
        applyHeaders(request,headers);
        return executeRequest(request);
    }

    @Override
    public String post(String url, Map<String, String> headers, String jsonBody) {
        HttpPost request = new HttpPost(url);
        applyHeaders(request,headers);
        if(jsonBody != null){
            request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            request.setHeader("Content-Type","application/json");
        }
        return executeRequest(request);
    }

    @Override
    public String put(String url, Map<String, String> headers, String jsonBody) {
        HttpPut request = new HttpPut(url);
        applyHeaders(request, headers);

        if (jsonBody != null) {
            request.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
            request.setHeader("Content-Type", "application/json");
        }
        return executeRequest(request);
    }

    @Override
    public String delete(String url, Map<String, String> headers) {
        HttpDelete request = new HttpDelete(url);
        applyHeaders(request,headers);
        return executeRequest(request);
    }

    /**
     * Execute request and handle response
     */
    private String executeRequest(HttpRequestBase request) {
        log.debug("Executing {} request to: {}", request.getMethod(), request.getURI());
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8):"";
            if (statusCode >= 200 && statusCode < 300) {
                log.debug("Request successful - Status: {}", statusCode);
                return responseBody;
            } else {
                log.warn("Request failed - Status: {}, URL: {}", statusCode, request.getURI());
                return buildErrorResponse(statusCode, responseBody);
            }

        } catch (Exception e) {
            log.error("Request failed - URL: {}, Error: {}", request.getURI(), e.getMessage());
            return buildErrorResponse(-1, e.getMessage());
        }
    }

    private String buildErrorResponse(int statusCode, String message) {
        return String.format("{\"error\":true,\"statusCode\":%d,\"message\":\"%s\"}",
                statusCode,
                message.replace("\"", "\\\""));
    }

    /**
     * Apply custom headers to request
     */
    private void applyHeaders(HttpRequestBase request, Map<String, String> headers) {
        // Default headers
        request.setHeader("Accept", "application/json");
        // Apply custom headers
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(request::setHeader);
        }
    }
}
