import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.MethodSorters;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Run this test 7th to ensure functionality.
 * <p>
 * A test to log in as a seller, edit a product, delete a product, and export
 * the products as a csv file.
 *
 * <p>Purdue University -- CS18000 -- Fall 2022</p>
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UnitTestSeven {
    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(TestCase.class);
        System.out.printf("Test Count: %d.\n", result.getRunCount());
        if (result.wasSuccessful()) {
            System.out.println("Excellent - all local tests ran successfully.");
        } else {
            System.out.printf("Tests failed: %d.\n", result.getFailureCount());
            for (Failure failure : result.getFailures()) {
                System.out.println(failure.getMessage());
                System.out.println(failure.getTestHeader());
                System.out.println(failure.getDescription());
                System.out.println(failure);
            }
        }
    }

    public static class TestCase {
        private final PrintStream originalOutput = System.out;
        private final InputStream originalSysin = System.in;

        @SuppressWarnings("FieldCanBeLocal")
        private ByteArrayInputStream testIn;

        @SuppressWarnings("FieldCanBeLocal")
        private ByteArrayOutputStream testOut;

        @Before
        public void outputStart() {
            testOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(testOut));
        }

        @After
        public void restoreInputAndOutput() {
            System.setIn(originalSysin);
            System.setOut(originalOutput);
        }

        private String getOutput() {
            return testOut.toString();
        }

        @SuppressWarnings("SameParameterValue")
        private void receiveInput(String str) {
            testIn = new ByteArrayInputStream(str.getBytes());
            System.setIn(testIn);
        }

        private static final String sellerUserInterface =
                "\nWhat would you like to do?" + System.lineSeparator() +
                        "-Create a product (1)" + System.lineSeparator() +
                        "-Edit a product (2)" + System.lineSeparator() +
                        "-Delete a product (3)" + System.lineSeparator() +
                        "-View your sales breakdown (4)" + System.lineSeparator() +
                        "-View your items in customer shopping carts (5)" + System.lineSeparator() +
                        "-Export products as a csv file (6)" + System.lineSeparator() +
                        "-Log out (7)" + System.lineSeparator();

        private static final String customerUserInterface =
                "\nWhat would you like to do?" + System.lineSeparator() +
                        "-Go to a specific product's page (1)" + System.lineSeparator() +
                        "-Search the marketplace (2)" + System.lineSeparator() +
                        "-Sort the marketplace (3)" + System.lineSeparator() +
                        "-View your purchase history (4)" + System.lineSeparator() +
                        "-View your shopping cart (5)" + System.lineSeparator() +
                        "-Log out (6)" + System.lineSeparator();

        private static final String productPage =
                "Would you like to:" + System.lineSeparator() +
                        "Buy this product (1)" + System.lineSeparator() +
                        "Add it to your shopping cart (2)" + System.lineSeparator() +
                        "Go back to the main page (3)" + System.lineSeparator();

        private static final String welcome = "Welcome to the marketplace!" + System.lineSeparator();
        private static final String home =
                "Would you like to:" + System.lineSeparator() +
                        "-Create a new account (1)" + System.lineSeparator() +
                        "-Log into an existing account (2)" + System.lineSeparator() +
                        "-Exit the marketplace (3)" + System.lineSeparator();

        private static final String farewell =
                "Thank you for using the marketplace. Have a nice day!" + System.lineSeparator();

        private static final String getProperty =
                "Which property would you like to edit?" + System.lineSeparator() +
                        "-Store name (1)" + System.lineSeparator() +
                        "-Product name (2)" + System.lineSeparator() +
                        "-Description (3)" + System.lineSeparator() +
                        "-Price (4)" + System.lineSeparator() +
                        "-Quantity available (5)" + System.lineSeparator() +
                        "-None (6)" + System.lineSeparator();

        private boolean fileExists(String fileName) {
            File file = new File(fileName);
            return file.exists() && file.isFile();
        }

        private long getFileSize(File file) {
            long length = file.length();
            return length;
        }

        @Test(timeout = 1000)
        public void testProductChanges() {
            String input = "2" + System.lineSeparator() +
                    "2" + System.lineSeparator() +
                    "seller" + System.lineSeparator() +
                    "password" + System.lineSeparator() +
                    "2" + System.lineSeparator() +
                    "apple" + System.lineSeparator() +
                    "1" + System.lineSeparator() +
                    "Target" + System.lineSeparator() +
                    "2" + System.lineSeparator() +
                    "apple" + System.lineSeparator() +
                    "2" + System.lineSeparator() +
                    "pencil" + System.lineSeparator() +
                    "2" + System.lineSeparator() +
                    "pencil" + System.lineSeparator() +
                    "3" + System.lineSeparator() +
                    "Just a pencil." + System.lineSeparator() +
                    "2" + System.lineSeparator() +
                    "pencil" + System.lineSeparator() +
                    "4" + System.lineSeparator() +
                    "0.99" + System.lineSeparator() +
                    "2" + System.lineSeparator() +
                    "pencil" + System.lineSeparator() +
                    "5" + System.lineSeparator() +
                    "600" + System.lineSeparator() +
                    "2" + System.lineSeparator() +
                    "sharpie" + System.lineSeparator() +
                    "1" + System.lineSeparator() +
                    "pencil" + System.lineSeparator() +
                    "6" + System.lineSeparator() +
                    "3" + System.lineSeparator() +
                    "banana" + System.lineSeparator() +
                    "6" + System.lineSeparator() +
                    "7" + System.lineSeparator() +
                    "3" + System.lineSeparator();

            String expected = welcome + home +
                    "Would you like to log into a customer account (1) or a seller account (2)?" +
                    System.lineSeparator() +
                    "Please enter your username: " + System.lineSeparator() +
                    "Enter the password for seller:" + System.lineSeparator() +
                    "You have successfully logged in!" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| apple              | fruitstand   | appleDescappleDescappleDescapp | 6        | $1.99        |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 14       | $4.92        |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    sellerUserInterface +
                    "Enter the name of the product you would like to edit:" + System.lineSeparator() +
                    getProperty +
                    "Enter the new store name:" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 14       | $4.92        |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    "| apple              | Target       | appleDescappleDescappleDescapp | 6        | $1.99        |"
                    + System.lineSeparator() +
                    sellerUserInterface +
                    "Enter the name of the product you would like to edit:" + System.lineSeparator() +
                    getProperty +
                    "Enter the new product name:" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 14       | $4.92        |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    "| pencil             | Target       | appleDescappleDescappleDescapp | 6        | $1.99        |"
                    + System.lineSeparator() +
                    sellerUserInterface +
                    "Enter the name of the product you would like to edit:" + System.lineSeparator() +
                    getProperty +
                    "Enter the new product description:" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 14       | $4.92        |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    "| pencil             | Target       |                 Just a pencil. | 6        | $1.99        |"
                    + System.lineSeparator() +
                    sellerUserInterface +
                    "Enter the name of the product you would like to edit:" + System.lineSeparator() +
                    getProperty +
                    "Enter the new price:" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 14       | $4.92        |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    "| pencil             | Target       |                 Just a pencil. | 6        | $0.99        |"
                    + System.lineSeparator() +
                    sellerUserInterface +
                    "Enter the name of the product you would like to edit:" + System.lineSeparator() +
                    getProperty +
                    "Enter the new quantity:" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 14       | $4.92        |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    "| pencil             | Target       |                 Just a pencil. | 600      | $0.99        |"
                    + System.lineSeparator() +
                    sellerUserInterface +
                    "Enter the name of the product you would like to edit:" + System.lineSeparator() +
                    "The product was not found. Would you like to:" + System.lineSeparator() +
                    "-Search for another product (1)" + System.lineSeparator() +
                    "-Go back to the main page (2)" + System.lineSeparator() +
                    "Enter the name of the product you would like to edit:" + System.lineSeparator() +
                    getProperty +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 14       | $4.92        |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    "| pencil             | Target       |                 Just a pencil. | 600      | $0.99        |"
                    + System.lineSeparator() +
                    sellerUserInterface +
                    "Enter the name of the product you would like to delete:" + System.lineSeparator() +
                    "Product was deleted!\n" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    "| pencil             | Target       |                 Just a pencil. | 600      | $0.99        |"
                    + System.lineSeparator() +
                    sellerUserInterface +
                    "Export successful" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    "| pencil             | Target       |                 Just a pencil. | 600      | $0.99        |"
                    + System.lineSeparator() +
                    sellerUserInterface +
                    home + farewell;

            receiveInput(input);
            Marketplace.main(new String[0]);

            String output = getOutput();

            expected = expected.replaceAll("\r\n", "\n");
            output = output.replaceAll("\r\n", "\n");
            assertEquals("Check output!",
                    expected.trim(), output.trim());

            try {
                BufferedReader actual = new BufferedReader(new FileReader("seller.txt"));
                assertEquals("seller.txt: 1st line error", "seller@gmail.com", actual.readLine());
                assertEquals("seller.txt: 2nd line error", "password", actual.readLine());
                assertEquals("seller.txt: 3rd line error", "Target,Floss,dental floss,3,2.50,0",
                        actual.readLine());
                assertEquals("seller.txt: 4th line error", "Target,pencil,Just a pencil.,600,0.99,0",
                        actual.readLine());
                assertTrue(fileExists("sellerExport.csv"));
            } catch (IOException e) {
                System.err.println("IOException: Likely cannot find file");
            }
        }
    }
}
