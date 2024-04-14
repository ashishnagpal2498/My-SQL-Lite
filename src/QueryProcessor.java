
import java.util.*;
/**
 * Choose and executes query. Store data into temporary storage
 *
 */
public class QueryProcessor{
    final private DBUtil util;
    protected String query = "";
    protected String[] queryParts;
    /**
     * Holds the actual data for each table.
     * Key: Table Name
     * Value: List of Strings representing table rows.
     */
    protected HashMap<String, List<String>> data;

    /**
     * Stores the column types associated with each table.
     * Key: Table Name
     * Value: String containing column details delimited by a special character.
     */
    protected HashMap<String, String> tables;

    /**
     * Temporary storage for new or updated data before committing to the database.
     * Key: Table Name
     * Value: List of Strings representing new or modified table rows.
     */
    protected HashMap<String, List<String>> tempData;

    /**
     * Temporary storage for new or updated table structures before committing to the database.
     * Key: Table Name
     * Value: String containing the modified column details delimited by a special character.
     */
    protected HashMap<String, String> tempTables;

    QueryProcessor(HashMap<String,String> tables, HashMap<String,List<String> > data){
        tempTables = new HashMap<>(tables);
        this.tables = new HashMap<>(tables);
        tempData = new HashMap<>(data);
        this.data = new HashMap<>(data);
        util = new DBUtil();
    }
    /**
     * Executes the specified query based on its operation.
     *
     * @return true if the query is processed successfully, false otherwise.
     */
    public boolean querySelector() {

        String operation = queryParts[0].toLowerCase();
        switch (operation) {
            case "select":
               return select();
            case "insert":
                return insert();
            case "create":
               return createTable();
            case "update":
                return update();
            case "delete":
                return delete();
            case "commit":
            case "start":
            case "rollback":
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * Selects and displays records from the table.
     *
     * @return true if the SELECT query executes successfully, false otherwise.
     */
    boolean select() {
        String tableName = queryParts[3].toLowerCase();
        if (!tableExist(tableName)) {
            System.out.println("Table doesn't exist");
            return false;
        }
        List<String> tableValues = tempData.get(tableName);
        for (String row :
                tableValues) {
            System.out.println(row.replace(util.delimiter(), "\t"));
        }
        return true;
    }

    /**
     * Inserts a new row into the table.
     *
     * @return true if the INSERT query executes successfully, false otherwise.
     */
    boolean insert(){
        String tableName = queryParts[2].toLowerCase();
        if(!tableExist(tableName)) {
            System.out.println("Table doesn't exist");
            return false;
        }

        String commaSepInsertValues = query.substring(query.lastIndexOf("(") + 1, query.lastIndexOf(")")).trim();
        String[] values = commaSepInsertValues.split(",");
        StringBuilder output = new StringBuilder();
        for(int i =0; i<values.length; i++){
            output.append(values[i].replace("\"","").trim().split(" ")[0]);
            if(!(i == values.length -1) )
                output.append(util.delimiter());
        }
        List<String> tableEntries = new ArrayList<>(tempData.get(tableName));
        tableEntries.add(output.toString());
        tempData.put(tableName,tableEntries);

        return true;
    }

    /**
     * Creates a new table.
     *
     * @return true if the CREATE TABLE query executes successfully, false otherwise.
     */
    boolean createTable(){
        String tableName = queryParts[2].split("\\(")[0].toLowerCase();
        if(tableExist(tableName)) {
            System.out.println("Table exist with same name");
            return false;
        }

        String commaSepCreateValues = query.substring(query.indexOf("(") + 1, query.lastIndexOf(")")).trim();
        String[] values = commaSepCreateValues.split(",");
        StringBuilder output = new StringBuilder();
        StringBuilder tableOutput = new StringBuilder();
         for(int i=0;i<values.length; i++ ) {
             String columnName = values[i].trim().split(" ")[0];
             String columnType = values[i].trim().split(" ")[1];
             output.append(columnName);
             tableOutput.append(columnName).append(util.delimiter()).append(columnType);
             if (i < values.length - 1) {
                 output.append(util.delimiter());
                 tableOutput.append(" ");
             }
         }
         List<String> dataList = new ArrayList<>();
         dataList.add(output.toString());
         tempData.put(tableName,dataList);
         tempTables.put(tableName, tableOutput.toString());
         return true;
    }
    /**
     * Updates existing records in the table based on a specified condition.
     *
     * @return true if the UPDATE query executes successfully, false otherwise.
     */
    boolean update() {
        String tableName = queryParts[1].toLowerCase();
        if (!tableExist(tableName)) {
            System.out.println("Table doesn't exist ");
            return false;
        }
        List<String> tableEntries = tempData.get(tableName);
        List<String> updatedEntries = new ArrayList<>();
        updatedEntries.add(tableEntries.get(0));
        List<String> rowColumn = Arrays.asList(tableEntries.get(0).split(util.delimiter()));

        String condition = queryParts[queryParts.length - 1].replace("\"","");
        int conditionColumnIndex = rowColumn.indexOf(condition.split("=")[0]);
        String conditionValue = condition.split("=")[1];

        String[] setParts = queryParts[3].split("=");
        int updateColumnIndex = rowColumn.indexOf(setParts[0].trim().replace("\"",""));
        String updateValue = setParts[1].trim().replace("\"","");

            for (int j = 1; j < tableEntries.size(); j++) {
                String[] row = tableEntries.get(j).split(util.delimiter());
                if (row[conditionColumnIndex].equalsIgnoreCase(conditionValue)) {
                    StringBuilder updatedRow = new StringBuilder();
                    for (int k = 0; k < row.length; k++) {
                        if (k == updateColumnIndex) {
                             updatedRow.append(updateValue);
                        } else{
                            updatedRow.append(row[k]);
                        }
                        if(k< row.length-1){
                            updatedRow.append(util.delimiter());
                        }
                    }
                    updatedEntries.add(updatedRow.toString());
                }
                else{
                    updatedEntries.add(tableEntries.get(j));
                }
            }

        tempData.put(tableName, updatedEntries);

        return true;
    }
    /**
     * Deletes records from the table based on a specified condition.
     *
     * @return true if the DELETE query executes successfully, false otherwise.
     */
    boolean delete() {
        String tableName = queryParts[2].toLowerCase();
        if (!tableExist(tableName)) {
            System.out.println("Table doesn't exist");
            return false;
        }

        List<String> tableEntries = tempData.get(tableName);
        List<String> updatedEntries = new ArrayList<>();
        updatedEntries.add(tableEntries.get(0));
        List<String> rowColumn = Arrays.asList(tableEntries.get(0).split(util.delimiter()));

        String condition = queryParts[4].replace("\"","");
        int conditionColumnIndex = rowColumn.indexOf(condition.split("=")[0]);
        String conditionValue = condition.split("=")[1];

        for (int j = 1; j < tableEntries.size(); j++) {
            String[] row = tableEntries.get(j).split(util.delimiter());
            if (!row[conditionColumnIndex].equalsIgnoreCase(conditionValue)) {
                updatedEntries.add(tableEntries.get(j));
            }
        }
        tempData.put(tableName, updatedEntries);

        return true;
    }

    private boolean tableExist(String tableName){
        return tempTables.containsKey(tableName);
    }
}
