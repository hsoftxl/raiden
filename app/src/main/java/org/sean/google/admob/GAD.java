package org.sean.google.admob;

import android.app.Activity;
import android.app.Application;

import com.app.skywar2021.BuildConfig;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.sean.BaseApplication;
import org.sean.util.DataStoreUtils;

public class GAD {
    public static String KEY_NO_AD = "noad";

    public static String UID_BANNER = BuildConfig.AD_BANNER;
    public static String UID_COVER = BuildConfig.AD_COVER;
    public static String UID_SPLASH = BuildConfig.AD_SPLASH;
    public static String UID_REWARD = BuildConfig.AD_REWARD;

    public static void init(Application application) {
        try {
            MobileAds.initialize(application, initializationStatus -> {
                if (!isNoAd()) {
                    AppOpenManager.init(application);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AdView createBanner(Activity act) {
        return ADBanner.createAdView(act);
    }

    public static void showbannerT(Activity ctx) {
        if (isNoAd()) {
            return;
        }
        ADBanner.addFloatingBanner(ctx, 1);
    }

    public static void showbannerB(Activity ctx) {
        if (isNoAd()) {
            return;
        }
        ADBanner.addFloatingBanner(ctx, 0);
    }

    public static void showCover(Activity activity) {
        if (isNoAd()) {
            return;
        }
        ADCover.show(activity, true);
    }

    public static void loadBanner(AdView adView) {
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device.
        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    public static void showReward(Activity activity) {
        showReward(activity, null);
    }

    public static void showReward(Activity activity, ADRewarded.Callback callback) {
        ADRewarded.show(activity, callback);
    }

    public static boolean hasRewardAd() {
        return ADRewarded.canShowAd();
    }

    public static boolean hasRewardAd(Activity activity, boolean preLoad) {
        boolean result = ADRewarded.canShowAd();
        if (!result && preLoad) {
            ADRewarded.load(activity);
        }
        return result;
    }


    public static boolean isNoAd() {
        return BuildConfig.isNoAd || DataStoreUtils.SP_TRUE.equals(DataStoreUtils.readLocalInfo(BaseApplication.getContext(), KEY_NO_AD));
    }

    public static void setNoAd() {
        DataStoreUtils.saveLocalInfo(BaseApplication.getContext(), KEY_NO_AD, DataStoreUtils.SP_TRUE);
    }

}
