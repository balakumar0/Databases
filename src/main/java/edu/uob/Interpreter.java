package edu.uob;

import java.io.File;
import java.util.*;
import java.util.HashSet;
import java.util.regex.Pattern;

import static edu.uob.SyntaxType.*;

public class Interpreter {
    private final Node root;
    private Database dbInUse;
    private static final String OK_MESSAGE = "[OK]";
    private final String storageFolderPath;
    public Interpreter(Node root, String storageFolderPath)
    {
        this.root = root;
        this.storageFolderPath = storageFolderPath;
    }

    public String evaluateTree()
    {
        StringBuilder errorMessage = new StringBuilder();
        if (errorNodesPresent(root, errorMessage))
        {
            return errorMessage.toString();
        }
        switch (root.getType()) {
            case USE -> {
                return interpretUse(root);
            }
            case CREATE_DATABASE -> {
                return interpretCreateDatabase(root);
            }
            case CREATE_TABLE -> {
                return interpretCreateTable(root);
            }
            case DROP -> {
                return interpretDrop(root);
            }
            case ALTER -> {
                return interpretAlter(root);
            }
            case INSERT -> {
                return interpretInsert(root);
            }
            case SELECT -> {
                return interpretSelect(root);
            }
            case UPDATE -> {
                return interpretUpdate(root);
            }
            case DELETE -> {
                return interpretDelete(root);
            }
            case JOIN -> {
                return interpretJoin(root);
            }
            default -> {
                return createErrorMessage("Unexpected instruction");
            }
        }
    }
    private String interpretUse(Node n)
    {
        if (n.getType() == USE) {
            if (!checkDBExists(n.getSibling().getValue().toLowerCase())) {
                return createErrorMessage("Database does not exist");
            }

            useDB(n.getSibling().getValue().toLowerCase());
            return OK_MESSAGE;
        }
        return createErrorMessage("Unexpected token?");
    }

    private String interpretCreateDatabase(Node n)
    {
        if (checkDBExists(n.getSibling().getValue().toLowerCase()))
        {
            return createErrorMessage("Database called " + n.getSibling().getValue() + " exists already");
        }

        if (createNewDB(n.getSibling().getValue().toLowerCase()))
        {
            return OK_MESSAGE;
        }

        return createErrorMessage("Could not create new database");
    }

    private String interpretCreateTable(Node n)
    {
        if (dbInUse == null)
        {
            return createErrorMessage("Need to use DB in order to create table");
        }

        String tableName = n.getSibling().getValue().toLowerCase();


        if (dbInUse.checkTableExists(tableName))
        {
            return createErrorMessage("Table called " + tableName + " exists already");
        }

        StringBuilder message = new StringBuilder();
        LinkedHashSet<String> columnNames = new LinkedHashSet<>();

        if (columnErrorsExist(n.getChild(), message, columnNames, tableName))
        {
            return createErrorMessage("Issue with column names: " + message);
        }

        if (createNewTable(tableName, new LinkedHashSet<>(columnNames), message))
        {
            return OK_MESSAGE;
        }

        return createErrorMessage("Could not create new table" + message);
    }

    private String interpretDrop(Node n)
    {
        switch (n.getSibling().getType()) {
            case TABLE_NAME -> {
                if (dbInUse == null) {
                    return createErrorMessage("Choose database first before deleting table");
                }

                if (!dbInUse.checkTableExists(n.getSibling().getValue())) {
                    return createErrorMessage("Table " + n.getSibling().getValue() +
                            " does not exist in database " + dbInUse.getDatabaseName());
                }

                if (!dbInUse.deleteTable(n.getSibling().getValue())) {
                    return createErrorMessage("Unable to delete table");
                }

                return OK_MESSAGE;

            }
            case DATABASE_NAME -> {
                if (deleteDatabase(n.getSibling().getValue().toLowerCase())) {
                    dbInUse = null;
                    return OK_MESSAGE;
                }

                return createErrorMessage("Unable to delete database");
            }
            default -> {
                return createErrorMessage("Unexpected node found; table/database name not entered");
            }
        }
    }

    private boolean deleteDatabase(String databaseName)
    {
        try
        {
            String path = storageFolderPath + File.separator + databaseName;
            File db = new File(path);
            File[] directoryFiles = db.listFiles();
            if (directoryFiles != null) {
                for (File f: directoryFiles)
                {
                    if (!f.delete())
                    {
                        return false;
                    }
                }
            }

            if (!db.delete())
            {
                return false;
            }
        }

        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    private boolean columnErrorsExist(Node n, StringBuilder s, LinkedHashSet<String> columnNames, String tableName)
    {
        if (n == null)
        {
            return false;
        }

        String currentValue = n.getValue();

        StringBuilder columnName = new StringBuilder();

        if (differentTableNames(tableName, currentValue, columnName))
        {
            s.append("Column name doesn't correspond with table name");
            return true;
        }

        currentValue = columnName.toString();

        for (String storedString: columnNames)
        {
            if (storedString.equalsIgnoreCase(currentValue))
            {
                s.append("Duplicate column names");
                return true;
            }
        }

        if (currentValue.equalsIgnoreCase("ID"))
        {
            s.append("ID is prohibited column name");
            return true;
        }

        columnNames.add(currentValue);
        return columnErrorsExist(n.getChild(), s, columnNames, tableName);
    }

    private boolean createNewTable(String tableName, HashSet<String> columnNames, StringBuilder message)
    {
        ArrayList<String> heading = new ArrayList<>(columnNames);
        heading.add(0, "id");
        HashMap<Integer, ArrayList<String>> rows = new HashMap<>();
        rows.put(-1, heading);

        Table t = Table.loadTable(tableName, dbInUse, rows);

        if (t == null)
        {
            message.append("    Error in constructing table");
            return false;
        }

        return true;
    }

    private String interpretAlter(Node n)
    {
        if (dbInUse == null)
        {
            return createErrorMessage("No database set");
        }

        String tableName = n.getSibling().getValue();

        if (!dbInUse.checkTableExists(tableName))
        {
            return createErrorMessage("Table to alter does not exist");
        }

        Table t = Table.loadTable(tableName, dbInUse, null);

        if (t == null)
        {
            return createErrorMessage("Unable to initialise table");
        }

        String attributeName = n.getChild().getSibling().getValue();

        StringBuilder attribute = new StringBuilder();

        if (differentTableNames(t.getTableName(), attributeName, attribute))
        {
            return createErrorMessage("Table name associated with attribute " + attributeName +
                    "is different to table loaded");
        }

        attributeName = attribute.toString();

        if (attributeName.equalsIgnoreCase("ID"))
        {
            return createErrorMessage("Unable to add or remove ID column");
        }

        switch (n.getChild().getType()) {
            case ADD -> {
                if (t.containsColumn(attributeName)) {
                    return createErrorMessage("Column name " + attributeName + " exists in table " + t.getTableName());
                }

                if (!t.addColumn(attributeName)) {
                    return createErrorMessage("Unable to add column");
                }

                return OK_MESSAGE;
            }
            case DROP -> {
                if (!t.containsColumn(attributeName)) {
                    return createErrorMessage("Column name " + attributeName + " does not exist in table " + t.getTableName());
                }

                if (!t.removeColumn(attributeName)) {
                    return createErrorMessage("Unable to remove column");
                }

                return OK_MESSAGE;

            }
            default -> {
                return createErrorMessage("Invalid alter option specified");
            }
        }

    }
    private String interpretInsert(Node n)
    {
        if (dbInUse == null)
        {
            return createErrorMessage("Unable to insert values into table before DB specified");
        }

        Table t = Table.loadTable(n.getSibling().getValue(), dbInUse, null);

        if (t == null)
        {
            return createErrorMessage("Could not load table");
        }

        LinkedHashSet<String> valuesToAdd = new LinkedHashSet<>();
        gatherValueList(n.getChild(), valuesToAdd);

        if (valuesToAdd.size() != t.editableColumns())
        {
            return createErrorMessage("You must insert exactly " + t.editableColumns() + " values");
        }

        if (!t.addRow(new ArrayList<>(valuesToAdd)))
        {
            return createErrorMessage("Unable to add values");
        }

        return OK_MESSAGE;
    }

    private void gatherValueList(Node n, HashSet<String> values)
    {
        if (n == null)
        {
            return;
        }

        values.add(n.getValue());

        gatherValueList(n.getSibling(), values);
    }

    private String interpretSelect(Node n)
    {
        if (dbInUse == null)
        {
            return createErrorMessage("Unable to select from table before DB specified");
        }

        String tableName = n.getSibling().getValue();

        if (!dbInUse.checkTableExists(tableName))
        {
            return createErrorMessage("Table named " + tableName + " does not exist in " + dbInUse.getDatabaseName());
        }

        Table t = Table.loadTable(tableName, dbInUse, null);

        if (t == null)
        {
            return createErrorMessage("Unable to load table");
        }

        ArrayList<String> rawSelectedAttributes = interpretWildAttributes(n.getChild(), t);
        ArrayList<String> selectedAttributes = new ArrayList<>();

        for (String attribute: rawSelectedAttributes) {

            StringBuilder tempAttribute = new StringBuilder();

            if (differentTableNames(t.getTableName(), attribute, tempAttribute))
            {
                return createErrorMessage("Table name associated with attribute " + attribute +
                        " is different to table loaded");
            }

            attribute = tempAttribute.toString();

            if (!t.containsColumn(attribute))
            {
                return createErrorMessage("Unable to find attribute " + attribute + " in table");
            }

            selectedAttributes.add(attribute);
        }

        StringBuilder output = new StringBuilder();

        if (n.getChild().getSibling() != null)
        {
            HashSet<Integer> rowsToSelect = interpretCondition(n.getChild().getSibling(), t);

            if (rowsToSelect == null)
            {
                return createErrorMessage("Error in interpreting condition(s)");
            }

            if (!t.selectTable(output, selectedAttributes, rowsToSelect))
            {
                return createErrorMessage("Error in selecting table");

            }
        }

        else
        {
            if (!t.selectTable(output, selectedAttributes))
            {
                return createErrorMessage("Error in selecting table");
            }
        }

        return OK_MESSAGE + System.lineSeparator() + output;
    }

    private ArrayList<String> interpretWildAttributes(Node n, Table t)
    {
        ArrayList<String> attributeList = null;
        switch (n.getType()) {
            case SELECT_ALL -> attributeList = t.getColumnNames();
            case ATTRIBUTE_NAME -> {
                attributeList = new ArrayList<>();
                visitAttributeNodes(n, attributeList);
            }
            default -> {

            }
        }

        return attributeList;
    }

    private void visitAttributeNodes(Node n, ArrayList<String> attributeList)
    {
        if (n == null)
        {
            return;
        }

        attributeList.add(n.getValue());
        visitAttributeNodes(n.getChild(), attributeList);
    }

    private HashSet<Integer> interpretCondition(Node n, Table t)
    {
        if (n == null)
        {
            return new HashSet<>();
        }

        switch(n.getType())
        {
            case CONDITION:
            {
                HashSet<Integer> values1 = interpretCondition(n.getChild(), t);

                if (values1 == null)
                {
                    return null;
                }

                if (isNodeBoolean(n.getSibling()))
                {
                    HashSet<Integer> values2 = interpretCondition(n.getSibling().getSibling(), t);

                    if (values2 == null)
                    {
                        return null;
                    }

                    switch(n.getSibling().getType())
                    {
                        case AND:
                        {
                            values1.retainAll(values2);
                            break;
                        }

                        case OR:
                        {
                            values1.addAll(values2);
                            break;
                        }

                        default:
                        {

                        }
                    }
                }

               return values1;
            }

            case ATTRIBUTE_NAME:
            {
                return getRelevantRows(n, t);
            }

            default:
            {

            }
        }

        return null;
    }

    private String interpretUpdate(Node n)
    {
        if (dbInUse == null)
        {
            return createErrorMessage("Choose DB before updating table");
        }

        String tableName = n.getSibling().getValue();

        if (!dbInUse.checkTableExists(tableName))
        {
            return createErrorMessage("Table to be updated does not exist");
        }

        Table t = Table.loadTable(tableName, dbInUse, null);

        if (t == null)
        {
            return createErrorMessage("Unable to load table");
        }

        ArrayList<String> rawAttributeNames = new ArrayList<>();
        ArrayList<String> attributeNames = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        getNameValuePairs(n.getChild(), rawAttributeNames, values);

        for (String attribute: rawAttributeNames)
        {
            StringBuilder tempAttribute = new StringBuilder();

            if (differentTableNames(t.getTableName(), attribute, tempAttribute))
            {
                return createErrorMessage("Table name associated with attribute " + attribute +
                        " is different to table loaded");
            }

            attribute = tempAttribute.toString();

            if (!t.containsColumn(attribute))
            {
                return createErrorMessage("Table " + tableName + " does not contain " + attribute);
            }

            attributeNames.add(attribute);
        }

        HashSet<Integer> rowsToChange = interpretCondition(n.getSibling().getSibling(), t);

        if (rowsToChange == null)
        {
            return createErrorMessage("Error in interpreting condition(s)");
        }

        for (int i: rowsToChange)
        {
            for (int j = 0; j < attributeNames.size(); j++)
            {
                if (!t.changeValue(attributeNames.get(j), values.get(j), i))
                {
                    return createErrorMessage("Unable to change value");
                }
            }
        }

        return OK_MESSAGE;

    }
    private void getNameValuePairs(Node n, ArrayList<String> attributes, ArrayList<String> values)
    {
        if (n == null)
        {
            return;
        }

        attributes.add(n.getValue());
        values.add(n.getSibling().getValue());

        getNameValuePairs(n.getChild(), attributes, values);
    }

    private HashSet<Integer> getRelevantRows(Node n, Table t)
    {
        String columnName = n.getValue();
        StringBuilder tempAttribute = new StringBuilder();

        if (differentTableNames(t.getTableName(), columnName, tempAttribute))
        {
            return null;
        }

        columnName = tempAttribute.toString();
        SyntaxType comparator = n.getSibling().getType();
        String value = n.getSibling().getSibling().getValue();

        return t.getRelevantRows(columnName, comparator, value);
    }

    private boolean isNodeBoolean(Node n)
    {
        if (n == null)
        {
            return false;
        }

        return n.getType() == OR || n.getType() == AND;
    }

    private String interpretDelete(Node n)
    {
        if (dbInUse == null)
        {
            return createErrorMessage("Choose DB before deleting rows from table");
        }

        String tableName = n.getSibling().getValue();

        if (!dbInUse.checkTableExists(tableName))
        {
            return createErrorMessage("Table to delete from does not exist");

        }

        Table t = Table.loadTable(tableName, dbInUse, null);

        if (t == null)
        {
            return createErrorMessage("Could not load table");
        }

        HashSet<Integer> rowsToDelete = interpretCondition(n.getChild(), t);

        if (rowsToDelete == null)
        {
            return createErrorMessage("Error in interpreting condition(s)");
        }

        for (int row: rowsToDelete)
        {
            t.removeRow(row);
        }

        return OK_MESSAGE;
    }

    private String interpretJoin(Node n)
    {
        if (dbInUse == null)
        {
            return createErrorMessage("Choose DB before deleting rows from table");
        }

        String table1 = n.getSibling().getValue();
        String table2 = n.getSibling().getSibling().getValue();

        if (!dbInUse.checkTableExists(table1) || !dbInUse.checkTableExists(table2))
        {
            return createErrorMessage("At least one of the selected tables does not exist");
        }

        Table t1 = Table.loadTable(table1, dbInUse, null);
        Table t2 = Table.loadTable(table2, dbInUse, null);

        String attribute1 = n.getChild().getValue();
        String attribute2 = n.getChild().getChild().getValue();

        if (t1 == null | t2 == null)
        {
            return createErrorMessage("Unable to load tables");
        }

        StringBuilder column1 = new StringBuilder();
        StringBuilder column2 = new StringBuilder();

        if (differentTableNames(t1.getTableName(), attribute1, column1) || differentTableNames(t2.getTableName(), attribute2, column2))
        {
            return createErrorMessage("Ensure that you are not referencing different table names in for each attribute");
        }

        attribute1 = column1.toString();
        attribute2 = column2.toString();

        if (!t1.containsColumn(attribute1) || !t2.containsColumn(attribute2))
        {
            return createErrorMessage("Ensure that selected attributes are present in table");
        }

        ArrayList<ArrayList<String>> joinedTable = dbInUse.joinTables(t1, t2, attribute1, attribute2);

        if (joinedTable == null)
        {
            return createErrorMessage("Unable to join tables");
        }

        StringBuilder output = new StringBuilder();

        DBHelper.printTable(output, joinedTable);

        return OK_MESSAGE + System.lineSeparator() + output;

    }

    private String createErrorMessage(String message)
    {
        return "[ERROR]" + System.lineSeparator() + message;
    }

    private boolean isErrorNode(Node n)
    {
        if (n == null)
        {
            return false;
        }

        return n.getType() == ERROR;
    }

    private boolean checkDBExists(String databaseName)
    {
        File f = new File(storageFolderPath + File.separator + databaseName);
        return f.isDirectory();
    }

    private void useDB(String databaseName)
    {
        dbInUse = new Database(storageFolderPath, databaseName);
    }

    public void setDbInUse(Database db)
    {
        dbInUse = db;
    }

    public Database getDbInUse()
    {
        return dbInUse;
    }

    private boolean createNewDB(String databaseName)
    {
        return Database.createDatabase(storageFolderPath, databaseName.toLowerCase());
    }

    private boolean errorNodesPresent(Node n, StringBuilder errorMessage) {
        if (n == null) {
            return false;
        }

        if (isErrorNode(n)) {
            errorMessage.append(createErrorMessage(n.getValue()));
            return true;
        }

        return errorNodesPresent(n.getSibling(), errorMessage) || errorNodesPresent(n.getChild(), errorMessage);

    }

    private boolean differentTableNames(String tableName, String attributeName, StringBuilder output)
    {
        if (attributeName.contains("."))
        {
            String[] components = attributeName.split(Pattern.quote("."));
            if (!tableName.equalsIgnoreCase(components[0]))
            {
                return true;
            }

            output.append(components[1]);
            return false;
        }

        output.append(attributeName);
        return false;
    }
}
