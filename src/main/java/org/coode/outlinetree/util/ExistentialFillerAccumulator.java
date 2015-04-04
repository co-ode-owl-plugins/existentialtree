package org.coode.outlinetree.util;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

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
 * Also accumulates inherited restrictions
 */
public class ExistentialFillerAccumulator extends OWLClassExpressionVisitorAdapter {

    private Set<OWLClassExpression> fillers = new HashSet<OWLClassExpression>();
    private Set<OWLObjectProperty> properties;
    private Set<OWLOntology> onts;

    public ExistentialFillerAccumulator() {
    }

    // allows for a set of properties to be specified to only retrieve
    public ExistentialFillerAccumulator(Set<OWLObjectProperty> properties){
        this.properties = properties;
    }

    public Set<OWLClassExpression> getExistentialFillers(OWLClassExpression cls, Set<OWLOntology> ontologies) {
        fillers.clear();
        onts = ontologies;
        if (cls instanceof OWLClass){
                for (OWLClassExpression restr : EntitySearcher.getSubClasses((OWLClass)cls, ontologies)) {
                    restr.accept(this);
                }
                for (OWLClassExpression restr : EntitySearcher.getEquivalentClasses((OWLClass)cls, ontologies)) {
                    restr.accept(this);
                }
        }
        else{
            cls.accept(this);
        }
        return fillers;
    }

    public Set<OWLClass> getNamedExistentialFillers(OWLClass cls, Set<OWLOntology> ontologies) {
        Set<OWLClass> results = new HashSet<OWLClass>();
        for (OWLClassExpression filler : getExistentialFillers(cls, ontologies)){
            if (filler instanceof OWLClass){
                results.add((OWLClass)filler);
            }
        }
        return results;
    }

    // Named supers are also queried for inherited restrictions
    @Override
    public void visit(OWLClass desc) {
        ExistentialFillerAccumulator acc = new ExistentialFillerAccumulator(properties);
        fillers.addAll(acc.getExistentialFillers(desc, onts));
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom desc) {
        if (properties == null || properties.contains(desc.getProperty())) {

            fillers.add(desc.getFiller());
        }
    }

    @Override
    public void visit(OWLObjectMinCardinality desc) {
        if (desc.getCardinality() > 0 &&
            (properties == null || properties.contains(desc.getProperty()))) {

            OWLClassExpression filler = desc.getFiller();
            fillers.add(filler);
        }
    }

    @Override
    public void visit(OWLObjectExactCardinality desc) {
        if (desc.getCardinality() > 0 &&
            properties == null || properties.contains(desc.getProperty())) {

            OWLClassExpression filler = desc.getFiller();
            fillers.add(filler);
        }
    }

    // need to flatten intersections - particularly for equiv classes which are often A and (restriction)
    @Override
    public void visit(OWLObjectIntersectionOf and) {
        for (OWLClassExpression desc : and.getOperands()) {
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
