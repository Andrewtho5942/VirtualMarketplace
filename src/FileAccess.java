import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * A "helper" class to save and access data about each player's account information,
 * history, shopping cart, and new, edited, or deleted products.
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 4
 *
 * @author Andrew Thompson
 * @version November 13, 2022
 */

public final class FileAccess implements Runnable {
    Socket socket;

    //Must be instantiated for each connection
    private FileAccess(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            String input = "";
            do { // string sent by client corresponds directly to server method name
                try {
                    input = (String) objectInputStream.readObject();

                    if (input.equals("replaceInTransactionHistory")) {
                        Product product = (Product) objectInputStream.readObject();
                        String oldName = (String) objectInputStream.readObject();
                        replaceInTransactionHistory(product, oldName);
                    } else if (input.equals("updateHistory")) {
                        String history = (String) objectInputStream.readObject();
                        String fileName = (String) objectInputStream.readObject();
                        updateHistory(history, fileName);
                    } else if (input.equals("updateStatus")) {
                        String status = (String) objectInputStream.readObject();
                        String accountFile = (String) objectInputStream.readObject();
                        updateStatus(status, accountFile);
                    } else if (input.equals("updateProduct")) {
                        Product product = (Product) objectInputStream.readObject();
                        String fileName = (String) objectInputStream.readObject();
                        int variable = (int) objectInputStream.readObject();
                        String oldName = (String) objectInputStream.readObject();
                        updateProduct(product, fileName, variable, oldName);
                    } else if (input.equals("getAllProducts")) {
                        ArrayList<Product> arrayList = getAllProducts();
                        objectOutputStream.writeObject(arrayList);
                        objectOutputStream.flush();
                    } else if (input.equals("getSellerProducts")) {
                        Seller s = (Seller) objectInputStream.readObject();
                        ArrayList<Product> arrayList = getSellerProducts(s);
                        objectOutputStream.writeObject(arrayList);
                    } else if (input.equals("getAllStores")) {
                        ArrayList<Store> arrayList = getAllStores();
                        objectOutputStream.writeObject(arrayList);
                    } else if (input.equals("readFile")) { // readFile
                        String filename = (String) objectInputStream.readObject();
                        ArrayList<String> arrayList = readFile(filename);
                        objectOutputStream.writeObject(arrayList);
                        objectOutputStream.flush();
                    } else if (input.equals("createCustomerFile")) { // createCustomerFile
                        Customer c = (Customer) objectInputStream.readObject();
                        createCustomerFile(c);
                    } else if (input.equals("createSellerFile")) { // createSellerFile
                        Seller s = (Seller) objectInputStream.readObject();
                        createSellerFile(s);
                    } else if (input.equals("searchProduct")) {
                        String name = (String) objectInputStream.readObject();
                        boolean status = searchProduct(name);
                        objectOutputStream.writeObject(status);
                    } else if (input.equals("searchStore")) {
                        String name = (String) objectInputStream.readObject();
                        Seller seller = (Seller) objectInputStream.readObject();
                        boolean status = searchStore(name, seller);
                        objectOutputStream.writeObject(status);
                    } else if (input.equals("appendToFile")) {
                        String line = (String) objectInputStream.readObject();
                        String fileName = (String) objectInputStream.readObject();
                        appendToFile(line, fileName);
                    } else if (input.equals("exportHistory")) { // exportHistory
                        Customer c = (Customer) objectInputStream.readObject();
                        exportHistory(c);
                    } else if (input.equals("deleteProductInFiles")) {
                        Product product = (Product) objectInputStream.readObject();
                        Seller s = (Seller) objectInputStream.readObject();
                        deleteProductInFiles(product, s);
                    } else if (input.equals("changeNameInCustomer")) {
                        String oldName = (String) objectInputStream.readObject();
                        String newName = (String) objectInputStream.readObject();
                        changeNameInCustomer(oldName, newName);
                    }
                } catch (EOFException | SocketException ex) {
                    System.out.println("A client disconnected!");
                    socket.close();
                    return;
                }
            } while (true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //adds a line to the transaction history file
    public static synchronized boolean updateTransactionHistory(String transaction) {
        return (appendToFile(transaction, "transactionHistory.txt"));
    }

    //replaces a product's name or store name in the transaction history file
    public static synchronized void replaceInTransactionHistory(Product product, String oldName) {
        ArrayList<String> fileContents = readFile("transactionHistory.txt");
        ArrayList<String> newContents = new ArrayList<>();
        for (int i = 0; i < fileContents.size(); i++) {
            String[] fileValues = fileContents.get(i).split(",");
            if (oldName.equalsIgnoreCase(fileValues[2])) {
                fileContents.set(i, fileValues[0] + "," + product.getStoreName() + "," + product.getName() +
                        "," + fileValues[3] + "," + fileValues[4]);
            }
            newContents.add(fileContents.get(i));
        }
        writeFile(newContents, "transactionHistory.txt");
    }

    //Rewrites the history in the given file with the history parameter.
    public static synchronized boolean updateHistory(String history, String customerAccountFile) {
        ArrayList<String> fileContents = readFile(customerAccountFile);
        //change line 3 to the new history
        if (fileContents.size() >= 3) {
            fileContents.set(2, history);
        } else {
            appendToFile(history, customerAccountFile);
            return true;
        }
        return (writeFile(fileContents, customerAccountFile));
    }

    //Rewrites the online status in the given file with the status parameter.
    public static synchronized boolean updateStatus(String status, String accountFile) {
        ArrayList<String> fileContents = readFile(accountFile);
        //change line 3 to the new history
        fileContents.set(0, status);
        return (writeFile(fileContents, accountFile));
    }

    //Rewrites the given customer’s shopping cart list to their file.
    public static synchronized void updateShoppingCart(String shoppingCart, Product product, String
            customerAccountFile) {
        ArrayList<String> fileContents = readFile(customerAccountFile);
        //change line 4 to the new shopping cart
        while (fileContents.size() < 4) {
            fileContents.add("");
        }
        fileContents.set(3, shoppingCart);
        //Save the customer file
        writeFile(fileContents, customerAccountFile);

        //--switch to update product's shopping cart quantity in its seller file
        ArrayList<String> sellerFiles = readFile("allSellers.txt");
        //traverse all seller files in central file
        for (int i = 0; i < sellerFiles.size(); i++) {
            ArrayList<String> fileLines = readFile(sellerFiles.get(i));
            //traverse all products in each seller file (starting at line 3)
            for (int x = 2; x < fileLines.size(); x++) {
                String[] lineContents = fileLines.get(x).split(",");
                if (lineContents[1].equalsIgnoreCase(product.getName())) {
                    lineContents[5] = product.getShoppingCartCount() + "";
                    //build new line based on the new contents
                    String newLine = String.join(",", lineContents);
                    fileLines.set(x, newLine);
                    writeFile(fileLines, sellerFiles.get(i));
                    break;
                }
            }
        }
    }

    //Rewrites the seller’s file with the changes to a given product
    public static synchronized void updateProduct(Product product, String sellerAccountFile, int variable,
                                                  String oldName) {
        ArrayList<String> fileContents = readFile(sellerAccountFile);
        ArrayList<String> newContents = new ArrayList<>();
        newContents.add(fileContents.get(0));
        newContents.add(fileContents.get(1));
        //find product, skip lines 1&2
        for (int i = 2; i < fileContents.size(); i++) {
            String[] lineValues = fileContents.get(i).split(",");
            //if a matching product name is found
            if (lineValues[1].equalsIgnoreCase(oldName)) {
                String productLine = "";
                //change a specific value to the new value
                switch (variable) {
                    case 1: //only change product name (searched with old name)
                        productLine = String.format("%s,%s,%s,%d,%.2f,%d", lineValues[0],
                                product.getName(), lineValues[2], Integer.parseInt(lineValues[3]),
                                Double.parseDouble(lineValues[4]), Integer.parseInt(lineValues[5]));
                        break;
                    case 2: //only change store name
                        productLine = String.format("%s,%s,%s,%d,%.2f,%d", product.getStoreName(),
                                lineValues[1], lineValues[2], Integer.parseInt(lineValues[3]),
                                Double.parseDouble(lineValues[4]), Integer.parseInt(lineValues[5]));
                        break;
                    case 3: //only change description
                        productLine = String.format("%s,%s,%s,%d,%.2f,%d", lineValues[0],
                                lineValues[1], product.getDescription(), Integer.parseInt(lineValues[3]),
                                Double.parseDouble(lineValues[4]), Integer.parseInt(lineValues[5]));
                        break;
                    case 4: //only change quantity
                        productLine = String.format("%s,%s,%s,%d,%.2f,%d", lineValues[0],
                                lineValues[1], lineValues[2], product.getQuantityAvailable(),
                                Double.parseDouble(lineValues[4]), Integer.parseInt(lineValues[5]));
                        break;
                    case 5: //only change price
                        productLine = String.format("%s,%s,%s,%d,%.2f,%d", lineValues[0],
                                lineValues[1], lineValues[2], Integer.parseInt(lineValues[3]),
                                product.getPrice(), Integer.parseInt(lineValues[5]));
                }
                //add changed line to the new contents
                newContents.add(productLine);
            } else {
                //if name doesn't match, make no changes
                newContents.add(fileContents.get(i));
            }
        }
        writeFile(newContents, sellerAccountFile);
    }

    //Loops through every seller’s file, creates products from the data, adds all
    // of them to an arraylist, and returns it.
    public synchronized static ArrayList<Product> getAllProducts() {
        ArrayList<Product> allProducts = new ArrayList<>();
        ArrayList<String> sellerFiles = readFile("allSellers.txt");
        //traverse all seller files in central file
        for (int i = 0; i < sellerFiles.size(); i++) {
            ArrayList<String> fileLines = readFile(sellerFiles.get(i));
            //traverse all products in each seller file (starting at line 3)
            for (int x = 2; x < fileLines.size(); x++) {
                String[] lineContents = fileLines.get(x).split(",");
                //create product with the information from the line
                if (lineContents.length == 6) {
                    Product returnProduct = new Product(lineContents[1], lineContents[2],
                            Double.parseDouble(lineContents[4]), Integer.parseInt(lineContents[3]),
                            Integer.parseInt(lineContents[5]), lineContents[0]);
                    allProducts.add(returnProduct);
                } else {
                    System.out.println("Error in getAllProducts method: A line does not have 6 CSVs!");
                }
            }
        }
        return allProducts;
    }

    //gets all products for a specific seller
    public synchronized static ArrayList<Product> getSellerProducts(Seller seller) {
        ArrayList<Product> sellerProducts = new ArrayList<>();
        String sellerFile = seller.getName() + ".txt";
        ArrayList<String> fileLines = readFile(sellerFile);
        //traverse all products in each seller file (starting at line 3)
        for (int x = 2; x < fileLines.size(); x++) {
            String[] lineContents = fileLines.get(x).split(",");
            //create product with the information from the line
            if (lineContents.length == 6) {
                Product returnProduct = new Product(lineContents[1], lineContents[2],
                        Double.parseDouble(lineContents[4]), Integer.parseInt(lineContents[3]),
                        Integer.parseInt(lineContents[5]), lineContents[0]);
                sellerProducts.add(returnProduct);
            } else {
                System.out.println("Error in getSellerProducts method: A line does not have 6 CSVs!");
            }
        }
        return sellerProducts;
    }

    //Loops through every seller’s file, creates stores from the data, adds all
    // of them to an arraylist, and returns it.
    public synchronized static ArrayList<Store> getAllStores() {
        ArrayList<Store> allStores = new ArrayList<>();
        ArrayList<String> allSellers = readFile("allSellers.txt");
        //traverse all seller files in central file
        for (int i = 0; i < allSellers.size(); i++) {
            ArrayList<String> fileLines = readFile(allSellers.get(i));
            //traverse all products in each seller file (starting at line 3)
            for (int j = 2; j < fileLines.size(); j++) {
                String[] split = fileLines.get(j).split(",");
                //create product with the information from the line
                if (split.length == 6) {
                    try {
                        Store returnStore = new Store(split[0],
                                allSellers.get(i).substring(0, allSellers.get(i).length() - 4));
                        allStores.add(returnStore);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Error in getAllProducts method: A line does not have 6 CSVs!");
                }
            }
        }
        return allStores;
    }

    //Reads a file and converts it to an arrayList of each line that it contains.
    public synchronized static ArrayList<String> readFile(String fileName) {
        if (!fileName.equals("")) {
            ArrayList<String> fileContents = new ArrayList<>();
            try {
                BufferedReader bfr = new BufferedReader(new FileReader(fileName));
                String line;
                //add every line to fileContents
                while ((line = bfr.readLine()) != null) {
                    fileContents.add(line);
                }
                bfr.close();
            } catch (IOException e) {
                System.out.println("File not found!");
            }
            return fileContents;
        }
        return new ArrayList<String>();
    }

    //Overwrites a file with the newContents ArrayList. Returns true if successful, false if not.
    public static synchronized boolean writeFile(ArrayList<String> newContents, String fileName) {
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName, false));
            //write every line to the file
            for (String line : newContents) {
                pw.println(line);
            }
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        //method worked, return true.
        return true;
    }

    //Adds a new file to the central customer file and fills that file with the data given
    //as an argument. Returns true if successful, false if not.
    public static synchronized boolean createCustomerFile(Customer customer) {
        //If name is a repeat, don't write to file and return false
        if (!searchUsername(customer.getName())) {
            ArrayList<String> contents = new ArrayList<>();
            contents.add("online");
            contents.add(customer.getPassword());
            contents.add(customer.getHistory());
            contents.add(customer.getShoppingCart());
            //add file name to the central customers file
            appendToFile(customer.getName() + ".txt", "allCustomers.txt");
            writeFile(contents, customer.getName() + ".txt");
            return true;
        }
        return false;
    }

    //Adds a new file to the central customer file and fills that file with the data given
    //as an argument. Returns true if successful, false if not.
    public static synchronized boolean createSellerFile(Seller seller) {
        //If name is a repeat, don't write to file and return false
        if (!searchUsername(seller.getName())) {
            ArrayList<String> contents = new ArrayList<>();
            contents.add("online");
            contents.add(seller.getPassword());
            //loop through all the seller's stores
            for (int i = 0; i < seller.getStoreList().size(); i++) {
                //loop through each product in each store
                for (int x = 0; x < seller.getStoreList().get(i).getProducts().size(); x++) {
                    //add the products from each store to the file
                    contents.add(seller.getStoreList().get(i).getProducts().get(x).toString());
                }
            }
            //add file name to the central sellers file
            appendToFile(seller.getName() + ".txt", "allSellers.txt");
            writeFile(contents, seller.getName() + ".txt");
            return true;
        }
        return false;
    }

    // Searches through the contents of the central account files, returning true
    // if a matching username file was found, false if not.
    public static synchronized boolean searchUsername(String username) {
        ArrayList<String> customerUsernames = readFile("allCustomers.txt");
        ArrayList<String> sellerUsernames = readFile("allSellers.txt");

        //traverse all customer usernames, return true if a match is found
        for (int i = 0; i < customerUsernames.size(); i++) {
            if (!((customerUsernames.get(i) == null) || (customerUsernames.get(i).isEmpty()))) {
                if (customerUsernames.get(i).substring(0, customerUsernames.get(i).
                        length() - 4).equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        //traverse all seller usernames, return true if a match is found
        for (int i = 0; i < sellerUsernames.size(); i++) {
            if (!((sellerUsernames.get(i) == null) || (sellerUsernames.get(i).isEmpty()))) {
                if (sellerUsernames.get(i).substring(0, sellerUsernames.get(i).
                        length() - 4).equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        //match was not found, return false
        return false;
    }

    //Updates and searches through the contents of the products array, returning true
// if a matching product name was found, false if not.
    public static synchronized boolean searchProduct(String productName) {
        ArrayList<Product> allProducts = getAllProducts();
        //traverse all products and check each name. If it matches productName, return true.
        for (int i = 0; i < allProducts.size(); i++) {
            if (allProducts.get(i).getName().equalsIgnoreCase(productName)) {
                return true;
            }
        }
        //No matches found, return false.
        return false;
    }

    //Updates and searches through the contents of the products array, returning true
    // if a matching store name was found, false if not.
    public static synchronized boolean searchStore(String storeName, Seller seller) {
        ArrayList<Product> allProducts = getAllProducts();
        //loop through all products
        for (int i = 0; i < allProducts.size(); i++) {
            String productStoreName = allProducts.get(i).getStoreName();
            //loop through the seller's stores
            for (Store store : seller.getStoreList()) {
                //if the product's store name matches and is not a store from this seller, return true
                if (!(productStoreName.equalsIgnoreCase(store.getName())) && (productStoreName.equalsIgnoreCase
                        (storeName))) {
                    return true;
                }
            }
        }
        //No matches found, return false.
        return false;
    }

    //Appends a line to a given file, returns true if the method worked and
// false if it encountered an error.
    public static synchronized boolean appendToFile(String line, String fileName) {
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(fileName, true));
            //append line to file
            pw.println(line);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        //method worked, return true.
        return true;
    }


    public static synchronized void updateQuantity(Product product) {
        ArrayList<String> sellerFiles = readFile("allSellers.txt");
        //traverse all seller files in central file
        for (int i = 0; i < sellerFiles.size(); i++) {
            ArrayList<String> fileLines = readFile(sellerFiles.get(i));
            //traverse all products in each seller file (starting at line 3)
            for (int x = 2; x < fileLines.size(); x++) {
                String[] lineContents = fileLines.get(x).split(",");
                //if names match
                if (lineContents[1].equals(product.getName())) {
                    lineContents[3] = product.getQuantityAvailable() + "";
                    String newLine = String.join(",", lineContents);
                    fileLines.set(x, newLine);
                    writeFile(fileLines, sellerFiles.get(i));
                    break;
                }
            }
        }
    }

    public static synchronized void exportHistory(Customer customer) {
        ArrayList<String> fileContents = readFile(customer.getName() + ".txt");
        String historyLine = fileContents.get(2);
        String[] historyList = historyLine.split(";");
        ArrayList<String> returnList = new ArrayList<>();
        returnList.add(customer.getName() + "'s Shopping History:");
        for (int i = 0; i < historyList.length; i++) {
            int commaIndex = historyList[i].indexOf(",");
            returnList.add(" - " + historyList[i].substring(commaIndex + 1) + " units of " + historyList[i].
                    substring(0, commaIndex) + ".");
        }
        writeFile(returnList, customer.getName() + "_history.txt");
    }

    // deletes a product from all customer's shopping carts
    public static synchronized void deleteProductInFiles(Product product, Seller seller) {
        ArrayList<String> customers = readFile("allCustomers.txt");
        //loop through all customer files
        for (int i = 0; i < customers.size(); i++) {
            ArrayList<String> fileContents = readFile(customers.get(i));
            String newCart = "";
            String shoppingCart;
            if (fileContents.size() == 4) {
                shoppingCart = fileContents.get(3);
            } else if (fileContents.size() == 3) {
                shoppingCart = "";
                fileContents.add("");
            } else {
                shoppingCart = "";
                fileContents.add("");
                fileContents.add("");
            }
            String[] cartItems = shoppingCart.split(";");
            //loop through customer's shopping cart (1 line)
            for (int x = 0; x < cartItems.length; x++) {
                int commaIndex = cartItems[x].indexOf(",");
                if (commaIndex != -1) {
                    if (!(product.getName().equalsIgnoreCase(cartItems[x].substring(0, commaIndex)))) {
                        //Not the product that is being looked for, add to newCart
                        newCart += cartItems[x] + ";";
                    }
                    //if a product matches the name, do not add it to the new cart
                }
            }
            fileContents.set(3, newCart);
            writeFile(fileContents, customers.get(i));
        }

        //now delete product from the seller's file
        ArrayList<String> fileContents = readFile(seller.getName() + ".txt");
        ArrayList<String> newContents = new ArrayList<>();
        newContents.add(fileContents.get(0));
        newContents.add(fileContents.get(1));
        for (int i = 2; i < fileContents.size(); i++) {
            String[] lineValues = fileContents.get(i).split(",");
            //if the names *don't* match, add to new contents
            if (!(lineValues[1].equalsIgnoreCase(product.getName()))) {
                newContents.add(fileContents.get(i));
            }
        }
        writeFile(newContents, seller.getName() + ".txt");
    }

    //changes the name of a product in every customer's shopping cart and history
    public static synchronized void changeNameInCustomer(String oldName, String newName) {
        ArrayList<String> customers = readFile("allCustomers.txt");
        //loop through all customer files
        for (int i = 0; i < customers.size(); i++) {
            ArrayList<String> fileContents = readFile(customers.get(i));
            String newHist = "";
            if (fileContents.size() >= 3) {
                String[] histItems = fileContents.get(2).split(";");

                //loop through customer's history (line 3)
                for (int x = 0; x < histItems.length; x++) {
                    int commaIndex = histItems[x].indexOf(",");
                    if (commaIndex != -1) {
                        if (oldName.equalsIgnoreCase(histItems[x].substring(0, commaIndex))) {
                            //Found a matching product, change name to new name and add to newCart
                            newHist += newName + histItems[x].substring(commaIndex) + ";";
                        } else {
                            //product does not match, do not change it
                            newHist += histItems[x] + ";";
                        }
                    }
                }
                fileContents.set(2, newHist);
            }
            //--now change the shopping cart
            if (fileContents.size() == 4) {
                String[] cartItems = fileContents.get(3).split(";");
                String newCart = "";
                //loop through customer's shopping cart (line 4)
                for (int x = 0; x < cartItems.length; x++) {
                    int commaIndex = cartItems[x].indexOf(",");
                    if (commaIndex != -1) {
                        if (oldName.equalsIgnoreCase(cartItems[x].substring(0, commaIndex))) {
                            //Found a matching product, change name to new name and add to newCart
                            newCart += newName + cartItems[x].substring(commaIndex) + ";";
                        } else {
                            //product does not match, do not change it
                            newCart += cartItems[x] + ";";
                        }
                    }
                }
                fileContents.set(3, newCart);
            }
            //save changes to customer file
            writeFile(fileContents, customers.get(i));
        }
    }

    public static void main(String[] args) throws IOException {
        //log out all accounts
        ArrayList<String> accountFileNames = readFile("allCustomers.txt");
        accountFileNames.addAll(readFile("allSellers.txt"));
        for (String fileName : accountFileNames) {
            updateStatus("offline", fileName);
        }

        ServerSocket serverSocket = new ServerSocket(44445);
        System.out.println("Server socket open, awaiting connections...");
        while (true) {
            Socket socket = serverSocket.accept();
            //start FileAccess thread
            FileAccess server = new FileAccess(socket);
            new Thread(server).start();
            System.out.println("A client connected!");
        }
    }
}