package filter;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(
        urlPatterns = "/*",
        dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE}
)
public class EncodingFilter implements Filter {

    private static final String UTF_8 = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            try {
                ((HttpServletRequest) request).setCharacterEncoding(UTF_8);
            } catch (Exception ignored) {
                // body may already have been read
            }
        }
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setCharacterEncoding(UTF_8);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
