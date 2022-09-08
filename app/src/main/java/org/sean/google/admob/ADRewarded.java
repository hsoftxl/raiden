package org.sean.google.admob;

import android.app.Activity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import org.sean.util.LogUtil;

public class ADRewarded {
    private static final String TAG = "ADRewarded";
    private static RewardedInterstitialAd rewardedInterstitialAd;
    private static boolean isLoading = false;
    private static Callback callback;

    public static boolean canShowAd() {
        return !isLoading && rewardedInterstitialAd != null;
    }

    public static void load(Activity activity) {
        load(activity, true, false);
    }

    public static void load(Activity activity, boolean preLoad, boolean showImmediately) {
        if (GAD.isNoAd()) {
            return;
        }
        if (isLoading) {
            return;
        }
        isLoading = true;
        RewardedInterstitialAd.load(activity, GAD.UID_REWARD, new AdRequest.Builder().build(), new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedInterstitialAd ad) {
                rewardedInterstitialAd = ad;
                isLoading = false;
                LogUtil.e("RewardedInterstitialAd onAdLoaded");
                rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    /** Called when the ad failed to show full screen content. */
                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        LogUtil.i("onAdFailedToShowFullScreenContent:%s", adError.getMessage());
                        callback.call(Callback.AD_ERROR, false);
                        rewardedInterstitialAd = null;
                    }

                    /** Called when ad showed the full screen content. */
                    @Override
                    public void onAdShowedFullScreenContent() {
                        LogUtil.i("onAdShowedFullScreenContent");
                    }

                    /** Called when full screen content is dismissed. */
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        LogUtil.i("onAdDismissedFullScreenContent");
                        rewardedInterstitialAd = null;
                        callback.call(Callback.AD_CANCEL, false);
                    }
                });
                if (activity.isFinishing()) {
                    callback.call(Callback.AD_ERROR, false);
                } else {
                    if (showImmediately && rewardedInterstitialAd != null) {
                        rewardedInterstitialAd.show(activity, rewardItem -> {
                            LogUtil.i("onUserEarnedReward");
                            rewardedInterstitialAd = null;
                            callback.call(Callback.AD_REWARD, true);
                            if (preLoad) {
                                load(activity, true, false);
                            }
                        });
                    }
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                rewardedInterstitialAd = null;
                LogUtil.e("onAdFailedToLoad:%s \ncode:%s\nmessage:%s",
                        loadAdError.getResponseInfo(), loadAdError.getCode(), loadAdError.getMessage());
                isLoading = false;
                callback.call(Callback.AD_ERROR, false);
            }
        });
    }

    // 展示广告
    public static void show(Activity activity, Callback callback) {
        show(activity, callback, true);
    }

    // 展示广告
    public static void show(Activity activity, Callback callback, boolean preLoad) {
        ADRewarded.callback = callback;
        if (GAD.isNoAd()) {
            callback.call(Callback.AD_REWARD, true);
            return;
        }
        try {
            if (rewardedInterstitialAd != null) {
                rewardedInterstitialAd.show(activity, rewardItem -> {
                    LogUtil.i("onUserEarnedReward");
                    rewardedInterstitialAd = null;
                    callback.call(Callback.AD_REWARD, true);
                    if (!preLoad) {
                        load(activity, true, false);
                    }
                });
            } else {
                if (!isLoading) {
                    load(activity, true, true);
                } else {
                    callback.call(Callback.AD_LOADING, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            rewardedInterstitialAd = null;
            callback.call(Callback.AD_ERROR, false);
            if (preLoad) {
                load(activity);
            }
        }
    }

    public static interface Callback {
        int AD_REWARD = 1;
        int AD_LOADING = 2;
        int AD_CANCEL = 3;
        int AD_ERROR = 4;

        void call(int code, boolean isReward);
    }
}
