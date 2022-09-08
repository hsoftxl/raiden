package org.sean.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import org.sean.BaseApplication;


public class AppUtil {
    private static final int WHAT_SCHEDULE_FULL_SYNC = 0;
    private static final int WHAT_TOAST = 1;

    private static Application application = BaseApplication.getApplication();

    static Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case WHAT_SCHEDULE_FULL_SYNC:
                    break;
                case WHAT_TOAST:
                    Toast.makeText(application, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public static void showToast(String msg) {
        if (application != null) {
            Message message = new Message();
            message.what = WHAT_TOAST;
            message.obj = msg;
            handler.sendMessage(message);
        }
    }

    public static void runScheduleFullSync() {
        runUiThread(WHAT_SCHEDULE_FULL_SYNC);
    }

    private static void runUiThread(int what) {
        Message msg = new Message();
        msg.what = what;
        handler.sendMessage(msg);
    }

    /**
     * 跳转到应用商店评分
     *
     * @param context
     */
    public static void goAppShop(Context context) {
        try {
            Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            // Do nothing
        }
    }

    public static void rateInApp(Activity activity) {
        ReviewManager manager = ReviewManagerFactory.create(activity);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(requestTask -> {
            if (requestTask.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = requestTask.getResult();
                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                flow.addOnCompleteListener(task -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            } else {
                // There was some problem, log or handle the error code.
                try {
                    requestTask.getException().printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void shareApp(Context context, String title, String text) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.setType("text/plain");
            context.startActivity(Intent.createChooser(intent, title));
        } catch (Exception e) {
            // Do nothing
        }
    }
}
