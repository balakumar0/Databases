package edu.uob;

import java.util.ArrayList;

import static edu.uob.SyntaxType.*;
public class DBHelper {
    private static final Character[] LETTERS = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private static final Character[] SYMBOLS = {
            '!',  '#',  '$',  '%',  '&' ,  '(',  ')',
            '*', '+',  ',',  '-',  '.',  '/',  ':',
            ';',  '>',  '=',  '<',  '?',  '@',  '[',  '\'',  ']',  '^',  '_',  '`',  '{',  '}',  '~'
    };

    private static final Character[] DIGITS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private static final Character[] FLOAT_SYMBOLS = {'.', '+', '-'};
    private static final Character[] INTEGER_SYMBOLS = {'+', '-'};

    public static boolean conditionMet(String value1, String value2, SyntaxType operator)
    {
        SyntaxType v1 = classifyValue(value1);
        SyntaxType v2 = classifyValue(value2);

        if (v1 == STRING_LITERAL && v2 == STRING_LITERAL)
        {
            return compareStrings(value1, value2, operator);
        }

        if (v1 == NULL_VALUE || v2 == NULL_VALUE)
        {
            return compareNulls(value1, value2, operator);
        }

        if (v1 == INTEGER_LITERAL && v2 == INTEGER_LITERAL)
        {
            return compareIntegers(value1, value2, operator);
        }

        if (v1 == FLOAT_LITERAL || v2 == FLOAT_LITERAL)
        {
            return compareFloats(value1, value2, operator);
        }

        if (v1 == BOOLEAN_LITERAL && v2 == BOOLEAN_LITERAL)
        {
            return compareBooleans(value1, value2, operator);
        }

        return false;
    }

    private static boolean compareStrings(String s1, String s2, SyntaxType operator)
    {
        switch(operator)
        {
            case EQUAL_TO:
            {
                return s1.equals(s2);
            }

            case NOT_EQUAL:
            {
                return (!s1.equals(s2));
            }

            case LIKE:
            {
                return s1.contains(s2);
            }

            case LT:
            {
                return (s1.compareTo(s2) < 0);
            }

            case LT_EQUAL_TO:
            {
                return (s1.compareTo(s2) <= 0);
            }

            case GT:
            {
                return (s1.compareTo(s2) > 0);
            }

            case GT_EQUAL_TO:
            {
                return (s1.compareTo(s2) >= 0);

            }

            default:
                return false;
        }
    }

    private static boolean compareBooleans(String s1, String s2, SyntaxType operator)
    {
        switch (operator) {
            case EQUAL_TO -> {
                return s1.equalsIgnoreCase(s2);
            }
            case NOT_EQUAL -> {
                return (!s1.equalsIgnoreCase(s2));
            }
            default -> {
                return false;
            }
        }
    }

    private static boolean compareIntegers(String s1, String s2, SyntaxType operator)
    {
        try {
            switch (operator) {
                case EQUAL_TO -> {
                    return Integer.parseInt(s1) == Integer.parseInt(s2);
                }
                case NOT_EQUAL -> {
                    return Integer.parseInt(s1) != Integer.parseInt(s2);
                }
                case LT -> {
                    return Integer.parseInt(s1) < Integer.parseInt(s2);
                }
                case LT_EQUAL_TO -> {
                    return Integer.parseInt(s1) <= Integer.parseInt(s2);
                }
                case GT -> {
                    return Integer.parseInt(s1) > Integer.parseInt(s2);
                }
                case GT_EQUAL_TO -> {
                    return Integer.parseInt(s1) >= Integer.parseInt(s2);
                }
                default -> {
                    return false;
                }
            }

        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static boolean compareFloats(String s1, String s2, SyntaxType operator)
    {
        try
        {
            float f1 = Float.parseFloat(s1);
            float f2 = Float.parseFloat(s2);

            switch (operator) {
                case EQUAL_TO -> {
                    return Float.compare(f1, f2) == 0;
                }

                case NOT_EQUAL -> {
                    return Float.compare(f1, f2) != 0;
                }

                case LT -> {
                    return Float.compare(f1, f2) < 0;
                }
                case LT_EQUAL_TO -> {
                    return Float.compare(f1, f2) <= 0;
                }
                case GT -> {
                    return Float.compare(f1, f2) > 0;
                }
                case GT_EQUAL_TO -> {
                    return Float.compare(f1, f2) >= 0;
                }
                default -> {
                    return false;
                }
            }
        }

        catch (Exception e)
        {
            return false;
        }

    }

    private static boolean compareNulls(String value1, String value2, SyntaxType operator)
    {
       if (operator == EQUAL_TO)
       {
           return value1.equalsIgnoreCase(value2);
       }

       if (operator == NOT_EQUAL)
       {
           return !value1.equalsIgnoreCase(value2);
       }

       return false;
    }

    private static SyntaxType classifyValue(String value)
    {
        value = value.toUpperCase();

        if (isIntegerLiteral(value))
        {
            return INTEGER_LITERAL;
        }

        if (isFloatLiteral(value))
        {
            return FLOAT_LITERAL;
        }

        if (isBooleanLiteral(value))
        {
            return BOOLEAN_LITERAL;
        }

        if (value.equals("NULL"))
        {
            return NULL_VALUE;
        }

        if (isValidStringLiteral(value))
        {
            return STRING_LITERAL;
        }

        return ERROR;
    }

    public static boolean isValidStringLiteral(String s)
    {
        if (s.equals(""))
        {
            return true;
        }

        for (char c: s.toCharArray())
        {
            if (!isValidCharLiteral(c))
            {
                return false;
            }

        }

        return true;
    }

    private static boolean isValidCharLiteral(char c)
    {
        if (c == ' ')
        {
            return true;
        }

        if (isLetter(c))
        {
            return true;
        }

        if (isSymbol(c))
        {
            return true;
        }

        return isDigit(c);
    }

    private static boolean isLetter(char c)
    {
        char ch = Character.toUpperCase(c);
        for (Character letter : LETTERS) {
            if (ch == letter) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSymbol(char c)
    {
        for (Character symbol : SYMBOLS) {
            if (c == symbol) {
                return true;
            }
        }

        return false;
    }

    public static boolean isDigit(char c)
    {
        for (Character digit : DIGITS) {
            if (c == digit) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBooleanLiteral(String s)
    {
        s = s.toUpperCase();
        return s.equals("TRUE") || s.equals("FALSE");
    }

    public static boolean isFloatLiteral(String s)
    {
        for (char c: s.toCharArray())
        {
            if (!isDigit(c) && !isFloatSymbol(c))
            {
                return false;
            }
        }

        try
        {
            Float f = Float.parseFloat(s);
        }

        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    private static boolean isFloatSymbol(char c)
    {
        for (Character floatSymbol : FLOAT_SYMBOLS) {
            if (c == floatSymbol) {
                return true;
            }
        }

        return false;
    }

    public static boolean isIntegerLiteral(String s)
    {
        for (char c: s.toCharArray())
        {
            if (!isDigit(c) && !isIntegerSymbol(c))
            {
                return false;
            }
        }

        try
        {
            Integer i = Integer.parseInt(s);
        }

        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    private static boolean isIntegerSymbol(char c)
    {
        for (Character intSymbol : INTEGER_SYMBOLS) {
            if (c == intSymbol) {
                return true;
            }
        }

        return false;
    }

    public static void printTable(StringBuilder s, ArrayList<ArrayList<String>> table)
    {
        for (ArrayList<String> tempRow: table)
        {
            for (String tempString: tempRow)
            {
                s.append(tempString);
                s.append('\t');
            }
            s.append(System.lineSeparator());

        }
    }

    public static boolean isPlainText(String s)
    {
        for (char c: s.toUpperCase().toCharArray())
        {
            if (!isLetter(c) && !isDigit(c))
            {
                return false;
            }
        }

        return true;
    }

}
