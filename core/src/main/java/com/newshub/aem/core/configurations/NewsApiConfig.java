package com.newshub.aem.core.configurations;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "NewsHub API Config")
public @interface NewsApiConfig {

    @AttributeDefinition(name = "API URL", description = "Endpoint for fetching news")
    String apiUrl() default "https://newsapi.org/v2";

    @AttributeDefinition(
            name = "Top Headlines Endpoint",
            description = "Top Headlines Endpoint"
    )
    String topHeadlines() default "/top-headlines";

    @AttributeDefinition(
            name = "Everything Endpoint",
            description = "Everything Endpoint"
    )
    String everythingEndpoint() default "/everything";

    @AttributeDefinition(
            name = "Sources Endpoint",
            description = "Sources Endpoint"
    )
    String sourcesEndpoint() default "/top-headlines/sources";

    @AttributeDefinition(
            name = "API Key",
            description = "API Key for authentication"
    )
    String apiKey() default "30bbb9c9d2af4d489cfa25d07792e1e0";

    @AttributeDefinition(
            name = "Country",
            description = "Country for headlines"
    )
    String country() default "us";

    @AttributeDefinition(
            name = "Timeout (ms)",
            description = "Connection timeout"
    )
    int timeout() default 5000;

    @AttributeDefinition(
            name = "Default Page Size",
            description = "Default number of articles if not specified (max 100)"
    )
    int defaultPageSize() default 10;
}