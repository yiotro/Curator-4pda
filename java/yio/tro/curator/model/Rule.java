package yio.tro.curator.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Rule implements Serializable, Comparable<Rule>{

    private String title;
    private String text;
    private int id;


    public Rule(int id) {
        this.id = id;
    }


    public void set(Rule src) {
        id = src.id;
        title = src.title;
        text = src.text;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }


    public int getId() {
        return id;
    }


    @Override
    public String toString() {
        return "[" + id + ": " + title + "]";
    }


    @Override
    public int compareTo(@NonNull Rule anotherRule) {
        return title.compareToIgnoreCase(anotherRule.getTitle());
    }
}
