package org.coode.outlinetree.util;

import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLRestriction;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Mar 5, 2008<br><br>
 *
 *
 * on top of existentials and cardinality restrictions also accummulate value restrictions
 */
public abstract class OutlineRestrictionVisitor extends AbstractExistentialFinder {

    private int min;

    public OutlineRestrictionVisitor(int min) {
        this.min = min;
    }


    @Override
    public void visit(OWLDataHasValue restriction) {
        handleRestriction(restriction);
    }


    @Override
    public void visit(OWLObjectHasValue restriction) {
        handleRestriction(restriction);
    }


    @Override
    protected void handleQuantifiedRestriction(OWLQuantifiedRestriction restriction) {
        handleRestriction(restriction);
    }


    @Override
    protected int getMinCardinality() {
        return min;
    }

    protected abstract void handleRestriction(OWLRestriction restriction);
}
