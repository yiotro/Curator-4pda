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


    void parseSection(String src) {
        ArrayList<String> lines = getStringsList(src);
        parseSectionLines(lines);
    }


    private ArrayList<String> getStringsList(String src) {
        ArrayList<String> lines = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(src, "\n");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            lines.add(token.substring(findIndexOfFirstNotSpaceSymbol(token)));
        }
        return lines;
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
    void parseFormattedSectionLines(ArrayList<String> lines) {
        Log.d("yiotro", "parsing default lines");
        Rule rule = null;
        StringBuilder stringBuilder = null;
        ListIterator<Rule> iterator = model.getRules().listIterator();

        for (String line : lines) {
            if (line.equalsIgnoreCase("[formatted]")) continue;

            // title
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

            // tag
            if (line.length() > 3 && line.substring(0, 3).equals("tag")) {
                rule.setTag(line.substring(4));
                continue;
            }

            if (stringBuilder.length() > 0) stringBuilder.append("\n");
            stringBuilder.append(line);
        }

        // last rule
        rule.setText(stringBuilder.toString());
        iterator.add(rule);
        model.databaseHandler.addRule(model.getCurrentSection(), rule);
    }


    void parseSectionLines(ArrayList<String> lines) {
        if (lines.size() == 0) {
            Log.d("yiotro", "parse lines size is zero");
            return;
        }

        // ignore empty lines
        ArrayList<String> formattedLines = new ArrayList<>();
        for (String line : lines) {
            if (line.length() > 0) {
                formattedLines.add(line);
            }
        }

        if (lines.get(0).equalsIgnoreCase("[formatted]")) {
            parseFormattedSectionLines(formattedLines);
            return;
        }

        Log.d("yiotro", "not found [formatted]");
    }


    public void parseFullBase(String src) {
        ArrayList<String> stringsList = getStringsList(src);

        if (stringsList.size() == 0) {
            Log.d("yiotro", "parse lines size is 0");
            return;
        }

        // clear everything
        model.getDatabaseHandler().clearDatabase();
        model.clearContents();

        ArrayList<ListContainer> containers = new ArrayList<>();
        ListContainer currentContainer = new ListContainer();
        for (String s : stringsList) {
            if (s.equals("---")) {
                containers.add(currentContainer);
                currentContainer = new ListContainer();
                continue;
            }
            currentContainer.strings.add(s);
        }
        containers.add(currentContainer); // last section

        for (ListContainer container : containers) {
            String firstLine = container.strings.get(0);
            firstLine = firstLine.substring(findIndexOfFirstNotSpaceSymbol(firstLine));
            String sectionName = firstLine.substring(firstLine.indexOf(' '));
            model.addSection(sectionName); // create section

            // remove first line with section name
            ListIterator<String> stringListIterator = container.strings.listIterator();
            stringListIterator.next();
            stringListIterator.remove();

            parseSectionLines(container.strings); // parse rules in section
        }
    }


    class ListContainer {
        ArrayList<String> strings;


        public ListContainer() {
            strings = new ArrayList<>();
        }
    }
}
