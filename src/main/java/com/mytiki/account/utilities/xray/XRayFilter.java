/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.account.utilities.xray;

import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.jakarta.SegmentNamingStrategy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XRayFilter extends AWSXRayServletFilter {

    private final List<String> ignoredEndpoints;

    XRayFilter(final List<String> ignoredEndpoints) {
        super(SegmentNamingStrategy.dynamic("CoreAccountService"));
        this.ignoredEndpoints = ignoredEndpoints;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws ServletException, IOException {

        final String uri = ((HttpServletRequest) request).getRequestURI();

        boolean shouldFilter = ignoredEndpoints.stream()
                .map(Pattern::compile)
                .map(pattern -> pattern.matcher(uri))
                .anyMatch(Matcher::find);

        if (shouldFilter) {
            chain.doFilter(request, response);
        } else {
            super.doFilter(request, response, chain);
        }
    }
}
