package com.android.mms.service_alt;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;

import com.klinker.android.send_message.R;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public class MmsConfig {

    public static final String CONFIG_ENABLED_MMS = "enabledMMS";
    public static final String CONFIG_ENABLED_TRANS_ID = "enabledTransID";
    public static final String CONFIG_ENABLED_NOTIFY_WAP_MMSC = "enabledNotifyWapMMSC";
    public static final String CONFIG_ALIAS_ENABLED = "aliasEnabled";
    public static final String CONFIG_ALLOW_ATTACH_AUDIO = "allowAttachAudio";

    public static final String CONFIG_ENABLE_MULTIPART_SMS = "enableMultipartSMS";
    public static final String CONFIG_ENABLE_SMS_DELIVERY_REPORTS = "enableSMSDeliveryReports";

    public static final String CONFIG_ENABLE_GROUP_MMS = "enableGroupMms";

    public static final String CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION =
            "supportMmsContentDisposition";

    public static final String CONFIG_CELL_BROADCAST_APP_LINKS = "config_cellBroadcastAppLinks";

    public static final String CONFIG_SEND_MULTIPART_SMS_AS_SEPARATE_MESSAGES =
            "sendMultipartSmsAsSeparateMessages";

    public static final String CONFIG_ENABLE_MMS_READ_REPORTS = "enableMMSReadReports";
    public static final String CONFIG_ENABLE_MMS_DELIVERY_REPORTS = "enableMMSDeliveryReports";

    public static final String CONFIG_SUPPORT_HTTP_CHARSET_HEADER = "supportHttpCharsetHeader";
    public static final String CONFIG_MAX_MESSAGE_SIZE = "maxMessageSize"; // in bytes
    public static final String CONFIG_MAX_IMAGE_HEIGHT = "maxImageHeight"; // in pixels
    public static final String CONFIG_MAX_IMAGE_WIDTH = "maxImageWidth"; // in pixels
    public static final String CONFIG_RECIPIENT_LIMIT = "recipientLimit";
    public static final String CONFIG_HTTP_SOCKET_TIMEOUT = "httpSocketTimeout";
    public static final String CONFIG_ALIAS_MIN_CHARS = "aliasMinChars";
    public static final String CONFIG_ALIAS_MAX_CHARS = "aliasMaxChars";
    public static final String CONFIG_SMS_TO_MMS_TEXT_THRESHOLD = "smsToMmsTextThreshold";
    public static final String CONFIG_SMS_TO_MMS_TEXT_LENGTH_THRESHOLD =
            "smsToMmsTextLengthThreshold";
    public static final String CONFIG_MAX_MESSAGE_TEXT_SIZE = "maxMessageTextSize";
    public static final String CONFIG_MAX_SUBJECT_LENGTH = "maxSubjectLength";
    public static final String CONFIG_UA_PROF_TAG_NAME = "uaProfTagName";
    public static final String CONFIG_USER_AGENT = "userAgent";
    public static final String CONFIG_UA_PROF_URL = "uaProfUrl";
    public static final String CONFIG_HTTP_PARAMS = "httpParams";
    public static final String CONFIG_EMAIL_GATEWAY_NUMBER = "emailGatewayNumber";
    public static final String CONFIG_NAI_SUFFIX = "naiSuffix";
    public static final String KEY_TYPE_INT = "int";
    public static final String KEY_TYPE_BOOL = "bool";
    public static final String KEY_TYPE_STRING = "string";
    public static final String MACRO_LINE1 = "LINE1";
    public static final String MACRO_LINE1NOCOUNTRYCODE = "LINE1NOCOUNTRYCODE";
    public static final String MACRO_NAI = "NAI";
    private static final String DEFAULT_HTTP_KEY_X_WAP_PROFILE = "x-wap-profile";
    private static final int MAX_IMAGE_HEIGHT = 480;
    private static final int MAX_IMAGE_WIDTH = 640;
    private static final int MAX_TEXT_LENGTH = 2000;
    private static final Map<String, Object> DEFAULTS = new ConcurrentHashMap<String, Object>();

    static {
        DEFAULTS.put(CONFIG_ENABLED_MMS, Boolean.valueOf(true));
        DEFAULTS.put(CONFIG_ENABLED_TRANS_ID, Boolean.valueOf(false));
        DEFAULTS.put(CONFIG_ENABLED_NOTIFY_WAP_MMSC, Boolean.valueOf(false));
        DEFAULTS.put(CONFIG_ALIAS_ENABLED, Boolean.valueOf(false));
        DEFAULTS.put(CONFIG_ALLOW_ATTACH_AUDIO, Boolean.valueOf(true));
        DEFAULTS.put(CONFIG_ENABLE_MULTIPART_SMS, Boolean.valueOf(true));
        DEFAULTS.put(CONFIG_ENABLE_SMS_DELIVERY_REPORTS, Boolean.valueOf(true));
        DEFAULTS.put(CONFIG_ENABLE_GROUP_MMS, Boolean.valueOf(true));
        DEFAULTS.put(CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION, Boolean.valueOf(true));
        DEFAULTS.put(CONFIG_CELL_BROADCAST_APP_LINKS, Boolean.valueOf(true));
        DEFAULTS.put(CONFIG_SEND_MULTIPART_SMS_AS_SEPARATE_MESSAGES, Boolean.valueOf(false));
        DEFAULTS.put(CONFIG_ENABLE_MMS_READ_REPORTS, Boolean.valueOf(false));
        DEFAULTS.put(CONFIG_ENABLE_MMS_DELIVERY_REPORTS, Boolean.valueOf(false));
        DEFAULTS.put(CONFIG_SUPPORT_HTTP_CHARSET_HEADER, Boolean.valueOf(false));
        DEFAULTS.put(CONFIG_MAX_MESSAGE_SIZE, Integer.valueOf(300 * 1024));
        DEFAULTS.put(CONFIG_MAX_IMAGE_HEIGHT, Integer.valueOf(MAX_IMAGE_HEIGHT));
        DEFAULTS.put(CONFIG_MAX_IMAGE_WIDTH, Integer.valueOf(MAX_IMAGE_WIDTH));
        DEFAULTS.put(CONFIG_RECIPIENT_LIMIT, Integer.valueOf(Integer.MAX_VALUE));
        DEFAULTS.put(CONFIG_HTTP_SOCKET_TIMEOUT, Integer.valueOf(60 * 1000));
        DEFAULTS.put(CONFIG_ALIAS_MIN_CHARS, Integer.valueOf(2));
        DEFAULTS.put(CONFIG_ALIAS_MAX_CHARS, Integer.valueOf(48));
        DEFAULTS.put(CONFIG_SMS_TO_MMS_TEXT_THRESHOLD, Integer.valueOf(-1));
        DEFAULTS.put(CONFIG_SMS_TO_MMS_TEXT_LENGTH_THRESHOLD, Integer.valueOf(-1));
        DEFAULTS.put(CONFIG_MAX_MESSAGE_TEXT_SIZE, Integer.valueOf(-1));
        DEFAULTS.put(CONFIG_MAX_SUBJECT_LENGTH, Integer.valueOf(40));
        DEFAULTS.put(CONFIG_UA_PROF_TAG_NAME, DEFAULT_HTTP_KEY_X_WAP_PROFILE);
        DEFAULTS.put(CONFIG_USER_AGENT, "");
        DEFAULTS.put(CONFIG_UA_PROF_URL, "");
        DEFAULTS.put(CONFIG_HTTP_PARAMS, "");
        DEFAULTS.put(CONFIG_EMAIL_GATEWAY_NUMBER, "");
        DEFAULTS.put(CONFIG_NAI_SUFFIX, "");
    }

    private final int mSubId;
    private final Map<String, Object> mKeyValues = new ConcurrentHashMap<String, Object>();
    private String mUserAgent = null;
    private String mUaProfUrl = null;

    public MmsConfig(Context context, int subId) {
        mSubId = subId;
        mKeyValues.clear();
        mKeyValues.putAll(DEFAULTS);
        loadDeviceUaSettings(context);
        Timber.v("MmsConfig: mUserAgent=" + mUserAgent + ", mUaProfUrl=" + mUaProfUrl);
        loadFromResources(context);
        Timber.v("MmsConfig: all settings -- " + mKeyValues);
    }

    public MmsConfig(Context context) {
        mSubId = -1;
        mKeyValues.clear();
        mKeyValues.putAll(DEFAULTS);
        loadDeviceUaSettings(context);
        Timber.v("MmsConfig: mUserAgent=" + mUserAgent + ", mUaProfUrl=" + mUaProfUrl);
        loadFromResources(context);
        Timber.v("MmsConfig: all settings -- " + mKeyValues);
    }

    public static boolean isValidKey(String key, String type) {
        if (!TextUtils.isEmpty(key) && DEFAULTS.containsKey(key)) {
            Object defVal = DEFAULTS.get(key);
            Class<?> valueType = defVal != null ? defVal.getClass() : String.class;
            if (KEY_TYPE_INT.equals(type)) {
                return valueType == Integer.class;
            } else if (KEY_TYPE_BOOL.equals(type)) {
                return valueType == Boolean.class;
            } else if (KEY_TYPE_STRING.equals(type)) {
                return valueType == String.class;
            }
        }
        return false;
    }

    public static boolean isValidValue(String key, Object value) {
        if (!TextUtils.isEmpty(key) && DEFAULTS.containsKey(key)) {
            Object defVal = DEFAULTS.get(key);
            Class<?> valueType = defVal != null ? defVal.getClass() : String.class;
            return value.getClass().equals(valueType);
        }
        return false;
    }

    public int getSubId() {
        return mSubId;
    }

    public Object getValueAsType(String key, String type) {
        if (isValidKey(key, type)) {
            return mKeyValues.get(key);
        }
        return null;
    }

    public Bundle getCarrierConfigValues() {
        final Bundle bundle = new Bundle();
        final Iterator<Map.Entry<String, Object>> iter = mKeyValues.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<String, Object> entry = iter.next();
            final String key = entry.getKey();
            final Object val = entry.getValue();
            Class<?> valueType = val != null ? val.getClass() : String.class;
            if (valueType == Integer.class) {
                bundle.putInt(key, (Integer) val);
            } else if (valueType == Boolean.class) {
                bundle.putBoolean(key, (Boolean) val);
            } else if (valueType == String.class) {
                bundle.putString(key, (String) val);
            }
        }
        return bundle;
    }

    private String getNullableStringValue(String key) {
        final Object value = mKeyValues.get(key);
        if (value != null) {
            return (String) value;
        }
        return null;
    }

    private void update(String key, String value, String type) {
        try {
            if (KEY_TYPE_INT.equals(type)) {
                mKeyValues.put(key, Integer.parseInt(value));
            } else if (KEY_TYPE_BOOL.equals(type)) {
                mKeyValues.put(key, Boolean.parseBoolean(value));
            } else if (KEY_TYPE_STRING.equals(type)) {
                mKeyValues.put(key, value);
            }
        } catch (NumberFormatException e) {
            Timber.e("MmsConfig.update: invalid " + key + "," + value + "," + type);
        }
    }

    private void loadDeviceUaSettings(Context context) {
        final TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        mUserAgent = telephonyManager.getMmsUserAgent();
        mUaProfUrl = telephonyManager.getMmsUAProfUrl();
    }

    private void loadFromResources(Context context) {
        Timber.d("MmsConfig.loadFromResources");
        final XmlResourceParser parser = context.getResources().getXml(R.xml.mms_config);
        final MmsConfigXmlProcessor processor = MmsConfigXmlProcessor.get(parser);
        processor.setMmsConfigHandler(new MmsConfigXmlProcessor.MmsConfigHandler() {
            @Override
            public void process(String key, String value, String type) {
                update(key, value, type);
            }
        });
        try {
            processor.process();
        } finally {
            parser.close();
        }
    }

    public static class Overridden {
        private final MmsConfig mBase;
        private final Bundle mOverrides;

        public Overridden(MmsConfig base, Bundle overrides) {
            mBase = base;
            mOverrides = overrides;
        }

        private static String getLine1(Context context, int subId) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                    Context.TELEPHONY_SERVICE);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                return telephonyManager.getLine1Number();
            } else {
                try {
                    Method method = telephonyManager.getClass().getMethod("getLine1NumberForSubscriber", int.class);
                    return (String) method.invoke(telephonyManager, subId);
                } catch (Exception e) {
                    return telephonyManager.getLine1Number();
                }
            }
        }

        private static String getLine1NoCountryCode(Context context, int subId) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                    Context.TELEPHONY_SERVICE);
            return PhoneUtils.getNationalNumber(
                    telephonyManager,
                    subId,
                    getLine1(context, subId));
        }

        private int getInt(String key) {
            final Integer def = (Integer) mBase.mKeyValues.get(key);
            return mOverrides != null ? mOverrides.getInt(key, def) : def;
        }

        private boolean getBoolean(String key) {
            final Boolean def = (Boolean) mBase.mKeyValues.get(key);
            return mOverrides != null ? mOverrides.getBoolean(key, def) : def;
        }

        private String getString(String key) {
            if (mOverrides != null && mOverrides.containsKey(key)) {
                return mOverrides.getString(key);
            }
            return mBase.getNullableStringValue(key);
        }

        public int getSmsToMmsTextThreshold() {
            return getInt(CONFIG_SMS_TO_MMS_TEXT_THRESHOLD);
        }

        public int getSmsToMmsTextLengthThreshold() {
            return getInt(CONFIG_SMS_TO_MMS_TEXT_LENGTH_THRESHOLD);
        }

        public boolean getMmsEnabled() {
            return getBoolean(CONFIG_ENABLED_MMS);
        }

        public int getMaxMessageSize() {
            return getInt(CONFIG_MAX_MESSAGE_SIZE);
        }

        public boolean getTransIdEnabled() {
            return getBoolean(CONFIG_ENABLED_TRANS_ID);
        }

        public String getUserAgent() {
            if (mOverrides != null && mOverrides.containsKey(CONFIG_USER_AGENT)) {
                return mOverrides.getString(CONFIG_USER_AGENT);
            }
            return !TextUtils.isEmpty(mBase.mUserAgent) ?
                    mBase.mUserAgent : mBase.getNullableStringValue(CONFIG_USER_AGENT);
        }

        public String getUaProfTagName() {
            return getString(CONFIG_UA_PROF_TAG_NAME);
        }

        public String getUaProfUrl() {
            if (mOverrides != null && mOverrides.containsKey(CONFIG_UA_PROF_URL)) {
                return mOverrides.getString(CONFIG_UA_PROF_URL);
            }
            return !TextUtils.isEmpty(mBase.mUaProfUrl) ?
                    mBase.mUaProfUrl : mBase.getNullableStringValue(CONFIG_UA_PROF_URL);
        }

        public String getHttpParams() {
            return getString(CONFIG_HTTP_PARAMS);
        }

        public String getEmailGateway() {
            return getString(CONFIG_EMAIL_GATEWAY_NUMBER);
        }

        public int getMaxImageHeight() {
            return getInt(CONFIG_MAX_IMAGE_HEIGHT);
        }

        public int getMaxImageWidth() {
            return getInt(CONFIG_MAX_IMAGE_WIDTH);
        }

        public int getRecipientLimit() {
            final int limit = getInt(CONFIG_RECIPIENT_LIMIT);
            return limit < 0 ? Integer.MAX_VALUE : limit;
        }

        public int getMaxTextLimit() {
            final int max = getInt(CONFIG_MAX_MESSAGE_TEXT_SIZE);
            return max > -1 ? max : MAX_TEXT_LENGTH;
        }

        public int getHttpSocketTimeout() {
            return getInt(CONFIG_HTTP_SOCKET_TIMEOUT);
        }

        public boolean getMultipartSmsEnabled() {
            return getBoolean(CONFIG_ENABLE_MULTIPART_SMS);
        }

        public boolean getSendMultipartSmsAsSeparateMessages() {
            return getBoolean(CONFIG_SEND_MULTIPART_SMS_AS_SEPARATE_MESSAGES);
        }

        public boolean getSMSDeliveryReportsEnabled() {
            return getBoolean(CONFIG_ENABLE_SMS_DELIVERY_REPORTS);
        }

        public boolean getNotifyWapMMSC() {
            return getBoolean(CONFIG_ENABLED_NOTIFY_WAP_MMSC);
        }

        public boolean isAliasEnabled() {
            return getBoolean(CONFIG_ALIAS_ENABLED);
        }

        public int getAliasMinChars() {
            return getInt(CONFIG_ALIAS_MIN_CHARS);
        }

        public int getAliasMaxChars() {
            return getInt(CONFIG_ALIAS_MAX_CHARS);
        }

        public boolean getAllowAttachAudio() {
            return getBoolean(CONFIG_ALLOW_ATTACH_AUDIO);
        }

        public int getMaxSubjectLength() {
            return getInt(CONFIG_MAX_SUBJECT_LENGTH);
        }

        public boolean getGroupMmsEnabled() {
            return getBoolean(CONFIG_ENABLE_GROUP_MMS);
        }

        public boolean getSupportMmsContentDisposition() {
            return getBoolean(CONFIG_SUPPORT_MMS_CONTENT_DISPOSITION);
        }

        public boolean getShowCellBroadcast() {
            return getBoolean(CONFIG_CELL_BROADCAST_APP_LINKS);
        }

        public String getNaiSuffix() {
            return getString(CONFIG_NAI_SUFFIX);
        }

        public boolean isMmsReadReportsEnabled() {
            return getBoolean(CONFIG_ENABLE_MMS_READ_REPORTS);
        }

        public boolean isMmsDeliveryReportsEnabled() {
            return getBoolean(CONFIG_ENABLE_MMS_DELIVERY_REPORTS);
        }

        public boolean getSupportHttpCharsetHeader() {
            return getBoolean(CONFIG_SUPPORT_HTTP_CHARSET_HEADER);
        }

        public String getHttpParamMacro(Context context, String macro) {
            if (MACRO_LINE1.equals(macro)) {
                return getLine1(context, mBase.getSubId());
            } else if (MACRO_LINE1NOCOUNTRYCODE.equals(macro)) {
                return getLine1NoCountryCode(context, mBase.getSubId());
            } else if (MACRO_NAI.equals(macro)) {
                return getNai(context, mBase.getSubId());
            }
            return null;
        }

        private String getNai(Context context, int subId) {
            final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                    Context.TELEPHONY_SERVICE);

            String nai = "";

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                nai = SystemPropertiesProxy.get(context, "persist.radio.cdma.nai");
            } else {
                try {
                    Method method = telephonyManager.getClass().getMethod("getNai", int.class);
                    Method getSlotId = SubscriptionManager.class.getMethod("getSlotId", int.class);
                    nai = (String) method.invoke(telephonyManager, getSlotId.invoke(null, subId));
                } catch (Exception e) {
                    nai = SystemPropertiesProxy.get(context, "persist.radio.cdma.nai");
                }
            }

            Timber.v("MmsConfig.getNai: nai=" + nai);

            if (!TextUtils.isEmpty(nai)) {
                String naiSuffix = getNaiSuffix();
                if (!TextUtils.isEmpty(naiSuffix)) {
                    nai = nai + naiSuffix;
                }
                byte[] encoded = null;
                encoded = Base64.encode(nai.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                nai = new String(encoded, StandardCharsets.UTF_8);
            }
            return nai;
        }
    }
}
