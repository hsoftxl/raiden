package org.sean.google.play.pay;

public enum ErrorCode {
    /**
     * 成功支付,正在提交消费
     */
    CONSUME,
    /**
     * 消费支付
     */
    SUCCESS,
    /**
     * 初始化错误
     */
    INIT_ERROR,
    /**
     * 商品不纯在
     */
    GOODS_NOT_EXIT,
    /**
     * 启动支付窗口失败
     */
    START_PURCHASE_FAILED,
    /**
     * 用户取消
     */
    USER_CANCELED,
    /**
     * 支付失败
     */
    PAY_ERROR
}
