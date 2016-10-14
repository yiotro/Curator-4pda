package yio.tro.curator.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import yio.tro.curator.AboutActivity;
import yio.tro.curator.R;
import yio.tro.curator.SettingsActivity;
import yio.tro.curator.controller.RulesControllerImpl;
import yio.tro.curator.model.Rule;
import yio.tro.curator.model.RulesModel;
import yio.tro.curator.model.Section;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    ArrayAdapter<Rule> listAdapter;
    RulesModel rulesModel;
    MenuItem lastDrawerItem;
    DrawerLayout drawer;
    ListView listView;
    int lastListLookIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initRules();

        initNavigationView(toolbar);

        initRulesList();
        getSupportActionBar().setTitle("");
    }


    /**
     * This method sets up rules controller and model.
     */
    private void initRules() {
        RulesControllerImpl.getInstance().setMainActivity(this);
        RulesControllerImpl.getInstance().loadFromDatabase();
        rulesModel = RulesModel.getInstance();
    }


    /**
     * This method sets up list view of rules.
     */
    public void initRulesList() {
        listView = (ListView) findViewById(R.id.main_list_view);
        initAdapter();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RulesControllerImpl.getInstance().onListItemClicked(position);
            }
        });

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new RuleMultiChoice(listView, R.menu.context_rule_menu, this));
    }


    /**
     * This method sets up list view adapter.
     * List view look can be changed in settings.
     * So this method can be called several times.
     */
    private void initAdapter() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String listLook = defaultSharedPreferences.getString("general_pref_list_look", "1");
        int lookIndex = Integer.valueOf(listLook);
        switch (lookIndex) {
            case 1: // default
                listAdapter = new DefaultListAdapter(this, rulesModel.getRules());
                break;
            case 2: // compact
                listAdapter = new CompactListAdapter(this, rulesModel.getRules());
                break;
        }

        lastListLookIndex = lookIndex;
        listView.setAdapter(listAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkToReInitAdapter();
    }


    /**
     * Checks if settings were changed and list view has to be recreated.
     */
    private void checkToReInitAdapter() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String listLook = defaultSharedPreferences.getString("general_pref_list_look", "1");
        int lookIndex = Integer.valueOf(listLook);

        if (lookIndex == lastListLookIndex) return;

        initAdapter();
    }


    /**
     * This method is called when section is added or deleted.
     */
    public void refreshDrawer() {
        if (navigationView == null) return;

        Menu drawerMenu = navigationView.getMenu();

        drawerMenu.clear();
        ArrayList<Section> sections = rulesModel.getSections();
        for (int i = 0; i < sections.size(); i++) {
            Section section = sections.get(i);
            drawerMenu.add(R.id.drawer_group, section.getId(), i, section.getName());
        }
    }


    public void refreshRulesList() {
        Log.d("yiotro", "refreshing rules");
        listAdapter.notifyDataSetChanged();
    }


    /**
     * This method sets up navigation view.
     * @param toolbar
     */
    private void initNavigationView(Toolbar toolbar) {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        refreshDrawer();

        lastDrawerItem = navigationView.getMenu().getItem(0);
        lastDrawerItem.setCheckable(true);
        lastDrawerItem.setChecked(true);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return true;
    }


    /**
     * This is called when user chooses an item in action bar menu.
     * @param item pressed item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_section:
                onActionAddSection();
                return true;
            case R.id.action_delete_section:
                onActionDeleteSection();
                return true;
            case R.id.action_add_rule:
                onActionAddRule();
                return true;
            case R.id.action_export_import:
                onActionExportImport();
                return true;
            case R.id.action_about:
                onActionAbout();
                return true;
            case R.id.action_settings:
                onActionSettings();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Shows a dialog to check if user really wants to delete section.
     */
    void onActionDeleteSection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.deletion);
        builder.setMessage(R.string.delete_question);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                RulesControllerImpl.getInstance().deleteSection();
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


    private void onActionSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    private void onActionAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }


    private void onActionAddRule() {
        Intent intent = new Intent(this, AddRuleActivity.class);
        startActivity(intent);
    }


    private void onActionExportImport() {
        Intent intent = new Intent(this, ExportImportActivity.class);
        startActivity(intent);
    }


    private void onActionAddSection() {
        Intent intent = new Intent(this, AddSectionActivity.class);
        startActivity(intent);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        selectSection(id);

        closeNavigationDrawer();
        return true;
    }


    public void selectSection(int id) {
        if (navigationView == null) return;

        MenuItem item = navigationView.getMenu().getItem(id);

        if (lastDrawerItem != null) {
            lastDrawerItem.setChecked(false);
        }

        item.setCheckable(true);
        item.setChecked(true);

        lastDrawerItem = item;

        RulesControllerImpl.getInstance().selectSection(id);
    }


    private void closeNavigationDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


    public void onLogButtonClick(View view) {
        rulesModel.showModelContentsInConsole();
    }
}
