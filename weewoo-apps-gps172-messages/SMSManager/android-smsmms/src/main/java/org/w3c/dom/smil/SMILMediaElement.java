package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

public interface SMILMediaElement extends ElementTime, SMILElement {
    String getAbstractAttr();

    void setAbstractAttr(String abstractAttr)
            throws DOMException;

    String getAlt();

    void setAlt(String alt)
            throws DOMException;

    String getAuthor();

    void setAuthor(String author)
            throws DOMException;

    String getClipBegin();

    void setClipBegin(String clipBegin)
            throws DOMException;

    String getClipEnd();

    void setClipEnd(String clipEnd)
            throws DOMException;

    String getCopyright();

    void setCopyright(String copyright)
            throws DOMException;

    String getLongdesc();

    void setLongdesc(String longdesc)
            throws DOMException;

    String getPort();

    void setPort(String port)
            throws DOMException;

    String getReadIndex();

    void setReadIndex(String readIndex)
            throws DOMException;

    String getRtpformat();

    void setRtpformat(String rtpformat)
            throws DOMException;

    String getSrc();

    void setSrc(String src)
            throws DOMException;

    String getStripRepeat();

    void setStripRepeat(String stripRepeat)
            throws DOMException;

    String getTitle();

    void setTitle(String title)
            throws DOMException;

    String getTransport();

    void setTransport(String transport)
            throws DOMException;

    String getType();

    void setType(String type)
            throws DOMException;

}

