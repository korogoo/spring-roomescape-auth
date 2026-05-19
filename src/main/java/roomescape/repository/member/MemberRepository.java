package roomescape.repository.member;

import java.util.Optional;
import roomescape.domain.Member;

public interface MemberRepository {

    Member save(Member member);

    Member findById(Long id);

    Optional<Member> findByUsername(String username);
}
