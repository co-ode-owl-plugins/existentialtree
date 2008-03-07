package org.coode.outlinetree.model;

import org.coode.outlinetree.util.OutlinePropertyIndexer;
import org.coode.outlinetree.util.SuperAndEquivAxiomFinder;
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
 *
 * Node for anonymous class expressions
 */
class OWLAnonymousClassNode extends AbstractOutlineNode<OWLDescription, OWLPropertyNode> {

    private OWLDescription descr;
    private List<OutlineNode> children;

    public OWLAnonymousClassNode(OWLDescription descr, OutlineTreeModel model){
        super(model);
        this.descr = descr;
    }

    public OWLDescription getUserObject() {
        return descr;
    }

    public OWLDescription getRenderedObject() {
        OWLDescription renderedObject = descr; // default to rendering the object directly
        if (descr instanceof OWLClass){

        }
        else if (getChildren().size() > 0){ // if there are any children, we should be able to name the object
            renderedObject =  getModel().getOWLThing(); // default to owl:Thing

            // if we have an intersection containing a single named class, show that
            if (descr instanceof OWLObjectIntersectionOf){
                Set<OWLClass> namedClasses = getNamedClassesFromIntersection((OWLObjectIntersectionOf) descr);
                if (namedClasses.size() == 1){
                    renderedObject = namedClasses.iterator().next();
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
        OutlineTreeModel model = getModel();

        // below just used to find the properties used
        OutlinePropertyIndexer finder = new OutlinePropertyIndexer(model.getOntologies(), model.getMin());
        descr.accept(finder);
        Set<OWLPropertyExpression> properties = finder.getProperties();

        // check for property filter and only follow those properties
        Set<OWLPropertyExpression> filterproperties = model.getFilterProperties();
        if (filterproperties != null){
            properties.retainAll(filterproperties);
        }

        // now find the subset of the axioms in this description that are pertinent to the property child nodes

        // for each of the "properties on the class/description" create a child node
        for (OWLPropertyExpression prop : properties){
            final OWLPropertyNode child = model.createNode(prop, this);
            child.addAxioms(getAxioms()); // all children of this node now only have the same axiom(s)
            child.setRestrictions(finder.getRestrictions(prop));
            children.add(child);
        }

        // @@TODO get rid of repetition

        // get any more global things we can say about the class at this node
        Set<OWLAxiom> globalAxioms = new HashSet<OWLAxiom>();
        for (OWLClass cls : finder.getClassesToInheritFrom()){
            System.out.println("cls = " + cls);
            globalAxioms.addAll((SuperAndEquivAxiomFinder.getAxioms(cls, getModel().getOntologies(), true)));
        }

        finder.clear();        
        for (OWLAxiom ax : globalAxioms){
            ax.accept(finder);
        }
        properties = finder.getProperties();

        if (filterproperties != null){
            properties.retainAll(filterproperties);
        }
        // for each of the "properties on the class/description" create an uneditable child node
        for (OWLPropertyExpression prop : properties){
            final OWLPropertyNode child = model.createNode(prop, this);
            child.setEditable(false);
            child.addAxioms(finder.getAxioms(prop));
            child.setRestrictions(finder.getRestrictions(prop));
            children.add(child);
        }

    }

    public boolean isNavigable() {
        return descr instanceof OWLClass;
    }

    public String toString() {
        return getUserObject().toString();
    }

    public boolean equals(Object object) {
        return object instanceof OWLAnonymousClassNode &&
                descr.equals(((OWLAnonymousClassNode)object).getUserObject());
    }

    private Set<OWLClass> getNamedClassesFromIntersection(OWLObjectIntersectionOf intersectionOf) {
        Set<OWLClass> namedClasses = new HashSet<OWLClass>();
        for (OWLDescription op : intersectionOf.getOperands()){
            if (op instanceof OWLClass){
                namedClasses.add((OWLClass)op);
            }
            else if (op instanceof OWLObjectIntersectionOf){ // deal with nesting
                namedClasses.addAll(getNamedClassesFromIntersection((OWLObjectIntersectionOf)op));
            }
        }
        return namedClasses;
    }

    protected void clear() {
        children = null;
    }
}
