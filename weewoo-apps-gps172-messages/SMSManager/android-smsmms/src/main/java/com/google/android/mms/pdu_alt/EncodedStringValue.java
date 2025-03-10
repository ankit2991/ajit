package com.google.android.mms.pdu_alt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import timber.log.Timber;

public class EncodedStringValue implements Cloneable {
    private static final boolean LOCAL_LOGV = false;

    private int mCharacterSet;

    private byte[] mData;

    public EncodedStringValue(int charset, byte[] data) {
        if (null == data) {
            throw new NullPointerException("EncodedStringValue: Text-string is null.");
        }

        mCharacterSet = charset;
        mData = new byte[data.length];
        System.arraycopy(data, 0, mData, 0, data.length);
    }

    public EncodedStringValue(byte[] data) {
        this(CharacterSets.DEFAULT_CHARSET, data);
    }

    public EncodedStringValue(String data) {
        try {
            mData = data.getBytes(CharacterSets.DEFAULT_CHARSET_NAME);
            mCharacterSet = CharacterSets.DEFAULT_CHARSET;
        } catch (UnsupportedEncodingException e) {
            Timber.e(e, "Default encoding must be supported.");
        }
    }

    public static EncodedStringValue[] extract(String src) {
        String[] values = src.split(";");

        ArrayList<EncodedStringValue> list = new ArrayList<EncodedStringValue>();
        for (int i = 0; i < values.length; i++) {
            if (values[i].length() > 0) {
                list.add(new EncodedStringValue(values[i]));
            }
        }

        int len = list.size();
        if (len > 0) {
            return list.toArray(new EncodedStringValue[len]);
        } else {
            return null;
        }
    }

    public static String concat(EncodedStringValue[] addr) {
        StringBuilder sb = new StringBuilder();
        int maxIndex = addr.length - 1;
        for (int i = 0; i <= maxIndex; i++) {
            sb.append(addr[i].getString());
            if (i < maxIndex) {
                sb.append(";");
            }
        }

        return sb.toString();
    }

    public static EncodedStringValue copy(EncodedStringValue value) {
        if (value == null) {
            return null;
        }

        return new EncodedStringValue(value.mCharacterSet, value.mData);
    }

    public static EncodedStringValue[] encodeStrings(String[] array) {
        int count = array.length;
        if (count > 0) {
            EncodedStringValue[] encodedArray = new EncodedStringValue[count];
            for (int i = 0; i < count; i++) {
                encodedArray[i] = new EncodedStringValue(array[i]);
            }
            return encodedArray;
        }
        return null;
    }

    public int getCharacterSet() {
        return mCharacterSet;
    }

    public void setCharacterSet(int charset) {
        mCharacterSet = charset;
    }

    public byte[] getTextString() {
        byte[] byteArray = new byte[mData.length];

        System.arraycopy(mData, 0, byteArray, 0, mData.length);
        return byteArray;
    }

    public void setTextString(byte[] textString) {
        if (null == textString) {
            throw new NullPointerException("EncodedStringValue: Text-string is null.");
        }

        mData = new byte[textString.length];
        System.arraycopy(textString, 0, mData, 0, textString.length);
    }

    public String getString() {
        if (CharacterSets.ANY_CHARSET == mCharacterSet) {
            return new String(mData); // system default encoding.
        } else {
            try {
                String name = CharacterSets.getMimeName(mCharacterSet);
                return new String(mData, name);
            } catch (UnsupportedEncodingException e) {
                if (LOCAL_LOGV) {
                    Timber.v(e, e.getMessage());
                }
                return new String(mData, StandardCharsets.ISO_8859_1);
            }
        }
    }

    public void appendTextString(byte[] textString) {
        if (null == textString) {
            throw new NullPointerException("Text-string is null.");
        }

        if (null == mData) {
            mData = new byte[textString.length];
            System.arraycopy(textString, 0, mData, 0, textString.length);
        } else {
            ByteArrayOutputStream newTextString = new ByteArrayOutputStream();
            try {
                newTextString.write(mData);
                newTextString.write(textString);
            } catch (IOException e) {
                Timber.e(e, "logging error");
                e.printStackTrace();
                throw new NullPointerException(
                        "appendTextString: failed when write a new Text-string");
            }

            mData = newTextString.toByteArray();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        int len = mData.length;
        byte[] dstBytes = new byte[len];
        System.arraycopy(mData, 0, dstBytes, 0, len);

        try {
            return new EncodedStringValue(mCharacterSet, dstBytes);
        } catch (Exception e) {
            Timber.e(e, "logging error");
            e.printStackTrace();
            throw new CloneNotSupportedException(e.getMessage());
        }
    }

    public EncodedStringValue[] split(String pattern) {
        String[] temp = getString().split(pattern);
        EncodedStringValue[] ret = new EncodedStringValue[temp.length];
        for (int i = 0; i < ret.length; ++i) {
            try {
                ret[i] = new EncodedStringValue(mCharacterSet,
                        temp[i].getBytes());
            } catch (NullPointerException e) {
                // Can't arrive here
                return null;
            }
        }
        return ret;
    }
}
