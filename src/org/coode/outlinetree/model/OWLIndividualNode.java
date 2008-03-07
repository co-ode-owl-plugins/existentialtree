package org.coode.outlinetree.model;

import org.semanticweb.owl.model.OWLIndividual;

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
class OWLIndividualNode extends AbstractOutlineNode<OWLIndividual, OWLPropertyNode> {

    private OWLIndividual individual;

    public OWLIndividualNode(OWLIndividual individual, OutlineTreeModel model) {
        super(model);
        this.individual = individual;
    }

    public OWLIndividual getUserObject() {
        return individual;
    }

    public OWLIndividual getRenderedObject() {
        return individual;
    }

    public List getChildren() {
        return Collections.EMPTY_LIST;
    }

    public boolean isNavigable() {
        return true;
    }

    public boolean equals(Object object) {
        return object instanceof OWLIndividualNode &&
                individual.equals(((OWLIndividualNode)object).getUserObject());
    }

    protected void clear() {
        // do nothing
    }
}
