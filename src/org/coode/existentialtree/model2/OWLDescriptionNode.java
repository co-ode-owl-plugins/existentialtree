package org.coode.existentialtree.model2;

import org.coode.existentialtree.util.AxiomAccumulator;
import org.semanticweb.owl.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
 * Date: Oct 29, 2007<br><br>
 */
public class OWLDescriptionNode extends AbstractFillerNode<OWLDescription> {

    private OWLDescription descr;
    private List<OutlineNode> children;
    private OutlineTreeModel model;

    public OWLDescriptionNode(OWLDescription descr, OutlineTreeModel model){
        this.descr = descr;
        this.model = model;
    }

    public OWLDescription getUserObject() {
        return descr;
    }

    public OWLDescription getRenderedObject() {
        OWLDescription renderedObject = descr; // default to rendering the object directly
        if (descr instanceof OWLClass){

        }
        else if (getChildren().size() > 0){ // if there are any children, we should be able to name the object
            renderedObject =  model.getOWLThing(); // default to owl:Thing

            // if we have an intersection containing a named class
            if (descr instanceof OWLObjectIntersectionOf){
                OWLClass root = getFirstNamedClassFromIntersection((OWLObjectIntersectionOf) descr);
                if (root != null){
                    renderedObject = root;
                }
            }
        }
        return renderedObject;
    }

    public List<OutlineNode> getChildren() {
        if (children == null){
            refresh();
        }
        return children;
    }

    private void refresh() {
        children = new ArrayList<OutlineNode>();
        AxiomAccumulator acc = new AxiomAccumulator(descr, model.getOntologies(), model.getMin());
        Set<OWLObject> objects = acc.getObjectsForDescription();
        if (!objects.isEmpty()){
            Set<OWLPropertyExpression> filterproperties = model.getProperties();
            Set<OWLPropertyExpression> properties;
            if (filterproperties == null){
                properties = acc.getUsedProperties();
            }
            else{
                properties = new HashSet<OWLPropertyExpression>(filterproperties);
            }
            for (OWLPropertyExpression prop : properties){
                Set<OWLObject> owlAxioms = acc.filterObjectsForProp(prop);
                if (owlAxioms != null){
                    final OWLPropertyNode child = model.createNode(prop, this);
                    child.setParent(this);
                    children.add(child);
                }
            }
        }
    }

    public boolean isNavigable() {
        return descr instanceof OWLClass;
    }

    public String toString() {
        return getUserObject().toString();
    }

    public boolean equals(Object object) {
        return object instanceof OWLDescriptionNode &&
                descr.equals(((OWLDescriptionNode)object).getUserObject());
    }

    private OWLClass getFirstNamedClassFromIntersection(OWLObjectIntersectionOf intersectionOf) {
        for (OWLDescription op : intersectionOf.getOperands()){
            if (op instanceof OWLClass){
                return (OWLClass)op;
            }
        }
        return null;
    }

    protected void clear() {
        children = null;
    }
}
