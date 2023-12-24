import java.io.Serializable;
import javax.swing.*;
import java.util.ArrayList;

/**
 * A class which creates Seller objects. It extends Person and allows
 * for the management of the Seller's stores and products within those stores.
 *
 * <p>
 * Purdue University -- CS18000 -- Fall 2022 -- Project 4
 *
 * @author Adrien Qi
 * @version November 10, 2022
 */

public class Seller extends Person implements Serializable {
    private ArrayList<Store> storeList;

    public Seller(String name, String email, String password, ArrayList<Store> storeList) {
        super(name, email, password);
        this.storeList = storeList;
    }

    public ArrayList<Store> getStoreList() {
        return storeList;
    }

    public void setStoreList(ArrayList<Store> storeList) {
        this.storeList = storeList;
    }

    // remove the given store from the seller's storeList
    public void removeStore(Store store) {
        for (int i = 0; i < storeList.size(); i++) {
            if (storeList.get(i).getName().equals(store.getName())) {
                storeList.remove(i);
                break;
            }
        }
    }

    //add the given store to the seller's storeList
    public void addStore(Store store) {
        storeList.add(store);
    }

    // edit store name
    public void editStoreName(Store store, String name) {
        for (int i = 0; i < storeList.size(); i++) {
            if (storeList.get(i).getName().equals(store.getName())) {
                storeList.get(i).setName(name);
                break;
            }
        }
    }

    // helper method to create ArrayLists for product information
    public ArrayList<String> arrayifyProduct(Product product) {
        ArrayList<String> productArrayList = new ArrayList<>();
        productArrayList.add(product.getName());
        productArrayList.add(product.getDescription());
        productArrayList.add(Double.toString(product.getPrice()));
        productArrayList.add(Integer.toString(product.getQuantityAvailable()));
        productArrayList.add(product.getStoreName());
        productArrayList.add(Integer.toString(product.getShoppingCartCount()));

        return productArrayList;
    }

    // export all products into a csv file
    public void exportProducts(Seller seller) {
        //FileAccess.writeFile(arrayifyProduct(product), product.getName());
        ArrayList<String> allSellerProducts = new ArrayList<>();
        for (int i = 0; i < seller.storeList.size(); i++) {
            for (int j = 0; j < seller.storeList.get(i).getProducts().size(); j++) {
                ArrayList<String> currentProduct = arrayifyProduct(seller.storeList.get(i).getProducts().get(j));
                String productString = "";
                for (int a = 0; a < currentProduct.size(); a++) {
                    if (a == currentProduct.size() - 1) {
                        productString += currentProduct.get(a);
                    } else {
                        productString += currentProduct.get(a) + ",";
                    }
                }
                allSellerProducts.add(productString);
            }
        }
        FileAccess.writeFile(allSellerProducts, seller.getName() + "Export.csv");
    }

    // add product to store given store and csv
    public ArrayList<Product> importProducts(String csv) {
        ArrayList<String> file = FileAccess.readFile(csv);
        ArrayList<Product> importedProducts = new ArrayList<>();
        for (int i = 0; i < file.size(); i++) {
            if (file.get(i).isEmpty()) {
                continue;
            }
            String currentLine = file.get(i);
            String[] productInfo = currentLine.split(",");

            if (productInfo.length != 5) {
                JOptionPane.showMessageDialog(null, "Incorrect formatting on line  "
                        + (i + 1) + "!", "Import Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            String productName = productInfo[0];
            //check if names are taken already
            if (!FileAccess.searchProduct(productName)) {
                String productDescription = productInfo[1];
                double productPrice = Double.parseDouble(productInfo[2]);
                int quantity = Integer.parseInt(productInfo[3]);
                String storeName = productInfo[4];
                Product product = new Product(productName, productDescription, productPrice, quantity, 0,
                        storeName);
                importedProducts.add(product);
            } else {
                JOptionPane.showMessageDialog(null, "The product name '" + productName +
                        "' is already taken!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return importedProducts;
    }
}