package com.google.android.mms.pdu_alt;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class CharacterSets {
    public static final int ANY_CHARSET = 0x00;
    public static final int US_ASCII = 0x03;
    public static final int ISO_8859_1 = 0x04;
    public static final int ISO_8859_2 = 0x05;
    public static final int ISO_8859_3 = 0x06;
    public static final int ISO_8859_4 = 0x07;
    public static final int ISO_8859_5 = 0x08;
    public static final int ISO_8859_6 = 0x09;
    public static final int ISO_8859_7 = 0x0A;
    public static final int ISO_8859_8 = 0x0B;
    public static final int ISO_8859_9 = 0x0C;
    public static final int SHIFT_JIS = 0x11;
    public static final int UTF_8 = 0x6A;
    public static final int BIG5 = 0x07EA;
    public static final int UCS2 = 0x03E8;
    public static final int UTF_16 = 0x03F7;

    public static final int DEFAULT_CHARSET = UTF_8;
    public static final String MIMENAME_ANY_CHARSET = "*";
    public static final String MIMENAME_US_ASCII = "us-ascii";
    public static final String MIMENAME_ISO_8859_1 = "iso-8859-1";
    public static final String MIMENAME_ISO_8859_2 = "iso-8859-2";
    public static final String MIMENAME_ISO_8859_3 = "iso-8859-3";
    public static final String MIMENAME_ISO_8859_4 = "iso-8859-4";
    public static final String MIMENAME_ISO_8859_5 = "iso-8859-5";
    public static final String MIMENAME_ISO_8859_6 = "iso-8859-6";
    public static final String MIMENAME_ISO_8859_7 = "iso-8859-7";
    public static final String MIMENAME_ISO_8859_8 = "iso-8859-8";
    public static final String MIMENAME_ISO_8859_9 = "iso-8859-9";
    public static final String MIMENAME_SHIFT_JIS = "shift_JIS";
    public static final String MIMENAME_UTF_8 = "utf-8";
    public static final String MIMENAME_BIG5 = "big5";
    public static final String MIMENAME_UCS2 = "iso-10646-ucs-2";
    public static final String MIMENAME_UTF_16 = "utf-16";
    public static final String DEFAULT_CHARSET_NAME = MIMENAME_UTF_8;
    private static final int[] MIBENUM_NUMBERS = {
            ANY_CHARSET,
            US_ASCII,
            ISO_8859_1,
            ISO_8859_2,
            ISO_8859_3,
            ISO_8859_4,
            ISO_8859_5,
            ISO_8859_6,
            ISO_8859_7,
            ISO_8859_8,
            ISO_8859_9,
            SHIFT_JIS,
            UTF_8,
            BIG5,
            UCS2,
            UTF_16,
    };
    private static final String[] MIME_NAMES = {
            MIMENAME_ANY_CHARSET,
            MIMENAME_US_ASCII,
            MIMENAME_ISO_8859_1,
            MIMENAME_ISO_8859_2,
            MIMENAME_ISO_8859_3,
            MIMENAME_ISO_8859_4,
            MIMENAME_ISO_8859_5,
            MIMENAME_ISO_8859_6,
            MIMENAME_ISO_8859_7,
            MIMENAME_ISO_8859_8,
            MIMENAME_ISO_8859_9,
            MIMENAME_SHIFT_JIS,
            MIMENAME_UTF_8,
            MIMENAME_BIG5,
            MIMENAME_UCS2,
            MIMENAME_UTF_16,
    };

    private static final HashMap<Integer, String> MIBENUM_TO_NAME_MAP;
    private static final HashMap<String, Integer> NAME_TO_MIBENUM_MAP;

    static {
        // Create the HashMaps.
        MIBENUM_TO_NAME_MAP = new HashMap<Integer, String>();
        NAME_TO_MIBENUM_MAP = new HashMap<String, Integer>();
        assert (MIBENUM_NUMBERS.length == MIME_NAMES.length);
        int count = MIBENUM_NUMBERS.length - 1;
        for (int i = 0; i <= count; i++) {
            MIBENUM_TO_NAME_MAP.put(MIBENUM_NUMBERS[i], MIME_NAMES[i]);
            NAME_TO_MIBENUM_MAP.put(MIME_NAMES[i], MIBENUM_NUMBERS[i]);
        }
    }

    private CharacterSets() {
    }

    public static String getMimeName(int mibEnumValue)
            throws UnsupportedEncodingException {
        String name = MIBENUM_TO_NAME_MAP.get(mibEnumValue);
        if (name == null) {
            throw new UnsupportedEncodingException();
        }
        return name;
    }

    public static int getMibEnumValue(String mimeName)
            throws UnsupportedEncodingException {
        if (null == mimeName) {
            return -1;
        }

        Integer mibEnumValue = NAME_TO_MIBENUM_MAP.get(mimeName);
        if (mibEnumValue == null) {
            throw new UnsupportedEncodingException();
        }
        return mibEnumValue;
    }
}
