package com.newshub.aem.core.services;

import java.util.Map;

public interface ApiClientService {
    /**
     * Execute HTTP Get request
     * @param url Full URL with query params
     * @param headers Request headers (can be null)
     * @return Response body as String
     */
    String get(String url, Map<String,String> headers);

    /**
     * Execute HTTP POST request
     * @param url Endpoint URL
     * @param headers Request headers (can be null)
     * @param jsonBody Request body as JSON string
     * @return Response body as String
     */
    String post(String url, Map<String, String> headers, String jsonBody);

    /**
     * Execute HTTP PUT request
     */
    String put(String url, Map<String, String> headers, String jsonBody);

    /**
     * Execute HTTP DELETE request
     */
    String delete(String url, Map<String, String> headers);

}