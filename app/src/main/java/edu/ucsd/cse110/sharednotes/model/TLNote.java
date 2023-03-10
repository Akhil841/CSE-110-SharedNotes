package edu.ucsd.cse110.sharednotes.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class TLNote {
    @SerializedName("content")
    @NonNull
    public String content;

    @SerializedName(value = "version")
    public long version = 0;

    public TLNote(String content, long version)
    {
        this.content = content;
        this.version = version;
    }

    public static TLNote createNewFromNote(Note note)
    {
        TLNote out = new TLNote("", 0);
        out.content = note.content;
        out.version = note.version;
        return out;
    }

    public static TLNote fromJSON(String json) {
        return new Gson().fromJson(json, TLNote.class);
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }
}
