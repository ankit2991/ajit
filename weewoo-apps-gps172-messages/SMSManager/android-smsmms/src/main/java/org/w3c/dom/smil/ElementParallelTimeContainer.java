package org.w3c.dom.smil;

import org.w3c.dom.DOMException;

public interface ElementParallelTimeContainer extends ElementTimeContainer {
    String getEndSync();

    void setEndSync(String endSync)
            throws DOMException;

    float getImplicitDuration();

}

