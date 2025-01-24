package com.android.mms.dom.smil;

import org.w3c.dom.DOMException;
import org.w3c.dom.smil.ElementTime;
import org.w3c.dom.smil.SMILElement;
import org.w3c.dom.smil.Time;
import org.w3c.dom.smil.TimeList;

import java.util.ArrayList;

import timber.log.Timber;

public abstract class ElementTimeImpl implements ElementTime {

    private static final String FILL_REMOVE_ATTRIBUTE = "remove";
    private static final String FILL_FREEZE_ATTRIBUTE = "freeze";
    private static final String FILL_HOLD_ATTRIBUTE = "hold";
    private static final String FILL_TRANSITION_ATTRIBUTE = "transition";
    private static final String FILL_AUTO_ATTRIBUTE = "auto";
    private static final String FILL_ATTRIBUTE_NAME = "fill";
    private static final String FILLDEFAULT_ATTRIBUTE_NAME = "fillDefault";

    final SMILElement mSmilElement;

    ElementTimeImpl(SMILElement element) {
        mSmilElement = element;
    }

    int getBeginConstraints() {
        return TimeImpl.ALLOW_ALL;
    }

    int getEndConstraints() {
        return TimeImpl.ALLOW_ALL;
    }

    abstract ElementTime getParentElementTime();

    public TimeList getBegin() {
        String[] beginTimeStringList = mSmilElement.getAttribute("begin").split(";");

        // TODO: Check other constraints on parsed values, e.g., "single, non-negative offset values
        ArrayList<Time> beginTimeList = new ArrayList<Time>();
        for (int i = 0; i < beginTimeStringList.length; i++) {
            try {
                beginTimeList.add(new TimeImpl(beginTimeStringList[i], getBeginConstraints()));
            } catch (IllegalArgumentException e) {
            }
        }
        if (beginTimeList.size() == 0) {

            beginTimeList.add(new TimeImpl("0", TimeImpl.ALLOW_ALL));
        }
        return new TimeListImpl(beginTimeList);
    }

    public void setBegin(TimeList begin) throws DOMException {
        // TODO Implement this
        mSmilElement.setAttribute("begin", "indefinite");
    }

    public float getDur() {
        float dur = 0;
        try {
            String durString = mSmilElement.getAttribute("dur");
            if (durString != null) {
                dur = TimeImpl.parseClockValue(durString) / 1000f;
            }
        } catch (IllegalArgumentException e) {

        }

        return dur;
    }

    public void setDur(float dur) throws DOMException {
        mSmilElement.setAttribute("dur", (int) (dur * 1000) + "ms");
    }

    public TimeList getEnd() {
        ArrayList<Time> endTimeList = new ArrayList<Time>();

        String[] endTimeStringList = mSmilElement.getAttribute("end").split(";");
        int len = endTimeStringList.length;
        if (!((len == 1) && (endTimeStringList[0].length() == 0))) {
            for (int i = 0; i < len; i++) {
                try {
                    endTimeList.add(new TimeImpl(endTimeStringList[i],
                            getEndConstraints()));
                } catch (IllegalArgumentException e) {
                    Timber.e(e, "Malformed time value.");
                }
            }
        }

        if (endTimeList.size() == 0) {
            float duration = getDur();

            if (duration < 0) {
                endTimeList.add(new TimeImpl("indefinite", getEndConstraints()));
            } else {
                TimeList begin = getBegin();
                for (int i = 0; i < begin.getLength(); i++) {
                    endTimeList.add(new TimeImpl(
                            begin.item(i).getResolvedOffset() + duration + "s",
                            getEndConstraints()));
                }
            }
        }

        return new TimeListImpl(endTimeList);
    }

    public void setEnd(TimeList end) throws DOMException {
        // TODO Implement this
        mSmilElement.setAttribute("end", "indefinite");
    }

    private boolean beginAndEndAreZero() {
        TimeList begin = getBegin();
        TimeList end = getEnd();
        if (begin.getLength() == 1 && end.getLength() == 1) {
            Time beginTime = begin.item(0);
            Time endTime = end.item(0);
            return beginTime.getOffset() == 0. && endTime.getOffset() == 0.;
        }
        return false;
    }

    public short getFill() {
        String fill = mSmilElement.getAttribute(FILL_ATTRIBUTE_NAME);
        if (fill.equalsIgnoreCase(FILL_FREEZE_ATTRIBUTE)) {
            return FILL_FREEZE;
        } else if (fill.equalsIgnoreCase(FILL_REMOVE_ATTRIBUTE)) {
            return FILL_REMOVE;
        } else if (fill.equalsIgnoreCase(FILL_HOLD_ATTRIBUTE)) {
            return FILL_FREEZE;
        } else if (fill.equalsIgnoreCase(FILL_TRANSITION_ATTRIBUTE)) {
            return FILL_FREEZE;
        } else if (!fill.equalsIgnoreCase(FILL_AUTO_ATTRIBUTE)) {
            short fillDefault = getFillDefault();
            if (fillDefault != FILL_AUTO) {
                return fillDefault;
            }
        }

        if (((mSmilElement.getAttribute("dur").length() == 0) &&
                (mSmilElement.getAttribute("end").length() == 0) &&
                (mSmilElement.getAttribute("repeatCount").length() == 0) &&
                (mSmilElement.getAttribute("repeatDur").length() == 0)) ||
                beginAndEndAreZero()) {
            return FILL_FREEZE;
        } else {
            return FILL_REMOVE;
        }
    }

    public void setFill(short fill) throws DOMException {
        if (fill == FILL_FREEZE) {
            mSmilElement.setAttribute(FILL_ATTRIBUTE_NAME, FILL_FREEZE_ATTRIBUTE);
        } else {
            mSmilElement.setAttribute(FILL_ATTRIBUTE_NAME, FILL_REMOVE_ATTRIBUTE); // default
        }
    }

    public short getFillDefault() {
        String fillDefault = mSmilElement.getAttribute(FILLDEFAULT_ATTRIBUTE_NAME);
        if (fillDefault.equalsIgnoreCase(FILL_REMOVE_ATTRIBUTE)) {
            return FILL_REMOVE;
        } else if (fillDefault.equalsIgnoreCase(FILL_FREEZE_ATTRIBUTE)) {
            return FILL_FREEZE;
        } else if (fillDefault.equalsIgnoreCase(FILL_AUTO_ATTRIBUTE)) {
            return FILL_AUTO;
        } else if (fillDefault.equalsIgnoreCase(FILL_HOLD_ATTRIBUTE)) {
            // FIXME handle it as freeze for now
            return FILL_FREEZE;
        } else if (fillDefault.equalsIgnoreCase(FILL_TRANSITION_ATTRIBUTE)) {
            // FIXME handle it as freeze for now
            return FILL_FREEZE;
        } else {

            ElementTime parent = getParentElementTime();
            if (parent == null) {
                return FILL_AUTO;
            } else {
                return parent.getFillDefault();
            }
        }
    }

    public void setFillDefault(short fillDefault) throws DOMException {
        if (fillDefault == FILL_FREEZE) {
            mSmilElement.setAttribute(FILLDEFAULT_ATTRIBUTE_NAME, FILL_FREEZE_ATTRIBUTE);
        } else {
            mSmilElement.setAttribute(FILLDEFAULT_ATTRIBUTE_NAME, FILL_REMOVE_ATTRIBUTE);
        }
    }

    public float getRepeatCount() {
        String repeatCount = mSmilElement.getAttribute("repeatCount");
        try {
            float value = Float.parseFloat(repeatCount);
            if (value > 0) {
                return value;
            } else {
                return 0;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setRepeatCount(float repeatCount) throws DOMException {
        String repeatCountString = "indefinite";
        if (repeatCount > 0) {
            repeatCountString = Float.toString(repeatCount);
        }
        mSmilElement.setAttribute("repeatCount", repeatCountString);
    }

    public float getRepeatDur() {
        try {
            float repeatDur =
                    TimeImpl.parseClockValue(mSmilElement.getAttribute("repeatDur"));
            if (repeatDur > 0) {
                return repeatDur;
            } else {
                return 0;
            }
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public void setRepeatDur(float repeatDur) throws DOMException {
        String repeatDurString = "indefinite";
        if (repeatDur > 0) {
            repeatDurString = repeatDur + "ms";
        }
        mSmilElement.setAttribute("repeatDur", repeatDurString);
    }

    public short getRestart() {
        String restart = mSmilElement.getAttribute("restart");
        if (restart.equalsIgnoreCase("never")) {
            return RESTART_NEVER;
        } else if (restart.equalsIgnoreCase("whenNotActive")) {
            return RESTART_WHEN_NOT_ACTIVE;
        } else {
            return RESTART_ALWAYS;
        }
    }

    public void setRestart(short restart) throws DOMException {
        if (restart == RESTART_NEVER) {
            mSmilElement.setAttribute("restart", "never");
        } else if (restart == RESTART_WHEN_NOT_ACTIVE) {
            mSmilElement.setAttribute("restart", "whenNotActive");
        } else {
            mSmilElement.setAttribute("restart", "always");
        }
    }
}
