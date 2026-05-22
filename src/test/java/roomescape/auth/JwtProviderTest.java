package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.MemberRole;
import roomescape.dto.member.MemberSummary;
import roomescape.exception.RoomEscapeException;

class JwtProviderTest {

    private static final String TEST_SECRET = "xFUfYCaRkSOPyLQ+f9lQTNKHiPLA8C1s4a0tErtULish7TgVyEMdPsYsZdm7CQv8vVE3pbdCjz9N9P2q8dJQaA==";

    private final JwtProvider jwtProvider = new JwtProvider(TEST_SECRET, 1);

    @Test
    void 회원아이디와_권한_정보로_토큰을_생성하고_복호화할_수_있다() {
        //given
        MemberSummary member = new MemberSummary(1L, MemberRole.NORMAL);

        //when
        String jwt = jwtProvider.generate(member.id(), member.role());
        MemberSummary extractedMember = jwtProvider.extract(jwt);

        //then
        assertThat(member).isEqualTo(extractedMember);
    }

    @Test
    void 만료된_토큰은_예외를_던진다() {
        //given
        JwtProvider expiredProvider = new JwtProvider(TEST_SECRET, -1);
        String expiredJwt = expiredProvider.generate(1L, MemberRole.NORMAL);

        //when & then
        assertThatThrownBy(() -> jwtProvider.extract(expiredJwt))
            .isInstanceOf(RoomEscapeException.class);
    }

    @Test
    void 변조된_토큰은_예외를_던진다() {
        //given
        String jwt = jwtProvider.generate(1L, MemberRole.NORMAL);
        String tampered = jwt.substring(0, jwt.lastIndexOf('.') + 1) + "invalidsignature";

        //when & then
        assertThatThrownBy(() -> jwtProvider.extract(tampered))
            .isInstanceOf(RoomEscapeException.class);
    }

}
