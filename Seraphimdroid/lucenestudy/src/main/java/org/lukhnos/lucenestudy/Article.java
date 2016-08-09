package org.lukhnos.lucenestudy;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by addiittya on 04/05/16.
 */

public class Article {
    public String id;
    public String title;
    public String text;
    public String category;
    public String cachefile;
    public ArrayList<String> tags;

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

    public Article(String id, String title, String text, String category, String tags) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.category = category;
        this.setTags(new ArrayList<>(Arrays.asList(tags.split("\\s*,\\s*"))));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(")
                .append(title)
                .append(", ")
                .append(text)
                .append(", ")
                .append(category)
                .append(", ")
                .append(tags.toString())
                .append(")");
        return builder.toString();
    }

}