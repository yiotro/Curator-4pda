package yio.tro.curator.model;

public interface RulesModelListener {

    void onSectionAdded(Section section);


    void onRuleAdded(Rule rule);


    void onSectionDeleted(Section section);


    void onUnableToDeleteSection();


    void onRulesDeleted();


    void onSectionChanged(Section section);


    void onRuleChanged(Rule rule);
}
