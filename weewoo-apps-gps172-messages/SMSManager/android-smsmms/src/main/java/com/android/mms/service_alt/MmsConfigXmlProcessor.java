package com.android.mms.service_alt;

import android.content.ContentValues;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import timber.log.Timber;

/*
 * XML processor for mms_config.xml
 */
public class MmsConfigXmlProcessor {

    private static final String TAG_MMS_CONFIG = "mms_config";
    private final StringBuilder mLogStringBuilder = new StringBuilder();
    private final XmlPullParser mInputParser;
    private MmsConfigHandler mMmsConfigHandler;

    private MmsConfigXmlProcessor(XmlPullParser parser) {
        mInputParser = parser;
        mMmsConfigHandler = null;
    }

    public static MmsConfigXmlProcessor get(XmlPullParser parser) {
        return new MmsConfigXmlProcessor(parser);
    }

    private static String xmlParserEventString(int event) {
        switch (event) {
            case XmlPullParser.START_DOCUMENT:
                return "START_DOCUMENT";
            case XmlPullParser.END_DOCUMENT:
                return "END_DOCUMENT";
            case XmlPullParser.START_TAG:
                return "START_TAG";
            case XmlPullParser.END_TAG:
                return "END_TAG";
            case XmlPullParser.TEXT:
                return "TEXT";
        }
        return Integer.toString(event);
    }

    public MmsConfigXmlProcessor setMmsConfigHandler(MmsConfigHandler handler) {
        mMmsConfigHandler = handler;
        return this;
    }

    private int advanceToNextEvent(int eventType) throws XmlPullParserException, IOException {
        for (; ; ) {
            int nextEvent = mInputParser.next();
            if (nextEvent == eventType
                    || nextEvent == XmlPullParser.END_DOCUMENT) {
                return nextEvent;
            }
        }
    }

    public void process() {
        try {
            if (advanceToNextEvent(XmlPullParser.START_TAG) != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("MmsConfigXmlProcessor: expecting start tag @"
                        + xmlParserDebugContext());
            }
            final ContentValues values = new ContentValues();
            String tagName = mInputParser.getName();
            if (TAG_MMS_CONFIG.equals(tagName)) {
                processMmsConfig();
            }
        } catch (IOException e) {
            Timber.e(e, "MmsConfigXmlProcessor: I/O failure " + e);
        } catch (XmlPullParserException e) {
            Timber.e(e, "MmsConfigXmlProcessor: parsing failure " + e);
        }
    }

    private String xmlParserDebugContext() {
        mLogStringBuilder.setLength(0);
        if (mInputParser != null) {
            try {
                final int eventType = mInputParser.getEventType();
                mLogStringBuilder.append(xmlParserEventString(eventType));
                if (eventType == XmlPullParser.START_TAG
                        || eventType == XmlPullParser.END_TAG
                        || eventType == XmlPullParser.TEXT) {
                    mLogStringBuilder.append('<').append(mInputParser.getName());
                    for (int i = 0; i < mInputParser.getAttributeCount(); i++) {
                        mLogStringBuilder.append(' ')
                                .append(mInputParser.getAttributeName(i))
                                .append('=')
                                .append(mInputParser.getAttributeValue(i));
                    }
                    mLogStringBuilder.append("/>");
                }
                return mLogStringBuilder.toString();
            } catch (XmlPullParserException e) {
                Timber.e(e, "xmlParserDebugContext: " + e);
            }
        }
        return "Unknown";
    }

    private void processMmsConfig()
            throws IOException, XmlPullParserException {
        // We are at the start tag
        for (; ; ) {
            int nextEvent;
            // Skipping spaces
            while ((nextEvent = mInputParser.next()) == XmlPullParser.TEXT) ;
            if (nextEvent == XmlPullParser.START_TAG) {
                processMmsConfigKeyValue();
            } else if (nextEvent == XmlPullParser.END_TAG) {
                break;
            } else {
                throw new XmlPullParserException("MmsConfig: expecting start or end tag @"
                        + xmlParserDebugContext());
            }
        }
    }

    private void processMmsConfigKeyValue() throws IOException, XmlPullParserException {
        final String key = mInputParser.getAttributeValue(null, "name");
        final String type = mInputParser.getName();
        int nextEvent = mInputParser.next();
        String value = null;
        if (nextEvent == XmlPullParser.TEXT) {
            value = mInputParser.getText();
            nextEvent = mInputParser.next();
        }
        if (nextEvent != XmlPullParser.END_TAG) {
            throw new XmlPullParserException("MmsConfigXmlProcessor: expecting end tag @"
                    + xmlParserDebugContext());
        }
        if (MmsConfig.isValidKey(key, type)) {
            if (mMmsConfigHandler != null) {
                mMmsConfigHandler.process(key, value, type);
            }
        } else {
            Timber.w("MmsConfig: invalid key=" + key + " or type=" + type);
        }
    }

    public interface MmsConfigHandler {
        void process(String key, String value, String type);
    }
}
