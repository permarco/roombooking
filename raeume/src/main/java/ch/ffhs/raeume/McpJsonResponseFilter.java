package ch.ffhs.raeume;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.io.*;

/** Workaround for malformed chunk framing of the MCP initialization JSON response.
 * JSON is sent with an explicit length; SSE remains unbuffered. */
@Component
public class McpJsonResponseFilter extends OncePerRequestFilter {
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().equals(request.getContextPath() + "/mcp");
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                              FilterChain chain) throws ServletException, IOException {
        response.setHeader("X-Mcp-Json-Filter", "active");
        var wrapped = new ContentCachingResponseWrapper(response) {
            private boolean streaming() {
                return getContentType() != null && getContentType().startsWith("text/event-stream");
            }
            @Override public ServletOutputStream getOutputStream() throws IOException {
                return streaming() ? response.getOutputStream() : super.getOutputStream();
            }
            @Override public PrintWriter getWriter() throws IOException {
                return streaming() ? response.getWriter() : super.getWriter();
            }
            @Override public void flushBuffer() throws IOException {
                if (streaming()) response.flushBuffer();
            }
        };
        chain.doFilter(request, wrapped);
        if (!request.isAsyncStarted()) {
            response.setHeader("Transfer-Encoding", null);
            wrapped.copyBodyToResponse();
        }
    }
}
