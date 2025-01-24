package com.google.android.mms.pdu_alt;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class PduBody {
    private final Vector<PduPart> mParts;

    private final Map<String, PduPart> mPartMapByContentId;
    private final Map<String, PduPart> mPartMapByContentLocation;
    private final Map<String, PduPart> mPartMapByName;
    private final Map<String, PduPart> mPartMapByFileName;

    public PduBody() {
        mParts = new Vector<>();

        mPartMapByContentId = new HashMap<>();
        mPartMapByContentLocation = new HashMap<>();
        mPartMapByName = new HashMap<>();
        mPartMapByFileName = new HashMap<>();
    }

    private void putPartToMaps(PduPart part) {
        byte[] contentId = part.getContentId();
        if (null != contentId) {
            mPartMapByContentId.put(new String(contentId), part);
        }

        byte[] contentLocation = part.getContentLocation();
        if (null != contentLocation) {
            String clc = new String(contentLocation);
            mPartMapByContentLocation.put(clc, part);
        }

        byte[] name = part.getName();
        if (null != name) {
            String clc = new String(name);
            mPartMapByName.put(clc, part);
        }

        byte[] fileName = part.getFilename();
        if (null != fileName) {
            String clc = new String(fileName);
            mPartMapByFileName.put(clc, part);
        }
    }

    public boolean addPart(PduPart part) {
        if (null == part) {
            throw new NullPointerException();
        }

        putPartToMaps(part);
        return mParts.add(part);
    }

    public void addPart(int index, PduPart part) {
        if (null == part) {
            throw new NullPointerException();
        }

        putPartToMaps(part);
        mParts.add(index, part);
    }

    public PduPart removePart(int index) {
        return mParts.remove(index);
    }

    public void removeAll() {
        mParts.clear();
    }

    public PduPart getPart(int index) {
        return mParts.get(index);
    }

    public int getPartIndex(PduPart part) {
        return mParts.indexOf(part);
    }

    public int getPartsNum() {
        return mParts.size();
    }

    public PduPart getPartByContentId(String cid) {
        return mPartMapByContentId.get(cid);
    }

    public PduPart getPartByContentLocation(String contentLocation) {
        return mPartMapByContentLocation.get(contentLocation);
    }

    public PduPart getPartByName(String name) {
        return mPartMapByName.get(name);
    }

    public PduPart getPartByFileName(String filename) {
        return mPartMapByFileName.get(filename);
    }
}
