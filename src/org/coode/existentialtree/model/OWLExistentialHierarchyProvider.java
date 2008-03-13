package org.coode.existentialtree.model;

import org.coode.outlinetree.util.ExistentialFillerAccumulator;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;

import java.util.Collections;
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
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Apr 24, 2007<br><br>
 * <p/>
 */
public class OWLExistentialHierarchyProvider extends AbstractHierarchyProvider<OWLDescription> {

    private Set<OWLOntology> ontologies;

    private ExistentialFillerAccumulator fillerAccumulator = new ExistentialFillerAccumulator();

    private OWLDescription root;

    private OWLObjectHierarchyProvider<OWLObjectProperty> hp;

    public OWLExistentialHierarchyProvider(OWLModelManager owlOntologyManager) {
        super(owlOntologyManager.getOWLOntologyManager());
        ontologies = owlOntologyManager.getOntologies();
        root = owlOntologyManager.getOWLDataFactory().getOWLThing();
        hp = owlOntologyManager.getOWLObjectPropertyHierarchyProvider();
    }

    public void setOntologies(Set<OWLOntology> ontologies) {
        this.ontologies = ontologies;
    }

    public Set<OWLDescription> getRoots() {
        if (root != null){
            return Collections.singleton(root);
        }
        else{
            return Collections.EMPTY_SET;
        }
    }

    public Set<OWLDescription> getChildren(OWLDescription object) {
            return fillerAccumulator.getExistentialFillers(object, ontologies);
    }

    public Set<OWLDescription> getParents(OWLDescription object) {
        return Collections.EMPTY_SET;
    }

    public Set<OWLDescription> getEquivalents(OWLDescription object) {
        return Collections.EMPTY_SET;
    }

    public boolean containsReference(OWLDescription object) {
        return object.equals(root);
    }

    public void setRoot(OWLDescription selectedClass) {
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
