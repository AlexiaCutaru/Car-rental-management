package ro.rentacar.rental;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ro.rentacar.car.Car;
import ro.rentacar.car.CarRepository;
import ro.rentacar.customer.Customer;
import ro.rentacar.customer.CustomerRepository;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    public RentalService(
            RentalRepository rentalRepository,
            CarRepository carRepository,
            CustomerRepository customerRepository) {

        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void create(RentalForm form) {
        LocalDate start = form.getStartDate();
        LocalDate end = form.getEndDate();

        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "Completează perioada închirierii."
            );
        }

        if (start.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Data preluării nu poate fi în trecut."
            );
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException(
                    "Data returnării trebuie să fie după data preluării."
            );
        }

        if (form.getCarId() == null || form.getCustomerId() == null) {
            throw new IllegalArgumentException(
                    "Selectează mașina și clientul."
            );
        }

        Car car = carRepository.findByIdForReservation(form.getCarId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mașina selectată nu mai există."
                ));

        if (car.isInService()) {
            throw new IllegalArgumentException(
                    "Mașina este în service și nu poate fi închiriată."
            );
        }

        Customer customer = customerRepository.findById(form.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Clientul selectat nu mai există."
                ));

        long overlaps = rentalRepository.countOverlapping(
                car.getId(), start, end, RentalStatus.CANCELLED
        );

        if (overlaps > 0) {
            throw new IllegalArgumentException(
                    "Mașina este deja rezervată în perioada aleasă. "
                            + "Alege altă perioadă sau altă mașină."
            );
        }

        BigDecimal dailyPrice = car.getPricePerDay();

        if (dailyPrice == null || dailyPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Mașina nu are un tarif valid. Actualizează tariful."
            );
        }

        long days = ChronoUnit.DAYS.between(start, end);
        BigDecimal total = dailyPrice.multiply(BigDecimal.valueOf(days));

        if (total.compareTo(new BigDecimal("9999999999.99")) > 0) {
            throw new IllegalArgumentException(
                    "Costul depășește limita acceptată. Verifică perioada."
            );
        }

        Rental rental = new Rental();
        rental.setCar(car);
        rental.setCustomer(customer);
        rental.setStartDate(start);
        rental.setEndDate(end);
        rental.setPricePerDay(dailyPrice);
        rental.setTotalPrice(total);
        rental.setStatus(RentalStatus.RESERVED);

        rentalRepository.save(rental);
    }

    @Transactional
public void changeStatus(Long id, String action) {
    Rental rental = rentalRepository.findByIdForStatusUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Închirierea nu mai există."
            ));

    Car car = carRepository.findByIdForReservation(
                    rental.getCar().getId()
            )
            .orElseThrow(() -> new IllegalArgumentException(
                    "Mașina asociată nu mai există."
            ));

    RentalStatus current = rental.getStatus();

    switch (action) {
        case "start" -> {
            if (current != RentalStatus.RESERVED) {
                throw new IllegalArgumentException(
                        "Poți prelua doar o închiriere rezervată."
                );
            }

            LocalDate today = LocalDate.now();

            if (today.isBefore(rental.getStartDate())) {
                throw new IllegalArgumentException(
                        "Mașina poate fi preluată începând cu "
                                + rental.getStartDate() + "."
                );
            }

            if (!today.isBefore(rental.getEndDate())) {
                throw new IllegalArgumentException(
                        "Perioada rezervării a expirat. "
                                + "Anulează rezervarea și creează una nouă."
                );
            }

            if (car.isInService()) {
                throw new IllegalArgumentException(
                        "Mașina este în service și nu poate fi preluată."
                );
            }

            if (rentalRepository.existsByCarIdAndStatus(
                    car.getId(), RentalStatus.ACTIVE)) {
                throw new IllegalArgumentException(
                        "Mașina are deja o închiriere în desfășurare. "
                                + "Înregistrează întâi returnarea acesteia."
                );
            }

            rental.setStatus(RentalStatus.ACTIVE);
        }

        case "complete" -> {
            if (current != RentalStatus.ACTIVE) {
                throw new IllegalArgumentException(
                        "Poți finaliza doar o închiriere în desfășurare."
                );
            }

            rental.setStatus(RentalStatus.COMPLETED);
        }

        case "cancel" -> {
            if (current != RentalStatus.RESERVED) {
                throw new IllegalArgumentException(
                        "Poți anula doar o închiriere rezervată."
                );
            }

            rental.setStatus(RentalStatus.CANCELLED);
        }

        default -> throw new IllegalArgumentException(
                "Acțiunea solicitată nu este validă."
        );
    }

    rentalRepository.save(rental);
}
}