package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.dto.ResourceIdResponse;
import roomescape.dto.member.MemberLoginRequest;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private static final String MEMBER_SESSION_KEY = "sessionKey";

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/admin/join")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceIdResponse joinAdmin(
        @Valid @RequestBody MemberLoginRequest request
    ) {
        Member member = memberService.save(request, MemberRole.ADMIN);
        return new ResourceIdResponse(member.getId());
    }

    @PostMapping("/normal/join")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceIdResponse joinNormal(
        @Valid @RequestBody MemberLoginRequest request
    ) {
        Member member = memberService.save(request, MemberRole.NORMAL);
        return new ResourceIdResponse(member.getId());
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public void login(
        HttpServletRequest httpRequest,
        @Valid @RequestBody MemberLoginRequest request
    ) {
        Member member = memberService.login(request);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(MEMBER_SESSION_KEY, member);
    }
}
