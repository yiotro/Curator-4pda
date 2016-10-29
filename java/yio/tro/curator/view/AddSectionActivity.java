package yio.tro.curator.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import yio.tro.curator.R;
import yio.tro.curator.controller.RulesControllerImpl;

public class AddSectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_section);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.action_toolbar_add_section) {
            createSection();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_add_section, menu);
        return true;
    }


    public void createSection() {
        EditText editName = (EditText) findViewById(R.id.add_section_edit_name);
        String name = editName.getEditableText().toString();

        EditText editPhrase = (EditText) findViewById(R.id.add_section_edit_phrase);
        String phrase = editPhrase.getText().toString();

        RulesControllerImpl.getInstance().addSection(name, phrase);

        finish();
    }
}
