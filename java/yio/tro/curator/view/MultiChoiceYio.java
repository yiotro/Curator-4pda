package yio.tro.curator.view;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.AbsListView;

/**
 * Used for context actions with rules.
 */
public abstract class MultiChoiceYio implements AbsListView.MultiChoiceModeListener {

    protected AbsListView listView;
    int menu_id;


    public MultiChoiceYio(AbsListView listView, int menu_id) {
        this.listView = listView;
        this.menu_id = menu_id;
    }


    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
        int selectedCount = listView.getCheckedItemCount();
        setSubtitle(actionMode, selectedCount);
    }


    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(menu_id, menu);
        return true;
    }


    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }


    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
    }


    private void setSubtitle(ActionMode mode, int selectedCount) {
        switch (selectedCount) {
            case 0:
                mode.setSubtitle(null);
                break;
            default:
                mode.setTitle(String.valueOf(selectedCount));
                break;
        }
    }
}

