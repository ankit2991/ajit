package com.android.mms.dom.smil;

import com.android.mms.dom.NodeListImpl;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.smil.ElementSequentialTimeContainer;
import org.w3c.dom.smil.ElementTime;
import org.w3c.dom.smil.SMILElement;

import java.util.ArrayList;

public abstract class ElementSequentialTimeContainerImpl extends
        ElementTimeContainerImpl implements ElementSequentialTimeContainer {

    ElementSequentialTimeContainerImpl(SMILElement element) {
        super(element);
    }

    public NodeList getActiveChildrenAt(float instant) {
        NodeList allChildren = this.getTimeChildren();
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (int i = 0; i < allChildren.getLength(); i++) {
            instant -= ((ElementTime) allChildren.item(i)).getDur();
            if (instant < 0) {
                nodes.add(allChildren.item(i));
                return new NodeListImpl(nodes);
            }
        }
        return new NodeListImpl(nodes);
    }

    public float getDur() {
        float dur = super.getDur();
        if (dur == 0) {
            NodeList children = getTimeChildren();
            for (int i = 0; i < children.getLength(); ++i) {
                ElementTime child = (ElementTime) children.item(i);
                if (child.getDur() < 0) {
                    return -1.0F;
                }
                dur += child.getDur();
            }
        }
        return dur;
    }
}
