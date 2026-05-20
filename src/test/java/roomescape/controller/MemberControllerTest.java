package roomescape.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.service.MemberService;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    private static final String SESSION_KEY = "sessionKey";
    private static final String SESSION_HEADER_KEY = "X-Session-Id";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @Test
    void 관리자는_회원가입할_수_있다() throws Exception {
        //given
        Member member = unSavedMember(MemberRole.ADMIN);
        MemberLoginRequest request = loginRequestFrom(member);

        when(memberService.save(any(), any()))
            .thenReturn(member.withId(1L));

        //when
        ResultActions result = mockMvc
            .perform(post("/members/admin/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L));

        verify(memberService, times(1)).save(request, MemberRole.ADMIN);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    void 일반_사용자는_회원가입할_수_있다() throws Exception {
        //given
        Member member = unSavedMember(MemberRole.NORMAL);
        MemberLoginRequest request = loginRequestFrom(member);

        when(memberService.save(any(), any()))
            .thenReturn(member.withId(1L));

        //when
        ResultActions result = mockMvc
            .perform(post("/members/normal/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L));

        verify(memberService, times(1)).save(request, MemberRole.NORMAL);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    void 닉네임_중복으로_회원가입에_실패할_경우_에러_응답을_반환한다() throws Exception {
        //given
        Member member = unSavedMember(MemberRole.NORMAL);
        MemberLoginRequest request = loginRequestFrom(member);

        RoomEscapeException exception = new RoomEscapeException(ErrorCode.DUPLICATED_USERNAME);
        doThrow(exception)
            .when(memberService).save(any(), any());

        //when
        ResultActions result = mockMvc
            .perform(post("/members/normal/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.DUPLICATED_USERNAME.name()));

        verify(memberService, times(1)).save(request, MemberRole.NORMAL);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    void 로그인하면_쿠키에_JSESSIONID가_세팅된다() throws Exception {
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
            .andExpect(request().sessionAttribute(SESSION_KEY, member));

        verify(memberService, times(1)).login(request);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    void 로그인에_실패하면_쿠키에_JSESSIONID가_세팅되지_않는다() throws Exception {
        //given
        MemberLoginRequest request = new MemberLoginRequest("other", "other");

        RoomEscapeException exception = new RoomEscapeException(ErrorCode.UNAUTHORIZED_MEMBER);
        doThrow(exception)
            .when(memberService).login(any());

        //when
        ResultActions result = mockMvc
            .perform(post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //then
        result
            .andExpect(status().isUnauthorized())
            .andExpect(request().sessionAttributeDoesNotExist(SESSION_KEY));

        verify(memberService, times(1)).login(request);
        verifyNoMoreInteractions(memberService);
    }

    @Test
    void 로그인하면_헤더에도_JSESSIONID가_세팅된다() throws Exception {
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
        result
            .andExpect(status().isOk())
            .andExpect(request().sessionAttribute(SESSION_KEY, member))
            .andExpect(header().exists(SESSION_HEADER_KEY));

        verify(memberService, times(1)).login(request);
        verifyNoMoreInteractions(memberService);
    }

    private MemberLoginRequest loginRequestFrom(Member member) {
        return new MemberLoginRequest(member.getUsername(), member.getPassword());
    }

    private Member unSavedMember(MemberRole role) {
        return new Member("name", "password", role);
    }

    private Member savedNormalMember() {
        return new Member(1L, "name", "password", MemberRole.NORMAL);
    }
}
