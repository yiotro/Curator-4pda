package yio.tro.curator.controller;

import android.content.Context;
import yio.tro.curator.model.Rule;
import yio.tro.curator.model.RulesModelListener;
import yio.tro.curator.view.MainActivity;

import java.util.ArrayList;

public interface RulesController extends RulesModelListener {


    void addSection(String name, String phrase);


    void deleteSection();


    void addRule(String title, String text, String tag);


    void editRule(Rule rule);


    void deleteRules(ArrayList<Rule> rules);


    void editRules(ArrayList<Rule> rules);


    void copyMultipleRulesToClipboard(ArrayList<Rule> rules);


    void exportFullBase(Context context);


    void importFullBase(Context context);


    void exportSection(Context context);


    void importSection(Context context);


    void onListItemClicked(int position);


    void setMainActivity(MainActivity mainActivity);


    void loadFromDatabase();


    void selectSection(int id);
}
