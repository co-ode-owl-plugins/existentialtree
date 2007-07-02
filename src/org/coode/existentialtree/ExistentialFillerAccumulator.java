package org.coode.existentialtree;

import org.semanticweb.owl.model.*;
import org.semanticweb.owl.util.OWLDescriptionVisitorAdapter;

import java.util.Set;
import java.util.HashSet;

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
public class ExistentialFillerAccumulator extends OWLDescriptionVisitorAdapter {

    private Set<OWLDescription> fillers = new HashSet<OWLDescription>();
    private Set<OWLObjectProperty> properties;
    private Set<OWLOntology> onts;

    public ExistentialFillerAccumulator() {
    }

    // allows for a set of properties to be specified to only retrieve
    public ExistentialFillerAccumulator(Set<OWLObjectProperty> properties){
        this.properties = properties;
    }

    public Set<OWLDescription> getExistentialFillers(OWLClass cls, Set<OWLOntology> ontologies) {
        fillers.clear();
        onts = ontologies;
        for (OWLOntology ont : ontologies){
            for (OWLDescription restr : cls.getSuperClasses(ont)) {
                restr.accept(this);
            }
            for (OWLDescription restr : cls.getEquivalentClasses(ont)) {
                restr.accept(this);
            }
        }
        return fillers;
    }

    public Set<OWLClass> getNamedExistentialFillers(OWLClass cls, Set<OWLOntology> ontologies) {
        Set<OWLClass> results = new HashSet<OWLClass>();
        for (OWLDescription filler : getExistentialFillers(cls, ontologies)){
            if (filler instanceof OWLClass){
                results.add((OWLClass)filler);
            }
        }
        return results;
    }


    public void visit(OWLClass desc) {
        ExistentialFillerAccumulator acc = new ExistentialFillerAccumulator(properties);
        fillers.addAll(acc.getExistentialFillers(desc, onts));
    }

    public void visit(OWLObjectSomeRestriction desc) {
        if (properties == null || properties.contains(desc.getProperty())) {

            fillers.add(desc.getFiller());
        }
    }

    public void visit(OWLObjectMinCardinalityRestriction desc) {
        if (desc.getCardinality() > 0 &&
            (properties == null || properties.contains(desc.getProperty()))) {

            OWLDescription filler = desc.getFiller();
            if (filler != null) {
                fillers.add(filler);
            }
        }
    }

    public void visit(OWLObjectExactCardinalityRestriction desc) {
        if (desc.getCardinality() > 0 &&
            properties == null || properties.contains(desc.getProperty())) {

            OWLDescription filler = desc.getFiller();
            if (filler != null) {
                fillers.add(filler);
            }
        }
    }

    // need to flatten intersections - particularly for equiv classes which are often A and (restriction)
    public void visit(OWLObjectIntersectionOf and) {
        for (OWLDescription desc : and.getOperands()) {
            if (!fillers.contains(desc)){
                desc.accept(this);
            }
        }
    }

    public Set<OWLObjectProperty> getProperties() {
        return properties;
    }


    public void setProperties(Set<OWLObjectProperty> props) {
        properties = props;
    }
}
