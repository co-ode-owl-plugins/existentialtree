package org.coode.outlinetree.model;

import org.semanticweb.owl.model.OWLClass;

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
class OWLClassNode extends OWLDescriptionNode<OWLClass>{
    
    public OWLClassNode(OWLClass cls, OutlineTreeModel model) {
        super(cls, model);
    }

//    protected void refresh() {

//        // things that can always be said about this class - root will always have these
//        Set<OWLAxiom> globalAxioms = new HashSet<OWLAxiom>();
//
//        // things that can only be said about this class in this context
//        Set<OWLAxiom> inheritedAxioms = new HashSet<OWLAxiom>();
//        final Set<OWLAxiom> allAxioms = getAxioms();
//
//        // get any more global things we can say about the class at this node
//        allAxioms.addAll(SuperAndEquivAxiomUtils.getAxioms(getUserObject(), getModel().getOntologies(), getModel().getClassHierarchyProvider()));
//        for (OWLAxiom ax : allAxioms){
//            if (isAboutThisClass(ax)){
//                globalAxioms.add(ax);
//            }
//            else{
//                inheritedAxioms.add(ax);
//            }
//        }
//
//        // if this is root class, the children created because of global axioms are editable
//        // otherwise, these axioms are not editable (as we are not "talking about" this class)
//        createChildren(globalAxioms, isRootClass());
//
////      // actually, a named class will never have further children because of inherited axioms
//        createChildren(inheritedAxioms, false);
//    }

//    private void createChildren(Set<OWLAxiom> axioms, boolean editable) {
//        OutlineTreeModel model = getModel();
//        OutlinePropertyIndexer finder = new OutlinePropertyIndexer(model.getOntologies(), model.getMin());
//        for (OWLAxiom ax : axioms){
//            ax.accept(finder);
//        }
//
//        createChildren(finder, editable);
//    }
//
//    private boolean isAboutThisClass(OWLAxiom ax) {
//        if (ax instanceof OWLSubClassAxiom){
//            return ((OWLSubClassAxiom)ax).getSubClass().equals(getUserObject());
//        }
//        else if (ax instanceof OWLEquivalentClassesAxiom){
//            return ((OWLEquivalentClassesAxiom)ax).getDescriptions().contains(getUserObject());
//        }
//        return false;
//    }
}
