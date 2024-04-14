import java.util.HashMap;
import java.util.List;

/**
 * Manages transactional operations involving the database.
 */
public class Transaction {
    /**
     * The count of pending queries within the transaction.
     */
    private int pendingQueriesCount;
    /**
     * Indicates whether a transaction is currently in progress.
     */
    private boolean inTransaction;
    /**
     * The QueryProcessor instance responsible for processing queries.
     */
    QueryProcessor processQuery;
    /**
     * DatabaseUpdate instance for managing database updates during transactions.
     */
    DatabaseUpdate dbUpdate;

    /**
     * Default constructor initializing the Transaction object.
     */
    public Transaction() {
        dbUpdate = new DatabaseUpdate();
        pendingQueriesCount = 0;
        inTransaction = false;
        HashMap<String,String> tablesResult = dbUpdate.readDbTables();
        HashMap<String,List<String> > data = dbUpdate.readTablesData(tablesResult);
        processQuery = new QueryProcessor(tablesResult,data);
    }

    /**
     * Initiates a database transaction by processing the list of queries.
     *
     * @param queries The list of queries to be processed.
     */
    public void beginTransaction(List<String> queries) {
        for (String query : queries) {
            String[] queryParts = query.split(" ");
            String operation = queryParts[0].toLowerCase();
            processQuery.query = query;
            processQuery.queryParts = queryParts;
            boolean result = processQuery.querySelector();
            if(result) {
                pendingQueriesCount++;
                switch (operation) {
                    case "start":
                        if(queryParts[1].equalsIgnoreCase("transaction")){
                            inTransaction = true;
                            System.out.println("Begin Transaction");
                        }
                        break;
                    case "select":
                        break;
                    case "insert":
                        System.out.println("Row Insertion in buffer.");
                        break;
                    case "create":
                        System.out.println("Table creation --> buffer");
                        break;
                    case "update":
                        System.out.println("Rows updated --> buffer");
                        break;
                    case "delete":
                        System.out.println("Rows Deleted --> buffer");
                        break;
                    case "commit":
                        commit();
                        break;
                    case "rollback":
                        rollback();
                    default:
                        break;
                }
            }
        }

        if(pendingQueriesCount > 0){
            commit();
        }

    }
    /**
     * Ends the transaction.
     */
    public void endTransaction() {
        if (inTransaction) {
            inTransaction = false;
            System.out.println("End Transaction");
        }
    }

    /**
     * Rolls back the ongoing transaction.
     */
    void rollback(){
        if (!inTransaction) {
            System.out.println("Transaction not started. Cannot rollback");
            return;
        }
        processQuery.tempTables = new HashMap<>(processQuery.tables);
        processQuery.tempData = new HashMap<>(processQuery.data);
        pendingQueriesCount = 0;

        System.out.println("Transaction Rollback successful");

    }

    /**
     * Commits the changes made during the transaction to the database.
     */
    public void commit() {
            dbUpdate.updateDbTables(processQuery.tempTables);
            dbUpdate.insertDataIntoTables(processQuery.tempData);
            pendingQueriesCount = 0;

        if(inTransaction) {
            System.out.println("Transaction committed. Pending queries applied to database");
        }
        else{
            System.out.println("Queries Executed Successfully");
        }
        endTransaction();
    }

}
