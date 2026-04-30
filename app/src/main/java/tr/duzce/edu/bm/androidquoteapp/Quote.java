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

    //getter & setters
    public String getText() {
        return text;
    }
    public void setText(String quoteText) { this.text = quoteText; }
    public String getAuthor() {
        return author;
    }
}
