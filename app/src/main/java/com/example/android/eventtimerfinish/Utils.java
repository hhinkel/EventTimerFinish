package com.example.android.eventtimerfinish;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class Utils {

    public static String serverUri;

    public static String clientId;
    public static String subscriptionTopic;

    public static String username;
    public static String key;

    public static Division[] divisions;

    public void loadJSONSetupData(Context context) {
        String json = null;
        try {
            InputStream inputStream = context.getAssets().open("setup.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException error) {
            error.printStackTrace();
        }

        try {
            JSONObject jsonObject = new JSONObject(json);
            serverUri = jsonObject.getString("server");
            clientId = jsonObject.getString("clientId");
            subscriptionTopic = jsonObject.getString("subscriptionTopic");
            username = jsonObject.getString("username");
            key = jsonObject.getString("key");
            JSONArray divisionData = jsonObject.getJSONArray("divisions");
            createDivisionArrays(divisionData);
        } catch (JSONException error) {
            error.printStackTrace();
        }
    }

    private void createDivisionArrays(JSONArray data) {
        divisions = new Division[data.length()];
        for(int i = 0; i < data.length(); i++) {
            try {
                JSONObject object = data.getJSONObject(i);
                divisions[i] = new Division(object.getString("division"), object.getInt("fences"), object.getInt("riders"));
            } catch (JSONException error) {
                error.printStackTrace();
            }
        }
    }

    public Division[] getDivisions () { return divisions; }

    public String[] getDivisionNames(Context context, String division) {
        loadJSONSetupData(context);
        Division[] divisions = getDivisions();
        LinkedList<String> divisionIndex = new LinkedList<>();
        String[] divisionNames = new String[divisions.length];

        for (int i = 0; i < divisions.length; i++) {
            divisionNames[i] = divisions[i].getName();
            divisionIndex.add(divisionNames[i]);
        }

        return divisionNames;
    }
}
