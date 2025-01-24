package com.android.mms.service_alt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.ArrayMap;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class MmsConfigManager {

    private static final MmsConfigManager sInstance = new MmsConfigManager();
    private final Map<Integer, MmsConfig> mSubIdConfigMap;
    private Context mContext;
    private SubscriptionManager mSubscriptionManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Timber.i("mReceiver action: " + action);
            if (action.equals("LOADED")) {
                loadInBackground();
            }
        }
    };

    private MmsConfigManager() {
        mSubIdConfigMap = new ArrayMap<Integer, MmsConfig>();
    }

    public static MmsConfigManager getInstance() {
        return sInstance;
    }

    public void init(final Context context) {
        mContext = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mSubscriptionManager = SubscriptionManager.from(context);

            // TODO: When this object "finishes" we should unregister.
            IntentFilter intentFilterLoaded =
                    new IntentFilter("LOADED");

            try {
                context.registerReceiver(mReceiver, intentFilterLoaded);
            } catch (Exception e) {

            }

            load(context);
        } else {
            load(context);
        }

    }

    private void loadInBackground() {
        new Thread() {
            @Override
            public void run() {
                Configuration configuration = mContext.getResources().getConfiguration();
                Timber.i("MmsConfigManager.loadInBackground(): mcc/mnc: " +
                        configuration.mcc + "/" + configuration.mnc);
                load(mContext);
            }
        }.start();
    }

    public MmsConfig getMmsConfigBySubId(int subId) {
        MmsConfig mmsConfig;
        synchronized (mSubIdConfigMap) {
            mmsConfig = mSubIdConfigMap.get(subId);
        }
        Timber.i("getMmsConfigBySubId -- for sub: " + subId + " mmsConfig: " + mmsConfig);
        return mmsConfig;
    }

    public MmsConfig getMmsConfig() {
        return new MmsConfig(mContext);
    }

    private void load(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            List<SubscriptionInfo> subs = mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subs == null || subs.size() < 1) {
                Timber.e("MmsConfigManager.load -- empty getActiveSubInfoList");
                return;
            }
            final Map<Integer, MmsConfig> newConfigMap = new ArrayMap<Integer, MmsConfig>();
            for (SubscriptionInfo sub : subs) {
                Configuration configuration = new Configuration();
                if (sub.getMcc() == 0 && sub.getMnc() == 0) {
                    Configuration config = mContext.getResources().getConfiguration();
                    configuration.mcc = config.mcc;
                    configuration.mnc = config.mnc;
                    Timber.i("MmsConfigManager.load -- no mcc/mnc for sub: " + sub +
                            " using mcc/mnc from Activities context: " + configuration.mcc + "/" +
                            configuration.mnc);
                } else {
                    Timber.i("MmsConfigManager.load -- mcc/mnc for sub: " + sub);

                    configuration.mcc = sub.getMcc();
                    configuration.mnc = sub.getMnc();
                }
                Context subContext = context.createConfigurationContext(configuration);

                int subId = sub.getSubscriptionId();
                newConfigMap.put(subId, new MmsConfig(subContext, subId));
            }
            synchronized (mSubIdConfigMap) {
                mSubIdConfigMap.clear();
                mSubIdConfigMap.putAll(newConfigMap);
            }
        }
    }

}
