package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

public interface SMILRegionElement extends SMILElement, ElementLayout {
    String getFit();

    void setFit(String fit)
            throws DOMException;

    int getLeft();

    void setLeft(int top)
            throws DOMException;

    int getTop();

    void setTop(int top)
            throws DOMException;

    int getZIndex();

    void setZIndex(int zIndex)
            throws DOMException;

}

