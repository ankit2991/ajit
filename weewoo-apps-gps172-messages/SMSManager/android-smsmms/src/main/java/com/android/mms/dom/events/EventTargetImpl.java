package com.android.mms.dom.events;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.util.ArrayList;

import timber.log.Timber;

public class EventTargetImpl implements EventTarget {
    private final EventTarget mNodeTarget;
    private ArrayList<EventListenerEntry> mListenerEntries;

    public EventTargetImpl(EventTarget target) {
        mNodeTarget = target;
    }

    public void addEventListener(String type, EventListener listener, boolean useCapture) {
        if ((type == null) || type.equals("") || (listener == null)) {
            return;
        }

        removeEventListener(type, listener, useCapture);

        if (mListenerEntries == null) {
            mListenerEntries = new ArrayList<EventListenerEntry>();
        }
        mListenerEntries.add(new EventListenerEntry(type, listener, useCapture));
    }

    public boolean dispatchEvent(Event evt) throws EventException {
        EventImpl eventImpl = (EventImpl) evt;

        if (!eventImpl.isInitialized()) {
            throw new EventException(EventException.UNSPECIFIED_EVENT_TYPE_ERR,
                    "Event not initialized");
        } else if ((eventImpl.getType() == null) || eventImpl.getType().equals("")) {
            throw new EventException(EventException.UNSPECIFIED_EVENT_TYPE_ERR,
                    "Unspecified even type");
        }

        eventImpl.setTarget(mNodeTarget);

        eventImpl.setEventPhase(Event.AT_TARGET);
        eventImpl.setCurrentTarget(mNodeTarget);
        if (!eventImpl.isPropogationStopped() && (mListenerEntries != null)) {
            for (int i = 0; i < mListenerEntries.size(); i++) {
                EventListenerEntry listenerEntry = mListenerEntries.get(i);
                if (!listenerEntry.mUseCapture
                        && listenerEntry.mType.equals(eventImpl.getType())) {
                    try {
                        listenerEntry.mListener.handleEvent(eventImpl);
                    } catch (Exception e) {
                        Timber.w(e, "Catched EventListener exception");
                    }
                }
            }
        }

        if (eventImpl.getBubbles()) {
            // TODO: BUBBLING_PHASE skipped
        }

        return eventImpl.isPreventDefault();
    }

    public void removeEventListener(String type, EventListener listener,
                                    boolean useCapture) {
        if (null == mListenerEntries) {
            return;
        }
        for (int i = 0; i < mListenerEntries.size(); i++) {
            EventListenerEntry listenerEntry = mListenerEntries.get(i);
            if ((listenerEntry.mUseCapture == useCapture)
                    && (listenerEntry.mListener == listener)
                    && listenerEntry.mType.equals(type)) {
                mListenerEntries.remove(i);
                break;
            }
        }
    }

    static class EventListenerEntry {
        final String mType;
        final EventListener mListener;
        final boolean mUseCapture;

        EventListenerEntry(String type, EventListener listener, boolean useCapture) {
            mType = type;
            mListener = listener;
            mUseCapture = useCapture;
        }
    }

}
