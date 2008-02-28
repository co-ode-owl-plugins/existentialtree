package org.coode.existentialtree.model2;

import org.coode.existentialtree.util.AbstractExistentialVisitorAdapter;
import org.coode.existentialtree.util.AxiomAccumulator;
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
public class OWLPropertyNode implements OutlineNode<OWLPropertyExpression, OWLDescriptionNode> {

    private OWLPropertyExpression property;
    private OWLDescriptionNode parent;
    private Set<OutlineNode> children = new HashSet<OutlineNode>();
    private List<OutlineNode> orderedChildren;
    private OutlineTreeModel model;


    public OWLPropertyNode(OWLPropertyExpression property,
                           OutlineTreeModel model){
        this.model = model;
        this.property = property;
    }

    /**
     *
     * @param parent
     */
    public void setParent(OWLDescriptionNode parent){
        if (parent != this.parent){
            this.parent = parent;
            orderedChildren = null;
        }
    }

    public OWLPropertyExpression getProperty(){
        return property;
    }


    public OWLDescriptionNode getParent() {
        return parent;
    }


    public List<OutlineNode> getChildren(){
        if (orderedChildren == null){
            refresh();
        }
        return orderedChildren;
    }

    public boolean isNavigable() {
        return false;
    }

    public OWLPropertyExpression getUserObject() {
        return property;
    }

    public OWLPropertyExpression getRenderedObject() {
        return getUserObject();
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
        ChildrenBuilder builder = new ChildrenBuilder(parent.getUserObject(), model.getOntologies());

        AxiomAccumulator acc = new AxiomAccumulator(parent.getUserObject(), model.getOntologies(), model.getMin());
        for (OWLObject object : acc.filterObjectsForProp(property)){
            object.accept(builder);
        }

        orderedChildren = new ArrayList<OutlineNode>(children);
        Collections.sort(orderedChildren, model.getComparator());
        orderedChildren = Collections.unmodifiableList(orderedChildren);
    }

    /**
     * Deals with OWLDescriptions and OWLDataRange fillers
     * Also follows value restrictions
     * Filters out owlThing children
     */
    class ChildrenBuilder extends AbstractExistentialVisitorAdapter {

        public ChildrenBuilder(OWLObject base, Set<OWLOntology> onts) {
            super(base, onts);
        }


        public void visit(OWLDataValueRestriction restriction) {
            if (restriction.getProperty().equals(property)){
                children.add(model.createNode(restriction.getValue(), OWLPropertyNode.this));
            }
        }


        public void visit(OWLObjectValueRestriction restriction) {
            if (restriction.getProperty().equals(property)){
                children.add(model.createNode(restriction.getValue(), OWLPropertyNode.this));
            }
        }


        protected void handleQuantifiedRestriction(OWLQuantifiedRestriction restriction) {
            if (restriction.getProperty().equals(property)){
                final OWLPropertyRange filler = restriction.getFiller();
                if (!filler.equals(model.getOWLOntologyManager().getOWLDataFactory().getOWLThing())){
                    children.add(model.createNode(filler, OWLPropertyNode.this));
                }
            }
        }


        protected int getMinCardinality() {
            return model.getMin();
        }
    }
}
