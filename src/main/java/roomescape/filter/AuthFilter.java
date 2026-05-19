package roomescape.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;
import roomescape.domain.Member;
import roomescape.exception.ErrorCode;
import roomescape.exception.ErrorResponse;

public class AuthFilter implements Filter {

    private static final String MEMBER_SESSION_KEY = "sessionKey";
    private static final Set<String> WHITE_LIST = Set.of(
        "/members/login",
        "/members/normal/join",
        "/members/admin/join"
    );

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(
        ServletRequest servletRequest,
        ServletResponse servletResponse,
        FilterChain filterChain
    ) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        if (WHITE_LIST.contains(httpRequest.getRequestURI())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            writeErrorResponse(httpResponse, ErrorCode.UNAUTHORIZED_MEMBER);
            return;
        }
        Member member = (Member) session.getAttribute(MEMBER_SESSION_KEY);
        if (member == null) {
            writeErrorResponse(httpResponse, ErrorCode.UNAUTHORIZED_MEMBER);
            return;
        }

        if (httpRequest.getRequestURI().startsWith("/admin") && !member.isAdmin()) {
            writeErrorResponse(httpResponse, ErrorCode.FORBIDDEN);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void writeErrorResponse(HttpServletResponse httpResponse, ErrorCode error) throws IOException {
        httpResponse.setStatus(error.getCode().value());
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.getWriter().write(
            objectMapper.writeValueAsString(ErrorResponse.of(error))
        );
    }
}
