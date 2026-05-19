package roomescape.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import roomescape.filter.AdminAuthFilter;
import roomescape.service.ReservationService;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private static final String MEMBER_SESSION_KEY = "sessionKey";

    @Mock
    private ReservationService reservationService;
    private MockMvc mockMvc;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setFilter() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(adminController)
            .addFilter(new AdminAuthFilter())
            .build();
    }

    @Test
    void 비로그인_사용자는_접근할_수_없다() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/admin"));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void 일반_사용자는_접근할_수_없다() throws Exception {
        // given
        Member member = savedMember(MemberRole.NORMAL);

        //when
        ResultActions result = mockMvc
            .perform(get("/admin")
                .sessionAttr(MEMBER_SESSION_KEY, member));

        //then
        result.andExpect(status().isForbidden());
    }

    private Member savedMember(MemberRole role) {
        return new Member(1L, "name", "password", role);
    }
}
