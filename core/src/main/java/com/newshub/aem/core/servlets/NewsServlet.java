package com.newshub.aem.core.servlets;

import com.newshub.aem.core.services.NewsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;


@Component(service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/newshub/news",
                "sling.servlet.methods=GET"
        })
public class NewsServlet extends SlingSafeMethodsServlet {

    @Reference
    private NewsService newsService;

    private static final Logger log = LoggerFactory.getLogger(NewsServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Normalize inputs
            String category = sanitize(request.getParameter("category"), "general");
            String country = sanitize(request.getParameter("country"), "us");
            String action = StringUtils.defaultIfBlank(request.getParameter("action"), "headlines");
            String query = StringUtils.trimToNull(request.getParameter("q"));

            int pageSize = parseInt(request.getParameter("pageSize"), 10);
            int page = parseInt(request.getParameter("page"), 1);

            String jsonResponse;

            // Route based on action
            if ("search".equalsIgnoreCase(action)) {

                log.debug("Calling searchArticles API");

                if (query == null) {
                    writeError(response, 400, "Query (q) is required for search");
                    return;
                }

                jsonResponse = newsService.searchArticles(
                        query,
                        request.getParameter("language"),
                        request.getParameter("sortBy"),
                        request.getParameter("from"),
                        request.getParameter("to"),
                        request.getParameter("domains"),
                        request.getParameter("excludeDomains"),
                        pageSize,
                        page
                );

            } else if ("sources".equalsIgnoreCase(action)) {

                log.debug("Calling getSources API");

                jsonResponse = newsService.getSources(
                        category,
                        request.getParameter("language"),
                        country
                );

            } else {

                log.debug("Calling getTopHeadlines API");

                jsonResponse = newsService.getTopHeadlines(
                        country,
                        category,
                        query,
                        pageSize,
                        page
                );
            }

            // Validate response
            if (jsonResponse == null) {
                writeError(response, 500, "Empty response from service");
                return;
            }

            response.setStatus(200);
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            log.error("Error in NewsServlet", e);
            writeError(response, 500, "Internal Server Error");
        }
    }

    // Sanitization helper
    private String sanitize(String value, String defaultVal) {
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        }
        return value.replaceAll("[^a-zA-Z]", "");
    }

    // Safe parsing
    private int parseInt(String value, int defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    // Standard error response
    private void writeError(SlingHttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);

        String errorJson = String.format(
                "{\"status\":\"error\",\"message\":\"%s\"}",
                message.replace("\"", "\\\"")
        );

        response.getWriter().write(errorJson);
    }
}