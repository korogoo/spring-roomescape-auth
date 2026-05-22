package roomescape.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.MemberRole;
import roomescape.dto.member.MemberSummary;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;

@Component
public class JwtProvider {

    private final Key secretKey;
    private final long expirationMs;

    public JwtProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration-days}") long expirationDays
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = Duration.ofDays(expirationDays).toMillis();
    }

    public String generate(long memberId, MemberRole role) {
        return Jwts.builder()
            .claim("memberId", memberId)
            .claim("role", role.name())
            .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(secretKey)
            .compact();
    }

    public MemberSummary extract(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            Long memberId = claims.get("memberId", Double.class).longValue();
            MemberRole role = MemberRole.valueOf(claims.get("role", String.class));

            return new MemberSummary(memberId, role);
        } catch (ExpiredJwtException e) {
            throw new RoomEscapeException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
    }
}
