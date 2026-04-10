package com.newshub.aem.core.services.impl;

import com.newshub.aem.core.configurations.NewsApiConfig;
import com.newshub.aem.core.services.ApiClientService;
import com.newshub.aem.core.services.NewsService;
import com.newshub.aem.core.utils.UrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Designate(ocd = NewsApiConfig.class)
@Component(service = NewsService.class)
public class NewsServiceImpl implements NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsServiceImpl.class);

    @Reference
    private ApiClientService apiClientService;

    private String baseUrl;
    private String apiKey;
    private int defaultPageSize;
    private String topHeadlines;
    private String country;
    private String sourcesEndpoint;
    private String everythingEndpoint;

    @Activate
    protected void activate(NewsApiConfig config) {
        this.baseUrl = config.apiUrl();
        this.apiKey = config.apiKey();
        this.country = config.country();
        this.sourcesEndpoint = config.sourcesEndpoint();
        this.everythingEndpoint = config.everythingEndpoint();
        this.defaultPageSize = config.defaultPageSize();
        this.topHeadlines = config.topHeadlines();
        log.info("NewsService activated - BaseURL: {}, DefaultPageSize: {}",
                baseUrl, defaultPageSize);
    }

    @Override
    public String getTopHeadlines(String country, String category, String query, int pageSize, int page) {
        log.debug("getTopHeadlines - country: {}, category: {}, query: {}",
                country, category, query);
        // Validate country - required for top headlines
        if (!NewsServiceHelper.isValidCountry(country)) {
            return errorResponse("Valid country code is required for headlines");
        }
        UrlBuilder urlBuilder = new UrlBuilder(baseUrl + topHeadlines)
                .addParam("country", country.toLowerCase())
                .addParam("pageSize", NewsServiceHelper.validatePageSize(pageSize,defaultPageSize))
                .addParam("page", NewsServiceHelper.validatePage(page));
        // Add category if valid
        if (NewsServiceHelper.isValidCategory(category)) {
            urlBuilder.addParam("category", category.toLowerCase());
        }
        // Add search query if provided
        if (StringUtils.isNotBlank(query)) {
            urlBuilder.addParam("q", NewsServiceHelper.encode(query));
        }
        return executeHTTPGet(urlBuilder.build());
    }

    @Override
    public String getSources(String category, String language, String country) {
        log.debug("getSources - category: {}, language: {}, country: {}",
                category, language, country);
        UrlBuilder urlBuilder = new UrlBuilder(baseUrl + sourcesEndpoint);
        // Add filters only if valid
        if (NewsServiceHelper.isValidCategory(category)) {
            urlBuilder.addParam("category", category.toLowerCase());
        }
        if (NewsServiceHelper.isValidLanguage(language)) {
            urlBuilder.addParam("language", language.toLowerCase());
        }
        if (NewsServiceHelper.isValidCountry(country)) {
            urlBuilder.addParam("country", country.toLowerCase());
        }
        return executeHTTPGet(urlBuilder.build());
    }

    @Override
    public String getTopHeadlinesBySources(String sources, String query, int pageSize, int page) {
        return "";
    }

    @Override
    public String searchArticles(String query, String language, String sortBy, String fromDate, String toDate, String domains, String excludeDomains, int pageSize, int page) {
        log.debug("searchArticles - query: {}, language: {}, sortBy: {}",
                query, language, sortBy);
        // Query is required for /everything endpoint
        if (StringUtils.isBlank(query)) {
            return errorResponse("Query parameter (q) is required for search");
        }
        UrlBuilder urlBuilder = new UrlBuilder(baseUrl + everythingEndpoint)
                .addParam("q", NewsServiceHelper.encode(query))
                .addParam("pageSize", NewsServiceHelper.validatePageSize(pageSize, defaultPageSize))
                .addParam("page", NewsServiceHelper.validatePage(page));
        // Add language if valid
        if (NewsServiceHelper.isValidLanguage(language)) {
            urlBuilder.addParam("language", language.toLowerCase());
        }
        // Add sort option if valid
        if (NewsServiceHelper.isValidSortBy(sortBy)) {
            urlBuilder.addParam("sortBy", sortBy.toLowerCase());
        }
        // Add date range if valid
        if (NewsServiceHelper.isValidDate(fromDate)) {
            urlBuilder.addParam("from", fromDate);
        }
        if (NewsServiceHelper.isValidDate(toDate)) {
            urlBuilder.addParam("to", toDate);
        }
        // Add domain filters
        if (StringUtils.isNotBlank(domains)) {
            urlBuilder.addParam("domains", domains.toLowerCase());
        }
        if (StringUtils.isNotBlank(excludeDomains)) {
            urlBuilder.addParam("excludeDomains", excludeDomains.toLowerCase());
        }
        return executeHTTPGet(urlBuilder.build());
    }

    private String buildUrl(String category) {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?country=").append(country);
        if(StringUtils.isNotBlank(category) && !"general".equals(category)){
            url.append("&category=").append(category);
        }
        return url.toString();
    }

    private String errorResponse(String message){
        return String.format("{\"status\":\"error\",\"message\":\"%s\"}",
                message.replace("\"", "\\\""));
    }

    private String executeHTTPGet(String url){
        if (StringUtils.isBlank(apiKey)) {
            log.error("API Key is missing!");
            return errorResponse("API Key not configured");
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Api-Key", apiKey);
        log.info("Final API URL: {}", url); // ✅ ADD THIS
        return apiClientService.get(url, headers);

    }

}
