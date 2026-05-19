package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.dto.member.MemberLoginRequest;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.repository.member.MemberRepository;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void 아이디와_비밀번호로_사용자_인증할_수_있다() {
        //given
        Member member = savedNormalMember();
        MemberLoginRequest request = loginRequestFrom(member);

        when(memberRepository.findByUsername(any()))
            .thenReturn(Optional.of(member));

        //when
        Member logined = memberService.login(request);

        //then
        assertThat(logined.getId()).isEqualTo(member.getId());
        assertThat(logined.getUsername()).isEqualTo(member.getUsername());
        assertThat(logined.getPassword()).isEqualTo(member.getPassword());
        assertThat(logined.getRole()).isEqualTo(member.getRole());

        verify(memberRepository, times(1)).findByUsername(request.username());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void 아이디가_다르면_사용자_인증할_수_없다() {
        //given
        MemberLoginRequest request = new MemberLoginRequest("other", "other");

        when(memberRepository.findByUsername(any()))
            .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> memberService.login(request))
            .isInstanceOf(RoomEscapeException.class)
            .hasMessageContaining(ErrorCode.UNAUTHORIZED_MEMBER.getMessage());

        verify(memberRepository, times(1)).findByUsername(request.username());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void 비밀번호가_다르면_사용자_인증할_수_없다() {
        //given
        Member member = savedNormalMember();
        MemberLoginRequest request = new MemberLoginRequest(member.getUsername(), "other");

        when(memberRepository.findByUsername(any()))
            .thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> memberService.login(request))
            .isInstanceOf(RoomEscapeException.class)
            .hasMessageContaining(ErrorCode.UNAUTHORIZED_MEMBER.getMessage());

        verify(memberRepository, times(1)).findByUsername(request.username());
        verifyNoMoreInteractions(memberRepository);
    }


    private MemberLoginRequest loginRequestFrom(Member member) {
        return new MemberLoginRequest(member.getUsername(), member.getPassword());
    }

    private Member savedNormalMember() {
        return new Member(1L, "name", "password", MemberRole.NORMAL);
    }
}
