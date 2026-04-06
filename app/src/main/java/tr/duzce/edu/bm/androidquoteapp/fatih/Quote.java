package tr.duzce.edu.bm.androidquoteapp.fatih;

import com.google.gson.annotations.SerializedName;

public class Quote {
    @SerializedName("q")
    private final String text;
    
    @SerializedName("a")
    private final String author;

    public Quote(String text, String author) {
        this.text = text;
        this.author = author;
    }

    //getter & setters
    public String getText() {
        return text;
    }
    public String getAuthor() {
        return author;
    }
}
