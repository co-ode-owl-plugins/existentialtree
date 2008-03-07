package org.coode.outlinetree.model;

import org.coode.outlinetree.util.AbstractExistentialFinder;
import org.coode.outlinetree.util.OutlineRestrictionVisitor;
import org.semanticweb.owl.model.*;

import java.util.*;
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
 * Date: Oct 5, 2007<br><br>
 *
 * Needed for existential tree that shows intermediate properties.
 * Each property node in the tree must be distinct so the tree can get the correct children
 *
 * eg Existential:
 * A subclassOf(p some B)
 * A subclassOf(p some C)
 *
 * shows as
 *
 * A
 * |-p
 *   |-B
 *   |-C
 *
 * eg Relations:
 * p(a, b)
 * p(a, c)
 *
 * shows as
 *
 * a
 * |-p
 *   |-b
 *   |-c
 *
 * Each property node is therefore a collection:
 * Multiple objects that relate a single description or individual to multiple fillers or individuals
 */
class OWLPropertyNode extends AbstractOutlineNode<OWLPropertyExpression, OutlineNode<OWLDescription, OutlineNode>> {

    private OWLPropertyExpression property;
    private List<OutlineNode> orderedChildren;

    private Set<OutlineNode> children = new HashSet<OutlineNode>();
    private Set<OWLRestriction> restrs = new HashSet<OWLRestriction>();


    public OWLPropertyNode(OWLPropertyExpression property,
                           OutlineTreeModel model){
        super(model);
        this.property = property;
    }

    public List<OutlineNode> getChildren(){
        if (orderedChildren == null){
            refresh();
        }
        return orderedChildren;
    }

    public boolean isNavigable() {
        return true;
    }

    public OWLPropertyExpression getUserObject() {
        return property;
    }

    public OWLPropertyExpression getRenderedObject() {
        return property;
    }

    public String toString() {
        return getUserObject().toString();
    }

    public boolean equals(Object object) {
        return object instanceof OWLPropertyNode &&
                property.equals(((OWLPropertyNode)object).getUserObject());
    }

    private void refresh() {
        children.clear();

        ChildrenBuilder builder = new ChildrenBuilder(getModel().getOntologies());

        for (OWLRestriction restriction : restrs){
            restriction.accept(builder);
        }

        orderedChildren = new ArrayList<OutlineNode>(children);
        Collections.sort(orderedChildren, getModel().getComparator());
        orderedChildren = Collections.unmodifiableList(orderedChildren);
    }

    protected void clear() {
        orderedChildren = null;
    }

    public void setRestrictions(Set<OWLRestriction> restrs) {
        this.restrs = restrs;
    }

    // maps the restrictions back to the axioms that contain them
    private Set<OWLAxiom> getAxiomsForRestriction(OWLRestriction restriction) {
        if (getParent().getUserObject() instanceof OWLClass){ // potentially multiple axioms to try
            Set<OWLAxiom> matchingAxioms = new HashSet<OWLAxiom>();
            for (OWLAxiom ax : getAxioms()){
                AxiomFinder v = new AxiomFinder(getModel().getOntologies(), getModel().getMin());
                if (v.doesAxiomContainRestriction(ax, restriction)){
                    matchingAxioms.add(ax);
                }
            }
            return matchingAxioms;
        }
        else{
            return getAxioms(); // always just pass the current axiom
        }
    }

    /**
     * Used to map the restrictions back to the axioms that contain them
     */
    class AxiomFinder extends OutlineRestrictionVisitor {

        private boolean result = false;
        private OWLRestriction searchRestriction;

        public AxiomFinder(Set<OWLOntology> onts, int min) {
            super(onts, min);
        }

        public boolean doesAxiomContainRestriction(OWLAxiom ax, OWLRestriction restriction){
            result = false;
            searchRestriction = restriction;
            ax.accept(this);
            return result;
        }

        protected void handleRestriction(OWLRestriction restriction) {
            if (searchRestriction.equals(restriction)){
                result = true;
            }
        }
    }

    /**
     * Filters out owlThing children
     */
    class ChildrenBuilder extends AbstractExistentialFinder {

        public ChildrenBuilder(Set<OWLOntology> onts) {
            super(onts);
        }


        public void visit(OWLDataValueRestriction restriction) {
            if (restriction.getProperty().equals(property)){
                OutlineNode child = getModel().createNode(restriction.getValue(), OWLPropertyNode.this);
                child.addAxioms(getAxiomsForRestriction(restriction));
                children.add(child);
            }
        }


        public void visit(OWLObjectValueRestriction restriction) {
            if (restriction.getProperty().equals(property)){
                OutlineNode child = getModel().createNode(restriction.getValue(), OWLPropertyNode.this);
                child.addAxioms(getAxiomsForRestriction(restriction));
                children.add(child);
            }
        }


        protected void handleQuantifiedRestriction(OWLQuantifiedRestriction restriction) {
            if (restriction.getProperty().equals(property)){
                final OWLPropertyRange filler = restriction.getFiller();
                if (!filler.equals(getModel().getOWLOntologyManager().getOWLDataFactory().getOWLThing())){
                    OutlineNode child = getModel().createNode(filler, OWLPropertyNode.this);
                    child.addAxioms(getAxiomsForRestriction(restriction));
                    children.add(child);
                }
            }
        }


        protected int getMinCardinality() {
            return getModel().getMin();
        }
    }
}
