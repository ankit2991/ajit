package com.android.mms.dom.events;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

public class EventImpl implements Event {

    private final long mTimeStamp = System.currentTimeMillis();
    private String mEventType;
    private boolean mCanBubble;
    private boolean mCancelable;
    // FIXME: Can we use mEventType for this purpose?
    private boolean mInitialized;
    private EventTarget mTarget;
    private short mEventPhase;
    private boolean mStopPropagation;
    private boolean mPreventDefault;
    private EventTarget mCurrentTarget;
    private int mSeekTo;

    public boolean getBubbles() {
        return mCanBubble;
    }

    public boolean getCancelable() {
        return mCancelable;
    }

    public EventTarget getCurrentTarget() {
        return mCurrentTarget;
    }

    void setCurrentTarget(EventTarget currentTarget) {
        mCurrentTarget = currentTarget;
    }

    public short getEventPhase() {
        return mEventPhase;
    }

    void setEventPhase(short eventPhase) {
        mEventPhase = eventPhase;
    }

    public EventTarget getTarget() {
        return mTarget;
    }

    void setTarget(EventTarget target) {
        mTarget = target;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public String getType() {
        return mEventType;
    }

    public void initEvent(String eventTypeArg, boolean canBubbleArg,
                          boolean cancelableArg) {
        mEventType = eventTypeArg;
        mCanBubble = canBubbleArg;
        mCancelable = cancelableArg;
        mInitialized = true;
    }

    public void initEvent(String eventTypeArg, boolean canBubbleArg, boolean cancelableArg,
                          int seekTo) {
        mSeekTo = seekTo;
        initEvent(eventTypeArg, canBubbleArg, cancelableArg);
    }

    public void preventDefault() {
        mPreventDefault = true;
    }

    public void stopPropagation() {
        mStopPropagation = true;
    }

    boolean isInitialized() {
        return mInitialized;
    }

    boolean isPreventDefault() {
        return mPreventDefault;
    }

    boolean isPropogationStopped() {
        return mStopPropagation;
    }

    public int getSeekTo() {
        return mSeekTo;
    }
}
