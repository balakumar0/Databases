package edu.uob;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Database {
    private final String storageFolderPath;
    private final String databaseName;
    public Database(String storageFolderPath, String databaseName)
    {
        this.storageFolderPath = storageFolderPath;
        this.databaseName = databaseName;
    }
    public static boolean createDatabase(String storageFolderPath, String databaseName)
    {
        File directory = new File(storageFolderPath + File.separator + databaseName);

        return directory.mkdir();
    }
    public boolean checkTableExists(String tableName)
    {
        tableName = tableName.toLowerCase();
        File f1 = new File(storageFolderPath + File.separator + databaseName + File.separator + tableName + ".tab");
        return f1.isFile();
    }
    public String getDatabaseName()
    {
        return databaseName;
    }
    public boolean deleteTable(String tableName)
    {
        tableName = tableName.toLowerCase();
        File f = new File(storageFolderPath + File.separator + databaseName + File.separator + tableName + ".tab");
        File config = new File(storageFolderPath + File.separator + databaseName + File.separator + tableName + ".config");
        try
        {
            if (!f.delete())
            {
                return false;
            }

            if (config.exists())
            {
                if (!config.delete())
                {
                    return false;
                }
            }
        }

        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    public ArrayList<ArrayList<String>> joinTables(Table t1, Table t2, String attribute1, String attribute2)
    {

        ArrayList<ArrayList<String>> t1Array = t1.tableToArrayList();
        ArrayList<ArrayList<String>> t2Array = t2.tableToArrayList();

        ArrayList<ArrayList<String>> joinedTable = new ArrayList<>();
        try {

            int index1 = t1Array.get(0).indexOf(attribute1);
            int index2 = t2Array.get(0).indexOf(attribute2);

            ArrayList<ArrayList<Integer>> matchingRows = new ArrayList<>();

            for (int i = 1; i < t1Array.size(); i++) {
                for (int j = 1; j < t2Array.size(); j++) {
                    if (t1Array.get(i).get(index1).equals(t2Array.get(j).get(index2))) {
                        ArrayList<Integer> positionNumbers = new ArrayList<>(Arrays.asList(i, j));
                        matchingRows.add(positionNumbers);
                    }
                }
            }

            ArrayList<String> temp1 = t1Array.get(0);
            temp1.remove(index1);

            temp1.replaceAll(s -> t1.getTableName() + "." + s);

            ArrayList<String> temp2 = t2Array.get(0);
            temp2.remove(index2);

            temp2.replaceAll(s -> t2.getTableName() + "." + s);

            temp1.addAll(temp2);
            joinedTable.add(temp1);

            for (ArrayList<Integer> match : matchingRows) {
                int rowIndex1 = match.get(0);
                int rowIndex2 = match.get(1);
                ArrayList<String> rowToAdd1 = t1Array.get(rowIndex1);
                ArrayList<String> rowToAdd2 = t2Array.get(rowIndex2);

                rowToAdd1.remove(index1);
                rowToAdd2.remove(index2);

                rowToAdd1.addAll(rowToAdd2);
                joinedTable.add(rowToAdd1);

            }
            
            removeID(joinedTable, t1.getTableName(), t2.getTableName());
            generateNewID(joinedTable);
        }

        catch (Exception e)
        {
            return null;
        }

        return joinedTable;

    }

    private void removeID(ArrayList<ArrayList<String>> table, String table1Name, String table2Name)
    {
        ArrayList<String> columnNames = table.get(0);
        ArrayList<Integer> idPosition = new ArrayList<>();

        for (int i = 0; i < columnNames.size(); i++)
        {
            if (columnNames.get(i).contains(table1Name + ".id") || columnNames.get(i).contains(table2Name + ".id"))
            {
                idPosition.add(i);
            }
        }

        for (int id: idPosition)
        {
            for (int i = 0; i < table.size(); i++)
            {
                ArrayList<String> row = table.get(i);
                row.remove(id);
                table.set(i, row);
            }
        }

    }

    private void generateNewID(ArrayList<ArrayList<String>> table)
    {
        int rowNumber = 1;

        ArrayList<String> columnName = table.get(0);
        columnName.add(0, "id");
        table.set(0, columnName);

        for (int i = 1; i < table.size(); i++)
        {
            ArrayList<String> row = table.get(i);
            row.add(0, Integer.toString(rowNumber));
            table.set(i, row);
            rowNumber++;
        }

    }

    public String getPath()
    {
        return storageFolderPath;
    }

}
