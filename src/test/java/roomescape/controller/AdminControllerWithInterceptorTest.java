package roomescape.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
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
import roomescape.domain.AuthConstants;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.Token;
import roomescape.interceptor.AuthInterceptor;
import roomescape.repository.token.TokenRepository;
import roomescape.service.ReservationService;

@ExtendWith(MockitoExtension.class)
class AdminControllerWithInterceptorTest {

    private static final String SESSION_HEADER_KEY = AuthConstants.SESSION_HEADER_KEY;
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalTime TIME = LocalTime.of(12, 0);
    private static final String THEME = "theme";

    @Mock
    private ReservationService reservationService;
    @Mock
    private TokenRepository tokenRepository;
    private MockMvc mockMvc;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setFilter() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(adminController)
            .addInterceptors(new AuthInterceptor(tokenRepository))
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
        when(tokenRepository.findByTokenValue("test-token"))
            .thenReturn(Optional.of(savedToken(member)));

        //when
        ResultActions result = mockMvc
            .perform(get("/admin/reservations")
                .header(SESSION_HEADER_KEY, "test-token"));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_전체_예약을_조회할_수_있다() throws Exception {
        // given
        Reservation reservation = savedReservation();
        Member member = savedMember(MemberRole.ADMIN);

        when(tokenRepository.findByTokenValue("test-token"))
            .thenReturn(Optional.of(savedToken(member)));
        when(reservationService.findAll())
            .thenReturn(List.of(
                reservation.withId(1L), reservation.withId(2L), reservation.withId(3L)));

        //when
        ResultActions result = mockMvc
            .perform(get("/admin/reservations")
                .header(SESSION_HEADER_KEY, "test-token"));

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

    private Token savedToken(Member member) {
        return new Token("test-token", LocalDateTime.now().plusDays(10), member.getId(), member.getRole());
    }

    private Reservation savedReservation() {
        return new Reservation(1L, savedMember(MemberRole.NORMAL), TOMORROW, TIME, THEME);
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, "name", "password", role);
    }
}
