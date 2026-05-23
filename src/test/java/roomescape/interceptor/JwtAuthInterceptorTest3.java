package roomescape.interceptor;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.auth.JwtProvider;
import roomescape.controller.AdminController;
import roomescape.controller.GlobalExceptionHandler;
import roomescape.domain.MemberRole;
import roomescape.dto.member.MemberSummary;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.service.ReservationService;

@ExtendWith(MockitoExtension.class)
class JwtAuthInterceptorTest3 {

    private static final String AUTH_HEADER = "Authorization";

    @Mock
    private ReservationService reservationService;
    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(adminController)
            .addInterceptors(new JwtAuthInterceptor(jwtProvider))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void 토큰_없이_요청하면_401을_반환한다() throws Exception {
        mockMvc.perform(get("/admin/reservations"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void Bearer_형식이_아닌_토큰은_401을_반환한다() throws Exception {
        mockMvc.perform(get("/admin/reservations")
                .header(AUTH_HEADER, "just-a-raw-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 만료된_토큰은_401을_반환한다() throws Exception {
        when(jwtProvider.extract("expired-token"))
            .thenThrow(new RoomEscapeException(ErrorCode.EXPIRED_TOKEN));

        mockMvc.perform(get("/admin/reservations")
                .header(AUTH_HEADER, "Bearer expired-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 변조된_토큰은_401을_반환한다() throws Exception {
        when(jwtProvider.extract("tampered-token"))
            .thenThrow(new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER));

        mockMvc.perform(get("/admin/reservations")
                .header(AUTH_HEADER, "Bearer tampered-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 일반_사용자가_admin_경로에_접근하면_404을_반환한다() throws Exception {
        when(jwtProvider.extract("normal-token"))
            .thenReturn(new MemberSummary(1L, MemberRole.NORMAL));

        mockMvc.perform(get("/admin/reservations")
                .header(AUTH_HEADER, "Bearer normal-token"))
            .andExpect(status().isNotFound());
    }

    @Test
    void 관리자는_admin_경로에_접근할_수_있다() throws Exception {
        when(jwtProvider.extract("admin-token"))
            .thenReturn(new MemberSummary(1L, MemberRole.ADMIN));
        when(reservationService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/reservations")
                .header(AUTH_HEADER, "Bearer admin-token"))
            .andExpect(status().isOk());
    }
}
