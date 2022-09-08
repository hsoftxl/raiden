package org.sean;

import android.app.Application;
import android.content.Context;

import org.sean.google.admob.GAD;
import org.sean.google.play.pay.PayUtil;


/**
 * Created by Administrator on 2015/10/21.
 */
public class BaseApplication extends Application {

    private static Context context;
    private static Application application;

    public static Context getContext() {
        return context;
    }

    public static Application getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        application = this;
        GAD.init(this);
        PayUtil.init();
    }
}
