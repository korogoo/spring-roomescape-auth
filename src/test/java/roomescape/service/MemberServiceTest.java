package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

    @ParameterizedTest
    @EnumSource(MemberRole.class)
    void 아이디와_비밀번호로_사용자를_생성할_수_있다(MemberRole role) {
        //given
        Member member = unSavedMember(role);
        MemberLoginRequest request = loginRequestFrom(member);

        when(memberRepository.save(any()))
            .thenReturn(member.withId(1L));

        //when
        Member saved = memberService.save(request, role);

        //then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getUsername()).isEqualTo(request.username());
        assertThat(saved.getPassword()).isEqualTo(request.password());
        assertThat(saved.getRole()).isEqualTo(role);

        verify(memberRepository, times(1)).save(any());
        verifyNoMoreInteractions(memberRepository);
    }

    @ParameterizedTest
    @EnumSource(MemberRole.class)
    void 이미_존재하는_아이디로_사용자를_생성하는_경우_예외가_발생한다(MemberRole role) {
        //given
        Member member = unSavedMember(role);
        MemberLoginRequest request = loginRequestFrom(member);

        RoomEscapeException exception = new RoomEscapeException(ErrorCode.DUPLICATED_USERNAME);
        doThrow(exception)
            .when(memberRepository).save(any());

        //when
        assertThatThrownBy(() -> memberService.save(request, role))
            .isInstanceOf(RoomEscapeException.class)
            .hasMessageContaining(ErrorCode.DUPLICATED_USERNAME.getMessage());

        //then
        verify(memberRepository, times(1)).save(any());
        verifyNoMoreInteractions(memberRepository);
    }

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
            .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());

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
            .hasMessageContaining(ErrorCode.MEMBER_NOT_FOUND.getMessage());

        verify(memberRepository, times(1)).findByUsername(request.username());
        verifyNoMoreInteractions(memberRepository);
    }

    private MemberLoginRequest loginRequestFrom(Member member) {
        return new MemberLoginRequest(member.getUsername(), member.getPassword());
    }

    private Member unSavedMember(MemberRole role) {
        return new Member("name", "password", role);
    }

    private Member savedNormalMember() {
        return new Member(1L, "name", "password", MemberRole.NORMAL);
    }
}
