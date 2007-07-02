package org.coode.existentialtree;

import org.semanticweb.owl.model.*;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.model.hierarchy.AbstractOWLObjectHierarchyProvider;
import org.protege.editor.owl.model.OWLModelManager;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

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
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Apr 24, 2007<br><br>
 * <p/>
 */
public class OWLExistentialHierarchyProvider extends AbstractOWLObjectHierarchyProvider<OWLClass> {

    private Set<OWLOntology> ontologies;

    private ExistentialFillerAccumulator fillerAccumulator = new ExistentialFillerAccumulator();

    private OWLClass root;

    private OWLObjectHierarchyProvider<OWLObjectProperty> hp;

    protected OWLExistentialHierarchyProvider(OWLModelManager owlOntologyManager) {
        super(owlOntologyManager.getOWLOntologyManager());
        ontologies = owlOntologyManager.getOntologies();
        root = owlOntologyManager.getOWLDataFactory().getOWLThing();
        hp = owlOntologyManager.getOWLObjectPropertyHierarchyProvider();
    }

    public void setOntologies(Set<OWLOntology> ontologies) {
        this.ontologies = ontologies;
    }

    public Set<OWLClass> getRoots() {
        if (root != null){
            return Collections.singleton(root);
        }
        else{
            return Collections.EMPTY_SET;
        }
    }

    public Set<OWLClass> getChildren(OWLClass object) {
        if (object instanceof OWLClass){
            return fillerAccumulator.getNamedExistentialFillers(object, ontologies);
        }
        else{
            return Collections.EMPTY_SET;
        }
    }

    public Set<OWLClass> getParents(OWLClass object) {
        return Collections.EMPTY_SET;
    }

    public Set<OWLClass> getEquivalents(OWLClass object) {
        return Collections.EMPTY_SET;
    }

    public boolean containsReference(OWLClass object) {
        return object.equals(root);
    }

    public void setRoot(OWLClass selectedClass) {
        root = selectedClass;
    }

    public void setProp(OWLObjectProperty prop){
        if (prop != null){
            Set<OWLObjectProperty> propAndDescendants = new HashSet<OWLObjectProperty>(hp.getDescendants(prop));
            propAndDescendants.add(prop);
            fillerAccumulator.setProperties(propAndDescendants);
        }
        else{
            fillerAccumulator.setProperties(null);
        }
    }
}
