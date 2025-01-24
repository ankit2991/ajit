package com.android.mms.dom.smil;

import com.android.mms.dom.DocumentImpl;
import com.android.mms.dom.events.EventImpl;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.smil.ElementSequentialTimeContainer;
import org.w3c.dom.smil.ElementTime;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;
import org.w3c.dom.smil.SMILLayoutElement;
import org.w3c.dom.smil.TimeList;

public class SmilDocumentImpl extends DocumentImpl implements SMILDocument, DocumentEvent {
    public final static String SMIL_DOCUMENT_START_EVENT = "SmilDocumentStart";
    public final static String SMIL_DOCUMENT_END_EVENT = "SimlDocumentEnd";

    ElementSequentialTimeContainer mSeqTimeContainer;

    public SmilDocumentImpl() {
        super();
    }

    public NodeList getActiveChildrenAt(float instant) {
        return mSeqTimeContainer.getActiveChildrenAt(instant);
    }

    public NodeList getTimeChildren() {
        return mSeqTimeContainer.getTimeChildren();
    }

    public boolean beginElement() {
        return mSeqTimeContainer.beginElement();
    }

    public boolean endElement() {
        return mSeqTimeContainer.endElement();
    }

    public TimeList getBegin() {
        return mSeqTimeContainer.getBegin();
    }

    public void setBegin(TimeList begin) throws DOMException {
        mSeqTimeContainer.setBegin(begin);
    }

    public float getDur() {
        return mSeqTimeContainer.getDur();
    }

    public void setDur(float dur) throws DOMException {
        mSeqTimeContainer.setDur(dur);
    }

    public TimeList getEnd() {
        return mSeqTimeContainer.getEnd();
    }

    public void setEnd(TimeList end) throws DOMException {
        mSeqTimeContainer.setEnd(end);
    }

    public short getFill() {
        return mSeqTimeContainer.getFill();
    }

    public void setFill(short fill) throws DOMException {
        mSeqTimeContainer.setFill(fill);
    }

    public short getFillDefault() {
        return mSeqTimeContainer.getFillDefault();
    }

    public void setFillDefault(short fillDefault) throws DOMException {
        mSeqTimeContainer.setFillDefault(fillDefault);
    }

    public float getRepeatCount() {
        return mSeqTimeContainer.getRepeatCount();
    }

    public void setRepeatCount(float repeatCount) throws DOMException {
        mSeqTimeContainer.setRepeatCount(repeatCount);
    }

    public float getRepeatDur() {
        return mSeqTimeContainer.getRepeatDur();
    }

    public void setRepeatDur(float repeatDur) throws DOMException {
        mSeqTimeContainer.setRepeatDur(repeatDur);
    }

    public short getRestart() {
        return mSeqTimeContainer.getRestart();
    }

    public void setRestart(short restart) throws DOMException {
        mSeqTimeContainer.setRestart(restart);
    }

    public void pauseElement() {
        mSeqTimeContainer.pauseElement();
    }

    public void resumeElement() {
        mSeqTimeContainer.resumeElement();
    }

    public void seekElement(float seekTo) {
        mSeqTimeContainer.seekElement(seekTo);
    }

    @Override
    public Element createElement(String tagName) throws DOMException {
        tagName = tagName.toLowerCase();
        if (tagName.equals("text") ||
                tagName.equals("img") ||
                tagName.equals("video")) {
            return new SmilRegionMediaElementImpl(this, tagName);
        } else if (tagName.equals("audio")) {
            return new SmilMediaElementImpl(this, tagName);
        } else if (tagName.equals("layout")) {
            return new SmilLayoutElementImpl(this, tagName);
        } else if (tagName.equals("root-layout")) {
            return new SmilRootLayoutElementImpl(this, tagName);
        } else if (tagName.equals("region")) {
            return new SmilRegionElementImpl(this, tagName);
        } else if (tagName.equals("ref")) {
            return new SmilRefElementImpl(this, tagName);
        } else if (tagName.equals("par")) {
            return new SmilParElementImpl(this, tagName);
        } else if (tagName.equals("vcard")) {
            return new SmilRegionMediaElementImpl(this, tagName);
        } else {
            return new SmilElementImpl(this, tagName);
        }
    }

    @Override
    public SMILElement getDocumentElement() {
        Node rootElement = getFirstChild();
        if (rootElement == null || !(rootElement instanceof SMILElement)) {
            rootElement = createElement("smil");
            appendChild(rootElement);
        }

        return (SMILElement) rootElement;
    }

    public SMILElement getHead() {
        Node rootElement = getDocumentElement();
        Node headElement = rootElement.getFirstChild();
        if (headElement == null || !(headElement instanceof SMILElement)) {
            headElement = createElement("head");
            rootElement.appendChild(headElement);
        }

        return (SMILElement) headElement;
    }

    public SMILElement getBody() {
        Node rootElement = getDocumentElement();
        Node headElement = getHead();
        Node bodyElement = headElement.getNextSibling();
        if (bodyElement == null || !(bodyElement instanceof SMILElement)) {
            bodyElement = createElement("body");
            rootElement.appendChild(bodyElement);
        }

        mSeqTimeContainer = new ElementSequentialTimeContainerImpl((SMILElement) bodyElement) {
            public NodeList getTimeChildren() {
                return getBody().getElementsByTagName("par");
            }

            public boolean beginElement() {
                Event startEvent = createEvent("Event");
                startEvent.initEvent(SMIL_DOCUMENT_START_EVENT, false, false);
                dispatchEvent(startEvent);
                return true;
            }

            public boolean endElement() {
                Event endEvent = createEvent("Event");
                endEvent.initEvent(SMIL_DOCUMENT_END_EVENT, false, false);
                dispatchEvent(endEvent);
                return true;
            }

            public void pauseElement() {
                // TODO Auto-generated method stub

            }

            public void resumeElement() {
                // TODO Auto-generated method stub

            }

            public void seekElement(float seekTo) {
                // TODO Auto-generated method stub

            }

            ElementTime getParentElementTime() {
                return null;
            }
        };

        return (SMILElement) bodyElement;
    }

    public SMILLayoutElement getLayout() {
        Node headElement = getHead();
        Node layoutElement = null;

        layoutElement = headElement.getFirstChild();
        while ((layoutElement != null) && !(layoutElement instanceof SMILLayoutElement)) {
            layoutElement = layoutElement.getNextSibling();
        }

        if (layoutElement == null) {
            layoutElement = new SmilLayoutElementImpl(this, "layout");
            headElement.appendChild(layoutElement);
        }
        return (SMILLayoutElement) layoutElement;
    }

    public Event createEvent(String eventType) throws DOMException {
        if ("Event".equals(eventType)) {
            return new EventImpl();
        } else {
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                    "Not supported interface");
        }
    }
}
