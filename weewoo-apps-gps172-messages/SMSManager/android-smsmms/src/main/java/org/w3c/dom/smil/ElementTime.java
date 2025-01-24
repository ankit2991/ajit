package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

public interface ElementTime {
    short RESTART_ALWAYS = 0;
    short RESTART_NEVER = 1;
    short RESTART_WHEN_NOT_ACTIVE = 2;
    short FILL_REMOVE = 0;
    short FILL_FREEZE = 1;
    short FILL_AUTO = 2;

    TimeList getBegin();

    void setBegin(TimeList begin)
            throws DOMException;

    TimeList getEnd();

    void setEnd(TimeList end)
            throws DOMException;

    float getDur();

    void setDur(float dur)
            throws DOMException;

    short getRestart();

    void setRestart(short restart)
            throws DOMException;

    short getFill();

    void setFill(short fill)
            throws DOMException;

    float getRepeatCount();

    void setRepeatCount(float repeatCount)
            throws DOMException;

    float getRepeatDur();

    void setRepeatDur(float repeatDur)
            throws DOMException;

    boolean beginElement();

    boolean endElement();

    void pauseElement();

    void resumeElement();

    void seekElement(float seekTo);

    short getFillDefault();

    void setFillDefault(short fillDefault)
            throws DOMException;

}

