import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.MethodSorters;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Run this test 6th to ensure functionality.
 * <p>
 * A test to buy all carted items at once and export a customer's
 * history as a .txt file.
 *
 * <p>Purdue University -- CS18000 -- Fall 2022</p>
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UnitTestSix {
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

        private boolean fileExists(String fileName) {
            File file = new File(fileName);
            return file.exists() && file.isFile();
        }

        private long getFileSize(File file) {
            long length = file.length();
            return length;
        }

        @Test(timeout = 1000)
        public void buyShoppingCart() {
            String input = "2" + System.lineSeparator() +
                    "1" + System.lineSeparator() +
                    "customer" + System.lineSeparator() +
                    "password" + System.lineSeparator() +
                    "1" + System.lineSeparator() +
                    "watermelon" + System.lineSeparator() +
                    "1" + System.lineSeparator() +
                    "banana" + System.lineSeparator() +
                    "2" + System.lineSeparator() +
                    "25" + System.lineSeparator() +
                    "5" + System.lineSeparator() +
                    "5" + System.lineSeparator() +
                    "1" + System.lineSeparator() +
                    "4" + System.lineSeparator() +
                    "Export history" + System.lineSeparator() +
                    "1" + System.lineSeparator() +
                    "6" + System.lineSeparator() +
                    "3" + System.lineSeparator();

            String expected = welcome + home +
                    "Would you like to log into a customer account (1) or a seller account (2)?" +
                    System.lineSeparator() +
                    "Please enter your username: " + System.lineSeparator() +
                    "Enter the password for customer:" + System.lineSeparator() +
                    "You have successfully logged in!" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Available Products: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| apple              | fruitstand   | appleDescappleDescappleDescapp | 10       | $1.99        |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 19       | $4.92        |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    customerUserInterface +
                    "Enter the name of the product whose page you would like to go to:" + System.lineSeparator() +
                    "This product was not found. Would you like to:" + System.lineSeparator() +
                    "-Search again (1)" + System.lineSeparator() +
                    "-Go back to the main page (2)" + System.lineSeparator() +
                    "Enter the name of the product whose page you would like to go to:" + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 19       | $4.92        |"
                    + System.lineSeparator() +
                    productPage +
                    "How many units of the product would you like to add to the shopping cart? (Enter 0 to exit)"
                    + System.lineSeparator() +
                    "There are only 19 units left!" + System.lineSeparator() +
                    "How many units of the product would you like to add to the shopping cart? (Enter 0 to exit)"
                    + System.lineSeparator() +
                    "This item has been added to your shopping cart!" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Available Products: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "|        Name        |  Store Name  |            Description         | Quantity |     Price    |"
                    + System.lineSeparator() +
                    "| ------------------ | ------------ | ------------------------------ | -------- | ------------ |"
                    + System.lineSeparator() +
                    "| apple              | fruitstand   | appleDescappleDescappleDescapp | 10       | $1.99        |"
                    + System.lineSeparator() +
                    "| banana             | fruitstand   |              bananadescription | 19       | $4.92        |"
                    + System.lineSeparator() +
                    "| Floss              | Target       |                   dental floss | 3        | $2.50        |"
                    + System.lineSeparator() +
                    customerUserInterface +
                    "User shopping cart:" + System.lineSeparator() +
                    "4 units of the apple product" + System.lineSeparator() +
                    "5 units of the banana product" + System.lineSeparator() +
                    "\nWould you like to:" + System.lineSeparator() +
                    "-Buy everything in the shopping cart (1)" + System.lineSeparator() +
                    "-Go back to the main page (2)" + System.lineSeparator() +
                    "Your cart has been successfully purchased! Thanks for shopping with us!\n"
                    + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Available Products: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
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
                    customerUserInterface +
                    "User purchase history:" + System.lineSeparator() +
                    " - 7 units of the Floss product." + System.lineSeparator() +
                    " - 4 units of the apple product." + System.lineSeparator() +
                    " - 5 units of the banana product." + System.lineSeparator() +
                    "\nWould you like to:" + System.lineSeparator() +
                    "-Export purchase history as a file (1)" + System.lineSeparator() +
                    "-Go back to the home page (2)" + System.lineSeparator() +
                    "Invalid input! Please enter \"1\" or \"2\"." + System.lineSeparator() +
                    "User purchase history:" + System.lineSeparator() +
                    " - 7 units of the Floss product." + System.lineSeparator() +
                    " - 4 units of the apple product." + System.lineSeparator() +
                    " - 5 units of the banana product." + System.lineSeparator() +
                    "\nWould you like to:" + System.lineSeparator() +
                    "-Export purchase history as a file (1)" + System.lineSeparator() +
                    "-Go back to the home page (2)" + System.lineSeparator() +
                    "Your history contents are in the file 'customer_history.txt'." + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Available Products: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
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
                    customerUserInterface +
                    home + farewell;

            receiveInput(input);
            Marketplace.main(new String[0]);

            String output = getOutput();

            expected = expected.replaceAll("\r\n", "\n");
            output = output.replaceAll("\r\n", "\n");
            assertEquals("Check output!",
                    expected.trim(), output.trim());
            try {
                BufferedReader actual = new BufferedReader(new FileReader("customer.txt"));
                assertEquals("customer.txt: 1st line error", "customer@gmail.com", actual.readLine());
                assertEquals("customer.txt: 2nd line error", "password", actual.readLine());
                assertEquals("customer.txt: 3rd line error", "Floss,7;apple,4;banana,5;",
                        actual.readLine());
                assertEquals("customer.txt: 4th line error", "", actual.readLine());

            } catch (IOException e) {
                System.err.println("IOException: Likely cannot find file");
            }
            try {
                BufferedReader actual = new BufferedReader(new FileReader("customer_history.txt"));
                assertEquals("customer.txt: 1st line error", "customer's Shopping History:",
                        actual.readLine());
                assertEquals("customer.txt: 2nd line error", " - 7 units of Floss.",
                        actual.readLine());
                assertEquals("customer.txt: 3rd line error", " - 4 units of apple.",
                        actual.readLine());
                assertEquals("customer.txt: 4th line error", " - 5 units of banana.",
                        actual.readLine());

            } catch (IOException e) {
                System.err.println("IOException: Likely cannot find file");
            }

        }
    }
}
