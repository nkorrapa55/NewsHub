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

        log.info("HTTP Client initialized with pooling");
    }

    @Deactivate
    protected void deactivate() {
        closeQuietly(httpClient);
        if (connectionManager != null) {
            connectionManager.close();
        }
    }

    @Override
    public String get(String url, Map<String, String> headers) {
        return execute(new HttpGet(url), headers, null);
    }

    @Override
    public String post(String url, Map<String, String> headers, String jsonBody) {
        HttpPost request = new HttpPost(url);
        return execute(request, headers, jsonBody);
    }

    @Override
    public String put(String url, Map<String, String> headers, String jsonBody) {
        HttpPut request = new HttpPut(url);
        return execute(request, headers, jsonBody);
    }

    @Override
    public String delete(String url, Map<String, String> headers) {
        return execute(new HttpDelete(url), headers, null);
    }

    // ==========================
    // CORE EXECUTION METHOD
    // ==========================
    private String execute(HttpRequestBase request, Map<String, String> headers, String body) {

        applyHeaders(request, headers);

        if (request instanceof HttpEntityEnclosingRequestBase && body != null) {
            ((HttpEntityEnclosingRequestBase) request)
                    .setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            request.setHeader("Content-Type", "application/json");
        }

        log.debug("Executing {} -> {}", request.getMethod(), request.getURI());

        try (CloseableHttpResponse response = httpClient.execute(request)) {

            int status = response.getStatusLine().getStatusCode();
            String responseBody = getResponseBody(response.getEntity());

            if (isSuccess(status)) {
                return responseBody;
            }

            log.warn("HTTP {} failed for {} | Status: {}", request.getMethod(), request.getURI(), status);
            return buildErrorResponse(status, responseBody);

        } catch (Exception e) {
            log.error("HTTP call failed: {}", request.getURI(), e);
            return buildErrorResponse(500, e.getMessage());
        }
    }

    // ==========================
    // HELPERS
    // ==========================

    private boolean isSuccess(int status) {
        return status >= 200 && status < 300;
    }

    private String getResponseBody(HttpEntity entity) throws IOException {
        return entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : "";
    }

    private void applyHeaders(HttpRequestBase request, Map<String, String> headers) {
        request.setHeader("Accept", "application/json");

        if (headers != null) {
            headers.forEach(request::setHeader);
        }
    }

    private void closeQuietly(CloseableHttpClient client) {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                log.error("Error closing HTTP client", e);
            }
        }
    }

    private String buildErrorResponse(int statusCode, String message) {
        return String.format(
                "{\"status\":\"error\",\"statusCode\":%d,\"message\":\"%s\"}",
                statusCode,
                sanitize(message)
        );
    }

    private String sanitize(String msg) {
        return msg != null ? msg.replace("\"", "\\\"") : "Unknown error";
    }
}