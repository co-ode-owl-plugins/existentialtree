package org.coode.existentialtree.model2;

import org.coode.existentialtree.util.AbstractExistentialVisitorAdapter;
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
public class PropertyNode implements ExistentialNode<OWLPropertyExpression> {

    private Set<OWLObject> objects = new HashSet<OWLObject>();
    private OWLPropertyExpression property;
    private OWLDescriptionNode parent;
    private Set<ExistentialNode> children = new HashSet<ExistentialNode>();
    private List<ExistentialNode> orderedChildren;
    private OWLExistentialTreeModel model;

    /**
     * @param objects
     * @param parent
     * @param property
     * @param model
     */
    public PropertyNode(Set<OWLObject> objects, OWLDescriptionNode parent,
                        OWLPropertyExpression property,
                        OWLExistentialTreeModel model) {

        this.model = model;
        this.parent = parent;
        this.property = property;

        // @@TODO could be lazy about this??
        ChildrenBuilder builder = new ChildrenBuilder(parent.getUserObject(), model.getOntologies());
        for (OWLObject object : objects){
            object.accept(builder);
        }
        
        orderedChildren = new ArrayList<ExistentialNode>(children);
        Collections.sort(orderedChildren, model.getComparator());
    }

    public OWLPropertyExpression getProperty(){
        return property;
    }

    public OWLDescriptionNode getParent(){
        return parent;
    }

    public List<ExistentialNode> getChildren(){
        return Collections.unmodifiableList(orderedChildren);
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
    

    class ChildrenBuilder extends AbstractExistentialVisitorAdapter {

        public ChildrenBuilder(OWLObject base, Set<OWLOntology> onts) {
            super(base, onts);
        }

        protected void handleRestriction(OWLQuantifiedRestriction restriction) {
            if (restriction.getProperty().equals(property)){
                if (restriction.getFiller() instanceof OWLDescription){
                    children.add(new OWLDescriptionNode((OWLDescription)restriction.getFiller(), model));
                }
                else{
                    children.add(new OWLDataRangeNode((OWLDataRange)restriction.getFiller()));
                }
            }
        }

        protected int getMinCardinality() {
            return model.getMin();
        }
    }
}
