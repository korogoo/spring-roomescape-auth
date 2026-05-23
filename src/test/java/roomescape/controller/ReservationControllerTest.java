package roomescape.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import roomescape.auth.JwtProvider;
import roomescape.domain.AuthConstants;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.Store;
import roomescape.dto.member.MemberSummary;
import roomescape.dto.reservation.ReservationCreateRequest;
import roomescape.exception.ErrorCode;
import roomescape.service.ReservationService;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TIME = LocalTime.of(12, 0);
    private static final String THEME = "theme";
    private static final String JWT_HEADER_KEY = AuthConstants.JWT_HEADER_KEY;
    private static final String JWT_HEADER_VALUE = AuthConstants.JWT_HEADER_PREFIX + "token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;
    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void 비로그인_사용자는_예약을_생성할_수_없다() throws Exception {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(TOMORROW, TIME, THEME, savedStore().getId());

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
    void 모든_사용자는_예약을_생성할_수_있다(MemberRole role) throws Exception {
        // given
        ReservationCreateRequest request = new ReservationCreateRequest(TOMORROW, TIME, THEME, savedStore().getId());
        Reservation savedReservation = savedReservation();
        Member member = savedMember(role);

        when(jwtProvider.extract(any()))
            .thenReturn(new MemberSummary(1L, role));
        when(reservationService.save(any(), anyLong()))
            .thenReturn(savedReservation);

        // when
        ResultActions result = mockMvc
            .perform(post("/reservations")
                .header(JWT_HEADER_KEY, JWT_HEADER_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(savedReservation.getId()));

        verify(reservationService, times(1)).save(request, member.getId());
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 헤더_인증_정보로_본인의_전체_예약을_조회할_수_있다() throws Exception {
        // given
        Reservation reservation = savedReservation();
        Member member = savedMember(MemberRole.NORMAL);

        when(jwtProvider.extract(any()))
            .thenReturn(new MemberSummary(1L, MemberRole.NORMAL));
        when(reservationService.findAllByMemberId(anyLong()))
            .thenReturn(List.of(
                reservation.withId(1L), reservation.withId(2L), reservation.withId(3L)));

        //when
        ResultActions result = mockMvc
            .perform(get("/reservations")
                .header(JWT_HEADER_KEY, JWT_HEADER_VALUE));

        //then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[2].id").value(3L));

        verify(reservationService, times(1)).findAllByMemberId(member.getId());
        verifyNoMoreInteractions(reservationService);
    }

    private Reservation savedReservation() {
        return new Reservation(1L, savedMember(MemberRole.NORMAL), savedStore(), TOMORROW, TIME, THEME);
    }

    private Store savedStore() {
        return new Store(1L, "store", savedMember(MemberRole.ADMIN));
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, "name", "password", role);
    }
}
