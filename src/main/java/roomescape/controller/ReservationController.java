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
import roomescape.annotation.LoginMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.dto.ResourceIdResponse;
import roomescape.dto.reservation.ReservationCreateRequest;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private static final String MEMBER_SESSION_KEY = "sessionKey";

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceIdResponse save(
        @LoginMember Member member,
        @Valid @RequestBody ReservationCreateRequest request
    ) {
        Reservation reservation = reservationService.save(request, member);
        return new ResourceIdResponse(reservation.getId());
    }
}
