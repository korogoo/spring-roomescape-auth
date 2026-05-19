package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.dto.member.MemberLoginRequest;
import roomescape.service.MemberService;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    private static final String MEMBER_SESSION_KEY = "sessionKey";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;


    @Test
    void 세션으로_로그인하면_쿠키에_JSESSIONID가_세팅된다() throws Exception {
        //given
        Member member = savedNormalMember();
        MemberLoginRequest request = loginRequestFrom(member);

        when(memberService.login(any()))
            .thenReturn(member);

        //when
        ResultActions result = mockMvc
            .perform(post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result.andExpect(status().isOk())
            .andExpect(request().sessionAttribute(MEMBER_SESSION_KEY, member));

        verify(memberService, times(1)).login(request);
        verifyNoMoreInteractions(memberService);
    }

    private MemberLoginRequest loginRequestFrom(Member member) {
        return new MemberLoginRequest(member.getUsername(), member.getPassword());
    }

    private Member savedNormalMember() {
        return new Member(1L, "name", "password", MemberRole.NORMAL);
    }
}
