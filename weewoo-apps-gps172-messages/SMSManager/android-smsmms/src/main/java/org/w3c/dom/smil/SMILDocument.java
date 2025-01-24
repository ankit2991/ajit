package org.w3c.dom.smil;

import org.w3c.dom.Document;

public interface SMILDocument extends Document, ElementSequentialTimeContainer {

    SMILElement getHead();

    SMILElement getBody();

    SMILLayoutElement getLayout();
}

