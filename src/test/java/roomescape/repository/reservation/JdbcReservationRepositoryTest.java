package roomescape.repository.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Test
    void 특정_메니저의_전체_예약을_조회한다() {
        //given
        Member member = memberRepository.save(unsavedMember("name1", MemberRole.NORMAL));

        Member manager1 = memberRepository.save(unsavedMember("manager1", MemberRole.ADMIN));
        Member manager2 = memberRepository.save(unsavedMember("manager2", MemberRole.ADMIN));
        Store store1 = storeRepository.save(unsavedStore("store1", manager1));
        Store store2 = storeRepository.save(unsavedStore("store2", manager2));

        reservationRepository.save(unSavedReservation(TOMORROW, member, store1));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(1), member, store1));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(2), member, store1));

        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(3), member, store2));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(4), member, store2));

        //when
        List<Reservation> all = reservationRepository.findAllByStoreMemberId(manager1.getId());

        //then
        assertThat(all).hasSize(3);
        assertThat(all).extracting(Reservation::getManagerId)
            .containsOnly(manager1.getId());
    }

    @Nested
    @DisplayName("특정 예약 아이디와 매니저 아이디로 예약이 존재하는지 조회한다")
    class ExistsByIdAndManagerId {

        @Test
        void 특정_매장의_매니저가_생성한_예약이_있다면_TRUE를_반환한다() {
            //given
            Member member = memberRepository.save(unsavedMember("name", MemberRole.NORMAL));

            Member manager = memberRepository.save(unsavedMember("manager", MemberRole.ADMIN));
            Store store = storeRepository.save(unsavedStore("store", manager));

            Reservation reservation = reservationRepository.save(unSavedReservation(TOMORROW, member, store));

            //when
            boolean exists = reservationRepository.existsByIdAndStoreMemberId(reservation.getId(), manager.getId());

            //then
            assertThat(exists).isTrue();
        }

        @Test
        void 특정_매장의_매니저가_생성한_예약이_없다면_FALSE를_반환한다() {
            //when
            boolean exists = reservationRepository.existsByIdAndStoreMemberId(1L, 1L);

            //then
            assertThat(exists).isFalse();
        }

        @Test
        void 다른_매니저의_매장_예약이라면_FALSE를_반환한다() {
            // given
            Member member = memberRepository.save(unsavedMember("name", MemberRole.NORMAL));

            Member manager = memberRepository.save(unsavedMember("manager", MemberRole.ADMIN));
            Member anotherManager = memberRepository.save(unsavedMember("another", MemberRole.ADMIN));

            Store store = storeRepository.save(unsavedStore("store", manager));
            Reservation reservation = reservationRepository.save(unSavedReservation(TOMORROW, member, store));

            // when
            boolean exists = reservationRepository.existsByIdAndStoreMemberId(reservation.getId(), anotherManager.getId());

            // then
            assertThat(exists).isFalse();
        }
    }

    @Test
    void 특정_예약_아이디와_매니저_아이디로_예약을_삭제한다() {
        // given
        Member member = memberRepository.save(unsavedMember("name", MemberRole.NORMAL));

        Member manager = memberRepository.save(unsavedMember("manager", MemberRole.ADMIN));

        Store store = storeRepository.save(unsavedStore("store", manager));
        Reservation reservation = reservationRepository.save(unSavedReservation(TOMORROW, member, store));

        //when
        reservationRepository.deleteByIdAndStoreMemberId(reservation.getId(), manager.getId());

        //then
        boolean exists = reservationRepository.existsByIdAndStoreMemberId(reservation.getId(), manager.getId());
        assertThat(exists).isFalse();
    }

    private Reservation unSavedReservation(LocalDate date, Member member, Store store) {
        return new Reservation(member.getId(), store.getId(), date, TIME, THEME);
    }

    private Member unsavedMember(String name, MemberRole role) {
        return new Member(name, "password", role);
    }

    private Store unsavedStore(String storeName, Member savedMember) {
        return new Store(storeName, savedMember);
    }
}
