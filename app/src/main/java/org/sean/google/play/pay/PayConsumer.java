package org.sean.google.play.pay;

import org.cocos2dx.cpp.Billing;
import org.cocos2dx.cpp.NativeInterface;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;

public class PayConsumer implements Consumer<String> {

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

}
