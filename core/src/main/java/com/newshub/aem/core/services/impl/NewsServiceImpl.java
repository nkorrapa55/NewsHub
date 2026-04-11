package com.newshub.aem.core.services.impl;

import com.newshub.aem.core.configurations.NewsApiConfig;
import com.newshub.aem.core.services.ApiClientService;
import com.newshub.aem.core.services.NewsService;
import com.newshub.aem.core.utils.UrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Component(service = NewsService.class)
@Designate(ocd = NewsApiConfig.class)
public class NewsServiceImpl implements NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsServiceImpl.class);

    @Reference
    private ApiClientService apiClientService;

    private String baseUrl;
    private String apiKey;
    private int defaultPageSize;
    private String topHeadlines;
    private String sourcesEndpoint;
    private String everythingEndpoint;

    // ==========================
    // ACTIVATE
    // ==========================
    @Activate
    protected void activate(NewsApiConfig config) {
        this.baseUrl = config.apiUrl();
        this.apiKey = config.apiKey();
        this.topHeadlines = config.topHeadlines();
        this.sourcesEndpoint = config.sourcesEndpoint();
        this.everythingEndpoint = config.everythingEndpoint();
        this.defaultPageSize = config.defaultPageSize();

        log.info("NewsService activated - BaseURL: {}", baseUrl);
    }

    // ==========================
    // HEADLINES
    // ==========================
    @Override
    public String getTopHeadlines(String country, String category, String query, int pageSize, int page) {

        if (!NewsServiceHelper.isValidCountry(country)) {
            return errorResponse("Valid country code is required");
        }

        UrlBuilder url = baseBuilder(topHeadlines)
                .addParam("country", country.toLowerCase());

        addPagination(url, pageSize, page);
        addIfValid(url, "category", category, NewsServiceHelper::isValidCategory);
        addIfNotBlank(url, "q", query, true);

        return execute(url);
    }

    // ==========================
    // SOURCES
    // ==========================
    @Override
    public String getSources(String category, String language, String country) {

        UrlBuilder url = baseBuilder(sourcesEndpoint);

        addIfValid(url, "category", category, NewsServiceHelper::isValidCategory);
        addIfValid(url, "language", language, NewsServiceHelper::isValidLanguage);
        addIfValid(url, "country", country, NewsServiceHelper::isValidCountry);

        return execute(url);
    }

    // ==========================
    // SEARCH
    // ==========================
    @Override
    public String searchArticles(String query, String language, String sortBy,
                                 String fromDate, String toDate,
                                 String domains, String excludeDomains,
                                 int pageSize, int page) {

        if (StringUtils.isBlank(query)) {
            return errorResponse("Query (q) is required");
        }

        UrlBuilder url = baseBuilder(everythingEndpoint)
                .addParam("q", NewsServiceHelper.encode(query));

        addPagination(url, pageSize, page);

        addIfValid(url, "language", language, NewsServiceHelper::isValidLanguage);
        addIfValid(url, "sortBy", sortBy, NewsServiceHelper::isValidSortBy);
        addIfValid(url, "from", fromDate, NewsServiceHelper::isValidDate);
        addIfValid(url, "to", toDate, NewsServiceHelper::isValidDate);

        addIfNotBlank(url, "domains", domains, false);
        addIfNotBlank(url, "excludeDomains", excludeDomains, false);

        return execute(url);
    }

    // ==========================
    // HEADLINES BY SOURCES
    // ==========================
    @Override
    public String getTopHeadlinesBySources(String sources, String query, int pageSize, int page) {

        if (StringUtils.isBlank(sources)) {
            return errorResponse("Sources are required");
        }

        UrlBuilder url = baseBuilder(topHeadlines)
                .addParam("sources", sources);

        addPagination(url, pageSize, page);
        addIfNotBlank(url, "q", query, true);

        return execute(url);
    }

    // ==========================
    // COMMON HELPERS
    // ==========================

    private UrlBuilder baseBuilder(String endpoint) {
        return new UrlBuilder(baseUrl + endpoint);
    }

    private void addPagination(UrlBuilder url, int pageSize, int page) {
        url.addParam("pageSize", NewsServiceHelper.validatePageSize(pageSize, defaultPageSize));
        url.addParam("page", NewsServiceHelper.validatePage(page));
    }

    private void addIfValid(UrlBuilder url, String key, String value, Predicate<String> validator) {
        if (validator.test(value)) {
            url.addParam(key, value.toLowerCase());
        }
    }

    private void addIfNotBlank(UrlBuilder url, String key, String value, boolean encode) {
        if (StringUtils.isNotBlank(value)) {
            url.addParam(key, encode ? NewsServiceHelper.encode(value) : value.toLowerCase());
        }
    }

    // ==========================
    // EXECUTION
    // ==========================
    private String execute(UrlBuilder urlBuilder) {

        if (StringUtils.isBlank(apiKey)) {
            log.error("API Key missing");
            return errorResponse("API Key not configured");
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Api-Key", apiKey);

        String finalUrl = urlBuilder.build();
        log.info("Calling API: {}", finalUrl);

        String response = apiClientService.get(finalUrl, headers);

        // If downstream error, pass through
        if (response != null && response.contains("\"status\":\"error\"")) {
            log.warn("Downstream API error: {}", response);
            return response;
        }

        return wrapSuccess(response);
    }

    // ==========================
    // RESPONSE HANDLING
    // ==========================
    private String wrapSuccess(String data) {
        return String.format(
                "{\"status\":\"success\",\"data\":%s}",
                data != null ? data : "{}"
        );
    }

    private String errorResponse(String message) {
        return String.format(
                "{\"status\":\"error\",\"message\":\"%s\"}",
                message.replace("\"", "\\\"")
        );
    }
}