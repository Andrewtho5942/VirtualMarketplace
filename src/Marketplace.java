import javax.swing.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Array;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;

/**
 * This class handles the user interface aspect of the marketplace, enabling the
 * user to: create/log into an account, search for and purchase items in the
 * marketplace as a customer, and manage stores as a seller
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 5
 *
 * @author Michael Lenkeit
 * @version November 17, 2022
 */

public class Marketplace extends Thread {
    private int startIndex;
    //Arraylist for product updates
    private ArrayList<String> globalChanges;
    private ArrayList<String> localChanges;

    private Socket socket;
    private static final Object lock = new Object();

    //constructor
    public Marketplace() {
        globalChanges = null;
        localChanges = new ArrayList<>();
    }

    // Main method where the threading starts (runs the run method at the bottom)
    public static void main(String[] args) {
        //create new file and/or reset globalChanges.txt contents to nothing
        try {
            new PrintWriter("globalChanges.txt").close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Marketplace market2 = new Marketplace();
        market2.start();
    }

    //Prints out all the products passed in as a parameter, using a Bubble Sort to sort based on alphabetical order
    //(sortType = 1), price (sortType = 2), quantity available (sortType = 3), or in no particular order (sortType = 4)
    public static ArrayList<Product> printAllProducts(ArrayList<Product> products, int sortType) {
        if (sortType != 0) {
            boolean sorted = false;
            Product temp;
            while (!sorted) {
                sorted = true;
                for (int i = 0; i < products.size() - 1; i++) {
                    //Bubble sort algorithm: if statement returns true if a swap should occur
                    if ((sortType == 1 && products.get(i).getName().toUpperCase()
                            .compareTo(products.get(i + 1).getName().toUpperCase()) > 0)
                            || (sortType == 2 && products.get(i).getPrice() > products.get(i + 1).getPrice())
                            || (sortType == 3 && products.get(i).getQuantityAvailable() > products.get(i + 1)
                            .getQuantityAvailable())) {
                        temp = products.get(i);
                        products.set(i, products.get(i + 1));
                        products.set(i + 1, temp);
                        sorted = false;
                    }
                }
            }
        }
        //Prints out a properly formatted version of the product
        return products;
    }

    //calculate and print any changes made by other threads since the thread's start.
    public void showThreadChanges(String sellerFile, Person person, ArrayList<String> passedChanges,
                                  ObjectInputStream objectInputStream,
                                  ObjectOutputStream objectOutputStream) throws SocketException {
        try {
            localChanges.addAll(passedChanges);
            synchronized (lock) {
                objectOutputStream.writeObject("readFile");
                objectOutputStream.flush();
                objectOutputStream.writeObject("globalChanges.txt");
                objectOutputStream.flush();
                globalChanges = (ArrayList<String>) objectInputStream.readObject();
            }
            //print out product updates from other threads here
            String allUpdates = "";
            for (int i = startIndex; i < globalChanges.size(); i++) {
                boolean matchWasFound = false;
                for (int x = localChanges.size() - 1; x >= 0; x--) {
                    if (globalChanges.get(i).equals(localChanges.get(x))) {
                        //if change is made from this thread, do not print message and delete from local changes.
                        matchWasFound = true;
                        localChanges.remove(localChanges.get(x));
                    }
                }
                //if no local changes match (change is from other thread), print out the update message:
                if (!matchWasFound) {
                    int commaIndex = globalChanges.get(i).indexOf(",");
                    String message = globalChanges.get(i).substring(commaIndex + 1);
                    String name = globalChanges.get(i).substring(0, commaIndex);
                    if (sellerFile.equals("")) {
                        switch (message) {
                            case "name":
                                allUpdates += String.format("-> Name was changed for product '%s'.\n", name);
                                break;
                            case "store":
                                allUpdates += String.format("-> Store name was changed for product '%s'.\n", name);
                                break;
                            case "description":
                                allUpdates += String.format("-> Description was changed for product '%s'.\n", name);
                                break;
                            case "quantity":
                                allUpdates += String.format("-> Quantity was changed for product '%s'.\n", name);
                                break;
                            case "price":
                                allUpdates += String.format("-> Price was changed for product '%s'.\n", name);
                                break;
                            case "add":
                                allUpdates += String.format("-> Product '%s' was added to the market.\n", name);
                                break;
                            case "delete":
                                allUpdates += String.format("-> Product '%s' was deleted from the market.\n", name);
                                //remove deleted product from the shopping cart:
                                ArrayList<Product> allProducts;
                                synchronized (lock) {
                                    objectOutputStream.writeObject("getAllProducts");
                                    objectOutputStream.flush();
                                    allProducts = (ArrayList<Product>) objectInputStream.readObject();
                                }
                                //remove the string from the customer's shopping cart
                                ((Customer) person).removeFromCart(name);
                                break;
                        }
                    } else if (isProductSoldBy(name, sellerFile, objectInputStream, objectOutputStream)) {
                        switch (message) {
                            case "quantity":
                                allUpdates += String.format("-> Quantity was changed for your product '%s'.\n", name);
                                break;
                            case "cart count":
                                allUpdates += String.format("-> Shopping cart count was changed for your product" +
                                        " '%s'.\n", name);
                                break;
                        }
                    }
                }
            }
            if (!allUpdates.equals("")) {
                JOptionPane.showMessageDialog(null, allUpdates, "Marketplace Update", -1);
            }
            startIndex = globalChanges.size();
        } catch (SocketException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //loops through the seller's file, returning true if a matching product name was found.
    public boolean isProductSoldBy(String productName, String sellerFile, ObjectInputStream objectInputStream,
                                   ObjectOutputStream objectOutputStream) throws SocketException {
        try {
            ArrayList<String> fileContents;
            synchronized (lock) {
                objectOutputStream.writeObject("readFile");
                objectOutputStream.flush();
                objectOutputStream.writeObject(sellerFile);
                objectOutputStream.flush();
                fileContents = (ArrayList<String>) objectInputStream.readObject();
            }
            for (int i = 2; i < fileContents.size(); i++) {
                String[] lineValues = fileContents.get(i).split(",");
                if (lineValues[1].equalsIgnoreCase(productName)) {
                    return true;
                }
            }
            return false;
        } catch (SocketException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Sets the customer with specific parameters based on whether  they have a purchase history or a shopping cart
    public static Customer returningCustomerSetter(String name, String email, String password,
                                                   ObjectInputStream objectInputStream,
                                                   ObjectOutputStream objectOutputStream) throws SocketException {
        try {
            ArrayList<String> fileContents;
            synchronized (lock) {
                objectOutputStream.writeObject("readFile");
                objectOutputStream.flush();
                objectOutputStream.writeObject(name + ".txt");
                objectOutputStream.flush();
                fileContents = (ArrayList<String>) objectInputStream.readObject();
            }

            if (fileContents.size() == 2) {
                return new Customer(name, email, password);
            } else if (fileContents.size() == 3) {
                return new Customer(name, email, password, fileContents.get(2), "");
            } else {
                if (fileContents.get(2).equals("")) {
                    return new Customer(name, email, password, "", fileContents.get(3));
                } else {
                    return new Customer(name, email, password, fileContents.get(2), fileContents.get(3));
                }
            }
        } catch (SocketException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Searches through the Seller's products and removes it if it exists
    public static String deleteProduct(Seller s, ObjectOutputStream objectOutputStream,
                                       ObjectInputStream objectInputStream) throws SocketException {
        try {
            ArrayList<Product> allProducts;
            synchronized (lock) {
                objectOutputStream.writeObject("getSellerProducts");
                objectOutputStream.flush();
                objectOutputStream.writeObject(s);
                objectOutputStream.flush();
                allProducts = (ArrayList<Product>) objectInputStream.readObject();
            }
            String[] productNames = new String[allProducts.size()];
            for (int i = 0; i < allProducts.size(); i++) {
                productNames[i] = allProducts.get(i).getName();
            }
            String selectedName = (String) JOptionPane.showInputDialog(null, "Which " +
                            "product would you like to delete?", "Delete Product", JOptionPane.QUESTION_MESSAGE,
                    null, productNames, null);
            if (selectedName != null) {
                Product selectedProduct = null;
                for (int j = 0; j < productNames.length; j++) {
                    if (productNames[j].equalsIgnoreCase(selectedName)) {
                        selectedProduct = allProducts.get(j);
                    }
                }
                if (selectedProduct != null) {
                    ArrayList<Store> tempStoreList = s.getStoreList();
                    for (int i = 0; i < tempStoreList.size(); i++) {
                        ArrayList<Product> tempProductList = tempStoreList.get(i).getProducts();
                        for (int j = 0; j < tempProductList.size(); j++) {
                            if (tempProductList.get(j).getName().equalsIgnoreCase(selectedName)) {
                                tempStoreList.get(i).removeProduct(selectedProduct);
                                //if the last product in a store is deleted, delete the store with it.
                                if (tempProductList.size() == 0) {
                                    ArrayList<Store> newList = s.getStoreList();
                                    newList.remove(tempStoreList.get(i));
                                    s.setStoreList(newList);
                                }
                            }
                        }
                    }

                    JOptionPane.showMessageDialog(null, "Product Deleted", "Delete Product",
                            JOptionPane.INFORMATION_MESSAGE);
                    synchronized (lock) {
                        objectOutputStream.writeObject("deleteProductInFiles");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(selectedProduct);
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(s);
                        objectOutputStream.flush();
                        objectOutputStream.writeObject("appendToFile");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(selectedProduct.getName() + ",delete");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject("globalChanges.txt");
                        objectOutputStream.flush();
                    }
                    return selectedProduct.getName() + ",delete";

                } else {
                    JOptionPane.showMessageDialog(null, "That product does not exist or is " +
                            "not owned by you.", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return "";
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    //Enables the user to change a specific aspect of one of their products, making sure that is still valid
    public String editProduct(Seller s, ObjectInputStream objectInputStream,
                              ObjectOutputStream objectOutputStream) throws SocketException {
        try {
            int variable = 0;
            String change = "";
            String oldName = "";
            String finalChange = "";
            ArrayList<Product> allProducts;
            synchronized (lock) {
                objectOutputStream.writeObject("getSellerProducts");
                objectOutputStream.flush();
                objectOutputStream.writeObject(s);
                objectOutputStream.flush();
                allProducts = (ArrayList<Product>) objectInputStream.readObject();
            }
            String[] productNames = new String[allProducts.size()];
            for (int i = 0; i < allProducts.size(); i++) {
                productNames[i] = allProducts.get(i).getName();
            }
            String selectedName = (String) JOptionPane.showInputDialog(null, "Which product " +
                            "would you like to edit?", "Edit Product", JOptionPane.QUESTION_MESSAGE, null,
                    productNames, null);
            Product selectedProduct = null;
            for (int i = 0; i < allProducts.size(); i++) {
                if (allProducts.get(i).getName().equals(selectedName)) {
                    selectedProduct = allProducts.get(i);
                }
            }
            if (selectedProduct != null) {
                oldName = selectedProduct.getName();
                String[] editOptions = {"Name", "Store Name", "Description", "Quantity", "Price"};
                int selectedIndex = JOptionPane.showOptionDialog(null, "What would you like " +
                                "to edit?", "Edit Product", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, editOptions, null);
                if (selectedIndex == -1)
                    return null;
                if (selectedIndex == 0) { // Edit Product Name
                    variable = 1;
                    change = "name";
                    String newName = "";
                    boolean status;
                    do {
                        newName = JOptionPane.showInputDialog(null, "What is the new pr" +
                                "oduct name?", "Edit Product", JOptionPane.QUESTION_MESSAGE);
                        if (newName == null)
                            return null;
                        synchronized (lock) {
                            objectOutputStream.writeObject("searchProduct");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(newName);
                            objectOutputStream.flush();
                            status = (boolean) objectInputStream.readObject();
                        }
                        if (newName.equals("")) {
                            JOptionPane.showMessageDialog(null, "Invalid input! Please ente" +
                                            "r at least one character for the product name.", "Edit Product",
                                    JOptionPane.ERROR_MESSAGE);
                        } else if (status) {
                            JOptionPane.showMessageDialog(null, "That product name is already " +
                                            "taken! Please enter a different product name.", "Edit Product",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } while (newName.equals("") || status);
                    //change the name in every customer's shopping cart *before* product's name is changed
                    synchronized (lock) {
                        objectOutputStream.writeObject("changeNameInCustomer");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(selectedProduct.getName());
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(newName);
                        objectOutputStream.flush();
                    }
                    //set new name
                    selectedProduct.setName(newName);
                } else if (selectedIndex == 1) { // Edit Store name
                    variable = 2;
                    change = "store";
                    String newName = "";
                    boolean nameTaken = false;
                    do {
                        nameTaken = false;
                        newName = JOptionPane.showInputDialog(null, "What is the new store " +
                                "name?", "Edit Product", JOptionPane.QUESTION_MESSAGE);
                        if (newName == null)
                            return null;
                        if (newName.equals("")) {
                            JOptionPane.showMessageDialog(null, "Invalid input! Please enter" +
                                            " at least one character for the store name.", "Edit Product",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            ArrayList<Store> allStores;
                            synchronized (lock) {
                                objectOutputStream.writeObject("getAllStores");
                                objectOutputStream.flush();
                                allStores = (ArrayList<Store>) objectInputStream.readObject();
                            }
                            for (int i = 0; i < allStores.size(); i++) {
                                if (allStores.get(i).getName().equalsIgnoreCase(newName) &&
                                        !(allStores.get(i).getOwnerName().equalsIgnoreCase(s.getName()))) {
                                    nameTaken = true;
                                    break;
                                }
                            }
                            if (nameTaken) {
                                JOptionPane.showMessageDialog(null, "That store name is " +
                                                "already taken! Please enter a unique store name.", "Edit Product",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } while (nameTaken || newName.equals(""));
                    //Removes the old product from whatever store it was in previously and adds the updated version
                    //of the product to its new store
                    Product oldP = selectedProduct;
                    selectedProduct.setStoreName(newName);
                    boolean wasAdded = false;

                    ArrayList<Store> tempStoreList = s.getStoreList();
                    for (int i = 0; i < tempStoreList.size(); i++) {
                        if (tempStoreList.get(i).getProducts().contains(oldP)) {
                            tempStoreList.get(i).removeProduct(oldP);
                        }
                        if (tempStoreList.get(i).getName().equalsIgnoreCase(newName)) {
                            tempStoreList.get(i).addProduct(selectedProduct);
                            wasAdded = true;
                        }
                    }
                    //If it wasn't added to an existing store then create a new Store and add it to that
                    if (!wasAdded) {
                        ArrayList<Product> temp = new ArrayList<>();
                        temp.add(selectedProduct);
                        s.addStore(new Store(selectedProduct.getStoreName(), s.getName(), temp));
                    }
                } else if (selectedIndex == 2) { // Edit description
                    variable = 3;
                    change = "description";
                    String description;
                    do {
                        description = JOptionPane.showInputDialog(null, "What is the new " +
                                "description?", "Edit Product", JOptionPane.QUESTION_MESSAGE);
                        if (description == null)
                            return null;
                        if (description.equals("")) {
                            JOptionPane.showMessageDialog(null, "Invalid input! Please enter " +
                                            "at least one character for the product description.", "Edit Product",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        if (description.contains(",")) {
                            JOptionPane.showMessageDialog(null, "Invalid input! The " +
                                            "description cannot contain commas.", "Edit Product",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } while (description.equals("") || description.contains(","));
                    ArrayList<Store> tempStoreList = s.getStoreList();
                    //match product name with object reference and change its description.
                    for (int i = 0; i < tempStoreList.size(); i++) {
                        ArrayList<Product> tempProductList = tempStoreList.get(i).getProducts();
                        for (int j = 0; j < tempProductList.size(); j++) {
                            if (tempProductList.get(j).getName().equalsIgnoreCase(selectedProduct.getName())) {
                                selectedProduct.setDescription(description);
                            }
                        }
                    }
                } else if (selectedIndex == 3) { // Edit quantity
                    variable = 4;
                    change = "quantity";
                    int newQuantity = 0;
                    do {
                        try {
                            String quantityString = JOptionPane.showInputDialog(null,
                                    "Enter the new quantity", "Edit Product",
                                    JOptionPane.QUESTION_MESSAGE);
                            if (quantityString == null)
                                return null;
                            newQuantity = Integer.parseInt(quantityString);
                            if (newQuantity <= 0) {
                                JOptionPane.showMessageDialog(null, "Invalid input!" +
                                                " Please enter a valid positive number for the quantity.", "Edit Product",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(null, "Invalid input! Please enter" +
                                            " a valid positive number for the quantity.", "Edit Product",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } while (newQuantity <= 0);
                    ArrayList<Store> tempStoreList = s.getStoreList();
                    for (int i = 0; i < tempStoreList.size(); i++) {
                        ArrayList<Product> tempProductList = tempStoreList.get(i).getProducts();
                        for (int j = 0; j < tempProductList.size(); j++) {
                            if (tempProductList.get(j).getName().equalsIgnoreCase(selectedProduct.getName())) {
                                tempStoreList.get(i).removeProduct(selectedProduct);
                                selectedProduct.setQuantityAvailable(newQuantity);
                                tempStoreList.get(i).addProduct(selectedProduct);
                            }
                        }
                    }
                } else if (selectedIndex == 4) { // Edit Price
                    variable = 5;
                    change = "price";
                    double price = 0.0;
                    do {
                        try {
                            String priceString = JOptionPane.showInputDialog(null, "Enter " +
                                    "the new price", "Edit Product", JOptionPane.QUESTION_MESSAGE);
                            if (priceString == null)
                                return null;
                            price = Double.parseDouble(priceString);
                            price = Double.parseDouble(String.format("%.2f", price));
                            if (price <= 0) {
                                JOptionPane.showMessageDialog(null, "Invalid input! Please " +
                                                "enter a valid positive number for the price.", "Edit Product",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(null, "Invalid input! Please enter " +
                                            "a valid positive number for the price.", "Edit Product",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } while (price <= 0);
                    ArrayList<Store> tempStoreList = s.getStoreList();
                    for (int i = 0; i < tempStoreList.size(); i++) {
                        ArrayList<Product> tempProductList = tempStoreList.get(i).getProducts();
                        for (int j = 0; j < tempProductList.size(); j++) {
                            if (tempProductList.get(j).getName().equalsIgnoreCase(selectedProduct.getName())) {
                                tempStoreList.get(i).removeProduct(selectedProduct);
                                selectedProduct.setPrice(price);
                                tempStoreList.get(i).addProduct(selectedProduct);
                            }
                        }
                    }
                }
                if (variable < 6) {
                    finalChange = oldName + "," + change;
                    synchronized (lock) {
                        objectOutputStream.writeObject("updateProduct");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(selectedProduct);
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(s.getName() + ".txt");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(variable);
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(oldName);
                        objectOutputStream.flush();
                    }
                    if (variable < 3) {
                        synchronized (lock) {
                            objectOutputStream.writeObject("replaceInTransactionHistory");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(selectedProduct);
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(oldName);
                            objectOutputStream.flush();
                        }
                    }
                    synchronized (lock) {
                        objectOutputStream.writeObject("appendToFile");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(selectedProduct.getName() + "," + change);
                        objectOutputStream.flush();
                        objectOutputStream.writeObject("globalChanges.txt");
                        objectOutputStream.flush();
                    }
                    localChanges.add(selectedProduct.getName() + "," + change);
                }
                return finalChange;
            }
        } catch (SocketException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Searches for a store to view the sales of and outputs it to the user
    public static void viewSales(Seller s, ObjectInputStream objectInputStream,
                                 ObjectOutputStream objectOutputStream) throws SocketException {
        try {
            ArrayList<String> transactionHistory;
            synchronized (lock) {
                objectOutputStream.writeObject("readFile");
                objectOutputStream.flush();
                objectOutputStream.writeObject("transactionHistory.txt");
                objectOutputStream.flush();
                transactionHistory = (ArrayList<String>) objectInputStream.readObject();
            }
            if (s.getStoreList().isEmpty()) {
                JOptionPane.showMessageDialog(null, "You don't have any stores yet!",
                        "View Sales", JOptionPane.INFORMATION_MESSAGE);
            } else {
                ArrayList<Store> allStores = new ArrayList<>();
                ArrayList<Store> tempStoreList = s.getStoreList();
                for (int i = 0; i < tempStoreList.size(); i++) {
                    allStores.add(tempStoreList.get(i));
                }

                String[] allStoreNames = new String[allStores.size()];
                for (int i = 0; i < allStores.size(); i++) {
                    allStoreNames[i] = allStores.get(i).getName();
                }
                String allStoresString = allStores.toString();
                int storeNumber = JOptionPane.showOptionDialog(null, "Which store would you" +
                                " like to view the sales of?", "View Sales", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, allStoreNames, null);
                if (storeNumber != -1) {
                    String storeString = allStoreNames[storeNumber];
                    ArrayList<String> salesList = new ArrayList<>();
                    //search for products if the store name was found
                    if (allStoresString.toLowerCase().contains(storeString.toLowerCase())) {

                        for (int i = 0; i < transactionHistory.size(); i++) {
                            if (!transactionHistory.get(i).isEmpty()) {
                                String[] split = transactionHistory.get(i).split(",");
                                if (storeString.equalsIgnoreCase(split[1])) {
                                    salesList.add(transactionHistory.get(i));
                                }
                            }
                        }
                    }
                    if (salesList.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No sales for this store yet!",
                                "View Sales", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        //processing sales strings to make them look nicer
                        ArrayList<String> processedSales = new ArrayList<>();
                        double totalRevenue = 0;
                        for (int i = 0; i < salesList.size(); i++) {
                            String[] saleInfo = salesList.get(i).split(",");
                            double localRevenue = (Double.parseDouble(saleInfo[3])) * (Integer.parseInt(saleInfo[4]));
                            processedSales.add(String.format("- %s bought %d %s(s)  |  $%.2f", saleInfo[0],
                                    Integer.parseInt(saleInfo[4]), saleInfo[2], localRevenue));
                            totalRevenue += localRevenue;
                        }
                        processedSales.add(String.format("\n>> Total Revenue for This Store: $%.2f", totalRevenue));

                        JOptionPane.showMessageDialog(null, String.join("\n", processedSales),
                                "Sales for " + storeString, -1);
                    }
                }
            }
        } catch (SocketException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Prints out all the products that users currently have in shopping carts
    public static void viewCartedProducts(ArrayList<Product> products) {
        if (products.size() == 0) {
            JOptionPane.showMessageDialog(null, "You don't currently have any products",
                    "View Carted Products", JOptionPane.ERROR_MESSAGE);
        } else {
            String cartCounter = "\tYour Products:\n";
            for (int i = 0; i < products.size(); i++) {
                int cartCount = products.get(i).getShoppingCartCount();
                if (cartCount == 1) {
                    cartCounter += " - There is " + cartCount + " " + products.get(i).getName() + " items \n";
                } else {
                    cartCounter += " - There are " + cartCount + " carted " + products.get(i).getName() + " items\n";
                }

            }
            JOptionPane.showMessageDialog(null, cartCounter, "View Carted Products",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    //Enables the user to create a new product by letting them input what they want its name, store name, description,
    //price and quantity to be
    public void newProduct(Seller currentSeller, ObjectInputStream objectInputStream,
                           ObjectOutputStream objectOutputStream) throws SocketException {
        try {
            String[] createOptions = new String[2];
            createOptions[0] = "From Scratch";
            createOptions[1] = "From CSV File";
            int createChoice = JOptionPane.showOptionDialog(null, "How would you like to " +
                            "create a product?", "Create Product", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, createOptions, null);
            if (createChoice == 0) { //create from scratch
                String productName;
                String productDescription;
                double price;
                int quantityAvailable;
                String storeName;
                productName = JOptionPane.showInputDialog(null, "Enter the product's name.",
                        "Create Product", JOptionPane.PLAIN_MESSAGE);
                if (productName == null) {
                    return;
                }
                ArrayList<Product> allProducts;
                synchronized (lock) {
                    objectOutputStream.writeObject("getAllProducts");
                    objectOutputStream.flush();
                    allProducts = (ArrayList<Product>) objectInputStream.readObject();
                }
                ArrayList<String> allProductNames = new ArrayList<>();
                for (int i = 0; i < allProducts.size(); i++) {
                    allProductNames.add(allProducts.get(i).getName());
                }
                if (allProductNames.contains(productName.toLowerCase())) {
                    JOptionPane.showMessageDialog(null, "A product with that name already" +
                            " exists!", "ERROR", JOptionPane.ERROR_MESSAGE);
                } else {
                    try {
                        storeName = JOptionPane.showInputDialog(null, "Enter the name of " +
                                "the store", "Create Product", JOptionPane.PLAIN_MESSAGE);
                        boolean storeFound;
                        synchronized (lock) {
                            objectOutputStream.writeObject("searchStore");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(storeName);
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(currentSeller);
                            objectOutputStream.flush();
                            storeFound = (boolean) objectInputStream.readObject();
                        }
                        if (storeFound) {
                            JOptionPane.showMessageDialog(null, "This store name is " +
                                    "already taken!", "ERROR", JOptionPane.ERROR_MESSAGE);
                        } else {
                            productDescription = JOptionPane.showInputDialog(null, "Enter" +
                                    " the product's description", "Create Product", JOptionPane.PLAIN_MESSAGE);
                            if (productDescription.contains(",")) {
                                JOptionPane.showMessageDialog(null, "Descriptions cannot " +
                                        "contain commas!", "ERROR", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            quantityAvailable = Integer.parseInt(JOptionPane.showInputDialog(null,
                                    "Enter the quantity available", "Create Product",
                                    JOptionPane.PLAIN_MESSAGE));
                            price = Double.parseDouble(JOptionPane.showInputDialog(null,
                                    "Enter the product's price", "Create Product",
                                    JOptionPane.PLAIN_MESSAGE));
                            if (quantityAvailable <= 0) {
                                JOptionPane.showMessageDialog(null, "Please enter a number" +
                                        " greater than 0 for the quantity", "ERROR", JOptionPane.ERROR_MESSAGE);
                            } else if (price <= 0) {
                                JOptionPane.showMessageDialog(null, "Please enter a number " +
                                        "greater than 0 for the price", "ERROR", JOptionPane.ERROR_MESSAGE);
                            } else {
                                Product newProduct = new Product(productName, productDescription, price,
                                        quantityAvailable, 0, storeName);
                                ArrayList<Store> allStores;
                                synchronized (lock) {
                                    objectOutputStream.writeObject("getAllStores");
                                    objectOutputStream.flush();
                                    allStores = (ArrayList<Store>) objectInputStream.readObject();
                                }
                                boolean inStore = false;
                                Store productStore = null;
                                for (int i = 0; i < allStores.size(); i++) {
                                    if (allStores.get(i).getName().equals(storeName)) {
                                        allStores.get(i).addProduct(newProduct);
                                        inStore = true;
                                        productStore = allStores.get(i);
                                    }
                                }
                                //add change to local and global lists
                                if (!inStore) {
                                    Store newStore = new Store(storeName, currentSeller.getName());
                                    currentSeller.addStore(newStore);
                                    newStore.addProduct(newProduct);
                                } else {
                                    productStore.addProduct(newProduct);
                                }
                                synchronized (lock) {
                                    objectOutputStream.writeObject("appendToFile");
                                    objectOutputStream.flush();
                                    objectOutputStream.writeObject(newProduct.toString());
                                    objectOutputStream.flush();
                                    objectOutputStream.writeObject(currentSeller.getName() + ".txt");
                                    objectOutputStream.flush();
                                    //add change to local and global lists
                                    objectOutputStream.writeObject("appendToFile");
                                    objectOutputStream.flush();
                                    objectOutputStream.writeObject(newProduct.getName() + ",add");
                                    objectOutputStream.flush();
                                    objectOutputStream.writeObject("globalChanges.txt");
                                    objectOutputStream.flush();
                                }
                                localChanges.add(newProduct.getName() + ",add");
                            }
                        }
                    } catch (NullPointerException ex) {
                    } catch (NumberFormatException a) {
                        JOptionPane.showMessageDialog(null, "There was an invalid input," +
                                " please try again", "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else { //import from csv
                String csvName = JOptionPane.showInputDialog(null, "Enter the CSV file" +
                        " name here: ", "Import CSV file", JOptionPane.PLAIN_MESSAGE);
                if (csvName == null) {
                    return;
                }
                ArrayList<Product> importedProducts = currentSeller.importProducts(csvName);
                for (int i = 0; i < importedProducts.size(); i++) {
                    boolean storeFound;
                    synchronized (lock) {
                        objectOutputStream.writeObject("searchStore");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(importedProducts.get(i).getStoreName());
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(currentSeller);
                        objectOutputStream.flush();
                        storeFound = (boolean) objectInputStream.readObject();
                    }
                    if (storeFound) {
                        JOptionPane.showMessageDialog(null, "The store name is already" +
                                        " taken for product " + importedProducts.get(i).getName() + "!", "ERROR",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        addToStore(currentSeller, importedProducts.get(i));
                        synchronized (lock) {
                            objectOutputStream.writeObject("appendToFile");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(importedProducts.get(i).toString());
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(currentSeller.getName() + ".txt");
                            objectOutputStream.flush();
                            //add change to local and global lists
                            objectOutputStream.writeObject("appendToFile");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(importedProducts.get(i).getName() + ",add");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject("globalChanges.txt");
                            objectOutputStream.flush();
                        }
                        localChanges.add(importedProducts.get(i).getName() + ",add");
                    }
                }
            }
        } catch (SocketException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Adds a specific product to a specific store
    public static void addToStore(Seller s, Product p) {
        ArrayList<Store> tempStoreList = s.getStoreList();
        for (int i = 0; i < tempStoreList.size(); i++) {
            if (tempStoreList.get(i).getName().equalsIgnoreCase(p.getStoreName())) {
                tempStoreList.get(i).addProduct(p);
                return;
            }
        }
        ArrayList<Product> temp = new ArrayList<>();
        temp.add(p);
        s.addStore(new Store(p.getStoreName(), s.getName(), temp));
    }

    //Returns an ArrayList of all the products associated with a specific seller
    public static ArrayList<Store> getProducts(String name, ObjectOutputStream objectOutputStream,
                                               ObjectInputStream objectInputStream) throws SocketException {
        try {
            ArrayList<String> fileContents;
            synchronized (lock) {
                objectOutputStream.writeObject("readFile");
                objectOutputStream.flush();
                objectOutputStream.writeObject(name + ".txt");
                objectOutputStream.flush();
                fileContents = (ArrayList<String>) objectInputStream.readObject();
            }
            fileContents.remove(0);
            fileContents.remove(0);
            ArrayList<Store> stores = new ArrayList<Store>();
            for (int i = 0; i < fileContents.size(); i++) {
                String[] line = fileContents.get(i).split(",");
                Product p = new Product(line[1], line[2], Double.parseDouble(line[4]), Integer.parseInt(line[3]),
                        Integer.parseInt(line[5]), line[0]);
                boolean storeExists = false;
                for (int j = 0; j < stores.size(); j++) {
                    if (stores.get(j).getName().equalsIgnoreCase(p.getStoreName())) {
                        stores.get(j).addProduct(p);
                        storeExists = true;
                        break;
                    }
                }
                if (!storeExists) {
                    ArrayList<Product> temp = new ArrayList<Product>();
                    temp.add(p);
                    stores.add(new Store(p.getStoreName(), name, temp));
                }
            }
            return stores;
        } catch (SocketException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Run method: asks the user how they want to log in and sends them to the appropriate user interface accordingly
    public void run() {
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            socket = new Socket("localHost", 44445);
            JOptionPane.showMessageDialog(null, "Connected to server 'localHost'\non port 44445",
                    "Marketplace", JOptionPane.INFORMATION_MESSAGE);
            synchronized (lock) {
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());

                objectOutputStream.writeObject("readFile");
                objectOutputStream.flush();
                objectOutputStream.writeObject("globalChanges.txt");
                objectOutputStream.flush();

                globalChanges = (ArrayList<String>) objectInputStream.readObject();
            }
            if (globalChanges.size() == 0) {
                startIndex = 0;
            } else {
                startIndex = globalChanges.size();
            }
            try {
                //Creates necessary files if they do not exist
                if (!new File("allCustomers.txt").exists()) {
                    FileWriter fr = new FileWriter("allCustomers.txt");
                    fr.close();
                }
                if (!new File("allSellers.txt").exists()) {
                    FileWriter fr = new FileWriter("allSellers.txt");
                    fr.close();
                }
                if (!new File("transactionHistory.txt").exists()) {
                    FileWriter fr = new FileWriter(new File("transactionHistory.txt"));
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ConnectException ex) {
            JOptionPane.showMessageDialog(null, "Server is not online!", "ERROR",
                    JOptionPane.ERROR_MESSAGE);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new LoginGUI(this, objectOutputStream, objectInputStream, socket, lock));
    }
}
