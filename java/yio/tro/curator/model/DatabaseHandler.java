package yio.tro.curator.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 12;
    private static final String DATABASE_NAME = "curator.db";
    public static final String TABLE_SECTIONS = "sections";
    public static final String TABLE_RULES = "rules";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SECTION_NAME = "_name";
    public static final String COLUMN_RULE_TITLE = "_title";
    public static final String COLUMN_RULE_TEXT = "_text";


    public DatabaseHandler(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }


    public void addSection(Section section) {
        Log.d("yiotro", "add section: " + section);

        // add new section to sections table
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, section.getId());
        values.put(COLUMN_SECTION_NAME, section.getName());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_SECTIONS, null, values);

        // create new rules table
        String queryNewTable = "create table " + TABLE_RULES + section.getId() + "(" +
                COLUMN_ID + " integer primary key autoincrement," +
                COLUMN_RULE_TITLE + " text," +
                COLUMN_RULE_TEXT + " text" +
                ");";
        db.execSQL(queryNewTable);

        db.close();
    }


    public void deleteSection(Section section) {
        Log.d("yiotro", "delete section: " + section);

        //drop rules table
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("drop table if exists " + TABLE_RULES + section.getId());

        // delete section from sections table
        db.execSQL("delete from " + TABLE_SECTIONS + " where " + COLUMN_ID + "=\"" + section.getId() + "\";");

        db.close();
    }


    public void addRule(Section section, Rule rule) {
        Log.d("yiotro", "add rule: " + rule + " in " + section);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, rule.getId());
        values.put(COLUMN_RULE_TITLE, rule.getTitle());
        values.put(COLUMN_RULE_TEXT, rule.getText());
        db.insert(TABLE_RULES + section.getId(), null, values);

        db.close();
    }


    public void editRule(Section section, Rule rule) {
        Log.d("yiotro", "edit rule: " + rule + " in " + section);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RULE_TITLE, rule.getTitle());
        values.put(COLUMN_RULE_TEXT, rule.getText());
        db.update(TABLE_RULES + section.getId(), values, COLUMN_ID + "=?", new String[]{rule.getId() + ""});

        db.close();
    }


    public void deleteRule(Section section, Rule rule) {
        Log.d("yiotro", "delete rule: " + rule + " in " + section);

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from " + TABLE_RULES + section.getId() + " where " + COLUMN_ID + "=\"" + rule.getId() + "\";");

        db.close();
    }


    public ArrayList<Section> getSections() {
        ArrayList<Section> result = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "select * from " + TABLE_SECTIONS;

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Section section = new Section(c.getInt(c.getColumnIndex(COLUMN_ID)));
            section.setName(c.getString(c.getColumnIndex(COLUMN_SECTION_NAME)));
            result.add(section);
            c.moveToNext();
        }
        c.close();

        return result;
    }


    public ArrayList<Rule> getRules(Section section) {
        ArrayList<Rule> result = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        String query = "select * from " + TABLE_RULES + section.getId();

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Rule rule = new Rule(c.getInt(c.getColumnIndex(COLUMN_ID)));
            rule.setTitle(c.getString(c.getColumnIndex(COLUMN_RULE_TITLE)));
            rule.setText(c.getString(c.getColumnIndex(COLUMN_RULE_TEXT)));
            result.add(rule);
            c.moveToNext();
        }
        c.close();

        return result;

    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("yiotro", "on create");

        String queryCreateSections = "create table " + TABLE_SECTIONS + "(" +
                COLUMN_ID + " integer primary key autoincrement," +
                COLUMN_SECTION_NAME + " text" +
                ");";
        db.execSQL(queryCreateSections);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("yiotro", "on upgrade");

        // drop all rules tables
        String query = "select * from " + TABLE_SECTIONS;
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            Section section = new Section(c.getInt(c.getColumnIndex(COLUMN_ID)));
            Log.d("yiotro", "dropping " + TABLE_RULES + section.getId());
            db.execSQL("drop table if exists " + TABLE_RULES + section.getId());
            c.moveToNext();
        }
        c.close();

        db.execSQL("drop table if exists " + TABLE_SECTIONS);
        onCreate(db);
    }
}
