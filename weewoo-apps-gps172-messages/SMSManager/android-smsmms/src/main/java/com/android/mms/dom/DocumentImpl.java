package com.android.mms.dom;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public abstract class DocumentImpl extends NodeImpl implements Document {

    public DocumentImpl() {
        super(null);
    }

    public Attr createAttribute(String name) throws DOMException {
        return new AttrImpl(this, name);
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public CDATASection createCDATASection(String data) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Comment createComment(String data) {
        // TODO Auto-generated method stub
        return null;
    }

    public DocumentFragment createDocumentFragment() {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract Element createElement(String tagName) throws DOMException;

    public Element createElementNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public EntityReference createEntityReference(String name) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public ProcessingInstruction createProcessingInstruction(String target, String data)
            throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Text createTextNode(String data) {
        // TODO Auto-generated method stub
        return null;
    }

    public DocumentType getDoctype() {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract Element getDocumentElement();

    public Element getElementById(String elementId) {
        // TODO Auto-generated method stub
        return null;
    }

    public NodeList getElementsByTagName(String tagname) {
        // TODO Auto-generated method stub
        return null;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        // TODO Auto-generated method stub
        return null;
    }

    public DOMImplementation getImplementation() {
        // TODO Auto-generated method stub
        return null;
    }

    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public short getNodeType() {
        return Node.DOCUMENT_NODE;
    }

    @Override
    public String getNodeName() {
        return "#document";
    }

    public String getInputEncoding() {
        return null;
    }

    public String getXmlEncoding() {
        return null;
    }

    public boolean getXmlStandalone() {
        return false;
    }

    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
    }

    public String getXmlVersion() {
        return null;
    }

    public void setXmlVersion(String xmlVersion) throws DOMException {
    }

    public boolean getStrictErrorChecking() {
        return true;
    }

    public void setStrictErrorChecking(boolean strictErrorChecking) {
    }

    public String getDocumentURI() {
        return null;
    }

    public void setDocumentURI(String documentURI) {
    }

    public Node adoptNode(Node source) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public DOMConfiguration getDomConfig() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public void normalizeDocument() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }

    public Node renameNode(Node n, String namespaceURI, String qualifiedName)
            throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, null);
    }
}
