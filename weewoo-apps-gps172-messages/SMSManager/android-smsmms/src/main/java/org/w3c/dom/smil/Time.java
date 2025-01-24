package org.w3c.dom.smil;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

public interface Time {
    short SMIL_TIME_INDEFINITE = 0;
    short SMIL_TIME_OFFSET = 1;
    short SMIL_TIME_SYNC_BASED = 2;
    short SMIL_TIME_EVENT_BASED = 3;
    short SMIL_TIME_WALLCLOCK = 4;
    short SMIL_TIME_MEDIA_MARKER = 5;

    boolean getResolved();

    double getResolvedOffset();

    short getTimeType();

    double getOffset();

    void setOffset(double offset)
            throws DOMException;

    Element getBaseElement();

    void setBaseElement(Element baseElement)
            throws DOMException;

    boolean getBaseBegin();

    void setBaseBegin(boolean baseBegin)
            throws DOMException;

    String getEvent();

    void setEvent(String event)
            throws DOMException;

    String getMarker();

    void setMarker(String marker)
            throws DOMException;

}

