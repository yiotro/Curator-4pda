package yio.tro.curator.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

public class RulesParser {

    RulesModel model;


    public RulesParser(RulesModel model) {
        this.model = model;
    }


    void parse(String src) {
        ArrayList<String> lines = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(src, "\n");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            lines.add(token.substring(findIndexOfFirstNotSpaceSymbol(token)));
        }

        parseLines(lines);
    }


    private int findIndexOfFirstNotSpaceSymbol(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') continue;
            return i;
        }
        return s.length();
    }


    /**
     * This is a main method of class. It receives list of lines and converts it to section.
     * First line usually has [formatted] in it and is ignored.
     * Every line that starts with '-' is treated like title.
     * Every other line is treated like text.
     * @param lines lines that contain rule titles or text
     */
    void parseFormattedLines(ArrayList<String> lines) {
        Log.d("yiotro", "parsing default lines");
        Rule rule = null;
        StringBuilder stringBuilder = null;
        ListIterator<Rule> iterator = model.getRules().listIterator();

        for (String line : lines) {
//            Log.d("yiotro", "parsing: " + line);
            if (line.equalsIgnoreCase("[formatted]")) continue;

            if (line.charAt(0) == '-') {
                if (rule != null) {
                    rule.setText(stringBuilder.toString());
                    iterator.add(rule);
                    model.databaseHandler.addRule(model.getCurrentSection(), rule);
                }
                rule = new Rule(model.getCurrentSection().getIdForNewRule());
                rule.setTitle(line.substring(1));
                stringBuilder = new StringBuilder();
                continue;
            }

            if (stringBuilder.length() > 0) stringBuilder.append("\n");
            stringBuilder.append(line);
        }
    }


    void parseLines(ArrayList<String> lines) {
        if (lines.size() == 0) {
            Log.d("yiotro", "parse lines size = 0");
            return;
        }

        if (lines.get(0).equalsIgnoreCase("[formatted]")) {
            parseFormattedLines(lines);
            return;
        }

        Log.d("yiotro", "not found [formatted]");
    }
}
