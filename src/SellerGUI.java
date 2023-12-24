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
import java.awt.event.*;

/**
 * This class handles creates the UI for Sellers allowing them to interact with the marketplace
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 5
 *
 * @author Grant Strickland
 * @version November 15, 2022
 */

public class SellerGUI implements Runnable {
    ArrayList<String> localChanges;
    Seller currentSeller;
    JFrame sellerUI;
    JPanel buttonPanel;
    JPanel marketPanel;
    JButton createButton;
    JButton editButton;
    JButton deleteButton;
    JButton salesButton;
    JButton cartButton;
    JButton exportButton;
    JButton refreshButton;
    JButton logoutButton;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;
    //market object for each thread
    Marketplace marketThread;
    Socket socket;
    private static Object lock;

    public SellerGUI(Seller currentSeller, Marketplace marketThread,
                     ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, Socket socket,
                     Object lock) {
        SellerGUI.lock = lock;
        this.socket = socket;
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
        this.marketThread = marketThread;
        localChanges = new ArrayList<>();
        this.currentSeller = currentSeller;
    }

    public void run() {
        ActionListener sellerButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (e.getSource() == createButton) {//create product
                        marketThread.newProduct(currentSeller, objectInputStream, objectOutputStream);
                        refresh();
                    } else if (e.getSource() == editButton) {//edit product
                        String change = marketThread.editProduct(currentSeller, objectInputStream, objectOutputStream);
                        if (change != null) {
                            localChanges.add(change);
                            System.out.println(localChanges);
                        }
                        refresh();
                    } else if (e.getSource() == deleteButton) {//delete product
                        String change = Marketplace.deleteProduct(currentSeller, objectOutputStream, objectInputStream);
                        if (change != null && !(change.equals(""))) {
                            localChanges.add(change);
                        }
                        refresh();
                    } else if (e.getSource() == refreshButton) {//refresh page
                        refresh();
                    } else if (e.getSource() == salesButton) {//view sales
                        Marketplace.viewSales(currentSeller, objectInputStream, objectOutputStream);
                    } else if (e.getSource() == cartButton) {//view shopping carts
                        ArrayList<Product> sellerProducts;
                        synchronized (lock) {
                            objectOutputStream.writeObject("getSellerProducts");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(currentSeller);
                            objectOutputStream.flush();
                            sellerProducts = (ArrayList<Product>) objectInputStream.readObject();
                        }
                        Marketplace.viewCartedProducts(sellerProducts);
                    } else if (e.getSource() == exportButton) {//export products
                        currentSeller.exportProducts(currentSeller);
                        JOptionPane.showMessageDialog(null, "Your products have been " +
                                        "exported\n File Name: " + currentSeller.getName() + "Export.csv",
                                "Export Products", JOptionPane.INFORMATION_MESSAGE);
                    } else if (e.getSource() == logoutButton) {//logout
                        synchronized (lock) {
                            objectOutputStream.writeObject("updateStatus");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject("offline");
                            objectOutputStream.flush();
                            objectOutputStream.writeObject(currentSeller.getName() + ".txt");
                            objectOutputStream.flush();
                        }
                        //FileAccess.updateStatus("offline", currentSeller.getName() + ".txt");
                        sellerUI.setVisible(false);
                        sellerUI.dispose();
                        SwingUtilities.invokeLater(new LoginGUI(marketThread, objectOutputStream, objectInputStream,
                                socket, lock));
                    }
                } catch (SocketException ex) {
                    JOptionPane.showMessageDialog(null, "Server is not available!" +
                            "\nYou were successfully logged out!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    sellerUI.setVisible(false);
                    sellerUI.dispose();
                    try {
                        socket.close();
                    } catch (IOException exc) {
                        exc.printStackTrace();
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        };
        sellerUI = new JFrame();
        sellerUI.setLayout(new BorderLayout());
        buttonPanel = new JPanel();
        sellerUI.add(buttonPanel, BorderLayout.SOUTH);
        marketPanel = new JPanel();
        sellerUI.add(marketPanel, BorderLayout.CENTER);
        createButton = new JButton("Create Product");
        createButton.addActionListener(sellerButtonListener);
        buttonPanel.add(createButton);
        editButton = new JButton("Edit Product");
        editButton.addActionListener(sellerButtonListener);
        buttonPanel.add(editButton);
        deleteButton = new JButton("Delete Product");
        deleteButton.addActionListener(sellerButtonListener);
        buttonPanel.add(deleteButton);
        salesButton = new JButton("View Sales");
        salesButton.addActionListener(sellerButtonListener);
        buttonPanel.add(salesButton);
        cartButton = new JButton("View Products in Carts");
        cartButton.addActionListener(sellerButtonListener);
        buttonPanel.add(cartButton);
        exportButton = new JButton("Export Products");
        exportButton.addActionListener(sellerButtonListener);
        buttonPanel.add(exportButton);
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(sellerButtonListener);
        buttonPanel.add(refreshButton);
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(sellerButtonListener);
        buttonPanel.add(logoutButton);

        try {
            ArrayList<Product> allProducts;
            synchronized (lock) {
                objectOutputStream.writeObject("getSellerProducts");
                objectOutputStream.flush();
                objectOutputStream.writeObject(currentSeller);
                objectOutputStream.flush();
                allProducts = (ArrayList<Product>) objectInputStream.readObject();
            }
            //ArrayList<Product> allProducts = FileAccess.getSellerProducts(currentSeller);
            marketPanel.setLayout(new GridLayout(allProducts.size() + 1, 3));
            if (allProducts.size() > 0) {
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

                for (int i = 0; i < allProducts.size(); i++) {
                    Product product = allProducts.get(i);

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
            } else {
                JLabel noProductsLabel = new JLabel("You're currently not selling any products!");
                marketPanel.add(noProductsLabel);
                noProductsLabel.setHorizontalAlignment(JLabel.CENTER);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        sellerUI.addWindowListener(sellerCloseListener);
        sellerUI.setSize(1000, 400);
        sellerUI.setLocationRelativeTo(null);
        sellerUI.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        sellerUI.setVisible(true);
        sellerUI.setTitle("Seller UI - " + currentSeller.getName() + "                  //              " +
                "    Marketplace");
    }

    WindowListener sellerCloseListener = new WindowListener() {
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
                    objectOutputStream.writeObject(currentSeller.getName() + ".txt");
                    objectOutputStream.flush();
                }
                // FileAccess.updateStatus("offline", currentSeller.getName() + ".txt");
                sellerUI.setVisible(false);
                sellerUI.dispose();
            } catch (SocketException ex) {
                JOptionPane.showMessageDialog(null, "Server is not available!\n" +
                        "You were successfully logged out!", "ERROR", JOptionPane.ERROR_MESSAGE);
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


    public void refresh() throws SocketException {
        sellerUI.setVisible(false);
        sellerUI.dispose();
        //show market thread updates
        marketThread.showThreadChanges(currentSeller.getName() + ".txt", currentSeller, localChanges,
                objectInputStream, objectOutputStream);
        SwingUtilities.invokeLater(new SellerGUI(currentSeller, marketThread, objectOutputStream,
                objectInputStream, socket, lock));
    }
}

