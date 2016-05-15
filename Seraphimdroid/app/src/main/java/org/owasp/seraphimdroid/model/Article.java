package org.owasp.seraphimdroid.model;

/**
 * Created by addiittya on 04/05/16.
 */

public class Article {
    private String id;
    private String title;
    private String text;
    private String category;

    // TODO Implement Image Upload and add to Articles Model

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


}