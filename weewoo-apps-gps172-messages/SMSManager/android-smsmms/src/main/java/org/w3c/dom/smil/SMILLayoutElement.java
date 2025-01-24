package org.w3c.dom.smil;

import org.w3c.dom.NodeList;

public interface SMILLayoutElement extends SMILElement {
    String getType();

    boolean getResolved();

    SMILRootLayoutElement getRootLayout();

    NodeList getRegions();
}

