import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.net.Socket;

/**
 * This class creates the UI for the login screen that allows users to access their accounts
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 5
 *
 * @author Grant Strickland
 * @version November 15, 2022
 */
public class LoginGUI implements Runnable {
    JButton registerButton;
    JButton signInButton;
    JButton backButton;
    JTextField usernameField;
    JTextField passwordField;
    JTextField emailField;
    JCheckBox sellerBox;
    JCheckBox customerBox;
    JFrame loginFrame;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    Socket socket;
    private static Object lock;

    boolean closed;
    //Marketplace object for each thread
    Marketplace marketThread;

    public LoginGUI(Marketplace marketThread, ObjectOutputStream objectOutputStream, ObjectInputStream
            objectInputStream, Socket socket, Object lock) {
        this.lock = lock;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
        this.marketThread = marketThread;
        this.socket = socket;
    }

    public void run() {
        String[] options = {"Sign Up", "Sign In"};
        int signInChoice = JOptionPane.showOptionDialog(null, "What would you like to do?",
                "Login", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
                null);
        ActionListener loginButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (e.getSource() == registerButton) {
                        String email = emailField.getText();
                        String username = usernameField.getText();
                        String password = passwordField.getText();
                        processRegister(email, username, password);
                    } else if (e.getSource() == signInButton) {
                        String username = usernameField.getText();
                        String password = passwordField.getText();
                        processSignIn(username, password);
                    } else if (e.getSource() == customerBox) {
                        if (sellerBox.isSelected()) {
                            sellerBox.setSelected(false);
                        }
                    } else if (e.getSource() == sellerBox) {
                        if (customerBox.isSelected()) {
                            customerBox.setSelected(false);
                        }
                    } else if (e.getSource() == backButton) {
                        loginFrame.setVisible(false);
                        closed = true;
                        loginFrame.dispose();
                        run();
                    }
                } catch (SocketException ex) {
                    JOptionPane.showMessageDialog(null, "Server is not available!\nYou" +
                            " were successfully logged out!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    try {
                        socket.close();
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                }
            }
        };
        if (signInChoice == 0) {
            closed = false;
            loginFrame = new JFrame();
            loginFrame.setLayout(new GridLayout(5, 2));
            JLabel emailLabel = new JLabel("                                 Email:");
            loginFrame.add(emailLabel);
            emailField = new JTextField();
            loginFrame.add(emailField);
            JLabel usernameLabel = new JLabel("                                 Username:");
            loginFrame.add(usernameLabel);
            usernameField = new JTextField();
            loginFrame.add(usernameField);
            JLabel passwordLabel = new JLabel("                                 Password:");
            loginFrame.add(passwordLabel);
            passwordField = new JTextField();
            loginFrame.add(passwordField);
            customerBox = new JCheckBox("Customer");
            customerBox.addActionListener(loginButtonListener);
            loginFrame.add(customerBox);
            sellerBox = new JCheckBox("Seller");
            loginFrame.add(sellerBox);
            sellerBox.addActionListener(loginButtonListener);
            registerButton = new JButton("Sign Up");
            registerButton.setHorizontalAlignment(JLabel.CENTER);
            loginFrame.add(registerButton);
            registerButton.addActionListener(loginButtonListener);
            backButton = new JButton("Back");
            backButton.addActionListener(loginButtonListener);
            loginFrame.add(backButton);
            loginFrame.setSize(500, 350);
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setVisible(true);
            loginFrame.setTitle("Sign up for the Marketplace");
        } else if (signInChoice == 1) {
            closed = false;
            loginFrame = new JFrame();
            loginFrame.setLayout(new GridLayout(4, 2));
            JLabel usernameLabel = new JLabel("                                 Username:");
            loginFrame.add(usernameLabel);
            usernameField = new JTextField();
            loginFrame.add(usernameField);
            JLabel passwordLabel = new JLabel("                                 Password:");
            loginFrame.add(passwordLabel);
            passwordField = new JTextField();
            loginFrame.add(passwordField);
            customerBox = new JCheckBox("Customer");
            customerBox.addActionListener(loginButtonListener);
            loginFrame.add(customerBox);
            sellerBox = new JCheckBox("Seller");
            sellerBox.addActionListener(loginButtonListener);
            loginFrame.add(sellerBox);
            signInButton = new JButton("Sign In");
            signInButton.addActionListener(loginButtonListener);
            loginFrame.add(signInButton);
            backButton = new JButton("Back");
            backButton.addActionListener(loginButtonListener);
            loginFrame.add(backButton);
            loginFrame.setSize(500, 300);
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setVisible(true);
            loginFrame.setTitle("Log into the Marketplace");
        }
    }


    public void processRegister(String email, String username, String password) throws SocketException {
        if (email.equals("") || username.equals("") || password.equals("") || (!(customerBox.isSelected()) &&
                !(sellerBox.isSelected()))) {
            JOptionPane.showMessageDialog(null, "Please fill out all fields!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                if (customerBox.isSelected()) {
                    ArrayList<String> takenNames;
                    synchronized (lock) {
                        objectOutputStream.writeObject("readFile");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject("allCustomers.txt");
                        objectOutputStream.flush();
                        takenNames = (ArrayList<String>) objectInputStream.readObject();
                    }
                    //takenNames = FileAccess.readFile("allCustomers.txt");
                    ArrayList<String> temp;
                    synchronized (lock) {
                        objectOutputStream.writeObject("readFile");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject("allCustomers.txt");
                        objectOutputStream.flush();
                        temp = (ArrayList<String>) objectInputStream.readObject();
                    }
                    // ArrayList<String> temp = FileAccess.readFile("allSellers.txt");
                    for (int x = 0; x < temp.size(); x++) {
                        takenNames.add(temp.get(x));
                    }
                    if (takenNames.contains(username + ".txt")) {
                        JOptionPane.showMessageDialog(null, "That username is already taken!" +
                                " Please select a unique name.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        Customer newCustomer = new Customer(username, email, password);
                        synchronized (lock) {
                            objectOutputStream.writeObject("createCustomerFile");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(newCustomer);
                            objectOutputStream.flush();
                        }
                        //FileAccess.createCustomerFile(newCustomer);
                        JOptionPane.showMessageDialog(null, "Account successfully created!",
                                "Account Created", JOptionPane.INFORMATION_MESSAGE);
                        loginFrame.setVisible(false);
                        closed = true;
                        loginFrame.dispose();
                        synchronized (lock) {
                            objectOutputStream.writeObject("updateStatus");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject("online");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(username + ".txt");
                            objectOutputStream.flush();
                        }
                        //FileAccess.updateStatus("online", username + ".txt");
                        ArrayList<Product> products;
                        synchronized (lock) {
                            objectOutputStream.writeObject("getAllProducts");
                            objectOutputStream.flush();
                            products = (ArrayList<Product>) objectInputStream.readObject();
                        }
                        //ArrayList<Product> products = FileAccess.getAllProducts();
                        SwingUtilities.invokeLater(new CustomerGUI(newCustomer, products, marketThread,
                                objectOutputStream, objectInputStream, socket, lock));

                    }
                } else {
                    ArrayList<String> takenNames;
                    synchronized (lock) {
                        objectOutputStream.writeObject("readFile");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject("allSellers.txt");
                        objectOutputStream.flush();
                        takenNames = (ArrayList<String>) objectInputStream.readObject();
                    }
                    //takenNames = FileAccess.readFile("allCustomers.txt");
                    ArrayList<String> temp;
                    synchronized (lock) {
                        objectOutputStream.writeObject("readFile");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject("allSellers.txt");
                        objectOutputStream.flush();
                        temp = (ArrayList<String>) objectInputStream.readObject();
                    }
                    //ArrayList<String> temp = FileAccess.readFile("allSellers.txt");
                    for (int x = 0; x < temp.size(); x++) {
                        takenNames.add(temp.get(x));
                    }
                    if (takenNames.contains(username + ".txt")) {
                        JOptionPane.showMessageDialog(null, "That username is already " +
                                "taken! Please select a unique name.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        Seller newSeller = new Seller(username, email, password, new ArrayList<Store>());
                        synchronized (lock) {
                            objectOutputStream.writeObject("createSellerFile");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(newSeller);
                            objectOutputStream.flush();
                        }
                        //FileAccess.createSellerFile(newSeller);
                        JOptionPane.showMessageDialog(null, "Account successfully created!",
                                "Account Created", JOptionPane.INFORMATION_MESSAGE);
                        loginFrame.setVisible(false);
                        closed = true;
                        loginFrame.dispose();
                        synchronized (lock) {
                            objectOutputStream.writeObject("updateStatus");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject("online");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(username + ".txt");
                            objectOutputStream.flush();
                        }
                        //FileAccess.updateStatus("online", username + ".txt");
                        SwingUtilities.invokeLater(new SellerGUI(newSeller, marketThread, objectOutputStream,
                                objectInputStream, socket, lock));
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void processSignIn(String username, String password) throws SocketException {
        if (username.equals("") || password.equals("") || (!(customerBox.isSelected()) && !(sellerBox.
                isSelected()))) {
            JOptionPane.showMessageDialog(null, "Please fill out all fields!",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } else if (customerBox.isSelected() && sellerBox.isSelected()) {
            JOptionPane.showMessageDialog(null, "Your account cannot be both a customer" +
                    " and a seller!", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                ArrayList<String> takenNames;
                synchronized (lock) {
                    objectOutputStream.writeObject("readFile");
                    objectOutputStream.flush();
                    objectOutputStream.writeObject("allCustomers.txt");
                    objectOutputStream.flush();
                    takenNames = (ArrayList<String>) objectInputStream.readObject();
                }
                //takenNames = FileAccess.readFile("allCustomers.txt");
                ArrayList<String> temp;
                synchronized (lock) {
                    objectOutputStream.writeObject("readFile");
                    objectOutputStream.flush();
                    objectOutputStream.writeObject("allSellers.txt");
                    objectOutputStream.flush();
                    temp = (ArrayList<String>) objectInputStream.readObject();
                }
                //ArrayList<String> temp = FileAccess.readFile("allSellers.txt");
                for (int x = 0; x < temp.size(); x++) {
                    takenNames.add(temp.get(x));
                }
                if (!(takenNames.contains(username + ".txt"))) {
                    JOptionPane.showMessageDialog(null, "No account with that username" +
                            " exists.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    String correctPassword;
                    synchronized (lock) {
                        objectOutputStream.writeObject("readFile");
                        objectOutputStream.flush();
                        objectOutputStream.writeObject(username + ".txt");
                        objectOutputStream.flush();
                        correctPassword = ((ArrayList<String>) objectInputStream.readObject()).get(1);
                    }
                    //String correctPassword = FileAccess.readFile(username + ".txt").get(1);
                    if (!(password.equals(correctPassword))) {
                        JOptionPane.showMessageDialog(null, "The password is incorrect!" +
                                " Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        ArrayList<String> allCustomers;
                        ArrayList<String> allSellers;
                        synchronized (lock) {
                            objectOutputStream.writeObject("readFile");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject("allCustomers.txt");
                            objectOutputStream.flush();
                            allCustomers = (ArrayList<String>) objectInputStream.readObject();
                            objectOutputStream.writeObject("readFile");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject("allSellers.txt");
                            objectOutputStream.flush();
                            allSellers = (ArrayList<String>) objectInputStream.readObject();
                        }
                        //ArrayList<String> allCustomers = FileAccess.readFile("allCustomers.txt");
                        //ArrayList<String> allSellers = FileAccess.readFile("allSellers.txt");
                        if (customerBox.isSelected()) {
                            Customer currentCustomer;
                            ArrayList<String> fileContents;
                            synchronized (lock) {
                                objectOutputStream.writeObject("readFile");
                                objectOutputStream.flush();
                                objectOutputStream.writeObject(username + ".txt");
                                objectOutputStream.flush();
                                fileContents = (ArrayList<String>) objectInputStream.readObject();
                            }
                            if (allCustomers.contains(username + ".txt")) {
                                //ArrayList<String> fileContents = FileAccess.readFile(username + ".txt");
                                if (fileContents.get(0).equals("online")) {
                                    JOptionPane.showMessageDialog(null, "This account is" +
                                            " already online", "Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    String email = "temp";
                                    currentCustomer = Marketplace.returningCustomerSetter(username, email,
                                            password, objectInputStream, objectOutputStream);
                                    loginFrame.setVisible(false);
                                    closed = true;
                                    loginFrame.dispose();
                                    ArrayList<Product> products;
                                    synchronized (lock) {
                                        objectOutputStream.writeObject("updateStatus");
                                        objectOutputStream.flush();
                                        objectOutputStream.writeObject("online");
                                        objectOutputStream.flush();
                                        objectOutputStream.writeObject(username + ".txt");
                                        objectOutputStream.flush();

                                        objectOutputStream.writeObject("getAllProducts");
                                        objectOutputStream.flush();
                                        products = (ArrayList<Product>) objectInputStream.readObject();
                                    }
                                    //FileAccess.updateStatus("online", username + ".txt");
                                    //ArrayList<Product> products = FileAccess.getAllProducts();
                                    SwingUtilities.invokeLater(new CustomerGUI(currentCustomer, products,
                                            marketThread, objectOutputStream, objectInputStream, socket, lock));
                                }
                            } else {
                                int userChoice = JOptionPane.showOptionDialog(null, "There is" +
                                        " a matching account with a different user type.\n Would you like to sign in" +
                                        " to that account?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.
                                        ERROR_MESSAGE, null, null, null);
                                if (userChoice == JOptionPane.YES_OPTION) {
                                    if (fileContents.get(0).equals("online")) {
                                        JOptionPane.showMessageDialog(null, "This account is" +
                                                " already online", "Error", JOptionPane.ERROR_MESSAGE);
                                    } else {
                                        Seller currentSeller;
                                        String email = "temp";
                                        if (fileContents.size() <= 2) {
                                            currentSeller = new Seller(username, email, password, new ArrayList<>());
                                        } else {
                                            currentSeller = new Seller(username, email, password, Marketplace.
                                                    getProducts(username, objectOutputStream, objectInputStream));
                                        }
                                        SwingUtilities.invokeLater(new SellerGUI(currentSeller, marketThread,
                                                objectOutputStream, objectInputStream, socket, lock));
                                        loginFrame.setVisible(false);
                                        closed = true;
                                        loginFrame.dispose();
                                        synchronized (lock) {
                                            objectOutputStream.writeObject("updateStatus");
                                            objectOutputStream.flush();
                                            objectOutputStream.writeObject("online");
                                            objectOutputStream.flush();
                                            objectOutputStream.writeObject(username + ".txt");
                                            objectOutputStream.flush();
                                        }
                                    }
                                    //FileAccess.updateStatus("online", username + ".txt");
                                }
                            }
                            //CustomerGUI customerGUI = new CustomerGUI();

                        } else {
                            Seller currentSeller;
                            String email = "temp";
                            ArrayList<String> fileContents;
                            synchronized (lock) {
                                objectOutputStream.writeObject("readFile");
                                objectOutputStream.flush();
                                objectOutputStream.writeObject(username + ".txt");
                                objectOutputStream.flush();
                                fileContents = (ArrayList<String>) objectInputStream.readObject();
                            }
                            if (allSellers.contains(username + ".txt")) {
                                //ArrayList<String> fileContents = FileAccess.readFile(username + ".txt");
                                if (fileContents.get(0).equals("online")) {
                                    JOptionPane.showMessageDialog(null, "This account is" +
                                            " already online", "Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    if (fileContents.size() <= 2) {
                                        currentSeller = new Seller(username, email, password, new ArrayList<>());
                                    } else {
                                        currentSeller = new Seller(username, email, password, Marketplace.getProducts
                                                (username, objectOutputStream, objectInputStream));
                                    }
                                    SwingUtilities.invokeLater(new SellerGUI(currentSeller, marketThread,
                                            objectOutputStream, objectInputStream, socket, lock));
                                    loginFrame.setVisible(false);
                                    closed = true;
                                    loginFrame.dispose();
                                    synchronized (lock) {
                                        objectOutputStream.writeObject("updateStatus");
                                        objectOutputStream.flush();
                                        objectOutputStream.writeObject("online");
                                        objectOutputStream.flush();
                                        objectOutputStream.writeObject(username + ".txt");
                                        objectOutputStream.flush();
                                    }
                                    //FileAccess.updateStatus("online", username + ".txt");
                                }
                            } else {
                                int userChoice = JOptionPane.showOptionDialog(null, "There is" +
                                        " a matching account with a different user type.\n Would you like to sign in" +
                                        " to that account?", "Error", JOptionPane.YES_NO_OPTION, JOptionPane.
                                        ERROR_MESSAGE, null, null, null);
                                if (userChoice == JOptionPane.YES_OPTION) {
                                    if (fileContents.get(0).equals("online")) {
                                        JOptionPane.showMessageDialog(null, "This account is" +
                                                " already online", "Error", JOptionPane.ERROR_MESSAGE);
                                    } else {
                                        Customer currentCustomer;
                                        currentCustomer = Marketplace.returningCustomerSetter(username, email,
                                                password, objectInputStream, objectOutputStream);
                                        loginFrame.setVisible(false);
                                        closed = true;
                                        loginFrame.dispose();
                                        ArrayList<Product> products;
                                        synchronized (lock) {
                                            objectOutputStream.writeObject("updateStatus");
                                            objectOutputStream.flush();
                                            objectOutputStream.writeObject("online");
                                            objectOutputStream.flush();
                                            objectOutputStream.writeObject(username + ".txt");
                                            objectOutputStream.flush();

                                            objectOutputStream.writeObject("getAllProducts");
                                            objectOutputStream.flush();
                                            products = (ArrayList<Product>) objectInputStream.readObject();
                                        }

                                        //FileAccess.updateStatus("online", username + ".txt");
                                        //ArrayList<Product> products = FileAccess.getAllProducts();
                                        SwingUtilities.invokeLater(new CustomerGUI(currentCustomer, products,
                                                marketThread, objectOutputStream, objectInputStream, socket, lock));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }
}
