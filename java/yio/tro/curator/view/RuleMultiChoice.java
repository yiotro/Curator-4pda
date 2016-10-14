package yio.tro.curator.view;

import android.content.DialogInterface;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
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

        switch (menuItem.getItemId()) {
            case R.id.cab_rule_delete:
                onCabDeleteRules();
                break;
            case R.id.cab_rule_edit:
                rulesController.editRules(selectedRules);
                break;
            case R.id.cab_rule_paste:
                rulesController.copyMultipleRulesToClipboard(selectedRules);
                break;
        }

        actionMode.finish();

        return false;
    }


    private void onCabDeleteRules() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);

        builder.setTitle(R.string.deletion);
        builder.setMessage(R.string.delete_rules_question);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                rulesController.deleteRules(selectedRules);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
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

