package org.coode.existentialtree.model2;

import org.semanticweb.owl.model.OWLConstant;

import java.util.Collections;
import java.util.List;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Nov 2, 2007<br><br>
 */
public class OWLConstantNode extends AbstractFillerNode<OWLConstant> {

    private OWLConstant constant;

    public OWLConstantNode(OWLConstant constant) {
        this.constant = constant;
    }

    public OWLConstant getUserObject() {
        return constant;
    }

    public OWLConstant getRenderedObject() {
        return constant;
    }

    public List getChildren() {
        return Collections.EMPTY_LIST;
    }

    public boolean isNavigable() {
        return false;
    }

    public boolean equals(Object object) {
        return object instanceof OWLConstantNode &&
                constant.equals(((OWLConstantNode)object).getUserObject());
    }

    protected void clear() {
        // do nothing
    }
}
