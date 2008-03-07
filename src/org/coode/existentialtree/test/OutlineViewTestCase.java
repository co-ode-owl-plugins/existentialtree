package org.coode.existentialtree.test;

import junit.framework.TestCase;
import org.coode.outlinetree.model.OutlineNode;
import org.coode.outlinetree.model.OutlineTreeModel;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.*;

import java.net.URI;
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

    private static final URI ONTOLOGY_URI = URI.create("http://www.co-ode.org/ontologies/test/outlineview.owl");

    public void testABCRoot(){
        OWLOntologyManager mngr = OWLManager.createOWLOntologyManager();
        try {
            OWLOntology ont = mngr.createOntology(ONTOLOGY_URI);
            final OWLDataFactory df = mngr.getOWLDataFactory();

            OWLClass a = df.getOWLClass(URI.create(ONTOLOGY_URI + "#A"));
            OWLClass b = df.getOWLClass(URI.create(ONTOLOGY_URI + "#B"));
            OWLClass c = df.getOWLClass(URI.create(ONTOLOGY_URI + "#C"));
            OWLClass d = df.getOWLClass(URI.create(ONTOLOGY_URI + "#D"));
            OWLClass e = df.getOWLClass(URI.create(ONTOLOGY_URI + "#E"));

            OWLObjectProperty p = df.getOWLObjectProperty(URI.create(ONTOLOGY_URI + "#p"));
            OWLObjectProperty q = df.getOWLObjectProperty(URI.create(ONTOLOGY_URI + "#q"));
            OWLObjectProperty r = df.getOWLObjectProperty(URI.create(ONTOLOGY_URI + "#r"));

            List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

            final OWLSubClassAxiom aPSomeB = df.getOWLSubClassAxiom(a, df.getOWLObjectSomeRestriction(p, b));
            Set<OWLDescription> and = new HashSet<OWLDescription>();
            and.add(c);
            and.add(df.getOWLObjectSomeRestriction(r, e));
            OWLObjectIntersectionOf cAndRSomeE = df.getOWLObjectIntersectionOf(and);

            final OWLSubClassAxiom aPSomeC = df.getOWLSubClassAxiom(a, df.getOWLObjectSomeRestriction(p, cAndRSomeE));
            final OWLSubClassAxiom aQSomeD = df.getOWLSubClassAxiom(a, df.getOWLObjectSomeRestriction(q, d));

            changes.add(new AddAxiom(ont, aPSomeB));
            changes.add(new AddAxiom(ont, aPSomeC));
            changes.add(new AddAxiom(ont, aQSomeD));

            mngr.applyChanges(changes);

            OutlineTreeModel model = new OutlineTreeModel(mngr, Collections.singleton(ont), new BasicComparator());
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

            // test axioms
//            final Set aNodeAxioms = aNode.getAxioms();
//            assertEquals(3, aNodeAxioms.size());
//            assertTrue(aNodeAxioms.contains(aPSomeB));
//            assertTrue(aNodeAxioms.contains(aPSomeC));
//            assertTrue(aNodeAxioms.contains(aQSomeD));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
