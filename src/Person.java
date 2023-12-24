import java.io.Serializable;

/**
 * The Person class contains basic attributes that both Sellers and Customers have,
 * cutting down on the amount of code we had to write by having both classes inherit from Person
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 4
 *
 * @author Grant Strickland
 * @version November 10, 2022
 */
public class Person implements Serializable {
    private String name;
    private String email;
    private String password;

    //Constructor won't be used on its own, should be called as super
    //in the Customer and Seller classes.
    public Person(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
