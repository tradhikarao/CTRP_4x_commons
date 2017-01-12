/**
 * 
 */
package gov.nih.nci.coppa.web;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author dkrylov
 * 
 */
public class RequestTrackingFilter implements Filter {

    /**
     * TRACKER.
     */
    public static final Map<Thread, ServletRequest> TRACKER = new ConcurrentHashMap<Thread, ServletRequest>();

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        TRACKER.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain fc) throws IOException, ServletException {
        try {
            TRACKER.put(Thread.currentThread(), req);
            fc.doFilter(req, resp);
        } finally {
            TRACKER.remove(Thread.currentThread());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig fc) throws ServletException {
        TRACKER.clear();
    }

}
