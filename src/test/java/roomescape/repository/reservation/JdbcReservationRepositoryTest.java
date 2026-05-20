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
import roomescape.repository.member.JdbcMemberRepository;
import roomescape.repository.member.MemberRepository;

@Import({JdbcReservationRepository.class, JdbcMemberRepository.class})
@JdbcTest
class JdbcReservationRepositoryTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TIME = LocalTime.of(12, 0);
    private static final String THEME = "theme";

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public JdbcReservationRepositoryTest(ReservationRepository reservationRepository,
                                         MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    @Test
    void 예약을_저장한다() {
        //given
        Member member = savedMember("name", MemberRole.NORMAL);
        Member savedMember = memberRepository.save(member);

        Reservation target = unSavedReservation(TOMORROW, savedMember);

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
    void 전체_예약을_조회한다() {
        //given
        Member member = savedMember("name",MemberRole.NORMAL);
        Member savedMember = memberRepository.save(member);

        reservationRepository.save(unSavedReservation(TOMORROW, savedMember));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(1), savedMember));

        //when
        List<Reservation> all = reservationRepository.findAll();

        //then
        assertThat(all).hasSize(2);
    }

    @Test
    void 특정_회원의_전체_예약을_조회한다() {
        //given
        Member member1 = savedMember("name",MemberRole.NORMAL);
        Member member2 = savedMember("other",MemberRole.NORMAL);

        Member savedMember1 = memberRepository.save(member1);
        Member savedMember2 = memberRepository.save(member2);


        reservationRepository.save(unSavedReservation(TOMORROW, savedMember1));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(1), savedMember1));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(2), savedMember1));

        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(3), savedMember2));
        reservationRepository.save(unSavedReservation(TOMORROW.plusDays(4), savedMember2));

        //when
        List<Reservation> all = reservationRepository.findAllByMemberId(savedMember1.getId());

        //then
        assertThat(all).hasSize(3);
    }

    private Reservation unSavedReservation(LocalDate date, Member member) {
        return new Reservation(member, date, TIME, THEME);
    }

    private Member savedMember(String name, MemberRole role) {
        return new Member(1L, name, "password", role);
    }
}
