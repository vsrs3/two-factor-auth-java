
package org.tfl.backend;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet Filter implementation class SecureHeadersFilter
 */
@WebFilter("/*")
public class AppSecurityHeadersFilter implements Filter
{

    /**
     * Default constructor.
     */
    public AppSecurityHeadersFilter()
    {

    }

    /**
     * @see Filter#destroy()
     */
    public void destroy()
    {

    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {

        ResponseWrapper resp = new ResponseWrapper((HttpServletResponse) response);
        // pass the request along the filter chain
        chain.doFilter(request, resp);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig fConfig) throws ServletException
    {

    }

    private class ResponseWrapper extends HttpServletResponseWrapper
    {
        public ResponseWrapper(HttpServletResponse response)
        {
            super(response);
            /*
             * Set the security headers. Note these headers may be overwritten by the
             * servlet/resource entity down the chain
             */
            response.setHeader("Strict-Transport-Security", "max-age=31536000;includeSubDomains");
            response.setHeader("Content-Security-Policy", "default-src 'self';");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-XSS-Protection", "1; mode=block");
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Referrer-Policy", "no-referrer");
            response.setHeader("Cache-Control", "no-store");
        }
    }

}
