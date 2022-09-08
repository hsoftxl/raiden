package org.sean.google.firebase;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.sean.BaseApplication;

import java.util.Map;

public class EventUtil {
    private static FirebaseAnalytics instance = getInstance(BaseApplication.getContext());

    public static FirebaseAnalytics getInstance(Context context) {
        return FirebaseAnalytics.getInstance(context);
    }

    public static void onEvent(String eventId, Map<String, String> map) {
        if (instance != null) {
            Bundle params = new Bundle();
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    params.putString(entry.getKey(), entry.getValue());
                }
            }
            instance.logEvent(eventId, params);
        }
    }

    public static void onEvent(String eventId) {
        if (instance != null) {
            Bundle params = new Bundle();
            instance.logEvent(eventId, params);
        }
    }
}
