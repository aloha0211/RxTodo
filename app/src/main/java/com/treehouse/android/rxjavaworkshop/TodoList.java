package com.treehouse.android.rxjavaworkshop;

import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;

public class TodoList implements Action1<Todo> {

    private ReplaySubject<TodoList> notifier = ReplaySubject.create();
    private List<Todo> todoList;

    public TodoList() {
        todoList = new ArrayList<>();
    }

    public TodoList(String json) {
        this();
        readJson(json);
    }

    public List<Todo> getAll() {
        return todoList;
    }

    public List<Todo> getIncomplete() {
        List<Todo> incompleteOnly = new ArrayList<>();
        for (int i = 0; i < todoList.size(); i++) {
            Todo item = todoList.get(i);
            if (!item.isCompleted) {
                incompleteOnly.add(item);
            }
        }
        return incompleteOnly;
    }

    public List<Todo> getComplete() {
        List<Todo> completeOnly = new ArrayList<>();
        for (int i = 0; i < todoList.size(); i++) {
            Todo item = todoList.get(i);
            if (item.isCompleted) {
                completeOnly.add(item);
            }
        }
        return completeOnly;
    }

    public int size() {
        return todoList.size();
    }

    public Todo get(int i) {
        return todoList.get(i);
    }

    public void add(Todo t) {
        todoList.add(t);
        notifier.onNext(this);
    }

    public void remove(Todo t) {
        todoList.remove(t);
        notifier.onNext(this);
    }

    public void toggle(Todo t) {
        Todo todo = todoList.get(todoList.indexOf(t));
        boolean curVal = todo.isCompleted;
        todo.isCompleted = !curVal;
        notifier.onNext(this);
    }

    @Override
    public void call(Todo todo) {
        Todo t = todoList.get(todoList.indexOf(todo));
        boolean curVal = t.isCompleted;
        t.isCompleted = !curVal;
        notifier.onNext(this);
    }

    public Observable<TodoList> asObservable() {
        return notifier;
    }

    private void readJson(String json) {

        if (json == null || TextUtils.isEmpty(json.trim())) {
            return;
        }

        JsonReader reader = new JsonReader(new StringReader(json));

        try {
            reader.beginArray();

            while (reader.peek().equals(JsonToken.BEGIN_OBJECT)) {
                reader.beginObject();

                String nameDesc = reader.nextName();
                if (!"description".equals(nameDesc)) {
                    Log.w(TodoList.class.getName(), "Expected 'description' but was " + nameDesc);
                }
                String description = reader.nextString();

                String nameComplete = reader.nextName();
                if (!"is_completed".equals(nameComplete)) {
                    Log.w(TodoList.class.getName(), "Expected 'is_completed' but was " + nameComplete);
                }
                boolean isComplete = reader.nextBoolean();

                todoList.add(new Todo(description, isComplete));

                reader.endObject();
            }

            reader.endArray();
        } catch (IOException e) {

        }

    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.beginArray();

            for (Todo t : todoList) {
                writer.beginObject();
                writer.name("description");
                writer.value(t.description);
                writer.name("is_completed");
                writer.value(t.isCompleted);
                writer.endObject();
            }

            writer.endArray();
            writer.close();
        } catch (IOException e) {
            Log.i(TodoList.class.getName(), "Exception writing JSON " + e.getMessage());
        }


        String json = new String(out.toByteArray());

        return json;
    }
}
