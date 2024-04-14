import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Handles database operations and performs database-related activities.
 */
public class DatabaseProcessor {
    /**
     * Array of files used within the database system.
     */
    static String[] files;
    /**
     * Transaction instance to manage transactional database operations.
     */
    Transaction transaction;
    static {
        files = new String[]{"users.txt"};
    }
    /**
     * Default constructor for DatabaseProcessor initializing the Transaction and initializes the DB.
     */
    DatabaseProcessor(){
        transaction = new Transaction();
        initializeDB();
    }

    /**
     * Initializes the database files.
     */
    void initializeDB(){
        System.out.println("Initialize Files ");
        for (String file : files) { // Added "String" before "file"
            try {
                File userFile = new File(file);
                if(!userFile.exists())
                {
                    userFile.createNewFile();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Performs database operations based on the selected option and username.
     *
     * @param option    The integer option representing the database operation.
     * @param username  The username to perform operations related to the user.
     */
    public void performOperation(int option, String username){
        boolean isSelectedOrExist;
        switch (option){
            case 1:
                // create db if doesn't exist
                dbExist(0);
                break;
            case 2:
                // Select DB
               isSelectedOrExist = dbExist(1);
               if(isSelectedOrExist){
                   if(!isDBSelected(username,1))
                    selectDB(username, true);
                   else
                       System.out.println("Database already selected");
               }
               break;
            case 3:
                isSelectedOrExist = dbExist(1);
                if(!isSelectedOrExist){
                   break;
                }
                isSelectedOrExist = isDBSelected(username,0);
                if(isSelectedOrExist){
                    showTables();
                }
                break;
            case 4:
                isSelectedOrExist = isDBSelected(username,0);
                if(isSelectedOrExist){
                    performQuery();
                }
                break;
            default:
                selectDB(username,false);
                break;
        }
    }
    /**
     * Take multi-line input for query as part of a transaction.
     */
    public void performQuery(){

        List<String> multiLineQueries = new ArrayList<>();
        Scanner scPerformQuery = new Scanner(System.in);

        System.out.println("------- Query --------");
        System.out.println("Enter your query. Type 'exit' or 'Commit' to finish your Query");
        while (true) {
            String line = scPerformQuery.nextLine();
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("commit")) {
                break;
            } else {
                if(!checkSyntax(line))
                {
                    continue;
                }
                multiLineQueries.add(line);
            }
        }
        transaction.beginTransaction(multiLineQueries);
    }

    /**
     * Checks if the operation is a Commit, Rollback, or Start Transaction.
     *
     * @param operation The operation to be checked.
     * @return True if the operation represents a transactional action, otherwise false.
     */
    private boolean transactionPart(String operation){
        String regex = "(?i)\\b(COMMIT|ROLLBACK|START)\\b.*";
        return operation.matches(regex);
    }

    /**
     * Checks the syntax of the provided query.
     *
     * @param query The query to validate.
     * @return True if the query syntax is valid, otherwise false.
     */
    private boolean checkSyntax(String query){
        String[] queryParts = query.split(" ");
        String operation = queryParts[0].toLowerCase();
        String regex = "(?i)\\b(SELECT|INSERT|UPDATE|DELETE|CREATE)\\b.*";
        if ((query.matches(regex) && queryParts.length < 4) || (!query.matches(regex) && !transactionPart(operation))) {
            System.out.println("Invalid " + operation.toUpperCase() + " query syntax");
            return false;
        }

        return true;
    }

    /**
     * Checks if the database exists and creates it if necessary.
     *
     * @param optional Determines the operation based on the optional value.
     * @return True if the database exists, otherwise false.
     */
    private boolean dbExist(int optional){
        try {
            File dbFile = new File("database.txt");
            if(!dbFile.exists() && optional == 0)
            {
                dbFile.createNewFile();
                System.out.println("Database created successfully");
                return false;
            }
            else if(dbFile.exists() && optional == 1) {
                return true;
            }
            else if(optional == 0){
                System.out.println("Database already exist.");
                return true;
            }

            System.out.println("Database doesn't exist. You must create a DB first");


        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Checks if the selected database corresponds to the given username.
     *
     * @param username The username for which the database is selected or exists.
     * @param call     Determines the call action based on the call value.
     * @return True if the database is selected for the given username, otherwise false.
     */
    private boolean isDBSelected(String username, int call){
        DBUtil util = new DBUtil();
        try {
            FileInputStream fis = new FileInputStream("database.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    System.out.println("DB not selected. Please choose the DB first");
                    return false;
                }
                String[] userPass = line.split(util.delimiter());
                String db_username = userPass[0];
                String db_selected = userPass[1];
                if (db_username.equals(username) && db_selected.contains("true") ){
                    return true;
                }
            }
        } catch (IOException error){
            System.out.println(error);
        }
        if(call == 0) {
            System.out.println("DB not selected. Please choose the DB first");
        }
        return false;
    }
    /**
     * Select and unselect database for given username.
     *
     * @param username username for which the database should be selected
     * @param value Determines the operation based on the optional boolean value
     */
    private void selectDB(String username, boolean value){
        DBUtil util = new DBUtil();

        FileLock lock = null;
        File file = new File("database.txt");

        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel()) {

            lock = channel.lock();
            if(raf.length() == 0){
                raf.writeBytes(username + util.delimiter() + value + System.lineSeparator());
                System.out.println("Database selected successfully");
                return;
            }
            String line;
            boolean newUser = true;
            long lastPosition = 0;
            while ((line = raf.readLine()) != null) {
                String[] parts = line.split(util.delimiter());
                if (parts.length >= 2 && parts[0].equalsIgnoreCase(username)) {
                    long pointer = raf.getFilePointer();
                    raf.seek(pointer - line.length() - System.lineSeparator().length());
                    raf.writeBytes(username + util.delimiter() + value + System.lineSeparator());
                    newUser = false;
                    break;
                }
                lastPosition = raf.getFilePointer();
            }
            if(newUser){
                System.out.println("New User");
                raf.seek(lastPosition);
                raf.writeBytes(  username + util.delimiter() + value + System.lineSeparator() );
            }
            if(value) System.out.println("Database selected successfully");
            else System.out.println("Database unselected");
            lock.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * Show tables present in the database
     */
    private void showTables(){
        try {
            File file = new File("tables.txt");
            if(!file.exists()){
                file.createNewFile();
            }
            FileInputStream fis = new FileInputStream("tables.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            System.out.println("------------");
            System.out.println("|  Tables |");
            System.out.println("----------");

            while ((line = reader.readLine()) != null) {
                System.out.println(line.split(":")[0]);
            }
            System.out.println();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
