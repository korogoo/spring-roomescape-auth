package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
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


    public Member save(MemberLoginRequest request, MemberRole role) {
        Member member = new Member(request.username(), request.password(), role);

        try {
            return memberRepository.save(member);
        } catch (DuplicateKeyException e) {
            throw new RoomEscapeException(ErrorCode.DUPLICATED_USERNAME);
        }
    }

    public Member login(MemberLoginRequest request) {
        Member member = memberRepository.findByUsername(request.username())
            .orElseThrow(() -> new RoomEscapeException(ErrorCode.MEMBER_NOT_FOUND));

        if (!member.isSamePassword(request.password())) {
            throw new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        }
        return member;
    }
}
