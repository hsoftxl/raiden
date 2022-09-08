package org.sean.google.play.pay;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.google.gson.Gson;

import org.sean.BaseApplication;
import org.sean.util.AppUtil;
import org.sean.util.DataStoreUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PayUtil {
    private static final String TAG = PayUtil.class.getName();
    private static final AtomicInteger count = new AtomicInteger(0);
    // 支付结果集合
    private static final Map<String, List<String>> purchasesMap = new HashMap<>();
    // SKU集合
    private static final Map<String, SkuDetails> skuMap = new HashMap<>();
    private static final Map<String, SkuDetails> subMap = new HashMap<>();
    private static final int STATUS_INIT_NOT_CALLED = 0;
    private static final int STATUS_INIT_ING = 1;
    private static final int STATUS_INIT_SUCCESS = 2;
    private static final int STATUS_INIT_FAILED = 3;
    // 是否初始化完成
    private static int initSkuStatus = 0;
    private static int initSubsStatus = 0;
    public static Handler mHandler = new Handler(Looper.getMainLooper());
    private static Executor executor = new ThreadPoolExecutor(1, 1,
            1, TimeUnit.MINUTES, new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());
    private static final ExecutorService connectExecutor = Executors.newFixedThreadPool(1);

    // 消耗成功监听
    private static AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {

        }
    };

    private static void logInfo(String msg) {
        Log.e(TAG, msg);
    }

    // 支付监听
    private static PurchasesListener purchasesUpdatedListener = new PurchasesListener(null);

    private static BillingClient billingClient = BillingClient.newBuilder(BaseApplication.getContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build();

    // 交易确认
    private static void handlePurchase(Purchase purchase, Consumer<String> consumer) {
        logInfo("handlePurchase start ");
        logInfo("handlePurchase - 状态: " + purchase.getPurchaseState());
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // 消耗品
            logInfo("handlePurchase : 已支付 ");
            ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
            purchasesMap.put(purchase.getPurchaseToken(), purchase.getSkus());
            final ConsumeResponseListener listener = new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
                    logInfo("handlePurchase : 消费回调  " + new Gson().toJson(billingResult));
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        DataStoreUtils.saveLocalInfo(BaseApplication.getApplication(), DataStoreUtils.PAY_NO_C_TOKEN, "");
                        logInfo("handlePurchase : 消费成功 ");
                        // 交易完成， 给用户提供商品
                        List<String> skus = purchasesMap.get(purchaseToken);
                        if (skus != null && skus.size() > 0) {
                            for (String sku : skus) {
                                logInfo("handlePurchase : 添加权益 " + sku);
                                consumer.accept(sku, ErrorCode.SUCCESS);
                            }
                        }
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
                        // 未支付商品

                    } else {
                        List<String> skus = purchasesMap.get(purchaseToken);
                        String sku = "";
                        if (skus != null && !skus.isEmpty()) {
                            sku = skus.get(0);
                        }
                        DataStoreUtils.saveLocalInfo(BaseApplication.getApplication(), DataStoreUtils.PAY_NO_C_TOKEN, purchaseToken);
                        if (!TextUtils.isEmpty(sku)) {
                            DataStoreUtils.saveLocalInfo(BaseApplication.getApplication(), DataStoreUtils.PAY_NO_C_SKU, sku);
                        }
                        final ConsumeResponseListener l = this;
                        connectExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                } catch (Exception e) {
                                }
                                billingClient.consumeAsync(consumeParams, l);
                            }
                        });
                    }
                }
            };

            logInfo("handlePurchase : 异步消费 ");
            consumer.accept("", ErrorCode.CONSUME);
            billingClient.consumeAsync(consumeParams, listener);

            // 非消耗品
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();

                logInfo("handlePurchase : 订阅商品消费 ");
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    public static void init() {
        if (initSkuStatus == STATUS_INIT_NOT_CALLED || initSkuStatus == STATUS_INIT_FAILED) {
            initSkuStatus = STATUS_INIT_ING;
            count.set(1);
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    Log.i(TAG, billingResult.getResponseCode() + ":" + billingResult.getDebugMessage());
                    logInfo("init : responseCode: " + billingResult.getResponseCode());
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        logInfo("init : 查询商品 ");
                        queryGoods();
                    } else {
                        initSkuStatus = STATUS_INIT_FAILED;
                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                    initSkuStatus = STATUS_INIT_FAILED;
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    connectExecutor.execute(() -> {
                        logInfo("init : 连接重试 ");
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (count.getAndIncrement() < 10) {
                            init();
                        }
                    });
                }
            });
        }
    }

    private static void queryGoods() {
        // 展示可供购买的商品
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(Goods.INAPP).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                (billingResult, skuDetailsList) -> {
                    // Process the result.
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (SkuDetails details : skuDetailsList) {
                            skuMap.put(details.getSku(), details);
                        }
                        Log.i(TAG, "INAPP:" + Arrays.deepToString(skuDetailsList.toArray()));
                        initSkuStatus = STATUS_INIT_SUCCESS;
                    }
                });
        // 訂閲
        SkuDetailsParams.Builder subParams = SkuDetailsParams.newBuilder();
        subParams.setSkusList(Goods.SUBS).setType(BillingClient.SkuType.SUBS);
        billingClient.querySkuDetailsAsync(subParams.build(),
                (billingResult, skuDetailsList) -> {
                    // Process the result.
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (SkuDetails details : skuDetailsList) {
                            subMap.put(details.getSku(), details);
                        }
                        Log.i(TAG, "SUBS:" + Arrays.deepToString(skuDetailsList.toArray()));
                        initSubsStatus = STATUS_INIT_SUCCESS;
                    }
                });
    }

    public static void startPay(Activity activity, String sku, Consumer<String> consumer) {
        startPay(activity, sku, null, consumer);
    }

    public static void startPay(Activity activity, String sku, String ext, Consumer<String> consumer) {
        try {
            executor.execute(() -> {
                try {
                    pay(activity, sku, ext, consumer);
                } catch (Exception e) {
                    consumer.accept(sku, ErrorCode.PAY_ERROR);
                }
            });
        } catch (Exception e) {
            consumer.accept(sku, ErrorCode.PAY_ERROR);
        }
    }

    private static void pay(Activity activity, String sku, String sub, Consumer<String> consumer) {
        int status = initSkuStatus;
        Map<String, SkuDetails> map = skuMap;
        String goods = sku;
        if (sku == null && sub != null) {
            status = initSubsStatus;
            map = subMap;
            goods = sub;
        }
        try {
            if (map.get(goods) == null) {
                init();
            }
            int times = 1;
            while (status <= STATUS_INIT_ING && times++ < 300) {
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
            consumer.accept(goods, ErrorCode.INIT_ERROR);
            return;
        }
        if (status != STATUS_INIT_SUCCESS) {
            // 查询sku失败
            consumer.accept(goods, ErrorCode.INIT_ERROR);
            return;
        }

        try {
            SkuDetails skuDetails = map.get(goods);
            if (skuDetails == null) {
                consumer.accept(goods, ErrorCode.GOODS_NOT_EXIT);
                return;
            }
            // 查询是否有购买未消耗
            purchasesUpdatedListener.consumer = consumer;
            String finalGoods = goods;
            billingClient.queryPurchasesAsync(goods, new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                    logInfo("pay : 查询购买记录 ");
                    logInfo("pay : responseCode: " + billingResult.getResponseCode());
                    logInfo("pay : list: " + new Gson().toJson(list));
                    if (list != null && list.size() > 0) {
                        purchasesUpdatedListener.onPurchasesUpdated(billingResult, list);
                    } else {
                        logInfo("pay : 查询购买历史 ");
                        billingClient.queryPurchaseHistoryAsync(finalGoods, new PurchaseHistoryResponseListener() {
                            @Override
                            public void onPurchaseHistoryResponse(@NonNull BillingResult billingResult, @Nullable List<PurchaseHistoryRecord> list) {
                                logInfo("pay : responseCode: " + billingResult.getResponseCode());
                                logInfo("pay : list: " + new Gson().toJson(list));
                            }
                        });

                        //启动购买流程
                        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
                        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
                        // Handle the result.
                        if (BillingClient.BillingResponseCode.OK == responseCode) {
                            // 响应成功
                            purchasesUpdatedListener.consumer = consumer;
                        } else {
                            consumer.accept(finalGoods, ErrorCode.START_PURCHASE_FAILED);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            consumer.accept(goods, ErrorCode.START_PURCHASE_FAILED);
        }
    }

    public static boolean consumer(String pToken) {
        String token = DataStoreUtils.readLocalInfo(BaseApplication.getApplication(), DataStoreUtils.PAY_NO_C_TOKEN);
        String sku = DataStoreUtils.readLocalInfo(BaseApplication.getApplication(), DataStoreUtils.PAY_NO_C_SKU);
        if (!TextUtils.isEmpty(pToken)) {
            token = pToken;
            sku = null;
        }
        final String fSku = sku;
        if (TextUtils.isEmpty(token)) {
            return false;
        }
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(token).build();
        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String s) {
                logInfo(new Gson().toJson(billingResult) + " -- " + s);
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (!TextUtils.isEmpty(fSku)) {
                        PayConsumer consumer = new PayConsumer();
                        consumer.accept(fSku, ErrorCode.SUCCESS);
                    }
                    AppUtil.showToast("未生效订单处理成功!");
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR || billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
                    AppUtil.showToast("无效订单!");
                } else {
                    AppUtil.showToast("未生效订单处理失败,请稍后再试!");
                }
            }
        });
        return true;
    }

    public static class PurchasesListener implements PurchasesUpdatedListener {

        private Consumer<String> consumer;

        public PurchasesListener(Consumer<String> c) {
            this.consumer = c;
        }

        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
            // To be implemented in a later section.
            if (consumer == null) {
                consumer = new PayConsumer();
            }
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase, consumer);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED && purchases != null) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase, consumer);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle an error caused by a user cancelling the purchase flow.
                consumer.accept(null, ErrorCode.USER_CANCELED);
            } else {
                // Handle any other error codes.
                consumer.accept("" + billingResult.getResponseCode(), ErrorCode.PAY_ERROR);
            }
        }
    }
}
