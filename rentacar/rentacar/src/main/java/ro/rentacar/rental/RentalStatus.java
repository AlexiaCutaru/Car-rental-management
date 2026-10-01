package ro.rentacar.rental;

public enum RentalStatus {

    RESERVED("Rezervată"),
    ACTIVE("În desfășurare"),
    COMPLETED("Finalizată"),
    CANCELLED("Anulată");

    private final String label;

    RentalStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}