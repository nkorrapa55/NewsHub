package com.newshub.aem.core.configurations;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

    @ObjectClassDefinition(name = "API Client Configuration")
    public @interface ApiClientConfig {

        @AttributeDefinition(name = "Connection Timeout (ms)")
        int connectionTimeout() default 5000;

        @AttributeDefinition(name = "Socket Timeout (ms)")
        int socketTimeout() default 10000;

        @AttributeDefinition(name = "Connection Request Timeout (ms)")
        int connectionRequestTimeout() default 3000;

        @AttributeDefinition(name = "Max Total Connections")
        int maxTotalConnections() default 200;

        @AttributeDefinition(name = "Max Connections Per Route")
        int maxConnectionsPerRoute() default 50;
    }
