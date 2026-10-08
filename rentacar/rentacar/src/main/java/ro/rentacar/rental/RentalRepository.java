package ro.rentacar.rental;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    @Query("""
        select count(r)
        from Rental r
        where r.car.id = :carId
          and r.status <> :cancelled
          and r.startDate < :endDate
          and r.endDate > :startDate
        """)
    long countOverlapping(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("cancelled") RentalStatus cancelled);

    boolean existsByCarIdAndStatus(
            Long carId,
            RentalStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Rental r where r.id = :id")
    Optional<Rental> findByIdForStatusUpdate(
            @Param("id") Long id);
}