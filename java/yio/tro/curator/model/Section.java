package yio.tro.curator.model;

import java.util.ArrayList;
import java.util.Collections;

public class Section {


    private String name;
    private ArrayList<Rule> rules;
    private int id;


    public Section(int id) {
        this.id = id;
        rules = new ArrayList<>();
        name = "Error";
    }


    public int getId() {
        return id;
    }


    public ArrayList<Rule> getRules() {
        return rules;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    int getIdForNewRule() {
        return getMaxRuleId() + 1;
    }


    private int getMaxRuleId() {
        int maxId = -1;
        for (Rule rule : rules) {
            if (maxId == -1 || rule.getId() > maxId) {
                maxId = rule.getId();
            }
        }
        return maxId;
    }


    public void sortRules() {
        Collections.sort(rules);
    }


    @Override
    public String toString() {
        return "[" + id + ": " + name + "]";
    }
}
