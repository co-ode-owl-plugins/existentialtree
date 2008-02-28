package org.coode.existentialtree.model2;

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
public class OWLIndividualNode extends AbstractFillerNode<OWLIndividual> {

    private OWLIndividual individual;

    public OWLIndividualNode(OWLIndividual individual) {
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
