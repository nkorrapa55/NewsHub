package com.newshub.aem.core.services;

import java.util.List;

public interface NewsService {

    String getTopHeadlines(String country, String category, String query, int pageSize, int page);
    String getTopHeadlinesBySources(String sources, String query, int pageSize, int page);
    String searchArticles(String query, String language, String sortBy, String fromDate, String toDate, String domains, String excludeDomains, int pageSize, int page);
    String getSources(String category, String language, String country);

}
