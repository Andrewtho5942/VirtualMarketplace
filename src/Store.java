import java.io.Serializable;
import java.util.ArrayList;

/**
 * The Store class creates a Store that is owned by a Seller and contains a list of Products.
 * Sellers are able to add and remove products from their stores and obtain info about their stores
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 4
 *
 * @author Grant Strickland
 * @version November 10, 2022
 */
public class Store implements Serializable {
    private String name;
    private ArrayList<Product> products;
    private String ownerName;

    //Constructor used to create an existing Store from a file
    public Store(String name, String ownerName, ArrayList<Product> products) {
        this.name = name;
        this.products = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            this.products.add(products.get(i));
        }
        this.ownerName = ownerName;
    }

    //Constructor used to create a completely new Store
    public Store(String name, String sellerName) {
        this.name = name;
        this.ownerName = sellerName;
        this.products = new ArrayList<>();
    }

    //Add a product to the store with an existing Product object
    public void addProduct(Product product) {
        products.add(product);
    }

    //Add a product to the Store using product info, that then creates a new object
    public void addProduct(String name, String description, double price, int quantityAvailable) {
        Product product = new Product(name, description, price, quantityAvailable, 0, name);
        products.add(product);
    }

    //Removes product from the list of products
    public boolean removeProduct(Product product) {
        for (int i = 0; i < products.size(); i++) {
            if (product.getName().equals(products.get(i).getName())) {
                products.remove(i);
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public String toString() {
        String output = name + "," + ownerName;
        for (int i = 0; i < products.size(); i++) {
            output += " | " + products.get(i).toString();
        }
        return output;

    }
}
