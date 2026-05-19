package roomescape.controller;

import static org.hamcrest.Matchers.hasSize;
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
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.filter.AuthFilter;
import roomescape.interceptor.AuthInterceptor;
import roomescape.service.ReservationService;

@ExtendWith(MockitoExtension.class)
class AdminControllerWithInterceptorTest {

    private static final String MEMBER_SESSION_KEY = "sessionKey";
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TIME = LocalTime.of(12, 0);
    private static final String THEME = "theme";
    public static final String NAME = "name";

    @Mock
    private ReservationService reservationService;
    private MockMvc mockMvc;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setFilter() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(adminController)
            .addInterceptors(new AuthInterceptor())
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
        Member member = savedMember(MemberRole.NORMAL);

        //when
        ResultActions result = mockMvc
            .perform(get("/admin/reservations")
                .sessionAttr(MEMBER_SESSION_KEY, member));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_전체_예약을_조회할_수_있다() throws Exception {
        // given
        Reservation reservation = savedReservation();

        when(reservationService.getReservations())
            .thenReturn(List.of(
                reservation.withId(1L), reservation.withId(2L), reservation.withId(3L)));

        Member member = savedMember(MemberRole.ADMIN);

        //when
        ResultActions result = mockMvc
            .perform(get("/admin/reservations")
                .sessionAttr(MEMBER_SESSION_KEY, member));

        //then
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[1].id").value(2L))
            .andExpect(jsonPath("$[2].id").value(3L));

        verify(reservationService, times(1)).getReservations();
        verifyNoMoreInteractions(reservationService);
    }

    private Reservation savedReservation() {
        return new Reservation(1L, NAME, TOMORROW, TIME, THEME);
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, "name", "password", role);
    }
}
