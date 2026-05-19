package roomescape.repository.reservation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;

@Repository
public class MemoryReservationRepository implements ReservationRepository {

    private static final Map<Long, Reservation> RESERVATIONS = new ConcurrentHashMap<>();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    @Override
    public List<Reservation> findAll() {
        return RESERVATIONS.values().stream().toList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        long id = ID_GENERATOR.getAndIncrement();
        Reservation target = reservation.withId(id);

        RESERVATIONS.put(id, target);
        return target.withId(id);
    }
}
