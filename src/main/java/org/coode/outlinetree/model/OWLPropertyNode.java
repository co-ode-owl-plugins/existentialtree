package org.coode.outlinetree.model;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.coode.outlinetree.util.AbstractExistentialFinder;
import org.coode.outlinetree.util.OutlineRestrictionVisitor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLRestriction;

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
class OWLPropertyNode extends AbstractOutlineNode<Set<OWLRestriction>/*OWLPropertyExpression*/, OutlineNode<OWLClassExpression, OutlineNode>> {

    protected OWLPropertyExpression property;
    private List<OutlineNode> orderedChildren;

    protected Set<OutlineNode> children = new HashSet<OutlineNode>();
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

    public Class<? extends OWLObject> getTypeOfChild() {
        return OWLClass.class;
    }

    public Set<OWLRestriction> getUserObject() {
        return new HashSet<OWLRestriction>(restrs);
    }

    public OWLPropertyExpression getRenderedObject() {
        return property;
    }

    @Override
    public String toString() {
        return getUserObject().toString();
    }

    private void refresh() {
        children.clear();

        ChildrenBuilder builder = new ChildrenBuilder();

        for (OWLRestriction restriction : restrs){
            restriction.accept(builder);
        }

        orderedChildren = new ArrayList<OutlineNode>(children);
        Collections.sort(orderedChildren, getModel().getComparator());
        orderedChildren = Collections.unmodifiableList(orderedChildren);
    }

    @Override
    protected void clear() {
        orderedChildren = null;
    }

    public void setRestrictions(Set<OWLRestriction> restrs) {
        this.restrs = restrs;
    }

    // maps the restrictions back to the axioms that contain them
    protected Set<OWLAxiom> getAxiomsForRestriction(OWLRestriction restriction) {
        if (getParent().getUserObject() instanceof OWLClass){ // potentially multiple axioms to try
            Set<OWLAxiom> matchingAxioms = new HashSet<OWLAxiom>();
            for (OWLAxiom ax : getAxioms()){
                AxiomFinder v = new AxiomFinder(getModel().getMin());
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
     *
     * Dangerous - the restriction could be in a different axiom in a different place in the tree
     */
    class AxiomFinder extends OutlineRestrictionVisitor {

        private boolean result = false;
        private OWLRestriction searchRestriction;

        public AxiomFinder(int min) {
            super(min);
        }

        public boolean doesAxiomContainRestriction(OWLAxiom ax, OWLRestriction restriction){
            result = false;
            searchRestriction = restriction;
            ax.accept(this);
            return result;
        }

        @Override
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

        @Override
        public void visit(OWLDataHasValue restriction) {
            if (restriction.getProperty().equals(property)){
                OutlineNode child = getModel().createNode(restriction.getFiller(), OWLPropertyNode.this);
                child.addAxioms(getAxiomsForRestriction(restriction));
                children.add(child);
            }
        }


        @Override
        public void visit(OWLObjectHasValue restriction) {
            if (restriction.getProperty().equals(property)){
                OutlineNode child = getModel().createNode(restriction.getFiller(), OWLPropertyNode.this);
                child.addAxioms(getAxiomsForRestriction(restriction));
                children.add(child);
            }
        }


        @Override
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


        @Override
        protected int getMinCardinality() {
            return getModel().getMin();
        }
    }
}
