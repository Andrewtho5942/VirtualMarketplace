import org.junit.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Run tests in numerical order to ensure that the files are in the correct state
 * to guarantee correct functionality.
 * <p>
 * A test to create main files automatically (allCustomers, allSellers, and transactionHistory),
 * to create a new seller account, and to test the output with no history, shopping cart, or products.
 *
 * <p>Purdue University -- CS18000 -- Fall 2022</p>
 */

public class UnitTestOne {
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


        // helper methods
        private boolean fileExists(String fileName) {
            File file = new File(fileName);
            return file.exists() && file.isFile();
        }


        @Test(timeout = 1000)
        public void sellerAccountCreation() {
            // setting input
            String input =
                    "4" + System.lineSeparator() +
                            "1" + System.lineSeparator() +
                            "3" + System.lineSeparator() +
                            "2" + System.lineSeparator() +
                            "seller" + System.lineSeparator() +
                            "" + System.lineSeparator() +
                            "seller@gmail.com" + System.lineSeparator() +
                            "" + System.lineSeparator() +
                            "password" + System.lineSeparator() +
                            "10" + System.lineSeparator() +
                            "7" + System.lineSeparator() +
                            "3" + System.lineSeparator();

            //set output
            String expected = welcome + home +
                    "Invalid input! Please enter \"1\", \"2\" or \"3\"." + System.lineSeparator() +
                    home +
                    "Would you like create a customer account (1) or a seller account (2)?" + System.lineSeparator() +
                    "Invalid input! Please enter \"1\" or \"2\"." + System.lineSeparator() +
                    "Would you like create a customer account (1) or a seller account (2)?" + System.lineSeparator() +
                    "What is your username?" + System.lineSeparator() +
                    "What is your email?" + System.lineSeparator() +
                    "Invalid input! Please enter at least 1 character for your email." + System.lineSeparator() +
                    "What is your email?" + System.lineSeparator() +
                    "Please enter your new password:" + System.lineSeparator() +
                    "Your password must be at least 1 character long" + System.lineSeparator() +
                    "Please enter your new password:" + System.lineSeparator() +
                    "Your account creation was a success! Welcome, seller!" + System.lineSeparator() +
                    "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Seller ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                    + System.lineSeparator() +
                    "You aren't selling any products currently!" + System.lineSeparator() +
                    sellerUserInterface +
                    "Invalid input! Please enter \"1\", \"2\", \"3\", \"4\", \"5\", \"6\" or \"7\"." +
                    sellerUserInterface +
                    "Would you like to:\n" +
                    "-Create a new account (1)\n" +
                    "-Log into an existing account (2)\n" +
                    "-Exit the marketplace (3)" + System.lineSeparator() +
                    "Thank you for using the marketplace. Have a nice day!" + System.lineSeparator();

            receiveInput(input);
            Marketplace.main(new String[0]);

            String output = getOutput();

            expected = expected.replaceAll("\r\n", "\n");
            output = output.replaceAll("\r\n", "\n");
            assertEquals("Check seller account creation!",
                    expected.trim(), output.trim());
            assertTrue(fileExists("seller.txt"));
            assertTrue(fileExists("allCustomers.txt"));
            assertTrue(fileExists("allSellers.txt"));
            assertTrue(fileExists("transactionHistory.txt"));

            try {
                BufferedReader actual = new BufferedReader(new FileReader("allSellers.txt"));
                assertEquals("allSellers.txt: 1st line error", "seller.txt", actual.readLine());
                assertTrue(fileExists("allCustomers.txt"));
                assertTrue(fileExists("allSellers.txt"));
                assertTrue(fileExists("transactionHistory.txt"));
            } catch (IOException e) {
                System.err.println("IOException: Likely cannot find file");
            }
        }
    }
}
