package roomescape.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.annotation.LoginMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.dto.ResourceIdResponse;
import roomescape.dto.member.MemberSummary;
import roomescape.dto.reservation.ReservationCreateRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceIdResponse save(
        @LoginMember MemberSummary member,
        @Valid @RequestBody ReservationCreateRequest request
    ) {
        Reservation reservation = reservationService.save(request, member.id());
        return new ResourceIdResponse(reservation.getId());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponse> findAllByMember(
        @LoginMember MemberSummary member
    ) {
        return reservationService.findAllByMemberId(member.id()).stream()
            .map(ReservationResponse::from)
            .toList();
    }
}
