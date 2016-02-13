package ru.thesn.app;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONAdapter {

    private Object object;

    public JSONAdapter(Object object) {
        this.object = object;
    }

    public JSONAdapter fromObject(Object key) {
        return new JSONAdapter(getObject().get(key + ""));
    }

    public JSONAdapter fromArray(int index) {
        return new JSONAdapter(getArray().get(index));
    }

    public JSONObject getObject()  {

        return (JSONObject)object;
    }

    public JSONArray getArray() {
        return (JSONArray)object;
    }

    public String getString(Object key) throws JSONException {
        if (object == null) throw new JSONException();
        return (String)getObject().get(key);
    }

    public Long getLong(Object key) throws JSONException {
        if (object == null) throw new JSONException();
        return (Long)((JSONObject)object).get(key);
    }

    public Integer getInt(Object key) throws JSONException {
        Long lng = getLong(key);
        return lng != null ? getLong(key).intValue() : null;
    }
}
