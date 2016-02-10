package org.gameswap.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpsForwardingFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        StringBuffer uri = ((HttpServletRequest) request).getRequestURL();
        if (uri.toString().startsWith("http://")) {
            String location = "https://" + uri.substring("http://".length());
            ((HttpServletResponse) response).sendRedirect(location);
        } else {
            chain.doFilter(request, response);
        }
    }


    public void destroy() {
    }
}
