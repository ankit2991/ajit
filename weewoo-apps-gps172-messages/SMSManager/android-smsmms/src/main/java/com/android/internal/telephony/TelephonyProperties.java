package com.android.internal.telephony;

public interface TelephonyProperties {

    String PROPERTY_BASEBAND_VERSION = "gsm.version.baseband";

    String PROPERTY_RIL_IMPL = "gsm.version.ril-impl";

    String PROPERTY_OPERATOR_ALPHA = "gsm.operator.alpha";

    String PROPERTY_OPERATOR_NUMERIC = "gsm.operator.numeric";

    String PROPERTY_OPERATOR_ISMANUAL = "operator.ismanual";

    String PROPERTY_OPERATOR_ISROAMING = "gsm.operator.isroaming";

    String PROPERTY_OPERATOR_ISO_COUNTRY = "gsm.operator.iso-country";

    String PROPERTY_LTE_ON_CDMA_PRODUCT_TYPE = "telephony.lteOnCdmaProductType";

    String PROPERTY_LTE_ON_CDMA_DEVICE = "telephony.lteOnCdmaDevice";

    String CURRENT_ACTIVE_PHONE = "gsm.current.phone-type";

    String PROPERTY_SIM_STATE = "gsm.sim.state";

    String PROPERTY_ICC_OPERATOR_NUMERIC = "gsm.sim.operator.numeric";

    String PROPERTY_ICC_OPERATOR_ALPHA = "gsm.sim.operator.alpha";

    String PROPERTY_ICC_OPERATOR_ISO_COUNTRY = "gsm.sim.operator.iso-country";

    String PROPERTY_DATA_NETWORK_TYPE = "gsm.network.type";

    String PROPERTY_INECM_MODE = "ril.cdma.inecmmode";

    String PROPERTY_ECM_EXIT_TIMER = "ro.cdma.ecmexittimer";

    String PROPERTY_IDP_STRING = "ro.cdma.idpstring";

    String PROPERTY_OTASP_NUM_SCHEMA = "ro.cdma.otaspnumschema";

    String PROPERTY_DISABLE_CALL = "ro.telephony.disable-call";

    String PROPERTY_RIL_SENDS_MULTIPLE_CALL_RING =
            "ro.telephony.call_ring.multiple";

    String PROPERTY_CALL_RING_DELAY = "ro.telephony.call_ring.delay";

    String PROPERTY_CDMA_MSG_ID = "persist.radio.cdma.msgid";

    String PROPERTY_WAKE_LOCK_TIMEOUT = "ro.ril.wake_lock_timeout";

    String PROPERTY_RESET_ON_RADIO_TECH_CHANGE = "persist.radio.reset_on_switch";

    String PROPERTY_SMS_RECEIVE = "telephony.sms.receive";

    String PROPERTY_SMS_SEND = "telephony.sms.send";

    String PROPERTY_TEST_CSIM = "persist.radio.test-csim";

    String PROPERTY_IGNORE_NITZ = "telephony.test.ignore.nitz";
}
