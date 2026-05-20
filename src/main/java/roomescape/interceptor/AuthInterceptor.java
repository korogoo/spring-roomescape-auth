package roomescape.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.AuthConstants;
import roomescape.domain.Token;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.repository.token.TokenRepository;

public class AuthInterceptor implements HandlerInterceptor {

    private final TokenRepository tokenRepository;

    public AuthInterceptor(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        String tokenValue = request.getHeader(AuthConstants.SESSION_HEADER_KEY);

        if (tokenValue == null) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
        Token token = tokenRepository.findByTokenValue(tokenValue)
            .orElseThrow(() -> new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER));
        if (token.isExpired()) {
            throw new RoomEscapeException(ErrorCode.EXPIRED_TOKEN);
        }
        if (request.getRequestURI().startsWith("/admin") && !token.isAdminMember()) {
            throw new RoomEscapeException(ErrorCode.FORBIDDEN);
        }

        request.setAttribute(AuthConstants.LOGIN_MEMBER_ATTRIBUTE, token.getLoginMember());
        return true;
    }
}
