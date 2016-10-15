package yio.tro.curator.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import yio.tro.curator.R;
import yio.tro.curator.controller.RulesController;
import yio.tro.curator.controller.RulesControllerImpl;
import yio.tro.curator.model.RulesModel;

public class ExportImportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_import);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_toolbar_export_base:
                RulesControllerImpl.getInstance().exportFullBase(this);
                finish();
                return true;
            case R.id.action_toolbar_import_base:
                RulesControllerImpl.getInstance().importFullBase(this);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void OnExportButtonClick(View view) {
        RulesControllerImpl.getInstance().exportSection(this);

        finish();
    }


    public void onImportButtonClick(View view) {
        RulesControllerImpl.getInstance().importSection(this);

        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_export_import, menu);
        return true;
    }
}
