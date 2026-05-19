package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.dto.member.MemberLoginRequest;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.repository.member.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    public Member login(MemberLoginRequest request) {
        Member member = memberRepository.findByUsername(request.username())
            .orElseThrow(() -> new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER));

        if (!member.isSamePassword(request.password())) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
        return member;
    }
}
