import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Reads and updates the database tables and its columns.
 */
public class DatabaseUpdate {
    /**
     * Reads the database tables and their columns from the 'tables.txt' file.
     *
     * @return A HashMap containing table names as keys and their respective columns as values.
     */
    HashMap<String,String> readDbTables(){
        HashMap<String,String> tablesData = new HashMap<>();
        try {
            FileInputStream fis = new FileInputStream("tables.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            while ((line = reader.readLine()) != null) {
                String tableName = line.split(":")[0];
                String columns = line.split(":")[1];
                tablesData.put(tableName,columns);
            }
            return tablesData;
        } catch (IOException e) {
            System.out.println("Error in table Exist");
        }
        return tablesData;
    }
    /**
     * Reads the data of each table from the corresponding files.
     *
     * @param tables A HashMap containing table names and their columns.
     * @return A HashMap containing table names as keys and their data rows as values in a list.
     */
    HashMap<String,List<String> > readTablesData(HashMap<String,String> tables){
        HashMap<String,List<String>> tablesData = new HashMap<>();
        try {
            for (String table:
                 tables.keySet()) {
                FileInputStream fis = new FileInputStream("table-" + table + ".txt");
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader reader = new BufferedReader(isr);
                String line;
                List<String> data = new ArrayList<>();
                while ((line = reader.readLine()) != null) {
                    data.add(line);
                }
                tablesData.put(table,data);
            }
        } catch (IOException e) {
            System.out.println("Error in reading table data from files");
        }
        return tablesData;
    }
    /**
     * Updates the table information in the 'tables.txt' file.
     *
     * @param tempTables A HashMap containing table names and their respective column mappings.
     */
    void updateDbTables(HashMap<String,String> tempTables){
        FileLock lock = null;
        try{
            FileOutputStream fos = new FileOutputStream("tables.txt");
            FileChannel channel = fos.getChannel();
            try {
                lock = channel.tryLock();
                StringBuilder values = new StringBuilder();
                ByteBuffer buffer;
                for (String key : tempTables.keySet()) {
                    String columnTypeMapping = tempTables.get(key);
                    values.append(key).append(":").append(columnTypeMapping).append(System.lineSeparator());
                    buffer = ByteBuffer.wrap(values.toString().getBytes());
                    channel.write(buffer);
                }

            } catch (OverlappingFileLockException e) {
                System.out.println("File is currently locked by another process.");
            } finally {
                if (lock != null) {
                    lock.release();
                }
                fos.close();
            }
        } catch (IOException err){
            System.err.println("Unable to open file: <tables.txt>");
        }
    }
    /**
     * Inserts data into their respective table files.
     *
     * @param tempData A HashMap containing table names and their data rows to be inserted.
     */
    void insertDataIntoTables (HashMap<String,List<String> > tempData){
        FileLock lock = null;
        try{
            for (String key : tempData.keySet()) {
                FileOutputStream fos = new FileOutputStream("table-"+key + ".txt");
                FileChannel channel = fos.getChannel();
                try {
                    lock = channel.tryLock();
                    ByteBuffer buffer;
                    for (String tableRow : tempData.get(key)) {
                        tableRow = tableRow + System.lineSeparator();
                        buffer = ByteBuffer.wrap(tableRow.getBytes());
                        channel.write(buffer);
                    }

                } catch (OverlappingFileLockException e) {
                    System.out.println("File is currently locked by another process.");
                } finally {
                    if (lock != null) {
                        lock.release();
                    }
                    fos.close();
                }
            }

        } catch (IOException err){
            System.err.println("Unable to open file: <table-file.txt>");
        }
    }
}
