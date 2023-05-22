package edu.uob;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static edu.uob.DBHelper.*;
import static edu.uob.SyntaxType.*;

public class Parser {
    private static final String[] KEYWORDS =
            {"USE", "CREATE", "DATABASE", "TABLE", "DROP",
            "ALTER", "INSERT", "INTO", "SELECT", "FROM", "WHERE",
            "UPDATE", "SET", "DELETE", "JOIN", "AND", "ON", "ADD",
            "TRUE", "FALSE", "OR", "LIKE", "NULL"};
    private final ArrayList<String> tokens;
    private int currentIndex;
    private Node temp;
    private String errorMessage;
    public Parser(ArrayList<String> tokens)
    {
        this.tokens = tokens;
        currentIndex = 0;
    }

    private Node containsKeyword(String word)
    {
        word = word.toUpperCase();

        boolean containsKeyword = false;

        for (String keyword : KEYWORDS) {
            if (word.equals(keyword)) {
                containsKeyword = true;
                break;
            }
        }

        if (containsKeyword)
        {
            return createErrorNode("Found reserved keyword unexpectedly: " + word);
        }

        return null;
    }
    public Node generateAST()
    {
        return parseQuery();
    }

    private Node parseQuery()
    {
        Node n = parseCommandType();

        if (currentIndex >= tokens.size() || !tokens.get(currentIndex).equals(";"))
        {
            return createErrorNode("Expected ;");
        }

        currentIndex++;

        if (currentIndex < tokens.size())
        {
            return createErrorNode("Additional text following ; - invalid query");
        }

        return n;
    }
    private Node parseCommandType()
    {
        if (currentIndex >= tokens.size())
        {
            return createErrorNode("No command found");
        }

        switch (tokens.get(currentIndex).toUpperCase()) {
            case "USE" -> {
                currentIndex++;
                return parseUseQuery();
            }
            case "CREATE" -> {
                currentIndex++;
                return parseCreateQuery();
            }
            case "DROP" -> {
                currentIndex++;
                return parseDropQuery();
            }
            case "ALTER" -> {
                currentIndex++;
                return parseAlterQuery();
            }
            case "INSERT" -> {
                currentIndex++;
                return parseInsertQuery();
            }
            case "SELECT" -> {
                currentIndex++;
                return parseSelectQuery();
            }
            case "UPDATE" -> {
                currentIndex++;
                return parseUpdateQuery();
            }
            case "DELETE" -> {
                currentIndex++;
                return parseDeleteQuery();
            }
            case "JOIN" -> {
                currentIndex++;
                return parseJoinQuery();
            }
            default -> {
                return createErrorNode("No valid CommandType encountered");
            }
        }
    }

    private Node parseUseQuery()
    {
        if (currentIndex >= tokens.size())
        {
            return createErrorNode("Expected database value");
        }

        String database = tokens.get(currentIndex);
        if ((temp = containsKeyword(database)) != null)
        {
            return temp;
        }

        Node n = new Node(USE);
        n.setSibling(new Node(DATABASE_NAME, database));
        currentIndex++;
        return n;

    }

    private Node parseCreateQuery()
    {
        if (currentIndex >= tokens.size())
        {
            return createErrorNode("Expected token following CREATE");
        }

        switch (tokens.get(currentIndex).toUpperCase()) {
            case "DATABASE" -> {
                currentIndex++;
                return parseCreateDatabase();
            }
            case "TABLE" -> {
                currentIndex++;
                return parseCreateTable();
            }
            default -> {
                return createErrorNode("Expected DATABASE or TABLE following CREATE");
            }
        }

    }

    private Node parseCreateDatabase()
    {
        if (currentIndex >= tokens.size())
        {
            return createErrorNode("Expected token following CREATE DATABASE");
        }

        String database = tokens.get(currentIndex);

        if (!DBHelper.isPlainText(database))
        {
            return createErrorNode("Database name contains non plain text char");
        }

        if ((temp = containsKeyword(database)) != null)
        {
            return temp;
        }

        Node n = new Node(CREATE_DATABASE);
        n.setSibling(new Node(DATABASE_NAME, database));
        currentIndex++;
        return n;

    }

    private Node parseCreateTable()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token following CREATE TABLE");
        }

        String table = tokens.get(currentIndex);

        if ((temp = containsKeyword(table)) != null)
        {
            return temp;
        }

        Node n = new Node(CREATE_TABLE);

        n.setSibling(parseTableName());

        if (currentIndex >= tokens.size() || tokens.get(currentIndex).equals(";")) {
            return n;
        }

        if (!tokens.get(currentIndex).equals("("))
        {
            return createErrorNode("Missing ( following CREATE TABLE");
        }

        currentIndex++;

        n.setChild(parseAttributeList());

        if (currentIndex >= tokens.size() || !tokens.get(currentIndex).equals(")")) {
            return createErrorNode("Missing ) following AttributeList");
        }

        currentIndex++;

        return n;
    }

    private Node parseDropQuery()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token following DROP");
        }

        Node n = new Node(DROP);

        switch (tokens.get(currentIndex).toUpperCase()) {
            case "TABLE" -> {
                currentIndex++;
                n.setSibling(parseTableName());
            }
            case "DATABASE" -> {
                currentIndex++;
                n.setSibling(parseDatabaseName());
            }
            default -> {
                return createErrorNode("Only can DROP table or database");
            }
        }

        return n;
    }

    private Node parseDatabaseName()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token following ALTER");
        }

        String db = tokens.get(currentIndex);

        if ((temp = containsKeyword(db)) != null)
        {
            return temp;
        }

        Node n = new Node(DATABASE_NAME, db);
        currentIndex++;

        return n;
    }

    private Node parseAlterQuery()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token following ALTER");
        }

        if (!tokens.get(currentIndex).equalsIgnoreCase("TABLE"))
        {
            return createErrorNode("Expected TABLE following ALTER");
        }

        Node n = new Node(ALTER);

        currentIndex++;

        n.setSibling(parseTableName());

        n.setChild(parseAlterationType());

        return n;

    }

    private Node parseAlterationType()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected AlterationType here");
        }

        Node n;
        switch (tokens.get(currentIndex).toUpperCase()) {
            case "ADD" -> {
                n = new Node(ADD);
                currentIndex++;
                n.setSibling(parseAttributeName());
                return n;
            }
            case "DROP" -> {
                n = new Node(DROP);
                currentIndex++;
                n.setSibling(parseAttributeName());
                return n;
            }
            default -> {
                return createErrorNode("Expected ADD or DROP, found " + tokens.get(currentIndex));
            }
        }
    }
    private Node parseAttributeName()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected Attribute Name here");
        }

        String name = tokens.get(currentIndex);

        if (!isValidAttributeName(name))
        {
            return createErrorNode("AttributeName " + name + " is not valid");
        }

        if ((temp = containsKeyword(name)) != null)
        {
            return temp;
        }

        Node n = new Node(ATTRIBUTE_NAME, name);
        currentIndex++;

        return n;
    }

    private boolean isValidAttributeName(String attributeName)
    {
        if (attributeName.contains("."))
        {
            String[] results = attributeName.split(Pattern.quote("."));
            if (results.length != 2)
            {
                return false;
            }

            for (String result: results)
            {
                if (result.isEmpty())
                {
                    return false;
                }

                if (!DBHelper.isPlainText(result))
                {
                    return false;
                }

            }

            return true;
        }

        return DBHelper.isPlainText(attributeName);
    }

    private Node parseInsertQuery()
    {
        if (currentIndex >= tokens.size() || !tokens.get(currentIndex).equalsIgnoreCase("INTO")) {
            return createErrorNode("Expected INTO here");
        }

        Node n = new Node(INSERT);
        currentIndex++;

        n.setSibling(parseTableName());
        n.setChild(parseValues());

        return n;
    }

    private Node parseTableName()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        String table = tokens.get(currentIndex);

        if (!DBHelper.isPlainText(table))
        {
            return createErrorNode("Table name contains non plain text");
        }

        if ((temp = containsKeyword(table)) != null)
        {
            return temp;
        }

        Node n = new Node(TABLE_NAME, table);
        currentIndex++;

        return n;
    }

    private Node parseValues()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        if (!tokens.get(currentIndex).equalsIgnoreCase("VALUES"))
        {
            return createErrorNode("Expected VALUES");
        }

        currentIndex++;

        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        if (!tokens.get(currentIndex).equals("("))
        {
            return createErrorNode("Expected (");
        }

        currentIndex++;

        Node n = parseValuesList();

        if (currentIndex >= tokens.size() || !tokens.get(currentIndex).equals(")")) {
            return createErrorNode("Expected )");
        }

        currentIndex++;

        return n;
    }

    private Node parseValuesList()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        Node n = parseValue();

        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        String currentToken = tokens.get(currentIndex);

        if (currentToken.equals(","))
        {
            currentIndex++;
            n.setSibling(parseValuesList());
        }

        return n;
    }

    private Node parseValue()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        String s = tokens.get(currentIndex);

        Node n;

        if (s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'' && s.length() >= 2)
        {
            if (isValidStringLiteral(s.substring(1, s.length() - 1)))
            {
                n = new Node(VALUE, s.substring(1, s.length() - 1));
                currentIndex++;
                return n;
            }

            else
            {
                return createErrorNode("Invalid string literal");
            }

        }

        if (isBooleanLiteral(s) || isFloatLiteral(s) || isIntegerLiteral(s))
        {
            n = new Node(VALUE, s);
            currentIndex++;
            return n;
        }

        if (s.equalsIgnoreCase("NULL"))
        {
            n = new Node(VALUE, s.toUpperCase());
            currentIndex++;
            return n;
        }

        return createErrorNode("No value found");

        }

    private Node parseSelectQuery()
    {
        Node n = new Node(SELECT);
        n.setChild(parseWildAttributes());

        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        String currentToken = tokens.get(currentIndex);

        if (!currentToken.equalsIgnoreCase("FROM"))
        {
            return createErrorNode("Expected FROM");
        }

        currentIndex++;

        n.setSibling(parseTableName());

        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        if (tokens.get(currentIndex).equalsIgnoreCase("WHERE"))
        {
            currentIndex++;
            n.getChild().setSibling(parseCondition());

        }

        return n;
    }

    private Node parseWildAttributes()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        Node n;

        if (tokens.get(currentIndex).equals("*"))
        {
            n = new Node(SELECT_ALL);
            currentIndex++;
            return n;
        }

        n = parseAttributeList();

        return n;

    }

    private Node parseAttributeList()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        Node n = parseAttributeName();

        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        String currentToken = tokens.get(currentIndex);

        if (currentToken.equals(","))
        {
            currentIndex++;
            n.setChild(parseAttributeList());
        }

        return n;
    }

    private Node parseCondition()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        Node n;

        if (tokens.get(currentIndex).equals("("))
        {
            n = new Node(CONDITION);
            currentIndex++;

            n.setChild(parseCondition());

            if (currentIndex >= tokens.size() || !tokens.get(currentIndex).equals(")")) {
                return createErrorNode("Expected token");
            }

            currentIndex++;

            if (currentIndex < tokens.size() && (tokens.get(currentIndex).equalsIgnoreCase("OR")
                    || tokens.get(currentIndex).equalsIgnoreCase("AND"))) {
                n.setSibling(parseBoolFunc());
            }

        }

        else
        {
            n = new Node(CONDITION);
            n.setChild(parseAttributeName());
            n.getChild().setSibling(parseComparator());
            n.getChild().getSibling().setSibling(parseValue());

            if (currentIndex < tokens.size() && (tokens.get(currentIndex).equalsIgnoreCase("OR")
                    || tokens.get(currentIndex).equalsIgnoreCase("AND"))) {
                n.setSibling(parseBoolFunc());
            }
        }

        return n;
    }

    private Node parseBoolFunc()
    {
        Node n;

        switch (tokens.get(currentIndex).toUpperCase()) {
            case "OR" -> {
                n = new Node(OR);
                currentIndex++;
                n.setSibling(parseCondition());
                return n;
            }
            case "AND" -> {
                n = new Node(AND);

                currentIndex++;

                n.setSibling(parseCondition());

                return n;
            }
            default -> {
                return createErrorNode("No OR or AND found");
            }
        }

    }

    private Node parseComparator()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        String token = tokens.get(currentIndex).toUpperCase();

        switch (token) {
            case "==" -> {
                currentIndex++;
                return new Node(EQUAL_TO);
            }
            case ">" -> {
                currentIndex++;
                return new Node(GT);
            }
            case "<" -> {
                currentIndex++;
                return new Node(LT);
            }
            case ">=" -> {
                currentIndex++;
                return new Node(GT_EQUAL_TO);
            }
            case "<=" -> {
                currentIndex++;
                return new Node(LT_EQUAL_TO);
            }
            case "!=" -> {
                currentIndex++;
                return new Node(NOT_EQUAL);
            }
            case "LIKE" -> {
                currentIndex++;
                return new Node(LIKE);
            }
            default -> {
                return createErrorNode("Invalid comparator");
            }
        }
    }

    private Node parseUpdateQuery()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        Node n = new Node(UPDATE);
        n.setSibling(parseTableName());

        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        if (!tokens.get(currentIndex).equalsIgnoreCase("SET"))
        {
            return createErrorNode("Expected token");
        }

        currentIndex++;

        n.setChild(parseNameValueList());

        if (currentIndex >= tokens.size() || !tokens.get(currentIndex).equalsIgnoreCase("WHERE")) {
            return createErrorNode("Expected WHERE");
        }

        currentIndex++;

        n.getSibling().setSibling(parseCondition());

        return n;
    }

    private Node parseNameValueList()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        Node n = parseNameValuePair();

        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        if (tokens.get(currentIndex).equals(","))
        {
            currentIndex++;
            n.setChild(parseNameValueList());
        }

        return n;
    }

    private Node parseNameValuePair()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        Node n = parseAttributeName();

        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        if (!tokens.get(currentIndex).equals("="))
        {
            return createErrorNode("Expected token");
        }

        currentIndex++;

        n.setSibling(parseValue());

        return n;

    }

    private Node parseDeleteQuery()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        if (!tokens.get(currentIndex).equalsIgnoreCase("FROM"))
        {
            return createErrorNode("Expected FROM");
        }

        currentIndex++;

        Node n = new Node(DELETE);
        n.setSibling(parseTableName());

        if (currentIndex >= tokens.size() || !tokens.get(currentIndex).equalsIgnoreCase("WHERE"))
        {
            return createErrorNode("Expected WHERE");
        }

        currentIndex++;
        n.setChild(parseCondition());

        return n;

    }

    private Node parseJoinQuery()
    {
        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected token");
        }

        Node n = new Node(JOIN);
        n.setSibling(parseTableName());

        if (currentIndex >= tokens.size()) {
            return createErrorNode("Expected AND");
        }

        currentIndex++;

        n.getSibling().setSibling(parseTableName());

        if (currentIndex >= tokens.size() || !tokens.get(currentIndex).equalsIgnoreCase("ON")) {
            return createErrorNode("Expected ON");
        }

        currentIndex++;

        n.setChild(parseAttributeName());

        if (currentIndex >= tokens.size() || !tokens.get(currentIndex).equalsIgnoreCase("AND")) {
            return createErrorNode("Expected AND");
        }

        currentIndex++;

        n.getChild().setChild(parseAttributeName());

        return n;
    }
    private Node createErrorNode(String errorMessage)
    {
        if (this.errorMessage != null)
        {
            errorMessage = this.errorMessage;
        }

        Node n = new Node(ERROR, errorMessage);
        this.errorMessage = errorMessage;
        return n;
    }


}
