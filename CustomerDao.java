import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * Data Access Object (DAO) for Customer records. This class talks
 * directly to SQLite to perform create, read, update, and delete
 * operations. All SQL work stays here to keep the rest of the project clean.
 */
public class CustomerDao {
    /**
     * Creates the customers table if it does not already exist.
     *
     * @throws SQLException if the table cannot be created
     */

    public static void ensureTable() throws SQLException {
        try (Connection c = ConnectionManager.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS customers (
                  phone TEXT PRIMARY KEY,
                  name TEXT NOT NULL,
                  address TEXT NOT NULL,
                  email TEXT
                )
            """);
        }
    }
    /**
     * Inserts a new customer into the database. If the phone number
     * already exists, SQLite will ignore it.
     *
     * @param c the customer to insert
     * @return true if inserted successfully, false if ignored
     * @throws SQLException database failure
     */
    // insert (ignore on dup)
    public static boolean insert(Customer c) throws SQLException {
        String sql = "INSERT OR IGNORE INTO customers(phone,name,address,email) VALUES(?,?,?,?)";
        try (Connection cn = ConnectionManager.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getPhoneNumber());
            ps.setString(2, c.getName());
            ps.setString(3, c.getAddress());
            ps.setString(4, c.getEmail());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates an existing customer's name, address, or email.
     *
     * @param c customer object containing updated values
     * @return true if the update was successful
     * @throws SQLException database failure
     */
        public static boolean update(Customer c) throws SQLException {
        String sql = "UPDATE customers SET name=?, address=?, email=? WHERE phone=?";
        try (Connection cn = ConnectionManager.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getAddress());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPhoneNumber());
            return ps.executeUpdate() > 0;
        }
    }
    /**
     * Deletes a customer by phone number.
     *
     * @param phone phone number used as the key
     * @return true if deleted successfully
     * @throws SQLException database failure
     */
    public static boolean delete(String phone) throws SQLException {
        String sql = "DELETE FROM customers WHERE phone=?";
        try (Connection cn = ConnectionManager.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, phone);
            return ps.executeUpdate() > 0;
        }
    }
    /**
     * Looks up and returns a customer by phone number.
     *
     * @param phone normalized phone number to search for
     * @return matching Customer or null if not found
     * @throws SQLException database failure
     */
    public static Customer find(String phone) throws SQLException {
        String sql = "SELECT phone,name,address,email FROM customers WHERE phone=?";
        try (Connection cn = ConnectionManager.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4)
                    );
                }
                return null;
            }
        }
    }
    /**
     * Lists all customers in alphabetical order.
     *
     * @return list of all Customer objects
     * @throws SQLException database failure
     */
    public static List<Customer> listAll() throws SQLException {
        String sql = "SELECT phone,name,address,email FROM customers ORDER BY name";
        List<Customer> out = new ArrayList<>();
        try (Connection cn = ConnectionManager.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Customer(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)
                ));
            }
        }
        return out;
    }
}
