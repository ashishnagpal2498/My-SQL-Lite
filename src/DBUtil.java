import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.lang.System;
import java.util.Random;
/**
 * Utility Class for helper functions
 */
public class DBUtil {
    /**
     * Hashes the input password using the MD5 algorithm.
     *
     * @param password The password to be hashed.
     * @return A string representing the hashed password in MD5 format.
     */
    public String hashPassword(String password ) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();

            StringBuilder hexStr = new StringBuilder();
            for (byte aByte : bytes) {
                hexStr.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return hexStr.toString();
        } catch(NoSuchAlgorithmException err){
           System.err.println(err);
        }
        return "";
    }
    /**
     * Generates a random 6-digit number as a captcha.
     *
     * @return An integer representing a randomly generated 6-digit number.
     */
    public int generateCaptcha(){
        Random random = new Random();
        return 100_000 + random.nextInt(900_000);
    }
    /**
     * Provides a delimiter string used for separating values in the database files.
     *
     * @return A string representing the delimiter used in the database files.
     */
    public String delimiter(){
        return "%#22054ever#%";
    }
}