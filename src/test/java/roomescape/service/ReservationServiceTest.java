package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

    @Test
    void 모든_예약을_조회할_수_있다() {
        //given
        when(reservationRepository.findAllByMemberId(anyLong()))
            .thenReturn(List.of(
                savedReservation().withId(1L), savedReservation().withId(2L), savedReservation().withId(3L)));

        //when
        List<Reservation> all = reservationService.findAllByMemberId(1L);

        //then
        assertThat(all).hasSize(3);
    }

    @Test
    void 본인의_모든_예약을_조회할_수_있다() {
        //given
        when(reservationRepository.findAllByMemberId(anyLong()))
            .thenReturn(List.of(
                savedReservation().withId(1L), savedReservation().withId(2L), savedReservation().withId(3L)));

        //when
        List<Reservation> all = reservationService.findAllByMemberId(1L);

        //then
        assertThat(all).hasSize(3);
    }

    private Reservation savedReservation() {
        return new Reservation(1L, savedMember(MemberRole.NORMAL), TOMORROW, TIME, "theme");
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, "name", "password", role);
    }
}
