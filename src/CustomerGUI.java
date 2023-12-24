import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * This class handles creates the UI for Customers allowing them to interact with the marketplace
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 5
 *
 * @author Grant Strickland
 * @version November 15, 2022
 */

public class CustomerGUI implements Runnable {
    ArrayList<String> localChanges;
    Customer currentCustomer;
    JFrame customerUI;
    JPanel buttonPanel;
    JPanel marketPanel;
    JButton viewPageButton;
    JButton searchButton;
    JButton sortButton;
    JButton historyButton;
    JButton cartButton;
    JButton refreshButton;
    JButton logoutButton;
    ArrayList<Product> products;
    //marketplace object for each thread
    Marketplace marketThread;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    Socket socket;
    private static Object lock;

    //Constructor that creates the JFrame using the customer passed in and the list of products
    public CustomerGUI(Customer currentCustomer, ArrayList<Product> products, Marketplace marketThread,
                       ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, Socket socket,
                       Object lock) {
        CustomerGUI.lock = lock;
        this.socket = socket;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
        this.marketThread = marketThread;
        this.currentCustomer = currentCustomer;
        this.products = products;
    }

    public void run() {
        ActionListener customerListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (e.getSource() == viewPageButton) {  //search for specific product
                        String productToView = JOptionPane.showInputDialog(null, "Enter the " +
                                "name of the product you want to view", "View Page", JOptionPane.QUESTION_MESSAGE);
                        if (productToView != null && productToView.equals("")) {
                            JOptionPane.showMessageDialog(null, "Please enter a product name!",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (productToView != null) {
                            ArrayList<Product> products;
                            synchronized (lock) {
                                objectOutputStream.writeObject("getAllProducts");
                                objectOutputStream.flush();
                                products = (ArrayList<Product>) objectInputStream.readObject();
                            }
                            //ArrayList<Product> products = FileAccess.getAllProducts();
                            Product foundProduct = null;
                            for (int i = 0; i < products.size(); i++) {
                                if (products.get(i).getName().equalsIgnoreCase(productToView)) {
                                    foundProduct = products.get(i);
                                }
                            }
                            if (foundProduct != null) {
                                String[] options = new String[3];
                                options[0] = "Buy Product";
                                options[1] = "Add to Cart";
                                options[2] = "Return to Home";
                                String productMessage = String.format("Product Name: %s\n Store Name: %s\n " +
                                                "Description: %s\n Price: %.2f\n Quantity Available: %d",
                                        foundProduct.getName()
                                        , foundProduct.getStoreName(), foundProduct.getDescription(),
                                        foundProduct.getPrice(), foundProduct.getQuantityAvailable());
                                if (productMessage != null) {
                                    int userChoice = JOptionPane.showOptionDialog(null, productMessage,
                                            foundProduct.getName(), JOptionPane.DEFAULT_OPTION, JOptionPane.
                                                    PLAIN_MESSAGE, null, options, null);
                                    if (userChoice == 0) {//buy product
                                        String quantity = JOptionPane.showInputDialog(null,
                                                "How many would you like to buy?", "Enter Quantity",
                                                JOptionPane.QUESTION_MESSAGE);
                                        int quantityToBuy = 0;
                                        if (quantity != null) {
                                            try {
                                                quantityToBuy = Integer.parseInt(quantity);
                                            } catch (NumberFormatException ex) {
                                                quantityToBuy = -1;
                                            }
                                            if (quantityToBuy == 0 || quantityToBuy < 0) {
                                                JOptionPane.showMessageDialog(null, "Please en" +
                                                        "ter a valid value!", "Error", JOptionPane.ERROR_MESSAGE);
                                            } else if (quantityToBuy > foundProduct.getQuantityAvailable()) {
                                                JOptionPane.showMessageDialog(null, "There are" +
                                                                " not that many of the product available!",
                                                        "Error", JOptionPane.ERROR_MESSAGE);
                                            } else {
                                                currentCustomer.buyProduct(foundProduct, quantityToBuy);
                                                synchronized (lock) {
                                                    objectOutputStream.writeObject("updateHistory");
                                                    objectOutputStream.flush();
                                                    objectOutputStream.writeObject(currentCustomer.getHistory());
                                                    objectOutputStream.flush();
                                                    objectOutputStream.writeObject(currentCustomer.getName() + ".txt");
                                                    objectOutputStream.flush();
                                                }
                                                JOptionPane.showMessageDialog(null,
                                                        "Product purchased!", "Success",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                            }
                                        }
                                    } else if (userChoice == 1) {//add to cart
                                        String quantity = JOptionPane.showInputDialog(null,
                                                "How many would you like to add?", "Enter Quantity",
                                                JOptionPane.QUESTION_MESSAGE);
                                        int quantityToBuy = 0;
                                        if (quantity != null) {
                                            quantityToBuy = Integer.parseInt(quantity);
                                            if (quantityToBuy == 0 || quantityToBuy < 0) {
                                                JOptionPane.showMessageDialog(null,
                                                        "Please enter a valid value!", "ERROR",
                                                        JOptionPane.ERROR_MESSAGE);
                                            } else if (quantityToBuy > foundProduct.getQuantityAvailable()) {
                                                JOptionPane.showMessageDialog(null,
                                                        "There are not that many of the product available!",
                                                        "Error", JOptionPane.ERROR_MESSAGE);
                                            } else {
                                                currentCustomer.addToCart(foundProduct, quantityToBuy);
                                                JOptionPane.showMessageDialog(null, "Product " +
                                                                "added to cart!", "Success",
                                                        JOptionPane.INFORMATION_MESSAGE);
                                            }
                                        }
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "That product doesn't " +
                                        "exist, try again", "ERROR", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else if (e.getSource() == searchButton) { //search with a string
                        String searchText = JOptionPane.showInputDialog(null, "Enter the " +
                                "text you want to search for", "Search", JOptionPane.QUESTION_MESSAGE);
                        if (searchText != null) {
                            ArrayList<Product> allProducts;
                            synchronized (lock) {
                                objectOutputStream.writeObject("getAllProducts");
                                objectOutputStream.flush();
                                allProducts = (ArrayList<Product>) objectInputStream.readObject();
                            }
                            //ArrayList<Product> allProducts = FileAccess.getAllProducts();
                            ArrayList<Product> searchedProducts = new ArrayList<>();
                            for (int i = 0; i < allProducts.size(); i++) {
                                Product currentProduct = allProducts.get(i);
                                if ((currentProduct.getName().toLowerCase()).contains(searchText.toLowerCase())) {
                                    searchedProducts.add(currentProduct);
                                } else if ((currentProduct.getDescription().toLowerCase()).contains(searchText.
                                        toLowerCase())) {
                                    searchedProducts.add(currentProduct);
                                } else if ((currentProduct.getStoreName().toLowerCase()).contains(searchText.
                                        toLowerCase())) {
                                    searchedProducts.add(currentProduct);
                                }
                            }
                            if (searchedProducts.size() > 0) {
                                customerUI.setVisible(false);
                                customerUI.dispose();
                                SwingUtilities.invokeLater(new CustomerGUI(currentCustomer, searchedProducts,
                                        marketThread, objectOutputStream, objectInputStream, socket, lock));
                            } else {
                                JOptionPane.showMessageDialog(null, "No products match " +
                                        "that search.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else if (e.getSource() == sortButton) {   //Sort the products
                        String[] sortOptions = new String[4];
                        sortOptions[0] = "Alphabetically";
                        sortOptions[1] = "By Price";
                        sortOptions[2] = "By Quantity";
                        sortOptions[3] = "Original Order";
                        int sortChoice = JOptionPane.showOptionDialog(null, "How would you " +
                                "like to sort?", "Sort", JOptionPane.DEFAULT_OPTION, JOptionPane.
                                QUESTION_MESSAGE, null, sortOptions, null);
                        ArrayList<Product> allProducts;
                        synchronized (lock) {
                            objectOutputStream.writeObject("getAllProducts");
                            objectOutputStream.flush();
                            allProducts = (ArrayList<Product>) objectInputStream.readObject();
                        }
                        //ArrayList<Product> allProducts = FileAccess.getAllProducts();
                        ArrayList<Product> sortedProducts = Marketplace.printAllProducts(allProducts,
                                sortChoice + 1);
                        refresh(sortedProducts);
                    } else if (e.getSource() == historyButton) {    //view history
                        String customerHistory = currentCustomer.getHistory();
                        if (customerHistory.equals("")) {
                            JOptionPane.showMessageDialog(null, "You haven't bought any products",
                                    "History", JOptionPane.PLAIN_MESSAGE);
                        } else {
                            String[] splitHistory = customerHistory.split(";");
                            String organizedHistory = "You have purchased:\n";
                            for (int i = 0; i < splitHistory.length; i++) {
                                int commaIndex = splitHistory[i].indexOf(',');
                                String product = splitHistory[i].substring(0, commaIndex);
                                String quantity = splitHistory[i].substring(commaIndex + 1);
                                organizedHistory += String.format("%s of the %s product\n", quantity, product);
                            }
                            String[] options = {"Export History", "Home"};
                            int historyChoice = JOptionPane.showOptionDialog(null, organizedHistory,
                                    "History", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                    null, options, null);
                            if (historyChoice == 0) {
                                synchronized (lock) {
                                    objectOutputStream.writeObject("exportHistory");
                                    objectOutputStream.flush();
                                    objectOutputStream.writeObject(currentCustomer);
                                    objectOutputStream.flush();
                                }
                                //FileAccess.exportHistory(currentCustomer);
                                JOptionPane.showMessageDialog(null, "Your history cont" +
                                                "ents are in the file '" + currentCustomer.getName() + "_history.txt'.",
                                        "Export Done", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    } else if (e.getSource() == cartButton) { //view cart
                        if (currentCustomer.getShoppingCart() == null || currentCustomer.getShoppingCart().equals("")) {
                            JOptionPane.showMessageDialog(null, "Your cart is empty.",
                                    "Customer Cart", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            String[] cartArray = currentCustomer.getShoppingCart().split(";");
                            String customerCart = "Your Cart:\n";
                            for (int i = 0; i < cartArray.length; i++) {
                                String[] productArray = cartArray[i].split(",");
                                customerCart += String.format("%s: %s\n", productArray[0], productArray[1]);
                            }
                            String[] options = {"Buy Cart", "Remove Product", "Close"};
                            int cartChoice = JOptionPane.showOptionDialog(null, customerCart,
                                    "Customer Cart", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                    null, options, null);
                            if (cartChoice == 0) {//buy cart
                                ArrayList<String> changes = currentCustomer.buyCart();
                                for (String change : changes) {
                                    synchronized (lock) {
                                        objectOutputStream.writeObject("appendToFile");
                                        objectOutputStream.flush();
                                        objectOutputStream.writeObject(change);
                                        objectOutputStream.flush();
                                        objectOutputStream.writeObject("globalChanges.txt");
                                        objectOutputStream.flush();
                                    }
                                    //FileAccess.appendToFile(change, "globalChanges.txt");
                                }
                                localChanges.addAll(changes);
                                synchronized (lock) {
                                    objectOutputStream.writeObject("updateHistory");
                                    objectOutputStream.flush();
                                    objectOutputStream.writeObject(currentCustomer.getHistory());
                                    objectOutputStream.flush();
                                    objectOutputStream.writeObject(currentCustomer.getName() + ".txt");
                                    objectOutputStream.flush();
                                }
                                if (changes.size() != 0) {
                                    JOptionPane.showMessageDialog(null, "Your cart has " +
                                                    "been successfully purchased! Thanks for shopping with us!",
                                            "Your Cart", JOptionPane.INFORMATION_MESSAGE);
                                }
                                //refresh screen
                                ArrayList<Product> allProducts;
                                synchronized (lock) {
                                    objectOutputStream.writeObject("getAllProducts");
                                    objectOutputStream.flush();
                                    allProducts = (ArrayList<Product>) objectInputStream.readObject();
                                }
                                refresh(allProducts);
                            } else if (cartChoice == 1) {//remove product
                                String[] cartNames = new String[currentCustomer.getShoppingCart().split(";").
                                        length];
                                for (int i = 0; i < cartArray.length; i++) {
                                    String[] productArray = cartArray[i].split(",");
                                    cartNames[i] = productArray[0];
                                }
                                String selectedProduct = (String) JOptionPane.showInputDialog(null,
                                        "Select the product to remove", "Customer Cart",
                                        JOptionPane.QUESTION_MESSAGE, null, cartNames, null);
                                ArrayList<Product> allProducts;
                                synchronized (lock) {
                                    objectOutputStream.writeObject("getAllProducts");
                                    objectOutputStream.flush();
                                    allProducts = (ArrayList<Product>) objectInputStream.readObject();
                                }
                                //ArrayList<Product> allProducts = FileAccess.getAllProducts();
                                boolean productFound = false;
                                for (int i = 0; i < allProducts.size(); i++) {
                                    if (allProducts.get(i).getName().equalsIgnoreCase(selectedProduct)) {
                                        productFound = true;
                                        currentCustomer.removeFromCart(allProducts.get(i));
                                        JOptionPane.showMessageDialog(null, "Product removed" +
                                                " from cart", "Customer Cart", JOptionPane.INFORMATION_MESSAGE);
                                        refresh(allProducts);
                                    }
                                }
                            }
                        }

                    } else if (e.getSource() == refreshButton) { //refresh GUI
                        ArrayList<Product> allProducts;
                        synchronized (lock) {
                            objectOutputStream.writeObject("getAllProducts");
                            objectOutputStream.flush();
                            allProducts = (ArrayList<Product>) objectInputStream.readObject();
                        }
                        //ArrayList<Product> allProducts = FileAccess.getAllProducts();
                        refresh(allProducts);
                    } else if (e.getSource() == logoutButton) { //log out
                        synchronized (lock) {
                            objectOutputStream.writeObject("updateStatus");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject("offline");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(currentCustomer.getName() + ".txt");
                            objectOutputStream.flush();
                        }
                        //FileAccess.updateStatus("offline", currentCustomer.getName() + ".txt");

                        customerUI.setVisible(false);
                        customerUI.dispose();
                        SwingUtilities.invokeLater(new LoginGUI(marketThread, objectOutputStream,
                                objectInputStream, socket, lock));
                    }
                } catch (SocketException exc) {
                    JOptionPane.showMessageDialog(null, "Server is not available!\n" +
                            "You were successfully logged out!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    customerUI.setVisible(false);
                    customerUI.dispose();
                    try {
                        socket.close();
                    } catch (IOException exce) {
                        exce.printStackTrace();
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        };
        localChanges = new ArrayList<>();
        customerUI = new JFrame();
        customerUI.setLayout(new BorderLayout());
        buttonPanel = new JPanel();

        viewPageButton = new JButton("View Page");
        viewPageButton.addActionListener(customerListener);
        buttonPanel.add(viewPageButton);

        searchButton = new JButton("Search");
        searchButton.addActionListener(customerListener);
        buttonPanel.add(searchButton);

        sortButton = new JButton("Sort");
        sortButton.addActionListener(customerListener);
        buttonPanel.add(sortButton);

        historyButton = new JButton("View History");
        historyButton.addActionListener(customerListener);
        buttonPanel.add(historyButton);

        cartButton = new JButton("View Cart");
        cartButton.addActionListener(customerListener);
        buttonPanel.add(cartButton);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(customerListener);
        buttonPanel.add(refreshButton);

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(customerListener);
        buttonPanel.add(logoutButton);
        customerUI.add(buttonPanel, BorderLayout.SOUTH);

        marketPanel = new JPanel();
        if (products.size() == 0) {
            JLabel noProductLabel = new JLabel("There are currently no products for sale. Come back later.");
            marketPanel.add(noProductLabel);
            noProductLabel.setHorizontalAlignment(JLabel.CENTER);
        } else {
            marketPanel.setLayout(new GridLayout(products.size() + 1, 3));
            JLabel productLabel = new JLabel("Product Name");
            JLabel storeLabel = new JLabel("Store Name");
            JLabel descriptionLabel = new JLabel("Description");
            JLabel quantityLabel = new JLabel("Quantity");
            JLabel priceLabel = new JLabel("Price");

            marketPanel.add(productLabel);
            marketPanel.add(storeLabel);
            marketPanel.add(descriptionLabel);
            marketPanel.add(quantityLabel);
            marketPanel.add(priceLabel);

            storeLabel.setHorizontalAlignment(JLabel.CENTER);
            productLabel.setHorizontalAlignment(JLabel.CENTER);
            descriptionLabel.setHorizontalAlignment(JLabel.CENTER);
            quantityLabel.setHorizontalAlignment(JLabel.CENTER);
            priceLabel.setHorizontalAlignment(JLabel.CENTER);

            for (int i = 0; i < products.size(); i++) {
                Product product = products.get(i);

                JLabel productName = new JLabel(product.getName());
                JLabel storeName = new JLabel(product.getStoreName());
                JLabel descriptionName = new JLabel(product.getDescription());
                JLabel quantityName = new JLabel(Integer.toString(product.getQuantityAvailable()));
                JLabel priceName = new JLabel("$" + String.format("%.2f", product.getPrice()));

                marketPanel.add(productName);
                marketPanel.add(storeName);
                marketPanel.add(descriptionName);
                marketPanel.add(quantityName);
                marketPanel.add(priceName);

                productName.setHorizontalAlignment(JLabel.CENTER);
                storeName.setHorizontalAlignment(JLabel.CENTER);
                descriptionName.setHorizontalAlignment(JLabel.CENTER);
                quantityName.setHorizontalAlignment(JLabel.CENTER);
                priceName.setHorizontalAlignment(JLabel.CENTER);
            }

        }
        //customerUI.add(marketPanel, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane(marketPanel);
        customerUI.add(scrollPane, BorderLayout.CENTER);
        marketPanel.setAutoscrolls(true);

        customerUI.addWindowListener(customerCloseListener);
        customerUI.setSize(1000, 400);
        customerUI.setLocationRelativeTo(null);
        customerUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        customerUI.setVisible(true);
        customerUI.setTitle("Customer UI - " + currentCustomer.getName() + "                  //            " +
                "      Marketplace");
    }


    WindowListener customerCloseListener = new WindowListener() {
        @Override
        public void windowOpened(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
            try {
                synchronized (lock) {
                    objectOutputStream.writeObject("updateStatus");
                    objectOutputStream.flush();
                    objectOutputStream.writeObject("offline");
                    objectOutputStream.flush();
                    objectOutputStream.writeObject(currentCustomer.getName() + ".txt");
                    objectOutputStream.flush();
                }
                // FileAccess.updateStatus("offline", currentSeller.getName() + ".txt");
                customerUI.setVisible(false);
                customerUI.dispose();
            } catch (SocketException ex) {
                JOptionPane.showMessageDialog(null, "Server is not available!\nYou were " +
                        "successfully logged out!", "ERROR", JOptionPane.ERROR_MESSAGE);
                try {
                    socket.close();
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }
    };

    public void refresh(ArrayList<Product> products) throws SocketException {
        customerUI.setVisible(false);
        customerUI.dispose();
        //show market thread updates
        //leave the first argument blank to detect customer type
        marketThread.showThreadChanges("", currentCustomer, localChanges, objectInputStream,
                objectOutputStream);

        SwingUtilities.invokeLater(new CustomerGUI(currentCustomer, products, marketThread, objectOutputStream,
                objectInputStream, socket, lock));
    }

}
