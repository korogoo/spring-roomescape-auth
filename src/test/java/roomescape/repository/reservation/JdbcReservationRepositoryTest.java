package roomescape.repository.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.Store;
import roomescape.repository.member.JdbcMemberRepository;
import roomescape.repository.member.MemberRepository;
import roomescape.repository.store.JdbcStoreRepository;
import roomescape.repository.store.StoreRepository;

@Import({JdbcReservationRepository.class, JdbcMemberRepository.class, JdbcStoreRepository.class})
@JdbcTest
class JdbcReservationRepositoryTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TIME = LocalTime.of(12, 0);
    private static final String THEME = "theme";

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;

    @Autowired
    public JdbcReservationRepositoryTest(
        ReservationRepository reservationRepository,
        MemberRepository memberRepository,
        StoreRepository storeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.storeRepository = storeRepository;
    }

    @Test
    void 예약을_저장한다() {
        //given
        Member savedMember = memberRepository.save(unsavedMember("name", MemberRole.NORMAL));

        Member savedManager = memberRepository.save(unsavedMember("manager", MemberRole.ADMIN));
        Store savedStore = storeRepository.save(unsavedStore("store", savedManager));

        Reservation target = unSavedReservation(TOMORROW, savedMember, savedStore);

        //when
        Reservation saved = reservationRepository.save(target);

        //then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMemberId()).isEqualTo(target.getMemberId());
        assertThat(saved.getMemberUsername()).isEqualTo(target.getMemberUsername());
        assertThat(saved.getDate()).isEqualTo(target.getDate());
        assertThat(saved.getTime()).isEqualTo(target.getTime());
        assertThat(saved.getTheme()).isEqualTo(target.getTheme());
    }

    @Test
    void 특정_회원의_전체_예약을_조회한다() {
        //given
        Member member1 = memberRepository.save(unsavedMember("name1", MemberRole.NORMAL));
        Member member2 = memberRepository.save(unsavedMember("name2", MemberRole.NORMAL));

        Member manager = memberRepository.save(unsavedMember("name", MemberRole.ADMIN));
        Store store = storeRepository.save(unsavedStore("store", manager));

        reservationRepository.save(unSavedReservation(TOMORROW, member1, store));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(1), member1, store));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(2), member1, store));

        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(3), member2, store));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(4), member2, store));

        //when
        List<Reservation> all = reservationRepository.findAllByMemberId(member1.getId());

        //then
        assertThat(all).hasSize(3);
        assertThat(all).extracting(Reservation::getMemberId)
            .containsOnly(member1.getId());
    }

    private Reservation unSavedReservation(LocalDate date, Member member, Store store) {
        return new Reservation(member.getId(), store.getId(), date, TIME, THEME);
    }

    private Member savedMember(String name, MemberRole role) {
        return new Member(1L, name, "password", role);
    }

    private Member unsavedMember(String name, MemberRole role) {
        return new Member(name, "password", role);
    }

    private Store unsavedStore(String storeName, Member savedMember) {
        return new Store(storeName, savedMember);
    }
}
