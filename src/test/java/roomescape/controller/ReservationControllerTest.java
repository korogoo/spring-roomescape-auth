package roomescape.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.exception.ErrorCode;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final String MEMBER_SESSION_KEY = "sessionKey";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 비로그인_사용자는_예약_전체조회를_할_수_없다() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/reservations"));

        // then
        result
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED_MEMBER.name()));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 일반_사용자는_예약_전체조회를_할_수_없다() throws Exception {
        // given
        Member member = savedMember(MemberRole.NORMAL);

        // when
        ResultActions result = mockMvc
            .perform(get("/reservations")
                .sessionAttr(MEMBER_SESSION_KEY, member));

        // then
        result
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.name()));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 관리자는_예약_전체조회를_할_수_있다() throws Exception {
        // given
        Reservation reservation = reservation();

        when(reservationService.getReservations())
            .thenReturn(List.of(
                reservation.withId(1L), reservation.withId(2L), reservation.withId(3L)));

        Member member = savedMember(MemberRole.ADMIN);

        // when
        ResultActions result = mockMvc
            .perform(get("/reservations")
                .sessionAttr(MEMBER_SESSION_KEY, member));

        // then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[2].id").value(3L));

        verify(reservationService, times(1)).getReservations();
        verifyNoMoreInteractions(reservationService);
    }

    private Reservation reservation() {
        return new Reservation(
            "이름",
            TOMORROW,
            LocalTime.of(12, 0),
            "테마"
        );
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, "name", "password", role);
    }
}
