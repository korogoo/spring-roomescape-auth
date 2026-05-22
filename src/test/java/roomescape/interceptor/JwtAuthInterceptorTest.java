package roomescape.interceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.CircularityLock.Global;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import roomescape.auth.JwtProvider;
import roomescape.controller.AdminController;
import roomescape.controller.GlobalExceptionHandler;
import roomescape.domain.AuthConstants;
import roomescape.domain.MemberRole;
import roomescape.dto.member.MemberSummary;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.service.ReservationService;

@ExtendWith(MockitoExtension.class)
class JwtAuthInterceptorTest {

    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(adminController)
            .addInterceptors(new JwtAuthInterceptor(jwtProvider))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void 토큰이_없는_요청은_401을_반환한다() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/admin/reservations"));

        //then
        result
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED_MEMBER.name()));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 토큰이_만료된_경우_401을_반환한다() throws Exception {
        // given
        RoomEscapeException exception = new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        when(jwtProvider.extract(any()))
            .thenThrow(exception);

        // when
        ResultActions result = mockMvc.perform(get("/admin/reservations")
            .header(AuthConstants.JWT_HEADER_KEY, AuthConstants.JWT_HEADER_PREFIX + "expired-token"));

        //then
        result
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED_MEMBER.name()));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 변조된_토큰인_경우_401을_반환한다() throws Exception {
        // given
        RoomEscapeException exception = new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        when(jwtProvider.extract(any()))
            .thenThrow(exception);

        // when
        ResultActions result = mockMvc.perform(get("/admin/reservations")
            .header(AuthConstants.JWT_HEADER_KEY, AuthConstants.JWT_HEADER_PREFIX + "fake-token"));

        //then
        result
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED_MEMBER.name()));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 일반_사용자가_관리자_리소스에_접근하는_경우_404을_반환한다() throws Exception {
        // given
        when(jwtProvider.extract(any()))
            .thenReturn(new MemberSummary(1L, MemberRole.NORMAL));

        // when
        ResultActions result = mockMvc.perform(get("/admin/reservations")
            .header(AuthConstants.JWT_HEADER_KEY, AuthConstants.JWT_HEADER_PREFIX + "token"));

        //then
        result
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.name()));

        verifyNoMoreInteractions(reservationService);
    }

    @Test
    void 헤더에_담긴_토큰이_Bearer_로_시작하지_않는_경우_401을_반환한다() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/admin/reservations")
            .header(AuthConstants.JWT_HEADER_KEY, "token"));

        //then
        result
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED_MEMBER.name()));

        verifyNoMoreInteractions(reservationService);
    }
}
