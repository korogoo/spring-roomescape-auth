package roomescape.repository.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;

@ExtendWith(SpringExtension.class)
@Import(MemoryReservationRepository.class)
class MemoryReservationRepositoryTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TIME = LocalTime.of(12, 0);
    private static final String THEME = "theme";
    private static final String MEMBER_SESSION_KEY = "sessionKey";
    public static final String NAME = "name";

    private ReservationRepository reservationRepository;

    @Autowired  // 추가
    public MemoryReservationRepositoryTest(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Test
    void 예약을_저장한다() {
        //given
        Reservation reservation = unSavedReservation();

        //when
        Reservation saved = reservationRepository.save(reservation);

        //then
        assertThat(saved.getId()).isNotNull();
    }

    private Reservation savedReservation() {
        return new Reservation(1L, NAME, TOMORROW, TIME, THEME);
    }

    private Reservation unSavedReservation() {
        return new Reservation(NAME, TOMORROW, TIME, THEME);
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, NAME, "password", role);
    }
}
