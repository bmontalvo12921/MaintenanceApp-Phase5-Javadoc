/**
 * Creates a new customer with the basic information used in the system.
 *
 * @param phoneNumber the customer's phone number (used as the unique ID)
 * @param name        the customer's name
 * @param address     where the customer lives or receives mail
 * @param email       optional email field for contact
 *
 *
 */
public class Customer {
    private String phoneNumber;
    private String name;
    private String address;
    private String email;

    public Customer(String phoneNumber, String name, String address, String email) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.address = address;
        this.email = email;
    }

    /**
     * Returns the customer's phone number.
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Returns the customer's full name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the customer's address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the customer's email. This field may be blank.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the customer's phone number.
     *
     * @param v new phone number
     */
    public void setPhoneNumber(String v) {
        phoneNumber = v;
    }

    /**
     * Updates the customer's name.
     *
     * @param v new name
     */
    public void setName(String v) {
        name = v;
    }

    /**
     * Updates the customer's address.
     *
     * @param v new address
     */
    public void setAddress(String v) {
        address = v;
    }

    /**
     * Updates the customer's email.
     *
     * @param v new email (optional field)
     */
    public void setEmail(String v) {
        email = v;
    }
}
