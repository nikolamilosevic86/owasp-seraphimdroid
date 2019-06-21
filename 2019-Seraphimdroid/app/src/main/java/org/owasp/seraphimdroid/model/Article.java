package org.owasp.seraphimdroid.model;

import java.util.ArrayList;

/**
 * Created by addiittya on 04/05/16.
 */

public class Article {
    private String id;
    private String title;
    private String text;
    private String category;
    private String cachefile;
    private ArrayList<String> tags;

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCachefile() {
        return cachefile;
    }

    public void setCachefile(String cachefile) {
        this.cachefile = cachefile;
    }

    public Article() {}

    public Article(String id, String title, String text, String category, ArrayList<String> tags) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.category = category;
        this.tags = tags;
    }
}