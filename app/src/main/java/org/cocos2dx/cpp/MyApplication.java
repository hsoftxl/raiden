package org.cocos2dx.cpp;

import org.sean.BaseApplication;


public class MyApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Billing.createInstance(this).loadLibrary(this);

        Channel.createInstance(this);

    }

}
