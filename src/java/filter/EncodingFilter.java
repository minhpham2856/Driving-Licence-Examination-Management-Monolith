package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class EncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (request instanceof HttpServletRequest) {
            ((HttpServletRequest) request).setCharacterEncoding("UTF-8");
        }
        chain.doFilter(request, response);
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResp = (HttpServletResponse) response;
            String contentType = httpResp.getContentType();
            if (contentType != null && !contentType.toLowerCase().contains("charset")) {
                httpResp.setContentType(contentType + ";charset=UTF-8");
            }
        }
    }

    @Override
    public void destroy() {
    }
}
