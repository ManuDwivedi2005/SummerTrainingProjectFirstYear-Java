import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/car_rental_system";
    private static final String DB_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "root"; // Change this to your MySQL root password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cars.add(new Car(
                        rs.getString("car_id"),
                        rs.getString("brand"),
                        rs.getString("model"),
                        rs.getDouble("base_price_per_day"),
                        rs.getBoolean("is_available")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    public static void addCar(Car car) {
        String sql = "INSERT INTO cars (car_id, brand, model, base_price_per_day, is_available) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, car.getCarId());
            pstmt.setString(2, car.getBrand());
            pstmt.setString(3, car.getModel());
            pstmt.setDouble(4, car.getBasePricePerDay());
            pstmt.setBoolean(5, car.isAvailable());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateCarAvailability(String carId, boolean isAvailable) {
        String sql = "UPDATE cars SET is_available = ? WHERE car_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isAvailable);
            pstmt.setString(2, carId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int addCustomer(String name) {
        String sql = "INSERT INTO customers (name) VALUES (?)";
        int customerId = 0;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                customerId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerId;
    }

    public static void addRental(String carId, int customerId, int rentalDays) {
        String sql = "INSERT INTO rentals (car_id, customer_id, rental_days, rental_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, carId);
            pstmt.setInt(2, customerId);
            pstmt.setInt(3, rentalDays);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Rental getRentalByCarId(String carId) {
        String sql = "SELECT * FROM rentals WHERE car_id = ? ORDER BY rental_date DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, carId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Rental(
                        rs.getInt("rental_id"),
                        rs.getString("car_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("rental_days")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void removeRental(int rentalId) {
        String sql = "DELETE FROM rentals WHERE rental_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, rentalId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
