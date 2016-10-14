package yio.tro.curator.controller;

import android.content.*;
import android.widget.Toast;
import yio.tro.curator.R;
import yio.tro.curator.model.Rule;
import yio.tro.curator.model.RulesModel;
import yio.tro.curator.model.RulesModelListener;
import yio.tro.curator.model.Section;
import yio.tro.curator.view.EditRuleActivity;
import yio.tro.curator.view.MainActivity;

import java.util.ArrayList;

public class RulesControllerImpl implements RulesController {

    private static RulesControllerImpl instance;
    RulesModel rulesModel; // model
    MainActivity mainActivity;


    public static RulesController getInstance() {
        if (instance == null) {
            instance = new RulesControllerImpl();
        }

        return instance;
    }


    private RulesControllerImpl() {
        RulesModel.getInstance().addListener(this);
        rulesModel = RulesModel.getInstance();
    }


    @Override
    public void addSection(String name) {
        rulesModel.addSection(name);
    }


    @Override
    public void deleteSection() {
        rulesModel.deleteSection();
    }


    @Override
    public void addRule(String title, String text) {
        rulesModel.addRule(title, text);
    }


    @Override
    public void editRule(Rule rule) {
        rulesModel.editRule(rule);
    }


    @Override
    public void deleteRules(ArrayList<Rule> rules) {
        rulesModel.deleteRules(rules);
    }


    @Override
    public void editRules(ArrayList<Rule> rules) {
        if (rules.size() != 1) return;

        Intent intent = new Intent(mainActivity, EditRuleActivity.class);
        intent.putExtra("rule", rules.get(0));
        mainActivity.startActivity(intent);
    }


    @Override
    public void onListItemClicked(int position) {
        Rule rule = rulesModel.getRules().get(position);
        rulesModel.copyToClipboard(mainActivity, rule);
    }


    @Override
    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }


    @Override
    public void loadFromDatabase() {
        rulesModel.loadFromDatabase(mainActivity);
    }


    @Override
    public void selectSection(int id) {
        rulesModel.selectSection(id);
        mainActivity.initRulesList();
    }


    @Override
    public void onSectionAdded(Section section) {
        mainActivity.refreshDrawer();
        mainActivity.selectSection(section.getId());
    }


    @Override
    public void onRuleAdded(Rule rule) {
        mainActivity.refreshRulesList();
    }


    @Override
    public void onSectionDeleted(Section section) {
        mainActivity.refreshDrawer();
        mainActivity.selectSection(0);
    }


    @Override
    public void onUnableToDeleteSection() {
        Toast.makeText(mainActivity.getApplicationContext(), R.string.can_not_delete_section, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRulesDeleted() {
        mainActivity.refreshRulesList();
    }


    @Override
    public void onSectionChanged(Section section) {

    }


    @Override
    public void onRuleChanged(Rule rule) {
        mainActivity.refreshRulesList();
    }
}
