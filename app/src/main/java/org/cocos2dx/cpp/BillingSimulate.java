package org.cocos2dx.cpp;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.app.skywar2021.R;
import com.google.dlg.LoadingDialog;

import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.sean.google.admob.ADRewarded;
import org.sean.google.admob.GAD;
import org.sean.google.play.pay.Consumer;
import org.sean.google.play.pay.ErrorCode;
import org.sean.google.play.pay.PayUtil;

public class BillingSimulate extends BillingInterface {

    private Activity mActivity;
    private static LoadingDialog pDialog;
    private static boolean isReceived;

    @Override
    public void loadLibrary(Context context) {

    }

    @Override
    public void initialize(Activity activity) {

        mActivity = activity;

    }

    @Override
    public boolean isMusicEnabled() {
        return true;
    }

    @Override
    public void moreGame() {
    }

    @Override
    public void showAd(String position) {
        mActivity.runOnUiThread(() -> {
//            if (!GAD.hasRewardAd()) {
//                showToash(R.string.ad_loading);
//            } else {
            showDlg();
            isReceived = false;
            GAD.showReward(mActivity, (code, isReward) -> Cocos2dxGLSurfaceView.getInstance().queueEvent(() -> {
                switch (code) {
                    case ADRewarded.Callback.AD_REWARD:
                        NativeInterface.winAdReward(position, Billing.ResultSuccess);
                        isReceived = true;
                        showToash(R.string.ad_reward);
                        break;
                    case ADRewarded.Callback.AD_LOADING:
                        showToash(R.string.ad_loading);
                        break;
                    case ADRewarded.Callback.AD_CANCEL:
                        if (!isReceived) {
                            showToash(R.string.ad_cancel);
                        }
                        break;
                    case ADRewarded.Callback.AD_ERROR:
                        showToash(R.string.ad_error);
                        break;
                }
                pDialog.cancel();
            }));
//            }
        });
    }

    private void showToash(int resId) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mActivity, resId, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDlg() {
        if (pDialog != null) {
            if (pDialog.isShowing()) {
                pDialog.cancel();
            }
        }
        pDialog = progressDialog(mActivity);
        // pDialog.setIndeterminate(indeterminate);//设置对话框里的进度条不显示进度值
        //pDialog.setProgressStyle(style);//设置对话框里进度条的进度值
        pDialog.show();
    }

    @Override
    public void pay(final String billingIndex, final String code, final String name, final int price, final String desc) {
//        LogUtil.d("bi:%s code:%s name:%s price:%d desc:%s", billingIndex, code, name, price, desc);

//        paySuccess(billingIndex);
//        if (mActivity == null) {
        mActivity.runOnUiThread(() -> {
                    showDlg();
                    PayUtil.startPay(mActivity, billingIndex, new Consumer<String>() {
                        @Override
                        public void accept(String billingIndex, ErrorCode code) {
                            switch (code) {
                                case SUCCESS:
                                    paySuccess(billingIndex);
                                    break;
                                case PAY_ERROR:
                                case INIT_ERROR:
                                case USER_CANCELED:
                                case GOODS_NOT_EXIT:
                                case START_PURCHASE_FAILED:
                                    payFailed(billingIndex);
                                    break;
                            }
                            pDialog.cancel();
                        }
                    });
                }
        );
//        }
    }

    public static LoadingDialog progressDialog(Context context) {
        LoadingDialog.Builder builder = new LoadingDialog.Builder(context)
                .setMessage(context.getString(R.string.dlg_pay_waiting))
                .setCancelable(false);
        return builder.create();
    }

    private void payFailed(String billingIndex) {
        Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
            @Override
            public void run() {
                // 游戏业务收到付费结果后的处理逻辑
                NativeInterface.payResult(billingIndex, Billing.ResultFailed);
            }
        });
    }

    private void paySuccess(String billingIndex) {
        Cocos2dxGLSurfaceView.getInstance().queueEvent(new Runnable() {
            @Override
            public void run() {
                // 游戏业务收到付费结果后的处理逻辑
                NativeInterface.payResult(billingIndex, Billing.ResultSuccess);
            }
        });
    }

    @Override
    public boolean exit() {
        return false;
    }

}
