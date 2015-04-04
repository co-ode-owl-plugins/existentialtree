package org.coode.existentialtree.model;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

import com.google.common.collect.Multimap;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Sep 13, 2007<br><br>
 */
public class OWLRelationHierarchyProvider extends AbstractHierarchyProvider<OWLIndividual> {

    private Set<OWLOntology> ontologies;

    private OWLIndividual root;

    private Set<OWLObjectProperty> propAndDescendants = new HashSet<OWLObjectProperty>();

    private OWLModelManager mngr;


    public OWLRelationHierarchyProvider(OWLModelManager mngr) {
        super(mngr.getOWLOntologyManager());
        this.mngr = mngr;
        ontologies = mngr.getOntologies();
    }

    public void setOntologies(Set<OWLOntology> ontologies) {
        this.ontologies = ontologies;
    }

    public Set<OWLIndividual> getRoots() {
        if (root != null){
            return Collections.singleton(root);
        }
        else{
            return Collections.EMPTY_SET;
        }
    }

    public Set<OWLIndividual> getChildren(OWLIndividual individual) {
        Multimap<OWLObjectPropertyExpression, OWLIndividual> values = EntitySearcher.getObjectPropertyValues(individual,ontologies);

        Set<OWLIndividual> children = new HashSet<OWLIndividual>();

        for (OWLObjectPropertyExpression p : values.keySet()){
            if (propAndDescendants.isEmpty() || propAndDescendants.contains(p)){
                children.addAll(values.get(p));
            }
        }
        return children;
    }

    public Set<OWLIndividual> getParents(OWLIndividual object) {
        return Collections.EMPTY_SET;
    }

    public Set<OWLIndividual> getEquivalents(OWLIndividual object) {
        return Collections.EMPTY_SET;
    }

    public boolean containsReference(OWLIndividual object) {
        return object.equals(root);
    }

    public void setRoot(OWLIndividual individual) {
        root = individual;
    }

    public void setProp(OWLObjectProperty prop){
        propAndDescendants.clear();
        if (prop != null){
            propAndDescendants.addAll(mngr.getOWLHierarchyManager().getOWLObjectPropertyHierarchyProvider().getDescendants(prop));
            propAndDescendants.add(prop);
        }
    }
}
