package com.example.android.eventtimerfinish;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class Utils {

    public static String serverUri;

    public static String clientId;
    public static String subscriptionTopic;

    public static String username;
    public static String key;

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
        } catch (JSONException error) {
            error.printStackTrace();
        }
    }
}
