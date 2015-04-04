package org.coode.existentialtree.model;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

import java.util.*;
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
 * Date: May 18, 2009<br><br>
 */
public class ReciprocalsHierarchyProvider extends AbstractHierarchyProvider<OWLClass>{

    private OWLClass root;

    private OWLObjectProperty prop;

    private Set<OWLOntology> ontologies = new HashSet<OWLOntology>();

    private Map<OWLClass, Set<OWLClass>> nodes = new HashMap<OWLClass, Set<OWLClass>>();

    private OWLOntologyManager mngr;


    public ReciprocalsHierarchyProvider(OWLOntologyManager owlOntologyManager) {
        super(owlOntologyManager);
        this.mngr = owlOntologyManager;
    }


    public void setRoot(OWLClass selectedClass) {
        this.root = selectedClass;
        reload();
    }


    public void setProp(OWLObjectProperty prop) {
        this.prop = prop;
        reload();
    }


    private void reload() {
        nodes.clear();
    }


    public void setOntologies(Set<OWLOntology> ontologies) {
        this.ontologies = ontologies;
        reload();
    }


    public Set<OWLClass> getRoots() {
        return root != null ? Collections.singleton(root) : Collections.EMPTY_SET;
    }


    public Set<OWLClass> getChildren(OWLClass cls) {
        Set<OWLClass> children = nodes.get(cls);
        if (children == null){
            children = new HashSet<OWLClass>();
            ReciprocalAxiomVisitor recVisitor = new ReciprocalAxiomVisitor(cls);
            for (OWLOntology ont : ontologies){
                for (OWLAxiom refAx : ont.getReferencingAxioms(cls)){
                    OWLClass rec = recVisitor.getReciprocal(refAx);
                    if (rec != null){
                        children.add(rec);
                    }
                }
            }
            nodes.put(cls, children);
        }
        return children;
    }


    public Set<OWLClass> getParents(OWLClass object) {
        return Collections.EMPTY_SET; // @@TODO is this ever used?
    }


    public Set<OWLClass> getEquivalents(OWLClass object) {
        return Collections.EMPTY_SET;
    }


    public boolean containsReference(OWLClass object) {
        return nodes.containsKey(object); // @@TODO should build the entire tree before it can answer
    }


    private boolean followProperty(OWLObjectPropertyExpression candidate){
        // @@TODO take property hierarchy into account
        return prop == null || prop.equals(candidate);
    }


    class ReciprocalAxiomVisitor extends OWLAxiomVisitorAdapter {

        private ReciprocalExpressionVisitor exprVisitor = new ReciprocalExpressionVisitor();

        private OWLClass reciprocal;

        private OWLClass cls;

        ReciprocalAxiomVisitor(OWLClass cls) {
            this.cls = cls;
        }

        public OWLClass getReciprocal(OWLAxiom ax){
            reciprocal = null;
            ax.accept(this);
            return reciprocal;
        }

        public void visit(OWLSubClassOfAxiom owlSubClassAxiom) {
            if (exprVisitor.isReciprocal(owlSubClassAxiom.getSuperClass()) &&
                !owlSubClassAxiom.getSubClass().isAnonymous()){
                reciprocal = owlSubClassAxiom.getSubClass().asOWLClass();
            }
        }

        // @@TODO defined classes


        class ReciprocalExpressionVisitor extends OWLClassExpressionVisitorAdapter {

            private boolean recip;

            public boolean isReciprocal(OWLClassExpression descr){
                recip = false;
                descr.accept(this);
                return recip;
            }


            public void visit(OWLObjectSomeValuesFrom restriction) {
                if (followProperty(restriction.getProperty()) &&
                    restriction.getFiller().equals(cls)){ // @@TODO fillers in intersections?
                    recip = true;
                }
            }


            public void visit(OWLObjectMinCardinality restriction) {
                if (restriction.getCardinality() > 0 &&
                    followProperty(restriction.getProperty()) &&
                    restriction.getFiller().equals(cls)){ // @@TODO fillers in intersections?
                    recip = true;
                }
            }


            public void visit(OWLObjectExactCardinality restriction) {
                if (restriction.getCardinality() > 0 &&
                    followProperty(restriction.getProperty()) &&
                    restriction.getFiller().equals(cls)){ // @@TODO fillers in intersections?
                    recip = true;
                }
            }
        }
    }
}
