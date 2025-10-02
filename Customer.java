public class Customer {
    private int customerId;
    private String name;

    public Customer(int customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    // Getters
    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
}
