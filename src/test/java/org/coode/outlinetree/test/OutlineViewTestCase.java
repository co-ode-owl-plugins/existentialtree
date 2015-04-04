package org.coode.outlinetree.test;

import junit.framework.TestCase;
import org.coode.outlinetree.model.OutlineNode;
import org.coode.outlinetree.model.OutlineTreeModel;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

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
 * Date: Mar 5, 2008<br><br>
 *
 *
 * Axioms
 * ------
 * A -> p some B
 * A -> p some (C and r some E)
 * A -> q some D
 *
 * Tree
 * ----
 * A
 * --p
 *   --B
 *   --C
 *     --r
 *       --E
 * --q
 *   --D
 */
public class OutlineViewTestCase extends TestCase {

    private static final IRI ONTOLOGY_IRI = IRI.create("http://www.co-ode.org/ontologies/test/outlineview.owl");

    private OWLOntologyManager mngr;
    private OWLOntology ont;
    private OWLClass a;
    private OWLClass b;
    private OWLClass c;
    private OWLClass d;
    private OWLClass e;

    private OWLObjectProperty p;
    private OWLObjectProperty q;
    private OWLObjectProperty r;

    private OWLSubClassOfAxiom aPSomeB;
    private OWLSubClassOfAxiom aPSomeC;
    private OWLSubClassOfAxiom aQSomeD;

    private OWLObjectIntersectionOf cAndRSomeE;

    public void init(){
        try {
            mngr = OWLManager.createOWLOntologyManager();
            ont = mngr.createOntology(ONTOLOGY_IRI);

            final OWLDataFactory df = mngr.getOWLDataFactory();

            a = df.getOWLClass(IRI.create(ONTOLOGY_IRI + "#A"));
            b = df.getOWLClass(IRI.create(ONTOLOGY_IRI + "#B"));
            c = df.getOWLClass(IRI.create(ONTOLOGY_IRI + "#C"));
            d = df.getOWLClass(IRI.create(ONTOLOGY_IRI + "#D"));
            e = df.getOWLClass(IRI.create(ONTOLOGY_IRI + "#E"));

            p = df.getOWLObjectProperty(IRI.create(ONTOLOGY_IRI + "#p"));
            q = df.getOWLObjectProperty(IRI.create(ONTOLOGY_IRI + "#q"));
            r = df.getOWLObjectProperty(IRI.create(ONTOLOGY_IRI + "#r"));

            // build the axioms and descriptions
            aPSomeB = df.getOWLSubClassOfAxiom(a, df.getOWLObjectSomeValuesFrom(p, b));
            aPSomeC = df.getOWLSubClassOfAxiom(a, df.getOWLObjectSomeValuesFrom(p, cAndRSomeE));
            aQSomeD = df.getOWLSubClassOfAxiom(a, df.getOWLObjectSomeValuesFrom(q, d));

            Set<OWLClassExpression> and = new HashSet<OWLClassExpression>();
            and.add(c);
            and.add(df.getOWLObjectSomeValuesFrom(r, e));
            cAndRSomeE = df.getOWLObjectIntersectionOf(and);

            // build the test ontology
            List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
            changes.add(new AddAxiom(ont, aPSomeB));
            changes.add(new AddAxiom(ont, aPSomeC));
            changes.add(new AddAxiom(ont, aQSomeD));
            mngr.applyChanges(changes);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testStructure(){
        init();
        OutlineTreeModel model = new OutlineTreeModel(mngr, Collections.singleton(ont), null, new BasicComparator());
        model.setRoot(a);

        OutlineNode aNode = model.getRoot();
        assertTrue(aNode.getUserObject().equals(a));
        assertTrue(aNode.getRenderedObject().equals(a));
        assertNull(aNode.getParent());
        assertEquals(2, model.getChildCount(aNode));

        // test p
        OutlineNode pNode = model.getChild(aNode, 1);
        assertSame(p, pNode.getUserObject());
        Set<OWLAxiom> pNodeAxioms = pNode.getAxioms();
        assertSame(2, pNodeAxioms.size());

        // test C
        OutlineNode cNode = model.getChild(pNode, 0);
        assertSame(cAndRSomeE, cNode.getUserObject()); // the anonymous class
        assertSame(c, cNode.getRenderedObject()); // the displayed object
        assertSame(1, model.getChildCount(cNode));

        // test q
        OutlineNode qNode = model.getChild(aNode, 0);
        assertSame(q, qNode.getUserObject());
        Set<OWLAxiom> qNodeAxioms = qNode.getAxioms();
        assertSame(1, qNodeAxioms.size());
    }

    public void testAxiomsLoaded(){
        init();
        OutlineTreeModel model = new OutlineTreeModel(mngr, Collections.singleton(ont), null, new BasicComparator());
        model.setRoot(a);
        OutlineNode aNode = model.getRoot();

        final Set aNodeAxioms = aNode.getAxioms();
        assertEquals(3, aNodeAxioms.size());
        assertTrue(aNodeAxioms.contains(aPSomeB));
        assertTrue(aNodeAxioms.contains(aPSomeC));
        assertTrue(aNodeAxioms.contains(aQSomeD));
    }
}
