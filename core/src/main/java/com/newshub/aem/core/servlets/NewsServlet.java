package com.newshub.aem.core.servlets;

import com.newshub.aem.core.services.NewsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        // Category
        String category = request.getParameter("category");
        if (StringUtils.isBlank(category)) {
            category = "general";
        }
        category = category.replaceAll("[^a-zA-Z]", "");

        // Country (IMPORTANT fallback)
        String country = request.getParameter("country");
        if (StringUtils.isBlank(country)) {
            country = "us";
        }

        // Query
        String query = request.getParameter("q");

        int pageSize = parseInt(request.getParameter("pageSize"), 10);
        int page = parseInt(request.getParameter("page"), 1);

        String jsonResponse = newsService.getTopHeadlines(
                country,
                category,
                query,
                pageSize,
                page
        );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }

    private int parseInt(String value, int defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultVal;
        }
    }
}