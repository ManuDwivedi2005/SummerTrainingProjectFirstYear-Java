public class Rental {
    private int rentalId;
    private String carId;
    private int customerId;
    private int days;

    public Rental(int rentalId, String carId, int customerId, int days) {
        this.rentalId = rentalId;
        this.carId = carId;
        this.customerId = customerId;
        this.days = days;
    }

    // Getters
    public int getRentalId() { return rentalId; }
    public String getCarId() { return carId; }
    public int getCustomerId() { return customerId; }
    public int getDays() { return days; }
}
