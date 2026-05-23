package roomescape.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.auth.JwtProvider;
import roomescape.domain.AuthConstants;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.Store;
import roomescape.dto.member.MemberSummary;
import roomescape.interceptor.JwtAuthInterceptor;
import roomescape.service.ReservationService;

@ExtendWith(MockitoExtension.class)
class AdminControllerWithJwtInterceptorTest {

    private static final String JWT_HEADER_KEY = AuthConstants.JWT_HEADER_KEY;
    private static final String JWT_HEADER_VALUE = AuthConstants.JWT_HEADER_PREFIX + "token";
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TIME = LocalTime.of(12, 0);
    private static final String THEME = "theme";

    @Mock
    private ReservationService reservationService;
    @Mock
    private JwtProvider jwtProvider;
    private MockMvc mockMvc;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setFilter() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(adminController)
            .addInterceptors(new JwtAuthInterceptor(jwtProvider))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void 비로그인_사용자는_접근할_수_없다() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/admin/reservations"));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void 일반_사용자는_접근할_수_없다() throws Exception {
        // given
        when(jwtProvider.extract(any()))
            .thenReturn(new MemberSummary(1L, MemberRole.NORMAL));

        //when
        ResultActions result = mockMvc
            .perform(get("/admin/reservations")
                .header(JWT_HEADER_KEY, JWT_HEADER_VALUE));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    void 관리자는_전체_예약을_조회할_수_있다() throws Exception {
        // given
        Reservation reservation = savedReservation();

        when(jwtProvider.extract(any()))
            .thenReturn(new MemberSummary(1L, MemberRole.ADMIN));
        when(reservationService.findAll())
            .thenReturn(List.of(
                reservation.withId(1L), reservation.withId(2L), reservation.withId(3L)));

        //when
        ResultActions result = mockMvc
            .perform(get("/admin/reservations")
                .header(JWT_HEADER_KEY, JWT_HEADER_VALUE));

        //then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[2].id").value(3L));

        verify(reservationService, times(1)).findAll();
        verifyNoMoreInteractions(reservationService);
    }

    private Reservation savedReservation() {
        return new Reservation(1L, savedMember(MemberRole.NORMAL), savedStore(), TOMORROW, TIME, THEME);
    }

    private Store savedStore() {
        return new Store("store", savedMember(MemberRole.ADMIN));
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, "name", "password", role);
    }
}
