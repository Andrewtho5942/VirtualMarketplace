# CS 180 Project 5 Readme
by Michael Lenkeit, Andrew Thompson, Adrien Qi, and Grant Strickland.

# Compilation Instructions
Run FileAccess.java to start the server, then run Marketplace.java to access the server. The necessary files will be created automatically when the program runs.

# Submission Breakdown
Andrew will be submitting everything, which includes the code, project report, and the presentation.

# Customer.java
This class creates a Customer object that inherits from Person.java. It contains the same functionality and fields, with additional variables containing the Customer's purchase history and shopping cart, enabling users to save and purchase items

Fields
- String history - the customer's purchase history. It is updated whenever the user buys something, recording the name of the product and the quantity purchased.
- String shoppingCart - the customer's current shopping cart. It is updated whenever the user adds something to their shopping cart, recording the name of the product and the quantity saved. 

Methods
- public Customer(String name, String email, String password) is the constructor used for new Customers. It instantiates the user's login information, initialized the history and shopping cart as empty Strings, and creates a new Customer File.
- public Customer(String name, String email, String password, String history, String shoppingCart) is the constructor used for returning Customers. It instanties the user's login information, history and shopping cart. 
- public void buyProduct(Product product, int quantityPurchased) is called whenever the customer buys a product. It adds the product to the customer's purchase history and decreases the quantity of the product purchased by the amount that the user bought.
- public void addToHistory(Product product, int quantity purchased) appends the product passed in to the end of the user's purchase history with the quantity purchased
- public void addToCart(Product product, int buyQuantity) adds the specified number of the product to the end of the user's shopping cart. If the product is already in there then it instead increases the amount of the product in the shopping cart
- public void removeFromCart(Product product) removes the specified product from the shopping cart entirely
- public void removeFromCart(String productName) removes the product with the specified name from the shopping cart entirely
- public void buyCart() purchases every item in the user's shopping cart at once, making sure that it doesn't purchase more of any one item than is available

# CustomerGUI.java
This class creates the custom user interface for the customer’s home page. It lists out all of the products available for purchase in the marketplace and provides the user with a series of options asking them how they would like to traverse the marketplace

Fields
- ArrayList<String> localChanges - stores a list of all changes made to the Marketplace while the customer is logged in, which are then displayed when the user refreshes
- Customer currentCustomer - the customer whose account is being utilized on this specific thread
- JFrame customerUI - frame to contain all relevant information
- JPanel buttonPanel - panel that holds all of the JButtons
- JPanel marketPlanel - panel that holds the product information
- JButton viewPageButton, searchButton, sortButton, historyButton, cartButton, refreshButton, logoutButton - buttons the user selects from to perform various tasks in the marketplace
- ArrayList<Product> products - an ArrayList of all the products available in the marketplace
- Marketplace marketThread - a Marketplace object to communicate with as a client in each thread
- ObjectOutputStream objectOutputStream - stream to send information back to Marketplace.java
- ObjectInputStream objectInputStream - stream to receive information from Marketplace.java
- Socket socket - socket used to handle server interactions
- static Object lock - static object used to synchronize across threads to prevent race conditions

Methods
- public CustomerGUI(Customer currentCustomer, ArrayList<Product> products, Marketplace marketThread, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, Socket socket, Object lock) - constructor that instantiates the necessary fields for communication between server and client
- public void run() - creates the components for and displays the custom GUI, and handles control flow based on which buttons the user selects
- public void refresh(ArrayList<Product> products) - refreshes the JFrame with any changes made to the product list

# FileAccess.java
This class acts as the server for the marketplace, handling all interactions involving files to avoid race conditions from occurring. It reads from and writes to central files containing a list of each account name and a record of all new transactions. It also accesses and changes the data within the files for each account, including the shopping cart and transaction history for Customers and the product list for Sellers.

Fields
- Socket socket - enables the class to function as a server, handling interactions with clients

Methods
- private FileAccess(Socket socket) - instantiates a Socket object for the server to function
- public void run() - creates input and output streams to handle interactions between the server and clients, and calls appropriate methods within FileAccess.java based on what is sent in from the clients
- public static synchronized boolean updateTransactionHistory(String transaction) - adds a specific purchase made to the end of the global transaction history file. Returns true if successful and false if not.
- public static synchronized void replaceInTransactionHistory(Product product, String oldName) - updates the product name or store name of the product in the transaction history file whenever a Seller changes a product name or store name
- public static synchronized boolean update History(String history, String customerAccountFile) - replaces the customer’s purchase history with the new history passed in as a parameter. Returns true if successful and false if not
- public static synchronized boolean updateStatus(String status, String accountFile) - updates the account file to correctly state whether the user is online or offline whenever they log in or out. Returns true if successful and false if not
- public static synchronized void updateShoppingCart(String shoppingCard, Product product, String customerAccountFile) - replaces the customer’s shopping cart with the new shopping cart passed in as a parameter, then updates the quantity of product in customer shopping carts for the seller of the products
- public static synchronized void updateProduct(Product product, String sellerAccountFile, int variable, String oldName) - updates the product’s properties in the file whenever the seller edits the product. 
- public synchronized static ArrayList<Product> getAllProducts() - returns an ArrayList of every product in the marketplace
- public synchronized static ArrayList<Product> getSellerProducts(Seller seller) - returns an ArrayList of every product that is sold by a specific seller
- public synchronized static ArrayList<Store> getAllStores() - returns an ArrayList of every store in the marketplace
- public synchronized static ArrayList<String> readFile(String fileName) - returns an ArrayList containing every line in the file from which information can be read.
- public synchronized static boolean writeFile(ArrayList<String> newContents, String fileName) overwrites the contents of the file with the new contents passed in as a parameter. Returns true if successful and false if not
- public static synchronized boolean createCustomerFile(Customer customer) - creates a new file for the parameterized customer if no such file exists. Returns true if a new file is successfully created and false if not
- public static synchronized boolean createSellerFile(Seller seller) - creates a new file for the parameterized seller if no such file exists. Returns true if a new file is successfully created and false if not
- public static synchronized boolean searchUsername(String username) - searches for a matching username within both the customer username database and the seller username database. Returns true if a username is found and false if it is not
- public static synchronized boolean searchProduct(String productName) - searches for a matching product name within the ArrayList of all products. Returns true if a product with that name exists and false if it does not
- public static synchronized boolean searchStore(String storeName, Seller seller) - searches for a matching store name within the list of all products. Returns true if a matching store name is found and false if it is not
- public static synchronized boolean appendToFile(String line, String fileName) - adds a line to the end of the specified file. Returns true if successful and false if it encounter an error
- public static synchronized void updateQuantity(Product product) - replaces the quantity of the product stored in the file with the changes made locally to its quantity whenever a product is purchased
- public static synchronized void exportHistory(Customer customer) - creates a file named “[customer name]_history.txt” that contains a formatted list of all the items the customer has bought
- public static synchronized void deleteProductInFiles(Product product, Seller seller) - when a product is deleted, goes through all of the customer files and removes that product from their shopping cart
- public static synchronized void changeNameInCustomer(String oldName, String newName) - when a product’s name is changed, goes through all of the customer files and updates the old name to the new name in their shopping carts
- public static void main(String[] args) - updates all users to be offline when the program starts running, then creates the ServerSocket and starts the server and threads

# LoginGUI.java
This class creates the custom user interface for the login page. It consists of text fields for the user’s email, username and password, as well as checkboxes to specify whether they are a Customer or a Seller

Fields:
- JButton registerButton, signInButton, backButton - buttons that let the user either confirm their login or go back to the previous page
- JTextField usernameField, passwordField, emailField - text fields that let the user enter their information when creating a new account or logging in
- JCheckBox sellerBox, customerBox - checkboxes that let the user indicate whether they are logging in as a seller or as a customer
- JFrame loginFrame - overall frame used to contain all of the individual components of the login screen
- ObjectOutputStream objectOutputStream - stream to send information back to Marketplace.java
- ObjectInputStream objectInputStream - stream to receive information from Marketplace.java
- Socket socket - socket used to handle server interactions
- static Object lock - static object used to synchronize across threads to prevent race conditions
- Marketplace marketThread - a Marketplace object to communicate with as a client in each thread

Methods:
- public LoginGUI(Marketplace marketThread, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, Socket socket, Object lock) - constructor that instantiates the necessary fields for communication between server and client
- public void run() - creates the components for and displays the custom GUI, and handles control flow based on which buttons the user selects
- public void processRegister(String email, String username, String password) - handles control flow and displays relevant errors for if the user is creating a new account
- public void processSignIn(String username, String password) - handles control flow and displays relevant errors for if the user is signing into an existing account

# Marketplace.java
This class handles the overall control flow of the Marketplace, handling the client side of things by organizing the correct sequence of events and processing user input

Fields:
- int startIndex
- ArrayList<String> globalChanges - ArrayList of all changes made in other threads, to be updated when the user refreshes
- ArrayList<String> localChanges - ArrayList of all changes made in this thread, to be sent to other threads and updated there
- Socket socket - socket used to handle server interactions
- static final Object lock - static object used to synchronize across threads to prevent race conditions

Methods
- public static void main(String[] args) - main method where the threading starts
- public static ArrayList<Product> printAllProducts(ArrayList<Product> products, int sortType) - returns an ArrayList of all of the products passed in as a parameter, sorted using a Bubble Sort algorithm based on the specified sort type (alphabetical, decreasing price or decreasing quantity)
- public void showThreadChanges(String sellerFile, Person person, ArrayList<String> passedChanges, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) - informs the user of any changes made in other threads when the user refreshes or performs an action
- public boolean isProductSoldBy(String productName, String sellerFile, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) - checks if a specific product is sold by a specific seller or not. Returns true if the seller does sell that product and false if they do not
-  public static Customer returningCustomerSetter(String name, String email, String password, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) - properly sets the new Customer object created after login with the necessary parameters based on what information exists or doesn’t exist in the file
-  public static String deleteProduct(Seller s, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) - iterates through the Seller’s products and removes the object if it exists
- public String editProduct(Seller s, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) - asks the user which product they want to edit, which variable of that product they would like to change and what they want the new value of it to be. If the product’s name is updated, returns a String consisting of the old name and the new name separated by a comma, otherwise returns a blank string if a value is changed and null if it is not
- public static void viewSales(Seller s, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) - enables the user to select a specific store they would like to view the sales breakdown of and displays it to them
- public static void viewCartedProducts(ArrayList<Product> products) - prints out all of the user’s products that exist in customer shopping carts, and how many of each exist there
- public void newProduct(Seller currentSeller, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) - asks the user to create a new product, either from scratch or through importation. Asks them what they want the product name, store name, description, quantity and price to be if creating from scratch, and asks for the file name if created through importation
- public static void addToStore(Seller s, Product p) - adds the parameter product to the parameter store, creating a new store if it doesn’t already exist
- public static ArrayList<Store> getProducts(String name, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) - returns an ArrayList of all the products being sold by a specific seller
- public void run() - handles file creation and the creation of a Marketplace object, then sends the user to the login GUI to handle the sign in process.

# MarketTester.java
Used to test multithreading in the Marketplace class by running multiple threads simultaneously

Methods
- public static void main(String[] args) - creates two Marketplace methods and starts both of their threads

# Person.java
This class serves as a parent class that Customer and Seller both inherit from. It contains fields that both subclasses have in common and setters and getters for each

Fields:
- String name - the username of the account. This is the same as the name of their associated file and is used to log into their account. Class contains a setter and getter.
- String email - the email of the account. Class contains a setter and getter.
- String password - the password of the account. This is used to log into their account. Class contains a setter and getter.

# Product.java
This class creates a Product object for sale in Stores by Sellers. It contains the necessary fields that makes each individual Product unique from each other.

Fields:
- String name - the name of the product. Has a setter and getter.
- String description - a brief description of the product provided by the Seller. Has a setter and getter.
- double price - the price of the product, rounded to 2 decimal places. Has a setter and getter.
- int quantityAvailable - how many of the product are available for purchase. Customers cannot purchase more of an item than is left for sale. Has a setter and getter.
- int shoppingCartCount - how many of the product exist in the shopping carts of customers. Has a setter and getter.
- String storeName - the name of the Store containing the product. Has a setter and getter.

Methods:
- public Product(String name, String description, double price, int quantityAvailable, int shoppingCartCount, String storeName) creates a Product object with the specified parameters
- public String toString() returns a properly formatted String containing all of the relevant information for the Product
- public String toReadable() returns a more readable version of the relevant information with proper spacing and a dollar sign for the price

# Seller.java
This class creates a Seller object that inherits from Person.java. It contains the same functionality and fields, with an additional ArrayList of Stores that house all of the products that the seller sells.

Fields:
- ArrayList storeList - the seller's stores. It lists all of the Stores owned by the Seller, which in turn contain all of the Products that the seller sells. Has a setter and getter

Methods:
- public Seller(String name, String email, String password, ArrayList<Store> storeList) - 
- public void removeStore(Store store) - removes the given store from the Seller's list of stores
- public void addStore(Store store) - adds the given store to the Seller's list of stores
- public void editStoreName(Store store, String name) - iterates through the Seller's list of stores until it finds the Store that matches the parameter, which it changes the name of.
- public ArrayList<String> arrayifyProduct(Product product) - returns an ArrayList version of the product's contents for easier use in other methods
- public void exportProducts(Seller seller) - exports all of the Seller’s products into a CSV file called “(seller name)Export.csv”.
- public ArrayList<Product> importProducts(String csv) - imports all products from the specified CSV file, updating products that are already in the store and adding ones that are not

# SellerGUI.java
This class creates the custom user interface for the seller’s home page. It lists out all of the products that they are selling and provides the user with a series of options asking them how they would like to traverse the marketplace

Fields
- ArrayList<String> localChanges - stores a list of all changes made to the Marketplace while the customer is logged in, which are then displayed when the user refreshes
- Seller currentSeller - the seller whose account is being utilized on this specific thread
- JFrame sellerUI - frame to contain all relevant information
- JPanel buttonPanel - panel that holds all of the JButtons
- JPanel marketPlanel - panel that holds the product information
- JButton viewPageButton, searchButton, sortButton, historyButton, cartButoon, refreshButton, logoutButton - buttons the user selects from to perform various tasks in the marketplace
- ArrayList<Product> products - an ArrayList of all the products available in the marketplace
- Marketplace marketThread - a Marketplace object to communicate with as a client in each thread
- ObjectOutputStream objectOutputStream - stream to send information back to Marketplace.java
- ObjectInputStream objectInputStream - stream to receive information from Marketplace.java
- Socket socket - socket used to handle server interactions
- static Object lock - static object used to synchronize across threads to prevent race conditions

Methods
- public SellerGUI(Seller currentSeller, Marketplace marketThread, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, Socket socket, Object lock) - constructor that instantiates the necessary fields for communication between server and client
- public void run() - creates the components for and displays the custom GUI, and handles control flow based on which buttons the user selects
- public void refresh(ArrayList<Product> products) - refreshes the JFrame with any changes made to the product list

# Store.java
This class creates a Store object for use by Sellers. Stores contain an ArrayList of Products that the owner is able to add to, remove from and edit.

Fields
- String name - the name of the store. Has a getter and setter.
- ArrayList<Product> product - an ArrayList of all of the Products for sale in the store. Has a getter and setter.
- String ownerName - the name of the owner of the store. Has a getter and setter.

Methods
- public Store(String name, String ownerName, ArrayList products) constructs a new Store object with the given parameters, including an existing ArrayList
- public Store(String name, String sellerName) constructs a new Store object with the given parameters. This constructor is used if there are no existing Products and creates an empty ArrayList of Products.
- public void addProduct(Product product) adds the parameter Product to the ArrayList of Products
- public void addProduct(String name, String description, double price, int quantityAvailable) constructs a new Product object with the given information and then adds it to the ArrayList of Products
- public boolean removeProduct(Product product) removes the parameter Product from the ArrayList of Products. It returns true if the product is successfully removed and false if it is not.
- public String toString() returns a String containing the store's name, the name of the store's owner, and the toString() of every Product sold in the Store.
