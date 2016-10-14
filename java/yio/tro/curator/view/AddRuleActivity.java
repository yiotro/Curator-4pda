package yio.tro.curator.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import yio.tro.curator.R;
import yio.tro.curator.controller.RulesControllerImpl;

public class AddRuleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rule);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.action_toolbar_add_rule) {
            createRule();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    void createRule() {
        EditText editTitle = (EditText) findViewById(R.id.add_rule_edit_title);
        String title = editTitle.getText().toString();

        EditText editText = (EditText) findViewById(R.id.add_rule_edit_text);
        String text = editText.getText().toString();

        RulesControllerImpl.getInstance().addRule(title, text);

        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_add_rule, menu);
        return true;
    }
}
