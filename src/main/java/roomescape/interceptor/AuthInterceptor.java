package roomescape.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Member;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;

public class AuthInterceptor implements HandlerInterceptor {

    private static final String MEMBER_SESSION_KEY = "sessionKey";

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
        Member member = (Member) session.getAttribute(MEMBER_SESSION_KEY);
        if (member == null) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        if (request.getRequestURI().startsWith("/admin") && !member.isAdmin()) {
            throw new RoomEscapeException(ErrorCode.FORBIDDEN);
        }

        return true;
    }
}
