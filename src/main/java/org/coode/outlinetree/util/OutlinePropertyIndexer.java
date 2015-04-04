package org.coode.outlinetree.util;

import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Mar 7, 2008<br><br>
 *
 * Pile through axioms or descriptions pulling out the appropriate restrictions for the
 * outline model (1) and indexes both axioms and restrictions by property
 *
 * (1) both obj and data someValueFrom, cardinality == or >0, hasValue
 */
public class OutlinePropertyIndexer extends OutlineRestrictionVisitor {

    private Map<OWLPropertyExpression, Set<OWLRestriction>> restrictionMap =
            new HashMap<OWLPropertyExpression, Set<OWLRestriction>>();

    private Map<OWLPropertyExpression, Set<OWLAxiom>> axiomMap =
            new HashMap<OWLPropertyExpression, Set<OWLAxiom>>();

    private OWLAxiom currentAxiom;

    private Set<OWLClass> classesToInheritFrom = new HashSet<OWLClass>();

    public OutlinePropertyIndexer(Set<OWLOntology> onts, int min) {
        super(onts, min);
    }
    
    public Set<OWLPropertyExpression> getProperties() {
        return restrictionMap.keySet();
    }

    public Set<OWLRestriction> getRestrictions(OWLPropertyExpression prop){
        return restrictionMap.get(prop);
    }

    public Set<OWLAxiom> getAxioms(OWLPropertyExpression prop) {
        return axiomMap.get(prop);
    }

    public Set<OWLClass> getClassesToInheritFrom(){
        return classesToInheritFrom;
    }

    public void clear(){
        classesToInheritFrom.clear();
        restrictionMap.clear();
        axiomMap.clear();
    }

    public void visit(OWLClass owlClass) {
        classesToInheritFrom.add(owlClass);
    }

    public void visit(OWLSubClassOfAxiom owlSubClassAxiom) {
        currentAxiom = owlSubClassAxiom;
        super.visit(owlSubClassAxiom);
        currentAxiom = null;
    }


    public void visit(OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
        currentAxiom = owlEquivalentClassesAxiom;
        super.visit(owlEquivalentClassesAxiom);
        currentAxiom = null;
    }

    protected void handleRestriction(OWLRestriction restriction) {
        OWLPropertyExpression property = restriction.getProperty();

        add(restriction, property, restrictionMap);

        if (currentAxiom != null){
            add(currentAxiom, property, axiomMap);
        }
    }

    private <T> void add(T restriction, OWLPropertyExpression property, Map<OWLPropertyExpression, Set<T>> map) {
        Set<T> restrs = map.get(property);
        if (restrs == null){
            restrs = new HashSet<T>();
            map.put(property, restrs);
        }
        restrs.add(restriction);
    }
}