package com.newshub.aem.core.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NewConstants {
    //Categories
    public static final List<String> VALID_CATEGORIES = Collections.unmodifiableList(
            Arrays.asList(
                    "general",
                    "business",
                    "entertainment",
                    "health",
                    "science",
                    "sports",
                    "technology"
            )
    );
    // COUNTRIES
    public static final List<String> VALID_COUNTRIES = Collections.unmodifiableList(
            Arrays.asList(
                    "ae", "ar", "at", "au", "be", "bg", "br", "ca", "ch", "cn",
                    "co", "cu", "cz", "de", "eg", "fr", "gb", "gr", "hk", "hu",
                    "id", "ie", "il", "in", "it", "jp", "kr", "lt", "lv", "ma",
                    "mx", "my", "ng", "nl", "no", "nz", "ph", "pl", "pt", "ro",
                    "rs", "ru", "sa", "se", "sg", "si", "sk", "th", "tr", "tw",
                    "ua", "us", "ve", "za"
            )
    );
    // LANGUAGES
    public static final List<String> VALID_LANGUAGES = Collections.unmodifiableList(
            Arrays.asList(
                    "ar", "de", "en", "es", "fr", "he", "it",
                    "nl", "no", "pt", "ru", "sv", "ud", "zh"
            )
    );
    // SORT OPTIONS
    public static final List<String> VALID_SORT_OPTIONS = Collections.unmodifiableList(
            Arrays.asList(
                    "relevancy",
                    "popularity",
                    "publishedAt"
            )
    );

}
