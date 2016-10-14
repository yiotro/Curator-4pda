package yio.tro.curator.model;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import yio.tro.curator.R;

import java.util.ArrayList;
import java.util.ListIterator;

public class RulesModel {

    private static RulesModel instance;
    ArrayList<Section> sections;
    Section currentSection;
    ArrayList<RulesModelListener> listeners;
    DatabaseHandler databaseHandler;


    /**
     * Implementation of singleton pattern
     * @return static instance of RulesModel class
     */
    public static RulesModel getInstance() {
        if (instance == null) {
            instance = new RulesModel();
        }

        return instance;
    }


    /**
     * Private constructor. Nothing special.
     */
    private RulesModel() {
        sections = new ArrayList<>();
        currentSection = null;
        listeners = new ArrayList<>();
    }


    /**
     * removes all rules and sections from model (not from database).
     * Used to clear model before loading from database.
     */
    public void clearContents() {
        for (Section section : sections) {
            section.getRules().clear();
        }

        sections.clear();
    }


    /**
     * Loads all rules and sections from database.
     * If database in empty then default section with rules is created.
     * @param context used for database
     */
    public void loadFromDatabase(Context context) {
        clearContents();
        databaseHandler = new DatabaseHandler(context, null);

        // loading sections
        for (Section section : databaseHandler.getSections()) {
            ListIterator<Section> iterator = sections.listIterator();
            while (iterator.hasNext()) iterator.next();
            iterator.add(section);
        }

        // loading rules for every section
        for (Section section : sections) {
            for (Rule rule : databaseHandler.getRules(section)) {
                ListIterator<Rule> iterator = section.getRules().listIterator();
                while (iterator.hasNext()) iterator.next();
                iterator.add(rule);
            }
        }

        checkToCreateDefaultRules(context);

        currentSection = sections.get(0);
        currentSection.sortRules();
    }


    /**
     * adds one section and fills it with default rules
     * @param context used to get string resources
     */
    private void checkToCreateDefaultRules(Context context) {
        if (sections.size() == 0) {
            addSection(context.getString(R.string.main_rules));
            currentSection = sections.get(0);

            RulesParser rulesParser = new RulesParser(this);
            rulesParser.parse(context.getResources().getString(R.string.default_rules));
        }
    }


    /**
     * Creates new section. Then this section is added to the model and to database.
     * After that all listeners are notified about this.
     * @param name name of new section
     * @return newly created section
     */
    public Section addSection(String name) {
        if (name.length() == 0) return null;

        Section section = new Section(getIdForNewSection());
        section.setName(name);

        ListIterator<Section> iterator = sections.listIterator();
        while (iterator.hasNext()) iterator.next();
        iterator.add(section);

        databaseHandler.addSection(section);

        for (RulesModelListener listener : listeners) {
            listener.onSectionAdded(section);
        }

        return section;
    }


    /**
     * Deletes current section from model and database.
     * Notifies listeners about this.
     * Default rules cannot be deleted.
     */
    public void deleteSection() {
        // don't delete main rules
        if (currentSection.getId() == 0) {
            for (RulesModelListener listener : listeners) {
                listener.onUnableToDeleteSection();
            }
            return;
        }

        databaseHandler.deleteSection(currentSection);

        ListIterator<Section> iterator = sections.listIterator();
        while (iterator.hasNext()) {
            Section section = iterator.next();
            if (section.getId() == currentSection.getId()) {
                iterator.remove();
                break;
            }
        }

        for (RulesModelListener listener : listeners) {
            listener.onSectionDeleted(currentSection);
        }
    }


    /**
     * Looks for rule with same id as in editedRule and then modifies it.
     * After that sorts rules and modifies rule in database.
     * Listeners are notified about this.
     * @param editedRule modified rule object
     */
    public void editRule(Rule editedRule) {
        for (Rule rule : currentSection.getRules()) {
            if (rule.getId() != editedRule.getId()) continue;
            rule.set(editedRule);
            break;
        }

        currentSection.sortRules();

        databaseHandler.editRule(currentSection, editedRule);

        for (RulesModelListener listener : listeners) {
            listener.onRuleChanged(editedRule);
        }
    }


    /**
     * Creates new rule object. Puts it into model and database.
     * Then sorts rules.
     * @param title title of new rule
     * @param text full text of new rule
     * @return newly created rule object
     */
    public Rule addRule(String title, String text) {
        if (title.length() == 0) return null;

        Rule rule = new Rule(currentSection.getIdForNewRule());
        rule.setTitle(title);
        rule.setText(text);

        ListIterator<Rule> iterator = currentSection.getRules().listIterator();
        while (iterator.hasNext()) iterator.next();
        iterator.add(rule);

        currentSection.sortRules();

        databaseHandler.addRule(currentSection, rule);

        for (RulesModelListener listener : listeners) {
            listener.onRuleAdded(rule);
        }

        return rule;
    }


    /**
     * Deletes all rules from list. Rules are deleted from model and database.
     * @param rules list of rules to delete
     */
    public void deleteRules(ArrayList<Rule> rules) {
        // delete rules from database
        for (Rule delRule : rules) {
            databaseHandler.deleteRule(currentSection, delRule);
        }

        for (Rule delRule : rules) {
            ListIterator<Rule> iterator = currentSection.getRules().listIterator();
            while (iterator.hasNext()) {
                Rule rule = iterator.next();
                if (rule == delRule) {
                    iterator.remove();
                    break;
                }
            }
        }

        for (RulesModelListener listener : listeners) {
            listener.onRulesDeleted();
        }
    }


    /**
     * Returns id for new section.
     * @return id for new section
     */
    int getIdForNewSection() {
        return getMaxId() + 1;
    }


    /**
     * Goes through all sections and looks for maximum id.
     * @return max id of all sections
     */
    int getMaxId() {
        int maxId = -1;
        for (Section section : sections) {
            if (section.getId() > maxId) {
                maxId = section.getId();
            }
        }
        return maxId;
    }


    /**
     * This method generates needed text with all tags and stuff.
     * Then copies it to clipboard and shows toast about this.
     * @param context used to get string resources
     * @param rule provides a text to copy
     */
    public void copyToClipboard(Context context, Rule rule) {
        // get touched rule
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // string builder for clipboard
        StringBuilder stringBuilder = new StringBuilder();

        beginBuildClipData(context, defaultSharedPreferences, stringBuilder);
        stringBuilder.append(rule.getText());
        endBuildClipData(context, defaultSharedPreferences, stringBuilder);

        // copy to clipboard
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("cur", stringBuilder.toString());
        clipboard.setPrimaryClip(clip);

        // make toast about this
        Toast.makeText(context.getApplicationContext(), rule.getTitle(), Toast.LENGTH_SHORT).show();
    }


    private void endBuildClipData(Context context, SharedPreferences defaultSharedPreferences, StringBuilder stringBuilder) {
        stringBuilder.append("[/size]").append("\n");

        // add ending phrase
        String endingPhrase = defaultSharedPreferences.getString("general_pref_ending_phrase", context.getResources().getString(R.string.default_ending_phrase));
        if (endingPhrase.length() > 0) {
            stringBuilder.append(endingPhrase).append("\n");
        }

        // final tag
        String tag = defaultSharedPreferences.getString("general_pref_tag", context.getResources().getString(R.string.default_tag));
        if (tag.length() > 0) {
            stringBuilder.append("[/").append(tag).append("]\n");
        }
    }


    private void beginBuildClipData(Context context, SharedPreferences defaultSharedPreferences, StringBuilder stringBuilder) {
        String tag = defaultSharedPreferences.getString("general_pref_tag", context.getResources().getString(R.string.default_tag));
        if (tag.length() > 0) {
            stringBuilder.append("[").append(tag).append("]\n");
        }

        // add welcome phrase
        String welcomePhrase = defaultSharedPreferences.getString("general_pref_welcome_phrase", context.getResources().getString(R.string.default_welcome_phrase));
        if (welcomePhrase.length() > 0) {
            stringBuilder.append(welcomePhrase).append("\n");
        }

        String textSize = defaultSharedPreferences.getString("general_pref_text_size", "1");
        stringBuilder.append("[size=").append(textSize).append("]");
    }


    /**
     * Same as copyToClipboard() but copies multiple rules
     * @param context used to get string resources
     * @param rules selected rules
     */
    public void copyMultipleRulesToClipboard(Context context, ArrayList<Rule> rules) {
        // get touched rule
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // string builder for clipboard
        StringBuilder stringBuilder = new StringBuilder();

        beginBuildClipData(context, defaultSharedPreferences, stringBuilder);

        for (Rule rule : rules) {
            stringBuilder.append(rule.getText()).append("\n");
        }

        endBuildClipData(context, defaultSharedPreferences, stringBuilder);

        // copy to clipboard
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("cur", stringBuilder.toString());
        clipboard.setPrimaryClip(clip);

        // make toast about this
        StringBuilder titleBuilder = new StringBuilder();
        for (Rule rule : rules) {
            titleBuilder.append(rule.getTitle()).append("\n");
        }
        Toast.makeText(context.getApplicationContext(), titleBuilder.toString(), Toast.LENGTH_SHORT).show();
    }


    /**
     * This method converts current section to text and places it to clipboard.
     * @param context used to get string resources
     */
    public void exportCurrentSectionToClipboard(Context context) {
        // string builder for clipboard
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[formatted]\n");

        currentSection.sortRules();
        for (Rule rule : currentSection.getRules()) {
            stringBuilder.append("-").append(rule.getTitle()).append("\n");
            stringBuilder.append(rule.getText()).append("\n");
        }

        // copy to clipboard
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("cur", stringBuilder.toString());
        clipboard.setPrimaryClip(clip);

        // make toast about this
        Toast.makeText(context.getApplicationContext(), currentSection.getName() + " " + context.getString(R.string.exported), Toast.LENGTH_SHORT).show();
    }


    /**
     * This method takes text from clipboard and gives it to RulesParser which then converts it into section.
     * @param context used to get string resources and for clipboard manager
     */
    public void importCurrentSectionFromClipboard(Context context) {
        // copy from clipboard
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        CharSequence text = null;
        try {
            text = clipboard.getPrimaryClip().getItemAt(0).getText();
        } catch (Exception e) {
            // wasn't able to copy text from import
            Toast.makeText(context.getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
            return;
        }

        RulesParser rulesParser = new RulesParser(this);
        rulesParser.parse(text.toString());

        for (RulesModelListener listener : listeners) {
            listener.onRuleAdded(null);
        }

        // make toast about this
        Toast.makeText(context.getApplicationContext(), currentSection.getName() + " " + context.getString(R.string.imported), Toast.LENGTH_SHORT).show();
    }


    /**
     * @return rules from selected section
     */
    public ArrayList<Rule> getRules() {
        return currentSection.getRules();
    }


    public ArrayList<Section> getSections() {
        return sections;
    }


    public void addListener(RulesModelListener rulesModelListener) {
        ListIterator<RulesModelListener> iterator = listeners.listIterator();
        iterator.add(rulesModelListener);
    }


    /**
     * Selects section by id.
     * @param id identifies selected section
     */
    public void selectSection(int id) {
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).getId() != id) continue;
            currentSection = sections.get(i);
            currentSection.sortRules();
            break;
        }
    }


    /**
     * Used for debug purposes.
     */
    public void showModelContentsInConsole() {
        Log.d("yiotro", " = MODEL CONTENTS = ");
        for (Section section : sections) {
            Log.d("yiotro", "section: " + section);
        }
        Log.d("yiotro", "---------");
        for (Section section : sections) {
            Log.d("yiotro", "------------ " + section);
            for (Rule rule : section.getRules()) {
                Log.d("yiotro", "rule: " + rule);
            }
        }
    }


    /**
     * Used for debug purposes.
     */
    public void showDatabaseInConsole() {
        Log.d("yiotro", " = DATABASE = ");
        for (Section section : databaseHandler.getSections()) {
            Log.d("yiotro", "section: " + section);
        }
        Log.d("yiotro", "=====");
        for (Section section : databaseHandler.getSections()) {
            Log.d("yiotro", "= table: " + section);
            for (Rule rule : databaseHandler.getRules(section)) {
                Log.d("yiotro", "rule: " + rule);
            }
        }
    }


    public Section getCurrentSection() {
        return currentSection;
    }


    public void clearListeners() {
        listeners.clear();
    }
}
