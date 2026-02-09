package com.example.pix.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

class ApiKeyFilterTest {
    private ApiKeyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new ApiKeyFilter(new ObjectMapper());
        ReflectionTestUtils.setField(filter, "expectedKey", "secret");
    }

    @Test
    void shouldReturnUnauthorized_whenApiKeyMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/pix");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean called = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> called.set(true);

        filter.doFilter(request, response, chain);

        assertThat(called).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    void shouldReturnUnauthorized_whenApiKeyInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/pix");
        request.addHeader(ApiKeyFilter.HEADER_NAME, "wrong");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean called = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> called.set(true);

        filter.doFilter(request, response, chain);

        assertThat(called).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    void shouldAllowRequest_whenApiKeyValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/pix");
        request.addHeader(ApiKeyFilter.HEADER_NAME, "secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean called = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> called.set(true);

        filter.doFilter(request, response, chain);

        assertThat(called).isTrue();
    }

    @Test
    void shouldSkipFilter_whenNonPixPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean called = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> called.set(true);

        filter.doFilter(request, response, chain);

        assertThat(called).isTrue();
    }
}
