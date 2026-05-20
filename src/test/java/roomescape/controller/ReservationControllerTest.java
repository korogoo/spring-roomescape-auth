package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.dto.reservation.ReservationCreateRequest;
import roomescape.exception.ErrorCode;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TIME = LocalTime.of(12, 0);
    private static final String THEME = "theme";
    private static final String MEMBER_SESSION_KEY = "sessionKey";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void 비로그인_사용자는_예약을_생성할_수_없다() throws Exception {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(TOMORROW, TIME, THEME);

        // when
        ResultActions result = mockMvc
            .perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED_MEMBER.name()));

        verifyNoMoreInteractions(reservationService);
    }

    @ParameterizedTest
    @EnumSource(MemberRole.class)
    void 모든_사용자는_예약을_생성할_수_있다() throws Exception {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(TOMORROW, TIME, THEME);
        Reservation savedReservation = savedReservation();

        when(reservationService.save(any(), any()))
            .thenReturn(savedReservation);

        Member member = savedMember(MemberRole.NORMAL);

        // when
        ResultActions result = mockMvc
            .perform(post("/reservations")
                .sessionAttr(MEMBER_SESSION_KEY, member)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(savedReservation.getId()));

        verify(reservationService, times(1)).save(request, member);
        verifyNoMoreInteractions(reservationService);
    }

    private Reservation savedReservation() {
        return new Reservation(1L, savedMember(MemberRole.NORMAL), TOMORROW, TIME, THEME);
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, "name", "password", role);
    }
}
