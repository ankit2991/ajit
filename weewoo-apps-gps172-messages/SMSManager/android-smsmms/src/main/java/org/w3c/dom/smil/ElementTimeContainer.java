package org.w3c.dom.smil;

import org.w3c.dom.NodeList;

public interface ElementTimeContainer extends ElementTime {
    NodeList getTimeChildren();

    NodeList getActiveChildrenAt(float instant);

}

