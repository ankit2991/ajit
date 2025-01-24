package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

public interface ElementLayout {
    String getTitle();

    void setTitle(String title)
            throws DOMException;

    String getBackgroundColor();

    void setBackgroundColor(String backgroundColor)
            throws DOMException;

    int getHeight();

    void setHeight(int height)
            throws DOMException;

    int getWidth();

    void setWidth(int width)
            throws DOMException;

}

