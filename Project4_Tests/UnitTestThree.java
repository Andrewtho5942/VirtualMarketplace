import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.MethodSorters;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Run this test third for correct functionality.
 * <p>
 * A test to create a new customer account.
 *
 * <p>Purdue University -- CS18000 -- Fall 2022</p>
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UnitTestThree {
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
        public void ACcustomerAccountCreation() {
            // setting input
            String input = "1" + System.lineSeparator() +
                    "1" + System.lineSeparator() +
                    "seller" + System.lineSeparator() +
                    "customer" + System.lineSeparator() +
                    "customer@gmail.com" + System.lineSeparator() +
                    "password" + System.lineSeparator() +
                    "4" + System.lineSeparator() +
                    "5" + System.lineSeparator() +
                    "log out!" + System.lineSeparator() +
                    "6" + System.lineSeparator() +
                    "3" + System.lineSeparator();


            //set output
            String expected = welcome + home +
                    "Would you like create a customer account (1) or a seller account (2)?" + System.lineSeparator() +
                    "What is your username?" + System.lineSeparator() +
                    "That username is already taken! Please select a unique name." + System.lineSeparator() +
                    "What is your username?" + System.lineSeparator() +
                    "What is your email?" + System.lineSeparator() +
                    "Please enter your new password:" + System.lineSeparator() +
                    "Your account creation was a success! Welcome, customer!" + System.lineSeparator() +
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
                    customerUserInterface +
                    "No items have been purchased yet!" + System.lineSeparator() +
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
                    customerUserInterface +
                    "Your shopping cart is empty!" + System.lineSeparator() +
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
                    customerUserInterface +
                    "Invalid input! Please enter \"1\", \"2\", \"3\", \"4\", \"5\" or \"6\"."
                    + System.lineSeparator() +
                    customerUserInterface +
                    home +
                    farewell;

            receiveInput(input);
            Marketplace.main(new String[0]);

            String output = getOutput();

            expected = expected.replaceAll("\r\n", "\n");
            output = output.replaceAll("\r\n", "\n");
            assertEquals("Check customer account creation!",
                    expected.trim(), output.trim());
            assertTrue(fileExists("customer.txt"));
        }
    }
}
