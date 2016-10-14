package yio.tro.curator.controller;

import yio.tro.curator.model.Rule;
import yio.tro.curator.model.RulesModelListener;
import yio.tro.curator.view.MainActivity;

import java.util.ArrayList;

public interface RulesController extends RulesModelListener {


    void addSection(String name);


    void deleteSection();


    void addRule(String title, String text);


    void editRule(Rule rule);


    void deleteRules(ArrayList<Rule> rules);


    void editRules(ArrayList<Rule> rules);


    void onListItemClicked(int position);


    void setMainActivity(MainActivity mainActivity);


    void loadFromDatabase();


    void selectSection(int id);
}
