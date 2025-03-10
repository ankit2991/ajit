package com.android.mms.dom;

import com.android.mms.dom.events.EventTargetImpl;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.util.NoSuchElementException;
import java.util.Vector;

public abstract class NodeImpl implements Node, EventTarget {
    private final Vector<Node> mChildNodes = new Vector<Node>();
    private final EventTarget mEventTarget = new EventTargetImpl(this);
    DocumentImpl mOwnerDocument;
    private Node mParentNode;

    protected NodeImpl(DocumentImpl owner) {
        mOwnerDocument = owner;
    }

    public Node appendChild(Node newChild) throws DOMException {
        ((NodeImpl) newChild).setParentNode(this);
        mChildNodes.remove(newChild);
        mChildNodes.add(newChild);
        return newChild;
    }

    public Node cloneNode(boolean deep) {
        return null;
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public NodeList getChildNodes() {
        return new NodeListImpl(this, null, false);
    }

    public Node getFirstChild() {
        Node firstChild = null;
        try {
            firstChild = mChildNodes.firstElement();
        } catch (NoSuchElementException e) {
        }
        return firstChild;
    }

    public Node getLastChild() {
        Node lastChild = null;
        try {
            lastChild = mChildNodes.lastElement();
        } catch (NoSuchElementException e) {
        }
        return lastChild;
    }

    public String getLocalName() {
        return null;
    }

    public String getNamespaceURI() {
        return null;
    }

    public Node getNextSibling() {
        if ((mParentNode != null) && (this != mParentNode.getLastChild())) {
            Vector<Node> siblings = ((NodeImpl) mParentNode).mChildNodes;
            int indexOfThis = siblings.indexOf(this);
            return siblings.elementAt(indexOfThis + 1);
        }
        return null;
    }

    public abstract String getNodeName();

    public abstract short getNodeType();

    public String getNodeValue() throws DOMException {
        return null;
    }

    public void setNodeValue(String nodeValue) throws DOMException {
    }

    public Document getOwnerDocument() {
        return mOwnerDocument;
    }

    public Node getParentNode() {
        return mParentNode;
    }

    private void setParentNode(Node parentNode) {
        mParentNode = parentNode;
    }

    public String getPrefix() {
        return null;
    }

    public void setPrefix(String prefix) throws DOMException {
    }

    public Node getPreviousSibling() {
        if ((mParentNode != null) && (this != mParentNode.getFirstChild())) {
            Vector<Node> siblings = ((NodeImpl) mParentNode).mChildNodes;
            int indexOfThis = siblings.indexOf(this);
            return siblings.elementAt(indexOfThis - 1);
        }
        return null;
    }

    public boolean hasAttributes() {
        return false;
    }

    public boolean hasChildNodes() {
        return !(mChildNodes.isEmpty());
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isSupported(String feature, String version) {
        // TODO Auto-generated method stub
        return false;
    }

    public void normalize() {
        // TODO Auto-generated method stub
    }

    public Node removeChild(Node oldChild) throws DOMException {
        if (mChildNodes.contains(oldChild)) {
            mChildNodes.remove(oldChild);
            ((NodeImpl) oldChild).setParentNode(null);
        } else {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Child does not exist");
        }
        return null;
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        if (mChildNodes.contains(oldChild)) {
            try {
                mChildNodes.remove(newChild);
            } catch (DOMException e) {
            }
            mChildNodes.setElementAt(newChild, mChildNodes.indexOf(oldChild));
            ((NodeImpl) newChild).setParentNode(this);
            ((NodeImpl) oldChild).setParentNode(null);
        } else {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Old child does not exist");
        }
        return oldChild;
    }

    public void addEventListener(String type, EventListener listener, boolean useCapture) {
        mEventTarget.addEventListener(type, listener, useCapture);
    }

    public void removeEventListener(String type, EventListener listener, boolean useCapture) {
        mEventTarget.removeEventListener(type, listener, useCapture);
    }

    public boolean dispatchEvent(Event evt) throws EventException {
        return mEventTarget.dispatchEvent(evt);
    }

    public String getBaseURI() {
        return null;
    }

    public short compareDocumentPosition(Node other) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public String getTextContent() throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public void setTextContent(String textContent) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public boolean isSameNode(Node other) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public String lookupPrefix(String namespaceURI) {
        return null;
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public String lookupNamespaceURI(String prefix) {
        return null;
    }

    public boolean isEqualNode(Node arg) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public Object getFeature(String feature, String version) {
        return null;
    }

    public Object setUserData(String key, Object data,
                              UserDataHandler handler) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public Object getUserData(String key) {
        return null;
    }
}
