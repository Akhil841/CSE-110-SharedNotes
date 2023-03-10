package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;

import okhttp3.Headers;
import okhttp3.MediaType;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: - getNote (maybe getNoteAsync)
    // TODO: - putNote (don't need putNotAsync, probably)
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    public static final String SERVERURL = "https://sharednotes.goto.ucsd.edu/notes/";

    public NoteAPI() {
        this.client = new OkHttpClient();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     *
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
    @WorkerThread
    public String echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
            return body;
        } catch (Exception e) {
            Log.e("IT DIDN'T WORK", "L");
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Get function
     */

    public Note get(String title){
        // URLs cannot contain spaces, so we replace them with %20.
        String titlePath = title.replace(" ", "%20");

        Request request = new Request.Builder()
                .url(SERVERURL + titlePath)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            return Note.fromJSON(response.body().string());
        }
        catch(Exception e){
            Log.e("Error getting note", e.toString());
        }

        return null;

    }



    public Future<Note> getAsync(String title) {

        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> get(title));

        return future;

    }

    /**
     * Put function
     */

    private int put(Note note) {
        note.title = note.title.replace(" ", "%20");
        RequestBody body = RequestBody.create(TLNote.createNewFromNote(note).toJSON(),JSON);
        Request request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/notes/" + note.title)
                .put(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            Log.d("PUT REQUEST", "HAHAHAHA");
            return response.code();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    public Future<Integer> putAsync(Note note)
    {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> put(note));

        return future;
    }

    public void upsert(Note note){
        Future<Integer> t = putAsync(note);
        while(!t.isDone());
    }






    @AnyThread
    public Future<String> echoAsync(String msg) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> echo(msg));

        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }
}