import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

/**
 * Task 5 - Password Generator
 *
 * Builds a random password with the length and character types chosen by the
 * user. It uses SecureRandom and guarantees one character from every enabled
 * character class.
 */
public class PasswordGenerator {

    static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    static final String DIGITS = "0123456789";
    static final String SYMBOLS = "!@#$%^&*()-_=+?";
    static final String AMBIGUOUS = "O0oIl1|";

    static final int MIN_LENGTH = 4;
    static final int MAX_LENGTH = 128;

    static final SecureRandom RANDOM = new SecureRandom();
    static Scanner scanner = new Scanner(System.in);
    static boolean inputEnded = false;

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--demo")) {
            runDemo();
        } else {
            runGenerator();
        }
        scanner.close();
    }

    static void runGenerator() {
        System.out.println("=== Password Generator ===");
        System.out.println("Passwords use java.security.SecureRandom.");

        boolean again = true;
        while (again) {
            int length = readLength();
            if (length == -1) {
                System.out.println("Input ended. Goodbye!");
                return;
            }

            boolean upper = askYesNo("Include uppercase letters? (y/n): ", true);
            boolean lower = askYesNo("Include lowercase letters? (y/n): ", true);
            boolean digits = askYesNo("Include digits? (y/n): ", true);
            boolean symbols = askYesNo("Include symbols? (y/n): ", true);
            boolean excludeAmbiguous = askYesNo(
                    "Exclude ambiguous characters (" + AMBIGUOUS + ")? (y/n): ", true);

            if (inputEnded) {
                System.out.println("Input ended. Goodbye!");
                return;
            }

            if (!upper && !lower && !digits && !symbols) {
                System.out.println("Enable at least one character class. Starting over.");
                System.out.println();
                continue;
            }

            String password = generate(length, upper, lower, digits, symbols,
                    excludeAmbiguous);
            int pool = poolSize(upper, lower, digits, symbols, excludeAmbiguous);
            double entropy = entropyBits(length, pool);

            System.out.println();
            System.out.println("Password : " + password);
            System.out.println("Length   : " + password.length());
            System.out.println("Classes  : " + describeClasses(upper, lower,
                    digits, symbols));
            System.out.println("Pool size: " + pool);
            System.out.printf(Locale.US, "Entropy  : %.1f bits (%s)%n",
                    entropy, strengthFor(entropy));
            System.out.println();

            again = askYesNo("Generate another password? (y/n): ", true);
            if (inputEnded) {
                System.out.println("Input ended. Goodbye!");
                return;
            }
        }

        System.out.println("Goodbye!");
    }

    static String generate(int length, boolean useUpper, boolean useLower,
                           boolean useDigits, boolean useSymbols,
                           boolean excludeAmbiguous) {
        ArrayList<String> pools = enabledPools(useUpper, useLower, useDigits,
                useSymbols, excludeAmbiguous);
        String all = combine(pools);

        char[] password = new char[length];
        int position = 0;

        for (int i = 0; i < pools.size(); i++) {
            String pool = pools.get(i);
            password[position] = pool.charAt(RANDOM.nextInt(pool.length()));
            position++;
        }

        while (position < length) {
            password[position] = all.charAt(RANDOM.nextInt(all.length()));
            position++;
        }

        for (int i = password.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }

        return new String(password);
    }

    static int poolSize(boolean useUpper, boolean useLower, boolean useDigits,
                        boolean useSymbols, boolean excludeAmbiguous) {
        ArrayList<String> pools = enabledPools(useUpper, useLower, useDigits,
                useSymbols, excludeAmbiguous);
        return combine(pools).length();
    }

    static ArrayList<String> enabledPools(boolean useUpper, boolean useLower,
                                          boolean useDigits, boolean useSymbols,
                                          boolean excludeAmbiguous) {
        ArrayList<String> pools = new ArrayList<String>();

        if (useUpper) {
            pools.add(filterPool(UPPERCASE, excludeAmbiguous));
        }
        if (useLower) {
            pools.add(filterPool(LOWERCASE, excludeAmbiguous));
        }
        if (useDigits) {
            pools.add(filterPool(DIGITS, excludeAmbiguous));
        }
        if (useSymbols) {
            pools.add(filterPool(SYMBOLS, excludeAmbiguous));
        }

        return pools;
    }

    static String filterPool(String pool, boolean removeAmbiguous) {
        if (!removeAmbiguous) {
            return pool;
        }

        StringBuilder filtered = new StringBuilder();
        for (int i = 0; i < pool.length(); i++) {
            char character = pool.charAt(i);
            if (AMBIGUOUS.indexOf(character) < 0) {
                filtered.append(character);
            }
        }
        return filtered.toString();
    }

    static String combine(ArrayList<String> pools) {
        StringBuilder combined = new StringBuilder();
        for (int i = 0; i < pools.size(); i++) {
            combined.append(pools.get(i));
        }
        return combined.toString();
    }

    static double entropyBits(int length, int poolSize) {
        return length * (Math.log(poolSize) / Math.log(2.0));
    }

    static String strengthFor(double entropy) {
        if (entropy < 40.0) {
            return "Weak";
        }
        if (entropy < 60.0) {
            return "Fair";
        }
        if (entropy < 80.0) {
            return "Strong";
        }
        return "Very strong";
    }

    static String describeClasses(boolean useUpper, boolean useLower,
                                  boolean useDigits, boolean useSymbols) {
        StringBuilder text = new StringBuilder();

        if (useUpper) {
            addClassName(text, "uppercase");
        }
        if (useLower) {
            addClassName(text, "lowercase");
        }
        if (useDigits) {
            addClassName(text, "digits");
        }
        if (useSymbols) {
            addClassName(text, "symbols");
        }

        return text.toString();
    }

    static void addClassName(StringBuilder text, String name) {
        if (text.length() > 0) {
            text.append(", ");
        }
        text.append(name);
    }

    static int readLength() {
        while (true) {
            String line = readLine("Length (" + MIN_LENGTH + "-"
                    + MAX_LENGTH + "): ");
            if (line == null) {
                inputEnded = true;
                return -1;
            }

            try {
                int length = Integer.parseInt(line.trim());
                if (length < MIN_LENGTH || length > MAX_LENGTH) {
                    System.out.println("Enter a whole number from "
                            + MIN_LENGTH + " to " + MAX_LENGTH + ".");
                    continue;
                }
                return length;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    static boolean askYesNo(String prompt, boolean defaultValue) {
        while (true) {
            String line = readLine(prompt);
            if (line == null) {
                inputEnded = true;
                return defaultValue;
            }

            String answer = line.trim().toLowerCase();
            if (answer.length() == 0) {
                return defaultValue;
            }
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }

            System.out.println("Please answer y or n.");
        }
    }

    static String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine();
    }

    static void runDemo() {
        System.out.println("=== Password Generator (demo) ===");
        System.out.println("All samples use java.security.SecureRandom.");
        System.out.println();

        printSample("Default, 16 characters:", 16, true, true, true, true, true);
        printSample("Short but complete, 4 characters:", 4, true, true, true, true, true);
        printSample("Long, 32 characters:", 32, true, true, true, true, true);
        printSample("Digits only, 8 characters:", 8, false, false, true, false, false);
        printSample("Letters only, 20 characters, ambiguous removed:",
                20, true, true, false, false, true);
    }

    static void printSample(String label, int length, boolean useUpper,
                            boolean useLower, boolean useDigits, boolean useSymbols,
                            boolean excludeAmbiguous) {
        String password = generate(length, useUpper, useLower, useDigits,
                useSymbols, excludeAmbiguous);
        int pool = poolSize(useUpper, useLower, useDigits, useSymbols,
                excludeAmbiguous);
        double entropy = entropyBits(length, pool);

        System.out.println(label);
        System.out.println("  Password : " + password);
        System.out.println("  Length   : " + password.length());
        System.out.println("  Classes  : " + describeClasses(useUpper, useLower,
                useDigits, useSymbols));
        System.out.println("  Pool     : " + pool + " characters");
        System.out.printf(Locale.US, "  Entropy  : %.1f bits (%s)%n",
                entropy, strengthFor(entropy));
        System.out.println();
    }
}
