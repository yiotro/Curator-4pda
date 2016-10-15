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
import java.util.StringTokenizer;

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
            rulesParser.parseSection(context.getResources().getString(R.string.default_rules));
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
    public Rule addRule(String title, String text, String tag) {
        if (title.length() == 0) return null;

        Rule rule = new Rule(currentSection.getIdForNewRule());
        rule.setTitle(title);
        rule.setText(text);
        rule.setTag(tag);

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
    public void copyRuleToClipboard(Context context, Rule rule) {
        // get touched rule
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // string builder for clipboard
        StringBuilder stringBuilder = new StringBuilder();

        beginBuildClipData(context, defaultSharedPreferences, stringBuilder, rule.getTag());
        stringBuilder.append(rule.getText());
        endBuildClipData(context, defaultSharedPreferences, stringBuilder, rule.getTag());

        // copy to clipboard
        copyToClipboard(context, stringBuilder.toString());

        // make toast about this
        showToast(context, rule.getTitle());
    }


    private void endBuildClipData(Context context, SharedPreferences defaultSharedPreferences, StringBuilder stringBuilder, String customTag) {
        stringBuilder.append("[/size]").append("\n");

        // add ending phrase
        String endingPhrase = defaultSharedPreferences.getString("general_pref_ending_phrase", context.getResources().getString(R.string.default_ending_phrase));
        if (endingPhrase.length() > 0) {
            stringBuilder.append(endingPhrase).append("\n");
        }

        // final tag
        String tag = defaultSharedPreferences.getString("general_pref_tag", context.getResources().getString(R.string.default_tag));
        if (customTag != null && customTag.length() > 0) {
            tag = customTag;
        }
        if (tag.length() > 0) {
            stringBuilder.append("[/").append(tag).append("]\n");
        }
    }


    private void beginBuildClipData(Context context, SharedPreferences defaultSharedPreferences, StringBuilder stringBuilder, String customTag) {
        String tag = defaultSharedPreferences.getString("general_pref_tag", context.getResources().getString(R.string.default_tag));
        if (customTag != null && customTag.length() > 0) {
            tag = customTag;
        }
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

        beginBuildClipData(context, defaultSharedPreferences, stringBuilder, null);

        for (Rule rule : rules) {
            stringBuilder.append(rule.getText()).append("\n");
        }

        endBuildClipData(context, defaultSharedPreferences, stringBuilder, null);

        // copy to clipboard
        copyToClipboard(context, stringBuilder.toString());

        // make toast about this
        StringBuilder titleBuilder = new StringBuilder();
        for (Rule rule : rules) {
            titleBuilder.append(rule.getTitle()).append("\n");
        }
        showToast(context, titleBuilder.toString());
    }


    /**
     * This method converts current section to text and places it to clipboard.
     * @param context used to get string resources
     */
    public void exportCurrentSectionToClipboard(Context context) {
        String convertedSection = convertSectionToText(currentSection);

        // copy to clipboard
        copyToClipboard(context, convertedSection);

        // make toast about this
        String toastMessage = currentSection.getName() + " " + context.getString(R.string.exported);
        showToast(context, toastMessage);
    }


    public void showToast(Context context, String toastMessage) {
        Toast.makeText(context.getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }


    private void copyToClipboard(Context context, String message) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("cur", message);
        clipboard.setPrimaryClip(clip);
    }


    /**
     * This method just converts section to text. Used for export.
     * @param section source for conversion
     * @return converted to text section
     */
    private String convertSectionToText(Section section) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("[formatted]\n");

        section.sortRules();
        for (Rule rule : section.getRules()) {
            stringBuilder.append("-").append(rule.getTitle()).append("\n");
            if (rule.hasTag()) {
                stringBuilder.append("tag ").append(rule.getTag()).append("\n");
            }
            stringBuilder.append(rule.getText()).append("\n");
        }

        return stringBuilder.toString();
    }


    /**
     * This method takes text from clipboard and gives it to RulesParser which then converts it into section.
     * @param context used to get string resources and for clipboard manager
     */
    public void importCurrentSectionFromClipboard(Context context) {
        String text = getTextFromClipboard(context);
        if (text == null) return;

        RulesParser rulesParser = new RulesParser(this);
        rulesParser.parseSection(text);

        for (RulesModelListener listener : listeners) {
            listener.onRuleAdded(null);
        }

        // make toast about this
        showToast(context, currentSection.getName() + " " + context.getString(R.string.imported));
    }


    /**
     * This method just achieves string from clipboard if possible.
     * @param context used to get clipboard
     * @return text from clipboard
     */
    private String getTextFromClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        CharSequence text;
        try {
            text = clipboard.getPrimaryClip().getItemAt(0).getText();
        } catch (Exception e) {
            // wasn't able to copy text from clipboard
            showToast(context, context.getString(R.string.cant_get_data_from_clipboard));
            return null;
        }

        return text.toString();
    }


    public void exportFullBase(Context context) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            stringBuilder.append("section ").append(section.getName()).append("\n");
            stringBuilder.append(convertSectionToText(section));
            if (i < sections.size() - 1) {
                stringBuilder.append("\n").append("---").append("\n");
            }
        }

        copyToClipboard(context, stringBuilder.toString());

        showToast(context, context.getString(R.string.exported_full_base));
    }


    public void importFullBase(Context context) {
        String text = getTextFromClipboard(context);
        if (text == null) return;

        RulesParser rulesParser = new RulesParser(this);
        rulesParser.parseFullBase(text);

        for (RulesModelListener listener : listeners) {
            listener.onRuleAdded(null);
        }

        showToast(context, context.getString(R.string.imported_full_base));
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


    public DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }


    public void clearListeners() {
        listeners.clear();
    }
}
