package edu.uob;
import java.util.ArrayList;
import java.util.Arrays;


/*Code from Simon */
public class Tokeniser {
    private static final String[] SPECIAL_CHARACTERS = {"(",")",",",";", "=", "<", ">"};
    private static final String[] OTHER_CHARACTERS = {" <  = ", " =  = ", " >  = ", "! ="};
    private static ArrayList<String> tokens;
    private String query;
    public Tokeniser(String query)
    {
        this.query = query;
        tokens = new ArrayList<>();
    }

    public ArrayList<String> tokenise()
    {
        // Remove any whitespace at the beginning and end of the query
        query = query.trim();

        query = query.replace("\t"," ");
        query = query.replaceAll("\\n", " ");
        query = query.replaceAll("\\r", " ");

        // Split the query on single quotes (to separate out query characters from string literals)
        String[] fragments = query.split("'");
        for (int i=0; i<fragments.length; i++) {
            // Every odd fragment is a string literal, so just append it without any alterations
            if (i%2 != 0) tokens.add("'" + fragments[i] + "'");
                // If it's not a string literal, it must be query characters (which need further processing)
            else {
                // Tokenise the fragments into an array of strings
                String[] nextBatchOfTokens = tokenise_help(fragments[i]);
                // Then add these to the "result" array list (needs a bit of conversion)
                tokens.addAll(Arrays.asList(nextBatchOfTokens));
            }
        }
        // Finally, loop through the result array list, printing out each token a line at a time
       return tokens;
    }

    private String[] tokenise_help(String input)
    {
        // Add in some extra padding spaces around the "special characters"
        // so we can be sure that they are separated by AT LEAST one space (possibly more)
        for (String specialCharacter : SPECIAL_CHARACTERS) {
            input = input.replace(specialCharacter, " " + specialCharacter + " ");
        }

        for (String temp: OTHER_CHARACTERS)
        {
            input = input.replace(temp, " "+ temp.replaceAll("\\s+","") + " ");
        }

        // Remove all double spaces (the previous replacements may had added some)
        // This is "blind" replacement - replacing if they exist, doing nothing if they don't
        while (input.contains("  ")) input = input.replaceAll("  ", " ");
        // Again, remove any whitespace from the beginning and end that might have been introduced
        input = input.trim();
        // Finally split on the space char (since there will now ALWAYS be a space between tokens)
        return input.split(" ");
    }
}
