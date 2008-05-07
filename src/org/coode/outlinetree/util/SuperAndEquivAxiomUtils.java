package org.coode.outlinetree.util;

import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLOntology;

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
 * Date: Oct 29, 2007<br><br>
 *
 */
public class SuperAndEquivAxiomUtils {

    /**
     * Pull together subclass and equivclass restriction axioms.
     * @param cls
     * @param onts must match the ontologies loaded into the hp if given
     * @param hp a hierarchy provider used to determine the superclasses to collect inherited axioms (if null, no inheritance)
     * @return If an hp is omitted, this method returns only directly asserted restrictions.
     * If a hp is provided, this method returns only inherited restrictions.
     */
    public static Set<OWLAxiom> getAxioms(OWLClass cls, Set<OWLOntology> onts, OWLObjectHierarchyProvider<OWLClass> hp){
        Set<OWLAxiom>allObjects = new HashSet<OWLAxiom>();
        for (OWLOntology ont : onts){
            if (hp != null){
                for (OWLClass parent : hp.getAncestors(cls)){
                    allObjects.addAll(getAxioms(parent, onts, null));
                }
            }
            else{
                allObjects.addAll(ont.getSubClassAxiomsForLHS(cls));
                allObjects.addAll(ont.getEquivalentClassesAxioms(cls));
            }
        }
        return allObjects;
    }

//    public static Set<OWLAxiom> filterAxiomsByRHS(Set<OWLAxiom> axioms){
//        Set<OWLAxiom> filtered = new HashSet<OWLAxiom>(axioms);
//        for (OWLAxiom ax : axioms){
//            if (ax instanceof OWLSubClassAxiom){
//                ax.get
//            }
//        }
//    }
}
