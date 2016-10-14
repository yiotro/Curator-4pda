package yio.tro.curator.view;

import android.support.v4.widget.DrawerLayout;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import yio.tro.curator.R;
import yio.tro.curator.controller.RulesController;
import yio.tro.curator.controller.RulesControllerImpl;
import yio.tro.curator.model.Rule;

import java.util.ArrayList;

public class RuleMultiChoice extends MultiChoiceYio{

    MainActivity mainActivity;
    ArrayList<Rule> selectedRules;
    RulesController rulesController;


    public RuleMultiChoice(AbsListView listView, int menu_id, MainActivity mainActivity) {
        super(listView, menu_id);
        this.mainActivity = mainActivity;
        rulesController = RulesControllerImpl.getInstance();
    }


    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        mainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        return super.onCreateActionMode(actionMode, menu);
    }


    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        super.onDestroyActionMode(actionMode);
        mainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }


    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        createSelectedRules();

        if (menuItem.getItemId() == R.id.cab_rule_delete) {
            rulesController.deleteRules(selectedRules);
        }

        if (menuItem.getItemId() == R.id.cab_rule_edit) {
            rulesController.editRules(selectedRules);
        }

        actionMode.finish();

        return false;
    }


    private void createSelectedRules() {
        selectedRules = new ArrayList<Rule>();

        SparseBooleanArray sparseBooleanArray = listView.getCheckedItemPositions();
        for (int i = 0; i < sparseBooleanArray.size(); i++) {
            if (sparseBooleanArray.valueAt(i)) {
                Rule rule = (Rule) listView.getItemAtPosition(sparseBooleanArray.keyAt(i));
                selectedRules.add(rule);
            }
        }
    }
}

