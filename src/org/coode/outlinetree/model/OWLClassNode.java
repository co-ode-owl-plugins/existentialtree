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
 * Date: Mar 05, 2008<br><br>
 */
public class OWLClassNode extends AbstractOutlineNode<OWLClass, OWLPropertyNode>{

    private OWLClass cls;

    private List<OutlineNode> children;


    public OWLClassNode(OWLClass cls, OutlineTreeModel model) {
        super(model);
        this.cls = cls;
    }

    protected void clear() {
        children = null;
    }

    public OWLClass getUserObject() {
        return cls;
    }

    public OWLClass getRenderedObject() {
        return cls;
    }

    public List<OutlineNode> getChildren() {
        if (children == null){
            refresh();
        }
        return children;
    }

    private void refresh() {
        if (children == null){
            children = new ArrayList<OutlineNode>();

            // things that can always be said about this class - root will always have these
            Set<OWLAxiom> globalAxioms = new HashSet<OWLAxiom>();

            // things that can only be said about this class in this context
            Set<OWLAxiom> localAxioms = new HashSet<OWLAxiom>();
            for (OWLAxiom ax : getAxioms()){
                if (isAboutThisClass(ax)){
                    globalAxioms.add(ax);
                }
                else{
                    localAxioms.add(ax);
                }
            }

            // get any more global things we can say about the class at this node
            globalAxioms.addAll((SuperAndEquivAxiomFinder.getAxioms(cls, getModel().getOntologies(), true)));

            // if this is root class, the children created because of global axioms are editable
            // otherwise, these axioms are not editable (as we are not "talking about" this class)
            createChildren(globalAxioms, isRootClass());

//            // actually, a named class will never have further children because of inherited axioms
//            assert(localAxioms.isEmpty()); // sanity check
        }
    }

    private void createChildren(Set<OWLAxiom> axioms, boolean editable) {
        OutlineTreeModel model = getModel();
        Set<OWLPropertyExpression> filterproperties = model.getFilterProperties();
        Set<OWLPropertyExpression> properties;
        OutlinePropertyIndexer restrFinder = new OutlinePropertyIndexer(model.getOntologies(), model.getMin());
        for (OWLAxiom ax : axioms){
            ax.accept(restrFinder);
        }
        properties = restrFinder.getProperties();
        if (filterproperties != null){
            properties.retainAll(filterproperties); // only retain thos in the filter
        }

        // for each of the "properties on the class/description" create a child node
        for (OWLPropertyExpression prop : properties){
            Set<OWLRestriction> restrs = restrFinder.getRestrictions(prop);
            final OWLPropertyNode child = model.createNode(prop, this);
            child.setEditable(editable);
            child.setRestrictions(restrs);
            final Set<OWLAxiom> owlAxioms = restrFinder.getAxioms(prop);
            child.addAxioms(owlAxioms);
            children.add(child);
        }
    }

    private boolean isAboutThisClass(OWLAxiom ax) {
        if (ax instanceof OWLSubClassAxiom){
            return ((OWLSubClassAxiom)ax).getSubClass().equals(cls);
        }
        else if (ax instanceof OWLEquivalentClassesAxiom){
            return ((OWLEquivalentClassesAxiom)ax).getDescriptions().contains(cls);
        }
        return false;
    }

    private boolean isRootClass() {
        return getParent() == null;
    }

    public boolean isNavigable() {
        return true;
    }
}
