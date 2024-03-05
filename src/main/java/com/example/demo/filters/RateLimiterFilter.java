package com.example.demo.filters;

import com.example.demo.ratelimit.slidingwindowcounter.SlidingWindowCounterRateLimiter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class RateLimiterFilter implements Filter {

    private final SlidingWindowCounterRateLimiter rateLimiter;

    @Autowired
    public RateLimiterFilter(SlidingWindowCounterRateLimiter slidingWindowCounterRateLimiter) {
        this.rateLimiter = slidingWindowCounterRateLimiter;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String tenantId = req.getHeader("tenantId");

        if(req.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
        }
        else {
            if (tenantId == null) {
                resp.sendError(HttpStatus.BAD_REQUEST.value(), "Tenant Not Provided");
                return;
            }

            if (!this.rateLimiter.isAllowed(tenantId)) {
                resp.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Rate Limit Exceeded");
                return;
            }

            chain.doFilter(request, response);
        }
    }
}
