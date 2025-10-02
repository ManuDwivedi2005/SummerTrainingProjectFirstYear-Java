import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class CarRentalSystemGUI extends JFrame {
    private List<Car> cars;
    private JTextArea displayArea;
    private JComboBox<Car> availableCarsComboBox;

    public CarRentalSystemGUI() {
        cars = DatabaseManager.getAllCars();

        setTitle("Car Rental System");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        JTextField customerNameField = new JTextField();
        JTextField rentalDaysField = new JTextField();

        inputPanel.add(new JLabel("Customer Name:"));
        inputPanel.add(customerNameField);

        inputPanel.add(new JLabel("Available Cars:"));
        availableCarsComboBox = new JComboBox<>();
        inputPanel.add(availableCarsComboBox);

        inputPanel.add(new JLabel("Rental Days:"));
        inputPanel.add(rentalDaysField);

        add(inputPanel, BorderLayout.NORTH);

        JButton rentButton = new JButton("Rent Car");
        rentButton.addActionListener(e -> rentCar(customerNameField.getText(), rentalDaysField.getText()));

        JButton returnButton = new JButton("Return Car");
        returnButton.addActionListener(e -> returnCar());

        JButton addCarButton = new JButton("Add Car");
        addCarButton.addActionListener(e -> adminLogin());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(rentButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(addCarButton);

        add(buttonPanel, BorderLayout.CENTER);

        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);
        add(new JScrollPane(displayArea), BorderLayout.SOUTH);

        updateAvailableCarsList();
    }

    public void updateAvailableCarsList() {
        availableCarsComboBox.removeAllItems();
        cars = DatabaseManager.getAllCars(); // Refresh car list from DB
        for (Car car : cars) {
            if (car.isAvailable()) {
                availableCarsComboBox.addItem(car);
            }
        }
    }

    public void rentCar(String customerName, String rentalDaysStr) {
        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter customer name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int rentalDays;
        try {
            rentalDays = Integer.parseInt(rentalDaysStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid rental days.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Car selectedCar = (Car) availableCarsComboBox.getSelectedItem();

        if (selectedCar != null && selectedCar.isAvailable()) {
            int customerId = DatabaseManager.addCustomer(customerName);
            DatabaseManager.addRental(selectedCar.getCarId(), customerId, rentalDays);
            DatabaseManager.updateCarAvailability(selectedCar.getCarId(), false);

            double totalPrice = selectedCar.getBasePricePerDay() * rentalDays;
            displayArea.append("Car rented: " + selectedCar.getBrand() + " " + selectedCar.getModel() +
                    " (ID: " + selectedCar.getCarId() + ") by " + customerName + ". Total Price: $" + totalPrice + "\n");

            updateAvailableCarsList();
        } else {
            displayArea.append("Car not available for rent.\n");
        }
    }

    public void returnCar() {
        String carIdToReturn = JOptionPane.showInputDialog(this, "Enter the Car ID to return:");
        if (carIdToReturn == null || carIdToReturn.trim().isEmpty()) {
            return;
        }

        Rental rental = DatabaseManager.getRentalByCarId(carIdToReturn);

        if (rental != null) {
            DatabaseManager.removeRental(rental.getRentalId());
            DatabaseManager.updateCarAvailability(carIdToReturn, true);
            
            // Find car brand and model for display message
            String carInfo = "";
            for(Car car : cars) {
                if(car.getCarId().equals(carIdToReturn)) {
                    carInfo = car.getBrand() + " " + car.getModel();
                    break;
                }
            }

            displayArea.append("Car returned: " + carInfo + "\n");
            updateAvailableCarsList();
        } else {
            displayArea.append("Car ID not found or was not rented.\n");
        }
    }

    public void adminLogin() {
        // ... (admin login and add car dialog logic remains the same)
        JPanel loginPanel = new JPanel(new GridLayout(2, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(this, loginPanel, "Admin Login", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            String adminUser = System.getenv("ADMIN_USERNAME") != null ? System.getenv("ADMIN_USERNAME") : "admin";
            String adminPass = System.getenv("ADMIN_PASSWORD") != null ? System.getenv("ADMIN_PASSWORD") : "12345";
            if (username.equals(adminUser) && password.equals(adminPass)) {
                showAddCarDialog();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid login credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void showAddCarDialog() {
        JDialog addCarDialog = new JDialog(this, "Add New Car", true);
        addCarDialog.setSize(300, 300);
        addCarDialog.setLayout(new GridLayout(5, 2));

        JTextField carIdField = new JTextField();
        JTextField brandField = new JTextField();
        JTextField modelField = new JTextField();
        JTextField basePriceField = new JTextField();

        addCarDialog.add(new JLabel("Car ID:"));
        addCarDialog.add(carIdField);
        addCarDialog.add(new JLabel("Brand:"));
        addCarDialog.add(brandField);
        addCarDialog.add(new JLabel("Model:"));
        addCarDialog.add(modelField);
        addCarDialog.add(new JLabel("Base Price Per Day:"));
        addCarDialog.add(basePriceField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String carId = carIdField.getText();
            String brand = brandField.getText();
            String model = modelField.getText();
            double basePrice = Double.parseDouble(basePriceField.getText());

            Car newCar = new Car(carId, brand, model, basePrice, true);
            DatabaseManager.addCar(newCar);
            updateAvailableCarsList();

            displayArea.append("New car added: " + brand + " " + model + "\n");
            addCarDialog.dispose();
        });

        addCarDialog.add(saveButton);
        addCarDialog.setVisible(true);
    }
}
