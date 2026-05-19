package roomescape.service;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.dto.reservation.ReservationCreateRequest;
import roomescape.repository.reservation.ReservationRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TIME = LocalTime.of(12, 0);
    private static final String THEME = "theme";
    private static final String MEMBER_SESSION_KEY = "sessionKey";
    public static final String NAME = "name";

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @ParameterizedTest
    @EnumSource(MemberRole.class)
    void 예약을_저장할_수_있다(MemberRole role) {
        //given
        Member member = savedMember(role);
        ReservationCreateRequest request = new ReservationCreateRequest(TOMORROW, TIME, THEME);

        when(reservationRepository.save(any()))
            .thenReturn(savedReservation());

        //when
        Reservation saved = reservationService.save(request, member);

        //then
        assertThat(saved.getId()).isNotNull();
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, NAME, "password", role);
    }

    private Reservation savedReservation() {
        return new Reservation(1L, NAME, TOMORROW, TIME, THEME);
    }
}
