package org.coode.outlinetree.test;

import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.coode.outlinetree.model.OutlineNode;
import org.coode.outlinetree.model.OutlineTreeModel;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.net.URI;
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
 * Date: Oct 30, 2007<br><br>
 */
public class TestExistentialModel extends TestCase {

    private OWLOntologyManager mngr;

    private static final String PIZZA_NS = "http://www.co-ode.org/ontologies/pizza/pizza.owl#";

    private OWLOntology ont;

    private int indent = 0;

    public void testDirectChildren(){
        init();
        OutlineTreeModel model = new OutlineTreeModel(mngr, mngr.getOntologies(), null, new BasicComparator());
        model.setRoot(getNamedClass("Pizza"));
        OutlineNode root = model.getRoot();
        assertEquals(1, model.getChildCount(root));

        OutlineNode hasBase = model.getChild(root, 0);
        assertEquals(getNamedProperty("hasBase"), hasBase.getUserObject());

        assertEquals(1, model.getChildCount(hasBase));
        OutlineNode pizzaBase = model.getChild(hasBase, 0);
        assertEquals(getNamedClass("PizzaBase"), pizzaBase.getUserObject());
    }

    public void testInheritedChildren(){
        init();
        OutlineTreeModel model = new OutlineTreeModel(mngr, mngr.getOntologies(), null, new BasicComparator());
        model.setRoot(getNamedClass("NamedPizza"));
        OutlineNode root = model.getRoot();
        System.out.println("root.getChildren() = " + root.getChildren());
        assertEquals(1, model.getChildCount(root));

        OutlineNode hasBase = model.getChild(root, 0);
        assertEquals(getNamedProperty("hasBase"), hasBase.getUserObject());

        System.out.println("hasBase.getChildren() = " + hasBase.getChildren());
        assertEquals(1, model.getChildCount(hasBase));
        OutlineNode pizzaBase = model.getChild(hasBase, 0);
        assertEquals(getNamedClass("PizzaBase"), pizzaBase.getUserObject());
    }

    public void testHandlingCycles(){
        init();
        OutlineTreeModel model = new OutlineTreeModel(mngr, mngr.getOntologies(), null, new BasicComparator());
        model.setRoot(getNamedClass("Hot"));
        OutlineNode root = model.getRoot();
        System.out.println("root.getChildren() = " + root.getChildren());
        assertEquals(0, model.getChildCount(root));
    }

    public void testDefinedClassChildren(){
        init();
        OutlineTreeModel model = new OutlineTreeModel(mngr, mngr.getOntologies(), null, new BasicComparator());
        model.setRoot(getNamedClass("CheeseyPizza"));
        OutlineNode root = model.getRoot();
        assertEquals(2, model.getChildCount(root));

        OutlineNode hasBase = null;
        for (int i=0; i<model.getChildCount(root); i++){
        OutlineNode child = model.getChild(root, i);
            if (child.getUserObject().equals(getNamedProperty("hasBase"))){
                hasBase = child;
            }
        }
        assertNotNull(hasBase);
        assertEquals(1, model.getChildCount(hasBase));
        OutlineNode pizzaBase = model.getChild(hasBase, 0);
        assertEquals(getNamedClass("PizzaBase"), pizzaBase.getUserObject());

        OutlineNode hasTopping = null;
        for (int i=0; i<model.getChildCount(root); i++){
        OutlineNode child = model.getChild(root, i);
            if (child.getUserObject().equals(getNamedProperty("hasTopping"))){
                hasTopping = child;
            }
        }
        assertNotNull(hasTopping);
        assertEquals(1, model.getChildCount(hasTopping));
        OutlineNode cheeseTopping = model.getChild(hasTopping, 0);
        assertEquals(getNamedClass("CheeseTopping"), cheeseTopping.getUserObject());
    }

    private OutlineNode findNode(OutlineTreeModel model, OutlineNode start, OWLClass namedClass) {
        indent++;
        for (int i=0; i<model.getChildCount(start); i++){
            OutlineNode child = model.getChild(start, i);
            System.out.println(StringUtils.leftPad("+ " + child, indent*2));
            Object obj = child.getUserObject();
            if (obj.equals(namedClass)){
                return child;
            }
            else if (!model.isLeaf(child)){
                OutlineNode node = findNode(model, child, namedClass);
                if (node != null){
                    return node;
                }
            }
        }
        indent--;
        return null;
    }

    private void init(){
        if (mngr == null){
            mngr = OWLManager.createOWLOntologyManager();
            try {
                ont = mngr.loadOntologyFromPhysicalURI(getClass().getResource("pizza.owl").toURI());
            }
            catch (Exception e) {
                e.printStackTrace();
                fail();
            }
        }
    }
    
    private OWLClass getNamedClass(String name) {
        URI uri = URI.create(PIZZA_NS + name);
        OWLClass cls = mngr.getOWLDataFactory().getOWLClass(uri);
        assertTrue(ont.containsClassReference(uri));
        return cls;
    }

    private OWLObjectProperty getNamedProperty(String name) {
        URI uri = URI.create(PIZZA_NS + name);
        OWLObjectProperty property = mngr.getOWLDataFactory().getOWLObjectProperty(uri);
        assertTrue(ont.containsObjectPropertyReference(uri));
        return property;
    }
}
