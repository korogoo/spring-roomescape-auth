package roomescape.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.auth.JwtProvider;
import roomescape.domain.AuthConstants;
import roomescape.dto.member.MemberSummary;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;

public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;

    public JwtAuthInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        String tokenValue = request.getHeader(AuthConstants.JWT_HEADER_KEY);

        if (tokenValue == null || !tokenValue.startsWith("Bearer ")) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        String token = tokenValue.substring(AuthConstants.JWT_HEADER_PREFIX.length());
        MemberSummary member = jwtProvider.extract(token);

        if (request.getRequestURI().startsWith("/admin") && !member.isAdmin()) {
            throw new RoomEscapeException(ErrorCode.NOT_FOUND);
        }

        request.setAttribute(AuthConstants.LOGIN_MEMBER_ATTRIBUTE, member);
        return true;
    }
}
