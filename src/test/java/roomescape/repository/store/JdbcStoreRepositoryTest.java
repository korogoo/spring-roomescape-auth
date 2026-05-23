package roomescape.repository.store;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Store;
import roomescape.repository.member.JdbcMemberRepository;
import roomescape.repository.member.MemberRepository;

@Import({JdbcStoreRepository.class, JdbcMemberRepository.class})
@JdbcTest
class JdbcStoreRepositoryTest {

    private StoreRepository storeRepository;
    private MemberRepository memberRepository;

    @Autowired
    public JdbcStoreRepositoryTest(StoreRepository storeRepository, MemberRepository memberRepository) {
        this.storeRepository = storeRepository;
        this.memberRepository = memberRepository;
    }

    @Test
    void 매장을_저장한다() {
        //given
        Member member = unSavedMember();
        Member savedMember = memberRepository.save(member);

        Store store = new Store("name", savedMember);

        //when
        Store saved = storeRepository.save(store);

        //then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(store.getName());
        assertThat(saved.getMemberId()).isEqualTo(savedMember.getId());
    }

    @Test
    void 매장_아이디로_매장을_조회한다() {
        //given
        Member member = unSavedMember();
        Member savedMember = memberRepository.save(member);

        Store store = new Store("name", savedMember);
        Store saved = storeRepository.save(store);

        //when
        Store find = storeRepository.findById(saved.getId()).get();

        //then
        assertThat(find.getId()).isEqualTo(saved.getId());
        assertThat(find.getName()).isEqualTo(saved.getName());
        assertThat(find.getMemberId()).isEqualTo(saved.getMemberId());
        assertThat(find.getMember().getUsername()).isEqualTo(member.getUsername());
        assertThat(find.getMember().getPassword()).isEqualTo(member.getPassword());
        assertThat(find.getMember().getRole()).isEqualTo(member.getRole());
    }

    @Test
    void 존재하지_않는_매장_아이디로_조회하면_empty를_반환한다() {
        //when
        Optional<Store> find = storeRepository.findById(99L);

        //then
        assertThat(find).isEmpty();
    }

    @Test
    void 회원_아이디로_모든_매장을_조회한다() {
        //given
        Member savedMember = memberRepository.save(unSavedMember("user1"));
        Member otherSavedMember = memberRepository.save(unSavedMember("user2"));

        storeRepository.save(new Store("name1", savedMember));
        storeRepository.save(new Store("name2", savedMember));
        storeRepository.save(new Store("name3", savedMember));

        storeRepository.save(new Store("name3", otherSavedMember));
        storeRepository.save(new Store("name3", otherSavedMember));

        //when
        List<Store> find = storeRepository.findAllByMemberId(savedMember.getId());

        //then
        assertThat(find).hasSize(3);
        assertThat(find).extracting(Store::getMemberId)
            .containsOnly(savedMember.getId());
    }

    private Member unSavedMember() {
        return new Member("name", "password", MemberRole.ADMIN);
    }

    private Member unSavedMember(String name) {
        return new Member(name, "password", MemberRole.ADMIN);
    }
}
