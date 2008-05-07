package org.coode.outlinetree.model;

import org.coode.outlinetree.util.OutlinePropertyIndexer;
import org.coode.outlinetree.util.SuperAndEquivAxiomUtils;
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
class OWLDescriptionNode<O extends OWLDescription> extends AbstractOutlineNode<O, OWLPropertyNode> {

    private O descr;
    private List<OutlineNode> children;

    public OWLDescriptionNode(O descr, OutlineTreeModel model){
        super(model);
        this.descr = descr;
    }

    public O getUserObject() {
        return descr;
    }

    public OWLDescription getRenderedObject() {
        OWLDescription renderedObject = descr; // default to rendering the object directly
        if (descr.isAnonymous() && getChildren().size() > 0){ // if there are any children, we should be able to name the object
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

    protected void clear() {
        children = null;
    }

    public List<OutlineNode> getChildren() {
        if (children == null){
            children = new ArrayList<OutlineNode>();
            refresh();
        }
        return children;
    }

    protected void refresh() {
        OutlineTreeModel model = getModel();

        OutlinePropertyIndexer finder = new OutlinePropertyIndexer(model.getOntologies(), model.getMin());
        descr.accept(finder);

        if (descr.isAnonymous()){
            // children "inlined" in the description
            createChildren(finder, isEditable()); // children of uneditable nodes are not editable
            for (OutlineNode child : children){
                child.addAxioms(getAxioms()); // always inherit the same axioms as this anon parent
            }
        }
        else{
            if (isRootClass() || model.getShowAssertedChildrenAllNodes()){
                createChildrenFromAxioms(getSuperAndEquivs(descr.asOWLClass(), false), isRootClass());
            }
        }

        if (model.getShowInheritedChildrenAllNodes()){ // children from eg named classes in an intersection
            createChildrenFromAxioms(getGlobalAxioms(descr, finder), false);
        }
    }


    // for each of the "properties on the class/description" create a child node
    protected void createChildren(OutlinePropertyIndexer finder, boolean editable) {
        for (OWLPropertyExpression prop : getFilteredProperties(finder)){
            final OWLPropertyNode child = getModel().createNode(prop, this);
            final Set<OWLAxiom> childAxioms = finder.getAxioms(prop);
            if (childAxioms != null){
                child.addAxioms(childAxioms);
            }
            child.setRestrictions(finder.getRestrictions(prop));
            child.setEditable(editable);
            if (!children.contains(child)){
                children.add(child);
            }
        }
    }

    /**
     *
     * @param finder
     * @return the intersection of the "used properties and those in current filter
     */
    protected Set<OWLPropertyExpression> getFilteredProperties(OutlinePropertyIndexer finder){
        OutlineTreeModel model = getModel();

        Set<OWLPropertyExpression> filterproperties = model.getFilterProperties();
        Set<OWLPropertyExpression> properties = finder.getProperties();

        if (filterproperties != null){
            properties.retainAll(filterproperties);
        }
        return properties;
    }

    // get any more global things we can say about the class at this node
    private Set<OWLAxiom> getGlobalAxioms(OWLDescription descr, OutlinePropertyIndexer finder) {
        Set<OWLAxiom> globalAxioms = new HashSet<OWLAxiom>();
        final Set<OWLClass> ancestors = finder.getClassesToInheritFrom();

        for (OWLClass cls : ancestors){
            globalAxioms.addAll(getSuperAndEquivs(cls, true));
        }
        return globalAxioms;
    }

    private void createChildrenFromAxioms(Set<OWLAxiom> axioms, boolean editable) {
        OutlinePropertyIndexer finder = new OutlinePropertyIndexer(getModel().getOntologies(), getModel().getMin());
        for (OWLAxiom ax : axioms){
            ax.accept(finder);
        }
        createChildren(finder, editable);
    }


    public boolean isNavigable() {
        return !descr.isAnonymous();
    }

    public Class<? extends OWLObject> getTypeOfChild() {
        return OWLProperty.class;
    }

    public String toString() {
        return getUserObject().toString();
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

    private Set<OWLAxiom> getSuperAndEquivs(OWLClass cls, boolean inherited) {
        return SuperAndEquivAxiomUtils.getAxioms(cls,
                                                 getModel().getOntologies(),
                                                 inherited ? getModel().getClassHierarchyProvider() : null);
    }
}
