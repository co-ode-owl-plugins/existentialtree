package org.coode.existentialtree.util;

import org.semanticweb.owl.model.*;

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
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 29, 2007<br><br>
 *
 * Pull together all subclass and equivclass someValuesFrom axioms for this class including inherited ones.
 */
public class AxiomAccumulator {

    private Set<OWLObject> allObjects = new HashSet<OWLObject>();

    private Map<OWLPropertyExpression, Set<OWLObject>> existentialPropMap =
            new HashMap<OWLPropertyExpression, Set<OWLObject>>();

    private boolean getInherited = true;
    private boolean axiomCacheBuilt = false;
    private OWLDescription base;
    private Set<OWLOntology> onts;
    private int min;

    public AxiomAccumulator(OWLDescription cls, Set<OWLOntology> onts, int min) {
        this.base = cls;
        this.onts = onts;
        this.min = min;
        if (cls instanceof OWLClass){
            for (OWLOntology ont : onts){
                allObjects.addAll(ont.getSubClassAxiomsForLHS((OWLClass)cls));
                allObjects.addAll(ont.getEquivalentClassesAxioms((OWLClass)cls));
                if (getInherited){
                    for (OWLDescription descr : ((OWLClass)cls).getSuperClasses(ont)){
                        if (descr instanceof OWLClass){
                            allObjects.addAll(new AxiomAccumulator(descr, onts, min).getObjectsForDescription());
                        }
                    }
                }
            }
        }
        else {
            allObjects.add(cls);
        }
    }

    public Set<OWLObject> getObjectsForDescription(){
        return allObjects;
    }

    public Set<OWLObject> filterObjectsForProp(OWLPropertyExpression prop){
        ensureCacheBuilt();
        return existentialPropMap.get(prop);
    }

    public Set<OWLPropertyExpression> getUsedProperties(){
        ensureCacheBuilt();
        return existentialPropMap.keySet();
    }

    private void ensureCacheBuilt() {
        if (!axiomCacheBuilt){
            for (OWLObject axiom : allObjects){
                OWLFillerHandler handler = new OWLFillerHandler(base, axiom, onts);
                axiom.accept(handler);
            }
        }
    }

    class OWLFillerHandler extends AbstractExistentialVisitorAdapter {

        private OWLObject ax;

        public OWLFillerHandler(OWLObject base, OWLObject ax, Set<OWLOntology> onts) {
            super(base, onts);
            this.ax = ax;
        }

        protected void handleRestriction(OWLQuantifiedRestriction restriction) {
            OWLPropertyExpression property = restriction.getProperty();
            Set<OWLObject> axioms = existentialPropMap.get(property);
            if (axioms == null){
                axioms = new HashSet<OWLObject>();
                existentialPropMap.put(property, axioms);
            }
            axioms.add(ax);
        }


        protected int getMinCardinality() {
            return min;
        }
    }
}
