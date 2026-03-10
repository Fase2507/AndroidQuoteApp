package tr.duzce.edu.bm.androidquoteapp;

import com.google.gson.annotations.SerializedName;

public class Quote {
    @SerializedName("q")
    private String text;
    
    @SerializedName("a")
    private String author;

    public Quote(String text, String author) {
        this.text = text;
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }
}
