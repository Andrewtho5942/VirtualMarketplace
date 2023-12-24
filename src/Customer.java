import java.io.Serializable;
import javax.swing.*;
import java.util.ArrayList;

/**
 * The Customer class provides the functionality of users trying to buy products from the Marketplace by
 * allowing them to buy products as well as add and remove things from their shopping cart.
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 4
 *
 * @author Grant Strickland
 * @version November 13, 2022
 */

public class Customer extends Person implements Serializable {
    private String history;
    private String shoppingCart;

    //Constructor used to create new customer
    public Customer(String name, String email, String password) {
        super(name, email, password);
        this.history = "";
        this.shoppingCart = "";
        FileAccess.createCustomerFile(this);
    }

    //Constructor used for returning customers
    public Customer(String name, String email, String password, String history, String shoppingCart) {
        super(name, email, password);
        this.history = history;
        this.shoppingCart = shoppingCart;
    }

    //Allows Customer to buy passed in product and decreases the amount of that product available
    public void buyProduct(Product product, int quantityPurchased) {
        addToHistory(product, quantityPurchased);
        int newQuantity = product.getQuantityAvailable() - quantityPurchased;
        product.setQuantityAvailable(newQuantity);
        FileAccess.updateQuantity(product);
        FileAccess.updateTransactionHistory(getName() + "," + product.getStoreName() + "," + product.getName() + "," +
                product.getPrice() + "," + quantityPurchased);
    }

    //Adds the name of the passed in Product to the Customer's history field
    public void addToHistory(Product product, int quantityPurchased) {
        history += product.getName() + "," + quantityPurchased + ";";
    }

    //Returns the history string
    public String getHistory() {
        return history;
    }

    //Returns shopping cart String
    public String getShoppingCart() {
        return shoppingCart;
    }

    //sets shopping cart String
    public void setShoppingCart(String newCart) {
        shoppingCart = newCart;
    }

    //Allows Customer to add product to their cart, along with how many of the product they want to buy
    //If the product is already in the cart, the quantity the user wants to buy is increased by
    //the passed in integer
    public void addToCart(Product product, int buyQuantity) {
        String[] cartArray = shoppingCart.split(";");
        boolean inCart = false;
        for (int i = 0; i < cartArray.length; i++) {
            int commaIndex = cartArray[i].indexOf(",");
            if (commaIndex != -1) {
                if (product.getName().equalsIgnoreCase(cartArray[i].substring(0, commaIndex))) {
                    int currentBuyQuantity = Integer.parseInt(cartArray[i].substring(commaIndex + 1));
                    currentBuyQuantity += buyQuantity;
                    cartArray[i] = cartArray[i].substring(0, commaIndex + 1) + currentBuyQuantity;
                    shoppingCart = String.join(";", cartArray) + ";";
                    inCart = true;
                }
            } else {
                shoppingCart = product.getName() + "," + buyQuantity + ";";
                inCart = true;
            }
        }
        if (!inCart) {
            shoppingCart += product.getName() + "," + buyQuantity + ";";
        }
        //update product's shopping cart count
        product.setShoppingCartCount(product.getShoppingCartCount() + buyQuantity);
        FileAccess.updateShoppingCart(shoppingCart, product, this.getName() + ".txt");
    }

    //Removes the passed in Product from the customer's shopping cart
    public void removeFromCart(Product product) {
        String[] cartArray = shoppingCart.split(";");
        for (int i = 0; i < cartArray.length; i++) {
            String[] temp = cartArray[i].split(",");
            if (temp[0].equalsIgnoreCase(product.getName())) {
                //update product's shopping cart count
                product.setShoppingCartCount(product.getShoppingCartCount() - Integer.parseInt(cartArray[i].
                        substring(cartArray[i].indexOf(",") + 1)));
                cartArray[i] = "";
            }
        }
        String newCart = "";
        for (int i = 0; i < cartArray.length; i++) {
            if (!(cartArray[i].equals(""))) {
                newCart += cartArray[i] + ";";
            }
        }
        shoppingCart = newCart;
        FileAccess.updateShoppingCart(shoppingCart, product, getName() + ".txt");
    }

    // ONLY removes the product's string from the shopping cart. used for updating the other thread when
    //a product is deleted.
    public void removeFromCart(String productName) {
        String[] cartArray = shoppingCart.split(";");
        for (int i = 0; i < cartArray.length; i++) {
            String[] temp = cartArray[i].split(",");
            if (temp[0].equalsIgnoreCase(productName)) {
                cartArray[i] = "";
            }
        }
        String newCart = "";
        for (int i = 0; i < cartArray.length; i++) {
            if (!(cartArray[i].equals(""))) {
                newCart += cartArray[i] + ";";
            }
        }
        shoppingCart = newCart;
    }

    //Allows the customer to buy every product in their cart at once
    public ArrayList<String> buyCart() {
        ArrayList<Product> products = FileAccess.getAllProducts();
        ArrayList<String> productChanges = new ArrayList<>();
        String[] cartArray = shoppingCart.split(";");
        for (int i = 0; i < cartArray.length; i++) {
            for (int j = 0; j < products.size(); j++) {
                int commaIndex = cartArray[i].indexOf(",");
                int quantity = Integer.parseInt(cartArray[i].split(",")[1]);
                String productName = products.get(j).getName();
                //if name matches, buy product and remove from cart
                if (cartArray[i].substring(0, commaIndex).equalsIgnoreCase(productName)) {
                    if (products.get(j).getQuantityAvailable() >= quantity) {
                        buyProduct(products.get(j), quantity);
                        removeFromCart(products.get(j));
                        //add product changes to the list
                        productChanges.add(productName + ",quantity");
                        productChanges.add(productName + ",cart count");
                    } else {
                        JOptionPane.showMessageDialog(null, "ERROR: There are not enough units" +
                                " of " + productName +
                                " to purchase.", "Error", JOptionPane.ERROR_MESSAGE);
                        removeFromCart(products.get(j));
                    }
                }
            }
        }
        return productChanges;
    }


}
