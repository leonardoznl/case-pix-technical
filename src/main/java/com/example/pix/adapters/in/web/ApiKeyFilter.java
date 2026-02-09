package com.example.pix.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {
    static final String HEADER_NAME = "X-API-KEY";

    private final ObjectMapper objectMapper;

    @Value("${pix.api-key}")
    private String expectedKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/pix");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER_NAME);
        if (apiKey == null || apiKey.isBlank()) {
            writeProblem(response, HttpStatus.UNAUTHORIZED, "api-key-missing", "missing-api-key-header");
            return;
        }
        if (!apiKey.equals(expectedKey)) {
            writeProblem(response, HttpStatus.UNAUTHORIZED, "api-key-invalid", "invalid-api-key");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeProblem(
            HttpServletResponse response,
            HttpStatus status,
            String title,
            String detail
    ) throws IOException {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setType(URI.create("apiKey"));
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setProperty("header", HEADER_NAME);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problemDetail);
    }
}
