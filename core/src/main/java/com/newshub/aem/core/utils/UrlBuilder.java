package com.newshub.aem.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlBuilder {
    private final StringBuilder url;
    private boolean hasParams = false;

    public UrlBuilder(String baseUrl) {
        this.url = new StringBuilder(baseUrl);
    }

    public UrlBuilder addParam(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            appendSeparator();
            url.append(key)
                    .append("=")
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
        return this;
    }
    public UrlBuilder addParam(String key, int value) {
        appendSeparator();
        url.append(key).append("=").append(value);
        return this;
    }
    private void appendSeparator() {
        url.append(hasParams ? "&" : "?");
        hasParams = true;
    }
    public String build() {
        return url.toString();
    }
}
