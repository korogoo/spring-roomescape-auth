package roomescape.repository.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;

@Import(JdbcMemberRepository.class)
@JdbcTest
class JdbcMemberRepositoryTest {

    private final MemberRepository memberRepository;

    @Autowired
    JdbcMemberRepositoryTest(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Test
    void 일반_회원정보를_저장한다() {
        //given
        Member member = new Member("n", "p", MemberRole.NORMAL);

        //when
        Member saved = memberRepository.save(member);

        //then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("n");
        assertThat(saved.getPassword()).isEqualTo("p");
        assertThat(saved.getRole()).isEqualTo(MemberRole.NORMAL);
    }

    @Test
    void 관리자_회원정보를_저장한다() {
        //given
        Member member = new Member("n", "p", MemberRole.ADMIN);

        //when
        Member saved = memberRepository.save(member);

        //then
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getUsername()).isEqualTo("n");
        assertThat(saved.getPassword()).isEqualTo("p");
        assertThat(saved.getRole()).isEqualTo(MemberRole.ADMIN);
    }

    @Test
    void 회원_아이디로_회원정보를_조회한다() {
        //given
        Member member = new Member("n", "p", MemberRole.NORMAL);
        Member saved = memberRepository.save(member);

        //when
        Member find = memberRepository.findById(saved.getId());

        //then
        assertThat(find.getId()).isEqualTo(saved.getId());
        assertThat(find.getUsername()).isEqualTo(saved.getUsername());
        assertThat(find.getPassword()).isEqualTo(saved.getPassword());
        assertThat(find.getRole()).isEqualTo(saved.getRole());
    }

    @Test
    void 회원_이름으로_회원정보를_조회한다() {
        //given
        String name = "name";

        Member member = new Member(name, "p", MemberRole.NORMAL);
        Member saved = memberRepository.save(member);

        //when
        Member find = memberRepository.findByUsername(name).get();

        //then
        assertThat(find.getId()).isEqualTo(saved.getId());
        assertThat(find.getUsername()).isEqualTo(saved.getUsername());
        assertThat(find.getPassword()).isEqualTo(saved.getPassword());
        assertThat(find.getRole()).isEqualTo(saved.getRole());
    }

    @Test
    void 특정_이름을_갖는_회원이_없는_경우_빈_값을_반환한다() {
        //given
        Member member = new Member("n", "p", MemberRole.NORMAL);
        memberRepository.save(member);

        String otherName = "other";
        //when
        Optional<Member> optionalMember = memberRepository.findByUsername(otherName);

        //then
        assertThat(optionalMember).isEmpty();
    }

    @Test
    void 같은_이름을_갖는_회원이_저장되는_경우_예외가_발생한다() {
        //given
        Member member1 = new Member("n", "p", MemberRole.NORMAL);
        Member member2 = new Member("n", "w", MemberRole.ADMIN);
        memberRepository.save(member1);

        //when & then
        assertThatThrownBy(() -> memberRepository.save(member2))
            .isInstanceOf(DuplicateKeyException.class);
    }
}
