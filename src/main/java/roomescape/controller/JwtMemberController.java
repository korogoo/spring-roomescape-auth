package roomescape.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.JwtProvider;
import roomescape.domain.AuthConstants;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.dto.ResourceIdResponse;
import roomescape.dto.member.MemberLoginRequest;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/members")
public class JwtMemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    public JwtMemberController(MemberService memberService, JwtProvider jwtProvider) {
        this.memberService = memberService;
        this.jwtProvider = jwtProvider;
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
        HttpServletResponse httpResponse,
        @Valid @RequestBody MemberLoginRequest request
    ) {
        Member member = memberService.login(request);

        String token = jwtProvider.generate(member.getId(), member.getRole());
        httpResponse.setHeader(AuthConstants.JWT_HEADER_KEY, token);
    }
}
