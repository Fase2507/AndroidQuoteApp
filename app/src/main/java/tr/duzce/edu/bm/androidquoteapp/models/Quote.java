package tr.duzce.edu.bm.androidquoteapp.models;

import com.google.gson.annotations.SerializedName;

public class Quote {
    @SerializedName("q")
    private String text;

    @SerializedName("a")
    private String author;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
