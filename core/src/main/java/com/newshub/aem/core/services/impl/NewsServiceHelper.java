package com.newshub.aem.core.services.impl;

import com.newshub.aem.core.constants.NewConstants;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

public final class NewsServiceHelper {

    // Precompiled regex (better performance)
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    // Prevent instantiation
    private NewsServiceHelper() {}


    // VALIDATIONS
    public static boolean isValidCategory(String category) {
        String normalized = normalize(category);
        return normalized != null && NewConstants.VALID_CATEGORIES.contains(normalized);
    }

    public static boolean isValidCountry(String country) {
        String normalized = normalize(country);
        return normalized != null && NewConstants.VALID_COUNTRIES.contains(normalized);
    }

    public static boolean isValidLanguage(String language) {
        String normalized = normalize(language);
        return normalized != null && NewConstants.VALID_LANGUAGES.contains(normalized);
    }

    public static boolean isValidSortBy(String sortBy) {
        String normalized = normalize(sortBy);
        return normalized != null && NewConstants.VALID_SORT_OPTIONS.contains(normalized);
    }

    public static boolean isValidDate(String date) {
        return StringUtils.isNotBlank(date) && DATE_PATTERN.matcher(date).matches();
    }

    // PAGINATION
    public static int validatePageSize(int pageSize, int defaultPageSize) {
        if (pageSize < 1) return defaultPageSize;
        return Math.min(pageSize, 100);
    }

    public static int validatePage(int page) {
        return Math.max(1, page);
    }

    // ENCODING
    public static String encode(String value) {
        return StringUtils.isBlank(value) ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // NORMALIZATION
    private static String normalize(String value) {
        return StringUtils.isBlank(value) ? null : value.toLowerCase();
    }

    // CONSTANT ACCESS
    public static List<String> getValidCategories() {
        return NewConstants.VALID_CATEGORIES;
    }

    public static List<String> getValidCountries() {
        return NewConstants.VALID_COUNTRIES;
    }

    public static List<String> getValidLanguages() {
        return NewConstants.VALID_LANGUAGES;
    }

    public static List<String> getValidSortOptions() {
        return NewConstants.VALID_SORT_OPTIONS;
    }
}