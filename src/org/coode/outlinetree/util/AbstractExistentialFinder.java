package org.coode.outlinetree.util;

import org.semanticweb.owl.model.*;
import org.semanticweb.owl.util.OWLObjectVisitorAdapter;

import java.util.HashSet;
import java.util.Set;
/*
* Copyright (C) 2007, University of Manchester
*
* Modifications to the initial code base are copyright of their
* respective authors, or their employers as appropriate.  Authorship
* of the modifications may be determined from the ChangeLog placed at
* the end of this file.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.

* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.

* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 30, 2007<br><br>
 *
 * Visits all object and data restrictions of cardinality > 0 and calls handleQuantifiedRestriction().
 * Also pulls them out of intersections and sub and equiv class axioms (including those inherited) 
 */
public abstract class AbstractExistentialFinder extends OWLObjectVisitorAdapter {

    private Set<OWLObject> visitedObjects = new HashSet<OWLObject>();

    public AbstractExistentialFinder(Set<OWLOntology> onts) {
    }


    public void visit(OWLSubClassAxiom owlSubClassAxiom) {
        if (!visitedObjects.contains(owlSubClassAxiom)){
            visitedObjects.add(owlSubClassAxiom); // prevent cycles
            owlSubClassAxiom.getSuperClass().accept(this);
        }
    }

    public void visit(OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
        if (!visitedObjects.contains(owlEquivalentClassesAxiom)){
            visitedObjects.add(owlEquivalentClassesAxiom); // prevent cycles
            Set<OWLDescription> equivs = owlEquivalentClassesAxiom.getDescriptions();
            for (OWLDescription equiv : equivs){
//                if (!visitedObjects.contains(base)){
                    equiv.accept(this);
//                }
            }
        }
    }

    public void visit(OWLObjectSomeRestriction restriction) {
        handleQuantifiedRestriction(restriction);
    }

    public void visit(OWLDataSomeRestriction restriction) {
        handleQuantifiedRestriction(restriction);
    }

    public void visit(OWLObjectMinCardinalityRestriction restriction) {
        handleCardinality(restriction);
    }

    public void visit(OWLObjectExactCardinalityRestriction restriction) {
        handleCardinality(restriction);
    }

    public void visit(OWLDataMinCardinalityRestriction restriction){
        handleCardinality(restriction);
    }

    public void visit(OWLDataExactCardinalityRestriction restriction){
        handleCardinality(restriction);
    }

    public void visit(OWLObjectIntersectionOf owlObjectIntersectionOf) {
        if (!visitedObjects.contains(owlObjectIntersectionOf)){
            visitedObjects.add(owlObjectIntersectionOf);
            for (OWLDescription desc : owlObjectIntersectionOf.getOperands()) {
                desc.accept(this);
            }
        }
    }

    protected void handleCardinality(OWLCardinalityRestriction restriction) {
        if (!visitedObjects.contains(restriction)){
            visitedObjects.add(restriction);
            if (restriction.getCardinality() >= getMinCardinality()){
                handleQuantifiedRestriction(restriction);
            }
        }
    }

    protected abstract void handleQuantifiedRestriction(OWLQuantifiedRestriction restriction);

    protected int getMinCardinality() {
        return 1;
    }
}
