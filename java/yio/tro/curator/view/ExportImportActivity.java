package yio.tro.curator.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import yio.tro.curator.R;
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
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void OnExportButtonClick(View view) {
        RulesModel rulesModel = RulesModel.getInstance();
        rulesModel.exportCurrentSectionToClipboard(this);

        finish();
    }


    public void onImportButtonClick(View view) {
        RulesModel rulesModel = RulesModel.getInstance();
        rulesModel.importCurrentSectionFromClipboard(this);

        finish();
    }
}
