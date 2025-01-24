package com.android.mms.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.TypeInfo;

public class AttrImpl extends NodeImpl implements Attr {
    private final String mName;
    private String mValue;


    protected AttrImpl(DocumentImpl owner, String name) {
        super(owner);
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public Element getOwnerElement() {
        return null;
    }

    public boolean getSpecified() {
        return mValue != null;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) throws DOMException {
        mValue = value;
    }

    @Override
    public String getNodeName() {
        return mName;
    }

    @Override
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }

    @Override
    public Node getParentNode() {
        return null;
    }

    @Override
    public Node getPreviousSibling() {
        return null;
    }

    @Override
    public Node getNextSibling() {
        return null;
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {
        setValue(nodeValue);
    }

    public TypeInfo getSchemaTypeInfo() {
        return null;
    }

    public boolean isId() {
        return false;
    }
}
