import java.lang.System;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.ByteBuffer;

public class Users {

    static File userFile = new File("users.txt");
    static FileLock lock;
    /**
     * Checks whether the user exists in the user file.
     *
     * @param username The username to check for existence.
     * @return True if the user exists; otherwise, false.
     */
    public boolean exists(String username){
        try {
            FileInputStream fis = new FileInputStream(userFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.isEmpty()) break;
                String db_username = line.split(" ")[0].split(":")[1];
                if ( db_username.equals(username) )
                    return true;
            }
        } catch (IOException error){
            System.out.println("User doesn't exist");
            System.out.println(error);
        }
        return false;
    }
    /**
     * Validates a user by username and hashed password.
     *
     * @param username  The username of the user to validate.
     * @param hashPass  The hashed password to validate.
     * @return True if the provided username and hashed password match; otherwise, false.
     */
    public boolean validateUser(String username, String hashPass){
        try {
            FileInputStream fis = new FileInputStream(userFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.isEmpty()) break;
                String db_username = line.split(" ")[0].split(":")[1];
                String db_password = line.split(" ")[1].split(":")[1];
                if ( db_username.equals(username) && db_password.equals(hashPass) )
                    return true;
            }
        } catch (IOException error){
            System.out.println(error);
        }
        return false;
    }
    /**
     * Creates a new user with the given username and hashed password.
     *
     * @param username  The username of the user to be created.
     * @param hashPass  The hashed password of the user to be created.
     */
    public void createUser(String username, String hashPass){
        if(exists(username)){
            System.err.println("User already exist. Try login");
            return;
        }
        try{
            FileOutputStream fos = new FileOutputStream(userFile, true);
            FileChannel channel = fos.getChannel();
            try {
                lock = channel.tryLock();
                String userData = "username:"+ username + " " + "password:" + hashPass + System.lineSeparator();

                ByteBuffer buffer = ByteBuffer.wrap(userData.getBytes());
                channel.write(buffer);
                lock.release();
            } catch (OverlappingFileLockException e) {
                System.out.println("File is currently locked by another process.");
            } finally {
                if (lock != null) {
                    lock.release();
                }
                fos.close();
            }
        } catch (IOException err){
            System.err.println("Unable to open file: <user.txt>");
        }
    }
}
