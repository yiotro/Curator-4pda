package yio.tro.curator.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import yio.tro.curator.R;
import yio.tro.curator.controller.RulesControllerImpl;
import yio.tro.curator.model.Rule;

public class EditRuleActivity extends AppCompatActivity{

    Rule rule;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_rule);

        initRule();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }


    private void initRule() {
        rule = (Rule) getIntent().getSerializableExtra("rule");

        EditText editTitle = (EditText) findViewById(R.id.add_rule_edit_title);
        editTitle.setText(rule.getTitle());

        EditText editText = (EditText) findViewById(R.id.add_rule_edit_text);
        editText.setText(rule.getText());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.action_toolbar_edit_rule) {
            editRule();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    void editRule() {
        EditText editTitle = (EditText) findViewById(R.id.add_rule_edit_title);
        String title = editTitle.getText().toString();
        rule.setTitle(title);

        EditText editText = (EditText) findViewById(R.id.add_rule_edit_text);
        String text = editText.getText().toString();
        rule.setText(text);

        RulesControllerImpl.getInstance().editRule(rule);

        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_edit_rule, menu);
        return true;
    }
}
