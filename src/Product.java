import java.io.Serializable;

/**
 * The Product class creates the products that are then sold in the stores. They have names, descriptions, prices,
 * a certain quantity that is available and a Store they are associated with.
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 4
 *
 * @author Grant Strickland
 * @version November 10, 2022
 */
public class Product implements Serializable {
    private String name;
    private String description;
    private double price;
    private int quantityAvailable;
    private int shoppingCartCount;
    private String storeName;

    public Product(String name, String description, double price, int quantityAvailable,
                   int shoppingCartCount, String storeName) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantityAvailable = quantityAvailable;
        this.shoppingCartCount = shoppingCartCount;
        this.storeName = storeName;

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantityAvailable(int quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public int getShoppingCartCount() {
        return shoppingCartCount;
    }

    public void setShoppingCartCount(int count) {
        shoppingCartCount = count;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String toString() {
        return String.format("%s,%s,%s,%d,%.2f,%d", storeName, name,
                description, quantityAvailable, price, shoppingCartCount);
    }

    public String toReadable() {
        return String.format("| %-18s | %-12s | %-30s | %-8d | $%-11.2f |", name, storeName,
                description, quantityAvailable, price);
    }
}