package com.newshub.aem.core.services.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.newshub.aem.core.constants.NewConstants;
import org.apache.commons.lang3.StringUtils;

public class NewsServiceHelper {

    public static boolean isValidCategory(String category) {
        return StringUtils.isNotBlank(category)
                && NewConstants.VALID_CATEGORIES.contains(category.toLowerCase());
    }

    public static boolean isValidCountry(String country) {
        return StringUtils.isNotBlank(country)
                && NewConstants.VALID_COUNTRIES.contains(country.toLowerCase());
    }

    public static boolean isValidLanguage(String language) {
        return StringUtils.isNotBlank(language)
                && NewConstants.VALID_LANGUAGES.contains(language.toLowerCase());
    }

    public static boolean isValidSortBy(String sortBy) {
        return StringUtils.isNotBlank(sortBy)
                && NewConstants.VALID_SORT_OPTIONS.contains(sortBy.toLowerCase());
    }

    public static boolean isValidDate(String date) {
        return StringUtils.isNotBlank(date) && date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    public static int validatePageSize(int pageSize, int defaultPageSize ){
        if (pageSize < 1) return defaultPageSize;
        if (pageSize > 100) return 100;
        return pageSize;
    }

    public static int validatePage(int page) {
        return Math.max(1, page);
    }

    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    public List<String> getValidCategories() {
        return NewConstants.VALID_CATEGORIES;
    }

    public List<String> getValidCountries() {
        return NewConstants.VALID_COUNTRIES;
    }

    public List<String> getValidLanguages() {
        return NewConstants.VALID_LANGUAGES;
    }

    public List<String> getValidSortOptions() {
        return NewConstants.VALID_SORT_OPTIONS;
    }
}