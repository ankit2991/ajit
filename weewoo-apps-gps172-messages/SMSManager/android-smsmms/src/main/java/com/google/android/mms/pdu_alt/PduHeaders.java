package com.google.android.mms.pdu_alt;

import com.google.android.mms.InvalidHeaderValueException;

import java.util.ArrayList;
import java.util.HashMap;

public class PduHeaders {
    public static final int BCC = 0x81;
    public static final int CC = 0x82;
    public static final int CONTENT_LOCATION = 0x83;
    public static final int CONTENT_TYPE = 0x84;
    public static final int DATE = 0x85;
    public static final int DELIVERY_REPORT = 0x86;
    public static final int DELIVERY_TIME = 0x87;
    public static final int EXPIRY = 0x88;
    public static final int FROM = 0x89;
    public static final int MESSAGE_CLASS = 0x8A;
    public static final int MESSAGE_ID = 0x8B;
    public static final int MESSAGE_TYPE = 0x8C;
    public static final int MMS_VERSION = 0x8D;
    public static final int MESSAGE_SIZE = 0x8E;
    public static final int PRIORITY = 0x8F;

    public static final int READ_REPLY = 0x90;
    public static final int READ_REPORT = 0x90;
    public static final int REPORT_ALLOWED = 0x91;
    public static final int RESPONSE_STATUS = 0x92;
    public static final int RESPONSE_TEXT = 0x93;
    public static final int SENDER_VISIBILITY = 0x94;
    public static final int STATUS = 0x95;
    public static final int SUBJECT = 0x96;
    public static final int TO = 0x97;
    public static final int TRANSACTION_ID = 0x98;
    public static final int RETRIEVE_STATUS = 0x99;
    public static final int RETRIEVE_TEXT = 0x9A;
    public static final int READ_STATUS = 0x9B;
    public static final int REPLY_CHARGING = 0x9C;
    public static final int REPLY_CHARGING_DEADLINE = 0x9D;
    public static final int REPLY_CHARGING_ID = 0x9E;
    public static final int REPLY_CHARGING_SIZE = 0x9F;

    public static final int PREVIOUSLY_SENT_BY = 0xA0;
    public static final int PREVIOUSLY_SENT_DATE = 0xA1;
    public static final int STORE = 0xA2;
    public static final int MM_STATE = 0xA3;
    public static final int MM_FLAGS = 0xA4;
    public static final int STORE_STATUS = 0xA5;
    public static final int STORE_STATUS_TEXT = 0xA6;
    public static final int STORED = 0xA7;
    public static final int ATTRIBUTES = 0xA8;
    public static final int TOTALS = 0xA9;
    public static final int MBOX_TOTALS = 0xAA;
    public static final int QUOTAS = 0xAB;
    public static final int MBOX_QUOTAS = 0xAC;
    public static final int MESSAGE_COUNT = 0xAD;
    public static final int CONTENT = 0xAE;
    public static final int START = 0xAF;

    public static final int ADDITIONAL_HEADERS = 0xB0;
    public static final int DISTRIBUTION_INDICATOR = 0xB1;
    public static final int ELEMENT_DESCRIPTOR = 0xB2;
    public static final int LIMIT = 0xB3;
    public static final int RECOMMENDED_RETRIEVAL_MODE = 0xB4;
    public static final int RECOMMENDED_RETRIEVAL_MODE_TEXT = 0xB5;
    public static final int STATUS_TEXT = 0xB6;
    public static final int APPLIC_ID = 0xB7;
    public static final int REPLY_APPLIC_ID = 0xB8;
    public static final int AUX_APPLIC_ID = 0xB9;
    public static final int CONTENT_CLASS = 0xBA;
    public static final int DRM_CONTENT = 0xBB;
    public static final int ADAPTATION_ALLOWED = 0xBC;
    public static final int REPLACE_ID = 0xBD;
    public static final int CANCEL_ID = 0xBE;
    public static final int CANCEL_STATUS = 0xBF;

    public static final int MESSAGE_TYPE_SEND_REQ = 0x80;
    public static final int MESSAGE_TYPE_SEND_CONF = 0x81;
    public static final int MESSAGE_TYPE_NOTIFICATION_IND = 0x82;
    public static final int MESSAGE_TYPE_NOTIFYRESP_IND = 0x83;
    public static final int MESSAGE_TYPE_RETRIEVE_CONF = 0x84;
    public static final int MESSAGE_TYPE_ACKNOWLEDGE_IND = 0x85;
    public static final int MESSAGE_TYPE_DELIVERY_IND = 0x86;
    public static final int MESSAGE_TYPE_READ_REC_IND = 0x87;
    public static final int MESSAGE_TYPE_READ_ORIG_IND = 0x88;
    public static final int MESSAGE_TYPE_FORWARD_REQ = 0x89;
    public static final int MESSAGE_TYPE_FORWARD_CONF = 0x8A;
    public static final int MESSAGE_TYPE_MBOX_STORE_REQ = 0x8B;
    public static final int MESSAGE_TYPE_MBOX_STORE_CONF = 0x8C;
    public static final int MESSAGE_TYPE_MBOX_VIEW_REQ = 0x8D;
    public static final int MESSAGE_TYPE_MBOX_VIEW_CONF = 0x8E;
    public static final int MESSAGE_TYPE_MBOX_UPLOAD_REQ = 0x8F;
    public static final int MESSAGE_TYPE_MBOX_UPLOAD_CONF = 0x90;
    public static final int MESSAGE_TYPE_MBOX_DELETE_REQ = 0x91;
    public static final int MESSAGE_TYPE_MBOX_DELETE_CONF = 0x92;
    public static final int MESSAGE_TYPE_MBOX_DESCR = 0x93;
    public static final int MESSAGE_TYPE_DELETE_REQ = 0x94;
    public static final int MESSAGE_TYPE_DELETE_CONF = 0x95;
    public static final int MESSAGE_TYPE_CANCEL_REQ = 0x96;
    public static final int MESSAGE_TYPE_CANCEL_CONF = 0x97;

    public static final int VALUE_YES = 0x80;
    public static final int VALUE_NO = 0x81;

    public static final int VALUE_ABSOLUTE_TOKEN = 0x80;
    public static final int VALUE_RELATIVE_TOKEN = 0x81;

    public static final int MMS_VERSION_1_3 = ((1 << 4) | 3);
    public static final int MMS_VERSION_1_2 = ((1 << 4) | 2);
    public static final int MMS_VERSION_1_1 = ((1 << 4) | 1);
    public static final int MMS_VERSION_1_0 = ((1 << 4) | 0);

    // Current version is 1.2.
    public static final int CURRENT_MMS_VERSION = MMS_VERSION_1_2;

    public static final int FROM_ADDRESS_PRESENT_TOKEN = 0x80;
    public static final int FROM_INSERT_ADDRESS_TOKEN = 0x81;

    public static final String FROM_ADDRESS_PRESENT_TOKEN_STR = "address-present-token";
    public static final String FROM_INSERT_ADDRESS_TOKEN_STR = "insert-address-token";

    public static final int STATUS_EXPIRED = 0x80;
    public static final int STATUS_RETRIEVED = 0x81;
    public static final int STATUS_REJECTED = 0x82;
    public static final int STATUS_DEFERRED = 0x83;
    public static final int STATUS_UNRECOGNIZED = 0x84;
    public static final int STATUS_INDETERMINATE = 0x85;
    public static final int STATUS_FORWARDED = 0x86;
    public static final int STATUS_UNREACHABLE = 0x87;

    public static final int MM_FLAGS_ADD_TOKEN = 0x80;
    public static final int MM_FLAGS_REMOVE_TOKEN = 0x81;
    public static final int MM_FLAGS_FILTER_TOKEN = 0x82;

    public static final int MESSAGE_CLASS_PERSONAL = 0x80;
    public static final int MESSAGE_CLASS_ADVERTISEMENT = 0x81;
    public static final int MESSAGE_CLASS_INFORMATIONAL = 0x82;
    public static final int MESSAGE_CLASS_AUTO = 0x83;

    public static final String MESSAGE_CLASS_PERSONAL_STR = "personal";
    public static final String MESSAGE_CLASS_ADVERTISEMENT_STR = "advertisement";
    public static final String MESSAGE_CLASS_INFORMATIONAL_STR = "informational";
    public static final String MESSAGE_CLASS_AUTO_STR = "auto";

    public static final int PRIORITY_LOW = 0x80;
    public static final int PRIORITY_NORMAL = 0x81;
    public static final int PRIORITY_HIGH = 0x82;

    public static final int RESPONSE_STATUS_OK = 0x80;
    public static final int RESPONSE_STATUS_ERROR_UNSPECIFIED = 0x81;
    public static final int RESPONSE_STATUS_ERROR_SERVICE_DENIED = 0x82;

    public static final int RESPONSE_STATUS_ERROR_MESSAGE_FORMAT_CORRUPT = 0x83;
    public static final int RESPONSE_STATUS_ERROR_SENDING_ADDRESS_UNRESOLVED = 0x84;

    public static final int RESPONSE_STATUS_ERROR_MESSAGE_NOT_FOUND = 0x85;
    public static final int RESPONSE_STATUS_ERROR_NETWORK_PROBLEM = 0x86;
    public static final int RESPONSE_STATUS_ERROR_CONTENT_NOT_ACCEPTED = 0x87;
    public static final int RESPONSE_STATUS_ERROR_UNSUPPORTED_MESSAGE = 0x88;
    public static final int RESPONSE_STATUS_ERROR_TRANSIENT_FAILURE = 0xC0;

    public static final int RESPONSE_STATUS_ERROR_TRANSIENT_SENDNG_ADDRESS_UNRESOLVED = 0xC1;
    public static final int RESPONSE_STATUS_ERROR_TRANSIENT_MESSAGE_NOT_FOUND = 0xC2;
    public static final int RESPONSE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM = 0xC3;
    public static final int RESPONSE_STATUS_ERROR_TRANSIENT_PARTIAL_SUCCESS = 0xC4;

    public static final int RESPONSE_STATUS_ERROR_PERMANENT_FAILURE = 0xE0;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_SERVICE_DENIED = 0xE1;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_MESSAGE_FORMAT_CORRUPT = 0xE2;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_SENDING_ADDRESS_UNRESOLVED = 0xE3;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND = 0xE4;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_CONTENT_NOT_ACCEPTED = 0xE5;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_REPLY_CHARGING_LIMITATIONS_NOT_MET = 0xE6;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_REPLY_CHARGING_REQUEST_NOT_ACCEPTED = 0xE6;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_REPLY_CHARGING_FORWARDING_DENIED = 0xE8;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_REPLY_CHARGING_NOT_SUPPORTED = 0xE9;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_ADDRESS_HIDING_NOT_SUPPORTED = 0xEA;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_LACK_OF_PREPAID = 0xEB;
    public static final int RESPONSE_STATUS_ERROR_PERMANENT_END = 0xFF;

    public static final int RETRIEVE_STATUS_OK = 0x80;
    public static final int RETRIEVE_STATUS_ERROR_TRANSIENT_FAILURE = 0xC0;
    public static final int RETRIEVE_STATUS_ERROR_TRANSIENT_MESSAGE_NOT_FOUND = 0xC1;
    public static final int RETRIEVE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM = 0xC2;
    public static final int RETRIEVE_STATUS_ERROR_PERMANENT_FAILURE = 0xE0;
    public static final int RETRIEVE_STATUS_ERROR_PERMANENT_SERVICE_DENIED = 0xE1;
    public static final int RETRIEVE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND = 0xE2;
    public static final int RETRIEVE_STATUS_ERROR_PERMANENT_CONTENT_UNSUPPORTED = 0xE3;
    public static final int RETRIEVE_STATUS_ERROR_END = 0xFF;

    public static final int SENDER_VISIBILITY_HIDE = 0x80;
    public static final int SENDER_VISIBILITY_SHOW = 0x81;

    public static final int READ_STATUS_READ = 0x80;
    public static final int READ_STATUS__DELETED_WITHOUT_BEING_READ = 0x81;

    public static final int CANCEL_STATUS_REQUEST_SUCCESSFULLY_RECEIVED = 0x80;
    public static final int CANCEL_STATUS_REQUEST_CORRUPTED = 0x81;

    public static final int REPLY_CHARGING_REQUESTED = 0x80;
    public static final int REPLY_CHARGING_REQUESTED_TEXT_ONLY = 0x81;
    public static final int REPLY_CHARGING_ACCEPTED = 0x82;
    public static final int REPLY_CHARGING_ACCEPTED_TEXT_ONLY = 0x83;

    public static final int MM_STATE_DRAFT = 0x80;
    public static final int MM_STATE_SENT = 0x81;
    public static final int MM_STATE_NEW = 0x82;
    public static final int MM_STATE_RETRIEVED = 0x83;
    public static final int MM_STATE_FORWARDED = 0x84;

    public static final int RECOMMENDED_RETRIEVAL_MODE_MANUAL = 0x80;

    public static final int CONTENT_CLASS_TEXT = 0x80;
    public static final int CONTENT_CLASS_IMAGE_BASIC = 0x81;
    public static final int CONTENT_CLASS_IMAGE_RICH = 0x82;
    public static final int CONTENT_CLASS_VIDEO_BASIC = 0x83;
    public static final int CONTENT_CLASS_VIDEO_RICH = 0x84;
    public static final int CONTENT_CLASS_MEGAPIXEL = 0x85;
    public static final int CONTENT_CLASS_CONTENT_BASIC = 0x86;
    public static final int CONTENT_CLASS_CONTENT_RICH = 0x87;

    public static final int STORE_STATUS_SUCCESS = 0x80;
    public static final int STORE_STATUS_ERROR_TRANSIENT_FAILURE = 0xC0;
    public static final int STORE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM = 0xC1;
    public static final int STORE_STATUS_ERROR_PERMANENT_FAILURE = 0xE0;
    public static final int STORE_STATUS_ERROR_PERMANENT_SERVICE_DENIED = 0xE1;
    public static final int STORE_STATUS_ERROR_PERMANENT_MESSAGE_FORMAT_CORRUPT = 0xE2;
    public static final int STORE_STATUS_ERROR_PERMANENT_MESSAGE_NOT_FOUND = 0xE3;
    public static final int STORE_STATUS_ERROR_PERMANENT_MMBOX_FULL = 0xE4;
    public static final int STORE_STATUS_ERROR_END = 0xFF;

    private HashMap<Integer, Object> mHeaderMap = null;

    public PduHeaders() {
        mHeaderMap = new HashMap<Integer, Object>();
    }

    protected int getOctet(int field) {
        Integer octet = (Integer) mHeaderMap.get(field);
        if (null == octet) {
            return 0;
        }

        return octet;
    }

    protected void setOctet(int value, int field)
            throws InvalidHeaderValueException {

        switch (field) {
            case REPORT_ALLOWED:
            case ADAPTATION_ALLOWED:
            case DELIVERY_REPORT:
            case DRM_CONTENT:
            case DISTRIBUTION_INDICATOR:
            case QUOTAS:
            case READ_REPORT:
            case STORE:
            case STORED:
            case TOTALS:
            case SENDER_VISIBILITY:
                if ((VALUE_YES != value) && (VALUE_NO != value)) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case READ_STATUS:
                if ((READ_STATUS_READ != value) &&
                        (READ_STATUS__DELETED_WITHOUT_BEING_READ != value)) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case CANCEL_STATUS:
                if ((CANCEL_STATUS_REQUEST_SUCCESSFULLY_RECEIVED != value) &&
                        (CANCEL_STATUS_REQUEST_CORRUPTED != value)) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case PRIORITY:
                if ((value < PRIORITY_LOW) || (value > PRIORITY_HIGH)) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case STATUS:
                if ((value < STATUS_EXPIRED) || (value > STATUS_UNREACHABLE)) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case REPLY_CHARGING:
                if ((value < REPLY_CHARGING_REQUESTED)
                        || (value > REPLY_CHARGING_ACCEPTED_TEXT_ONLY)) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case MM_STATE:
                if ((value < MM_STATE_DRAFT) || (value > MM_STATE_FORWARDED)) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case RECOMMENDED_RETRIEVAL_MODE:
                if (RECOMMENDED_RETRIEVAL_MODE_MANUAL != value) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case CONTENT_CLASS:
                if ((value < CONTENT_CLASS_TEXT)
                        || (value > CONTENT_CLASS_CONTENT_RICH)) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            case RETRIEVE_STATUS:
                // According to oma-ts-mms-enc-v1_3, section 7.3.50, we modify the invalid value.
                if ((value > RETRIEVE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM) &&
                        (value < RETRIEVE_STATUS_ERROR_PERMANENT_FAILURE)) {
                    value = RETRIEVE_STATUS_ERROR_TRANSIENT_FAILURE;
                } else if ((value > RETRIEVE_STATUS_ERROR_PERMANENT_CONTENT_UNSUPPORTED) &&
                        (value <= RETRIEVE_STATUS_ERROR_END)) {
                    value = RETRIEVE_STATUS_ERROR_PERMANENT_FAILURE;
                } else if ((value < RETRIEVE_STATUS_OK) ||
                        ((value > RETRIEVE_STATUS_OK) &&
                                (value < RETRIEVE_STATUS_ERROR_TRANSIENT_FAILURE)) ||
                        (value > RETRIEVE_STATUS_ERROR_END)) {
                    value = RETRIEVE_STATUS_ERROR_PERMANENT_FAILURE;
                }
                break;
            case STORE_STATUS:
                // According to oma-ts-mms-enc-v1_3, section 7.3.58, we modify the invalid value.
                if ((value > STORE_STATUS_ERROR_TRANSIENT_NETWORK_PROBLEM) &&
                        (value < STORE_STATUS_ERROR_PERMANENT_FAILURE)) {
                    value = STORE_STATUS_ERROR_TRANSIENT_FAILURE;
                } else if ((value > STORE_STATUS_ERROR_PERMANENT_MMBOX_FULL) &&
                        (value <= STORE_STATUS_ERROR_END)) {
                    value = STORE_STATUS_ERROR_PERMANENT_FAILURE;
                } else if ((value < STORE_STATUS_SUCCESS) ||
                        ((value > STORE_STATUS_SUCCESS) &&
                                (value < STORE_STATUS_ERROR_TRANSIENT_FAILURE)) ||
                        (value > STORE_STATUS_ERROR_END)) {
                    value = STORE_STATUS_ERROR_PERMANENT_FAILURE;
                }
                break;
            case RESPONSE_STATUS:
                // According to oma-ts-mms-enc-v1_3, section 7.3.48, we modify the invalid value.
                if ((value > RESPONSE_STATUS_ERROR_TRANSIENT_PARTIAL_SUCCESS) &&
                        (value < RESPONSE_STATUS_ERROR_PERMANENT_FAILURE)) {
                    value = RESPONSE_STATUS_ERROR_TRANSIENT_FAILURE;
                } else if (((value > RESPONSE_STATUS_ERROR_PERMANENT_LACK_OF_PREPAID) &&
                        (value <= RESPONSE_STATUS_ERROR_PERMANENT_END)) ||
                        (value < RESPONSE_STATUS_OK) ||
                        ((value > RESPONSE_STATUS_ERROR_UNSUPPORTED_MESSAGE) &&
                                (value < RESPONSE_STATUS_ERROR_TRANSIENT_FAILURE)) ||
                        (value > RESPONSE_STATUS_ERROR_PERMANENT_END)) {
                    value = RESPONSE_STATUS_ERROR_PERMANENT_FAILURE;
                }
                break;
            case MMS_VERSION:
                if ((value < MMS_VERSION_1_0) || (value > MMS_VERSION_1_3)) {
                    value = CURRENT_MMS_VERSION; // Current version is the default value.
                }
                break;
            case MESSAGE_TYPE:
                if ((value < MESSAGE_TYPE_SEND_REQ) || (value > MESSAGE_TYPE_CANCEL_CONF)) {
                    // Invalid value.
                    throw new InvalidHeaderValueException("Invalid Octet value!");
                }
                break;
            default:
                // This header value should not be Octect.
                throw new RuntimeException("Invalid header field!");
        }
        mHeaderMap.put(field, value);
    }

    protected byte[] getTextString(int field) {
        return (byte[]) mHeaderMap.get(field);
    }

    protected void setTextString(byte[] value, int field) {
        /**
         * Check whether this field can be set for specific
         * header and check validity of the field.
         */
        if (null == value) {
            throw new NullPointerException();
        }

        switch (field) {
            case TRANSACTION_ID:
            case REPLY_CHARGING_ID:
            case AUX_APPLIC_ID:
            case APPLIC_ID:
            case REPLY_APPLIC_ID:
            case MESSAGE_ID:
            case REPLACE_ID:
            case CANCEL_ID:
            case CONTENT_LOCATION:
            case MESSAGE_CLASS:
            case CONTENT_TYPE:
                break;
            default:
                // This header value should not be Text-String.
                throw new RuntimeException("Invalid header field!");
        }
        mHeaderMap.put(field, value);
    }

    protected EncodedStringValue getEncodedStringValue(int field) {
        return (EncodedStringValue) mHeaderMap.get(field);
    }

    protected EncodedStringValue[] getEncodedStringValues(int field) {
        ArrayList<EncodedStringValue> list =
                (ArrayList<EncodedStringValue>) mHeaderMap.get(field);
        if (null == list) {
            return null;
        }
        EncodedStringValue[] values = new EncodedStringValue[list.size()];
        return list.toArray(values);
    }

    protected void setEncodedStringValue(EncodedStringValue value, int field) {
        /**
         * Check whether this field can be set for specific
         * header and check validity of the field.
         */
        if (null == value) {
            throw new NullPointerException();
        }

        switch (field) {
            case SUBJECT:
            case RECOMMENDED_RETRIEVAL_MODE_TEXT:
            case RETRIEVE_TEXT:
            case STATUS_TEXT:
            case STORE_STATUS_TEXT:
            case RESPONSE_TEXT:
            case FROM:
            case PREVIOUSLY_SENT_BY:
            case MM_FLAGS:
                break;
            default:
                // This header value should not be Encoded-String-Value.
                throw new RuntimeException("Invalid header field!");
        }

        mHeaderMap.put(field, value);
    }

    protected void setEncodedStringValues(EncodedStringValue[] value, int field) {

        if (null == value) {
            throw new NullPointerException();
        }

        switch (field) {
            case BCC:
            case CC:
            case TO:
                break;
            default:
                throw new RuntimeException("Invalid header field!");
        }

        ArrayList<EncodedStringValue> list = new ArrayList<EncodedStringValue>();
        for (int i = 0; i < value.length; i++) {
            list.add(value[i]);
        }
        mHeaderMap.put(field, list);
    }

    protected void appendEncodedStringValue(EncodedStringValue value,
                                            int field) {
        if (null == value) {
            throw new NullPointerException();
        }

        switch (field) {
            case BCC:
            case CC:
            case TO:
                break;
            default:
                throw new RuntimeException("Invalid header field!");
        }

        ArrayList<EncodedStringValue> list =
                (ArrayList<EncodedStringValue>) mHeaderMap.get(field);
        if (null == list) {
            list = new ArrayList<EncodedStringValue>();
        }
        list.add(value);
        mHeaderMap.put(field, list);
    }

    protected long getLongInteger(int field) {
        Long longInteger = (Long) mHeaderMap.get(field);
        if (null == longInteger) {
            return -1;
        }

        return longInteger.longValue();
    }

    protected void setLongInteger(long value, int field) {
        switch (field) {
            case DATE:
            case REPLY_CHARGING_SIZE:
            case MESSAGE_SIZE:
            case MESSAGE_COUNT:
            case START:
            case LIMIT:
            case DELIVERY_TIME:
            case EXPIRY:
            case REPLY_CHARGING_DEADLINE:
            case PREVIOUSLY_SENT_DATE:
                break;
            default:
                throw new RuntimeException("Invalid header field!");
        }
        mHeaderMap.put(field, value);
    }
}
