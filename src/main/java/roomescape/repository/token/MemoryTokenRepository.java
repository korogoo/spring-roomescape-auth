package roomescape.repository.token;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import roomescape.domain.Token;

@Repository
public class MemoryTokenRepository implements TokenRepository {

    private static final Map<String, Token> TOKENS = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    @Override
    public Token save(Token token) {
        long id = ID_GENERATOR.getAndIncrement();
        Token tokenWithId = token.withId(id);
        TOKENS.put(token.getTokenValue(), tokenWithId);
        return tokenWithId;
    }

    @Override
    public Optional<Token> findByTokenValue(String tokenValue) {
        return TOKENS.values().stream()
            .filter(token -> token.isSameTokenValue(tokenValue))
            .findFirst();
    }
}
