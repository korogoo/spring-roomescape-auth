package roomescape.repository.token;

import java.util.Optional;
import roomescape.domain.Token;

public interface TokenRepository {

    Token save(Token token);

    Optional<Token> findByTokenValue(String tokenValue);
}
