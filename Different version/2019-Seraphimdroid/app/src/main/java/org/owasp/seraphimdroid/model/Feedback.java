package org.owasp.seraphimdroid.model;

/**
 * Created by addiittya on 30/06/16.
 */
public class Feedback {

    private String title;
    private String description;
    private int upvotes;

    public Feedback() {
    }

    public Feedback(String title, String description, int upvotes) {
        this.title = title;
        this.description = description;
        this.upvotes = upvotes;

    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
