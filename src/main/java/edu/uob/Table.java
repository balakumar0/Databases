package edu.uob;

import java.io.*;
import java.util.*;

public class Table {
    private static final String EXTENSION = ".tab";
    private static final String TAB = "\t";
    private static final Integer COLUMN_IDENTIFIER = -1;
    private final String tableName;
    private HashMap<Integer, ArrayList<String>> data;
    private int currentIDNumber;
    private final Database database;

    public Table(String tableName, Database database, int currentIDNumber)
    {
        this.tableName = tableName;
        this.database = database;
        this.currentIDNumber = currentIDNumber;

    }

    public Table(String tableName, Database database, int currentIDNumber, HashMap<Integer, ArrayList<String>> data)
    {
        this.tableName = tableName;
        this.database = database;
        this.currentIDNumber = currentIDNumber;
        this.data = data;
    }

    public static Table loadTable(String tableName, Database database, HashMap<Integer, ArrayList<String>> data)
    {
        tableName = tableName.toLowerCase();

        File f = new File(database.getPath() + File.separator + database.getDatabaseName() + File.separator + tableName + EXTENSION);
        File configFile = new File(database.getPath() + File.separator + database.getDatabaseName() + File.separator + tableName + ".info");

        if (f.isFile())
        {
            HashMap<Integer, ArrayList<String>> rows = loadAllValues(f);
            int currentIDNumber = loadCurrentID(rows, configFile);
            return new Table(tableName, database, currentIDNumber, rows);
        }

        else
        {
            if (createNewTable(tableName, database, data))
            {
                return new Table(tableName, database, 1);

            }

            else
            {
                return null;
            }
        }
    }
    private static HashMap<Integer, ArrayList<String>> loadAllValues(File f)
    {
        LinkedHashMap<Integer, ArrayList<String>> data = new LinkedHashMap<>();
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = reader.readLine();
            String[] cols = line.split(TAB);
            ArrayList<String> columns = new ArrayList<>();

            Collections.addAll(columns, cols);

            data.put(COLUMN_IDENTIFIER, columns);

            while((line = reader.readLine()) != null && (!line.isBlank()))
            {
                String[] temp = line.split(TAB);

                ArrayList<String> values = new ArrayList<>(Arrays.asList(temp).subList(1, temp.length));

                data.put(Integer.parseInt(temp[0]), values);
            }

            reader.close();

        }

        catch (Exception e)
        {
            return null;
        }

    return data;
    }

    public static int loadCurrentID(HashMap<Integer, ArrayList<String>> row, File configFile)
    {
        int currentID;

        try
        {
            int maxKey = 0;
            if (!configFile.isFile())
            {
                for (int key: row.keySet())
                {
                    if (key != COLUMN_IDENTIFIER)
                    {
                        maxKey = Math.max(key, maxKey);

                    }
                }

                currentID = maxKey + 1;
            }

            else
            {
                FileReader fileReader = new FileReader(configFile);
                BufferedReader reader = new BufferedReader(fileReader);

                String line = reader.readLine();

                currentID = Integer.parseInt(line);

                reader.close();
            }
        }

        catch(Exception e)
        {
            return 0;
        }
        return currentID;
    }

    public boolean addRow(ArrayList<String> row)
    {
        data.put(currentIDNumber, row);
        currentIDNumber++;
        return saveTable();
    }

    public void removeRow(int num)
    {
        if (num < 1)
        {
            return;
        }

        if (!data.containsKey(num))
        {
            return;
        }

        data.remove(num);
        saveTable();
    }

    private static boolean createNewTable(String tableName, Database database, HashMap<Integer, ArrayList<String>> data)
    {
        try
        {
            FileWriter fw = new FileWriter(database.getPath() + File.separator + database.getDatabaseName() + File.separator + tableName + ".tab");
            FileWriter index = new FileWriter(database.getPath() + File.separator + database.getDatabaseName() + File.separator + tableName + ".info");
            BufferedWriter bw = new BufferedWriter(fw);
            BufferedWriter indexWriter = new BufferedWriter(index);
            for (Map.Entry<Integer, ArrayList<String>> map: data.entrySet())
            {
                ArrayList<String> values = map.getValue();
                bw.write(String.join(TAB, values));
                bw.newLine();

            }

            bw.close();
            fw.close();

            indexWriter.write("1");
            indexWriter.close();

        }

        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public boolean saveTable()
    {
        try
        {
            FileWriter fw = new FileWriter(database.getPath() + File.separator + database.getDatabaseName() + File.separator + tableName + EXTENSION);
            FileWriter fileIndex = new FileWriter(database.getPath() + File.separator + database.getDatabaseName() + File.separator + tableName + ".info");

            BufferedWriter bw = new BufferedWriter(fw);
            BufferedWriter writeIndex = new BufferedWriter(fileIndex);

            for (Map.Entry<Integer, ArrayList<String>> map: data.entrySet())
            {
                int key = map.getKey();
                ArrayList<String> values = map.getValue();
                if (key != COLUMN_IDENTIFIER)
                {
                    bw.write(key + TAB);
                }
                bw.write(String.join(TAB, values));
                bw.newLine();

            }

            writeIndex.write(Integer.toString(currentIDNumber));

            writeIndex.flush();
            writeIndex.close();
            bw.close();

            fw.close();

        }

        catch(Exception e)
        {
            return false;
        }

        return true;
    }


    public boolean containsColumn(String columnName)
    {
        columnName = columnName.toUpperCase();
        ArrayList<String> values = data.get(COLUMN_IDENTIFIER);

        for (String name: values)
        {
            name = name.toUpperCase();
            if (name.equals(columnName))
            {
                return true;
            }
        }

        return false;

    }

    public boolean addColumn(String columnName)
    {
        try
        {
            for (Map.Entry<Integer, ArrayList<String>> map: data.entrySet())
            {
                int key = map.getKey();
                ArrayList<String> values = map.getValue();
                if (key != COLUMN_IDENTIFIER)
                {
                    values.add("NULL");
                }

                else
                {
                    values.add(columnName);
                }
            }
        }

        catch (Exception e)
        {
            return false;
        }

        return saveTable();
    }

    public boolean removeColumn(String columnName)
    {
        ArrayList<String> columns = data.get(COLUMN_IDENTIFIER);

        //Off by one due to discrepancies in how column names and values are stored
        int index = columns.indexOf(columnName) - 1;

        try
        {
            for (Map.Entry<Integer, ArrayList<String>> map: data.entrySet())
            {
                int currentKey = map.getKey();
                if (currentKey != COLUMN_IDENTIFIER)
                {
                    ArrayList<String> values = map.getValue();
                    values.remove(index);

                    data.put(currentKey, values);
                }
            }

            columns.remove(columns.indexOf(columnName));
        }

        catch (Exception e)
        {
            return false;
        }

        return saveTable();
    }

    public int editableColumns()
    {
        ArrayList<String> columns = data.get(COLUMN_IDENTIFIER);
        return columns.size() - 1;
    }

    public HashSet<Integer> getRelevantRows(String columnName, SyntaxType operator, String value)
    {
        HashSet<Integer> relevantRows = new HashSet<>();

        try
        {
            int relevantIndex = getColumnPosition(columnName) - 1;
            for (Map.Entry<Integer, ArrayList<String>> map: data.entrySet())
                {
                    int currentKey = map.getKey();

                    String relevantValue;

                    if (relevantIndex == COLUMN_IDENTIFIER)
                    {
                        relevantValue = Integer.toString(currentKey);
                    }

                    else
                    {
                        relevantValue = map.getValue().get(relevantIndex);
                    }

                    if (currentKey != COLUMN_IDENTIFIER)
                    {
                       if (DBHelper.conditionMet(relevantValue, value, operator))
                       {
                           relevantRows.add(currentKey);
                       }

                    }

                }

            }

            catch (Exception e)
            {
                return null;
            }

        return relevantRows;
    }

    public boolean changeValue(String columnName, String value, int row)
    {
        try
        {
            int indexToChange = getColumnPosition(columnName) - 1;
            data.get(row).set(indexToChange, value);
        }

        catch (Exception e)
        {
            return false;
        }

        return saveTable();
    }

    public ArrayList<String> getColumnNames()
    {
        return data.get(COLUMN_IDENTIFIER);
    }

    private int getColumnPosition(String columnName)
    {
        ArrayList<String> columnNames= data.get(COLUMN_IDENTIFIER);

        for (int i = 0; i < columnNames.size(); i++)
        {
            if (columnName.equalsIgnoreCase(columnNames.get(i)))
            {
                return i;
            }
        }

        return -1;
    }

    public boolean selectTable(StringBuilder s, ArrayList<String> attributes)
    {
        ArrayList<Integer> columnIndex = new ArrayList<>();
        selectTableHelper(s, attributes, columnIndex);

        for (Map.Entry<Integer, ArrayList<String>> map: data.entrySet())
        {
            int key = map.getKey();
            ArrayList<String> values = map.getValue();

            if (key == COLUMN_IDENTIFIER)
            {
                continue;
            }

            for (int i: columnIndex)
            {
                if (i == COLUMN_IDENTIFIER)
                {
                    s.append(key);
                    s.append(TAB);
                }

                else
                {
                    s.append(values.get(i));
                    s.append(TAB);
                }

            }

            s.deleteCharAt(s.length() - 1);

            s.append(System.lineSeparator());

        }

        s.deleteCharAt(s.length() - 1);
        return true;
    }

    private void selectTableHelper(StringBuilder s, ArrayList<String> attributes, ArrayList<Integer> columnIndex) {

        for (String attribute: attributes)
        {
            int position = getColumnPosition(attribute);
            columnIndex.add(position - 1);
            s.append(data.get(COLUMN_IDENTIFIER).get(position));
            s.append(TAB);
        }

        s.deleteCharAt(s.length() - 1);

        s.append(System.lineSeparator());
    }

    public boolean selectTable(StringBuilder s, ArrayList<String> attributes, HashSet<Integer> rows)
    {
        ArrayList<Integer> columnIndex = new ArrayList<>();
        selectTableHelper(s, attributes, columnIndex);

        ArrayList<Integer> rowsToSelect = new ArrayList<>(rows);
        Collections.sort(rowsToSelect);

        for (int i: rowsToSelect)
        {
            for (int j: columnIndex)
            {
                if (j == COLUMN_IDENTIFIER)
                {
                    s.append(i);
                    s.append(TAB);
                }

                else
                {
                    s.append(data.get(i).get(j));
                    s.append(TAB);
                }

            }

            s.deleteCharAt(s.length() - 1);

            s.append(System.lineSeparator());
        }

        s.deleteCharAt(s.length() - 1);

        return true;

    }
    public ArrayList<ArrayList<String>> tableToArrayList()
    {
        ArrayList<ArrayList<String>> table = new ArrayList<>();

        table.add(data.get(COLUMN_IDENTIFIER));

        for (Map.Entry<Integer, ArrayList<String>> map: data.entrySet())
        {
            int key = map.getKey();
            ArrayList<String> values = map.getValue();
            if (key != COLUMN_IDENTIFIER)
            {
               values.add(0, Integer.toString(key));
               table.add(values);
            }

        }

        return table;
    }

    public String getTableName()
    {
        return tableName;
    }


}
