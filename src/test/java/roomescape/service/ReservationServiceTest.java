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
import roomescape.domain.Store;
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
        ReservationCreateRequest request = new ReservationCreateRequest(TOMORROW, TIME, THEME, savedStore().getId());

        when(reservationRepository.save(any()))
            .thenReturn(savedReservation());

        //when
        Reservation saved = reservationService.save(request, member.getId());

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
    void 사용자의_모든_예약을_조회할_수_있다() {
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
    void 메니저의_모든_예약을_조회할_수_있다() {
        //given
        when(reservationRepository.findAllByStoreMemberId(anyLong()))
            .thenReturn(List.of(
                savedReservation().withId(1L), savedReservation().withId(2L), savedReservation().withId(3L)));

        Member manager = savedManager();

        //when
        List<Reservation> all = reservationService.findAllByManagerId(manager.getId());

        //then
        assertThat(all).hasSize(3);
        assertThat(all).extracting(Reservation::getManagerId)
            .containsOnly(manager.getId());
    }

    private Reservation savedReservation() {
        return new Reservation(1L, savedMember(MemberRole.NORMAL), savedStore(), TOMORROW, TIME, "theme");
    }

    private Store savedStore() {
        return new Store(1L, "store", savedManager());
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, "name", "password", role);
    }

    private Member savedManager() {
        return new Member(2L, "manager", "password", MemberRole.ADMIN);
    }
}
