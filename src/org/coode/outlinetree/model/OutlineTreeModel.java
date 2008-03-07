package org.coode.outlinetree.model;

import org.semanticweb.owl.model.*;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
 */
public class OutlineTreeModel implements TreeModel, OutlineNodeFactory {

    private OutlineNode<OWLClass, OutlineNode> root;
    private Comparator<OutlineNode> comparator;

    private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

    private Set<OWLPropertyExpression> props;
    private Set<OWLOntology> onts;
    private OWLOntologyManager mngr;
    private int min = 1;

    public OutlineTreeModel(OWLOntologyManager mngr,
                            Set<OWLOntology> onts,
                            Comparator<OutlineNode> comparator) {
        this.mngr = mngr;
        this.onts = onts;
        this.comparator = comparator;
    }

    public void setRoot(OWLClass cls){
        OutlineNode oldRoot = root;
        this.root = createNode(cls, null);
        for (TreeModelListener l : listeners){
            l.treeStructureChanged(new TreeModelEvent(this, new Object[]{oldRoot}));
        }
    }

    public OutlineNode getRoot() {
        return root;
    }

    public OutlineNode getChild(Object object, int i) {
        return (OutlineNode) ((OutlineNode)object).getChildren().get(i);
    }

    public int getChildCount(Object object) {
        return ((OutlineNode)object).getChildren().size();
    }

    public boolean isLeaf(Object object) {
        return getChildCount(object) == 0;
    }

    public void valueForPathChanged(TreePath treePath, Object object) {
        //@@TODO implement
    }

    public int getIndexOfChild(Object object, Object object1) {
        return ((OutlineNode)object).getChildren().indexOf(object1);
    }

    public void addTreeModelListener(TreeModelListener treeModelListener) {
        listeners.add(treeModelListener);
    }

    public void removeTreeModelListener(TreeModelListener treeModelListener) {
        listeners.remove(treeModelListener);
    }

    public Set<OWLOntology> getOntologies() {
        return onts;
    }

    public Comparator<OutlineNode> getComparator() {
        return comparator;
    }

    public void setProperties(Set<OWLPropertyExpression> props) {
        this.props = props;
        for (TreeModelListener l : listeners){
            l.treeStructureChanged(new TreeModelEvent(this, new Object[]{root}));
        }
    }

    public Set<OWLPropertyExpression> getFilterProperties(){
        return props;
    }

    public OWLClass getOWLThing() {
        return mngr.getOWLDataFactory().getOWLThing();
    }

    public int getMin() {
        return min;
    }

    public void setMin(int m){
        min = m;
        setRoot(root.getUserObject()); // regenerate the root object
    }

    public <T extends OutlineNode> T createNode(OWLObject object, OutlineNode parent) {
        T node;
        if (object instanceof OWLConstant){
            node = (T)new OWLConstantNode((OWLConstant)object, this);
        }
        else if (object instanceof OWLDataRange){
            node = (T)new OWLDataRangeNode((OWLDataRange)object, this);
        }
        else if (object instanceof OWLPropertyExpression){
            node = (T)new OWLPropertyNode((OWLPropertyExpression)object, this);
        }
        else if (object instanceof OWLIndividual){
            node = (T)new OWLIndividualNode((OWLIndividual)object, this);
        }
        else if (object instanceof OWLClass){
            node = (T)new OWLClassNode((OWLClass)object, this);
        }
        else {
            node = (T)new OWLAnonymousClassNode((OWLDescription)object, this);
        }
        node.setParent(parent);
        return node;
    }

//    public void add(OutlineNode child, OutlineNode parent, OWLOntology ont) throws OWLOntologyChangeException {
//        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
//        if (parent instanceof OWLPropertyNode && child instanceof OWLAnonymousClassNode){
//            OWLDescription cls = ((OWLPropertyNode)parent).getParent().getUserObject();
//            OWLObjectProperty p = (OWLObjectProperty)((OWLPropertyNode)parent).getProperty();
//            OWLDescription filler = ((OWLAnonymousClassNode)child).getUserObject();
//            OWLDescription restr = mngr.getOWLDataFactory().getOWLObjectSomeRestriction(p, filler);
//            changes.add(new AddAxiom(ont, mngr.getOWLDataFactory().getOWLSubClassAxiom(cls, restr)));
//        }
//        else if (parent instanceof OWLAnonymousClassNode && child instanceof OWLPropertyNode){
//            // "adding a property to a class" = adding subClassOf(cls, someValuesFrom(p, owlThing))
//            OWLDescription cls = ((OWLAnonymousClassNode)parent).getUserObject();
//            OWLObjectProperty p = (OWLObjectProperty)((OWLPropertyNode)child).getProperty();
//            OWLDescription restr = mngr.getOWLDataFactory().getOWLObjectSomeRestriction(p, mngr.getOWLDataFactory().getOWLThing());
//            changes.add(new AddAxiom(ont, mngr.getOWLDataFactory().getOWLSubClassAxiom(cls, restr)));
//        }
//        mngr.applyChanges(changes);
//    }

    public OWLOntologyManager getOWLOntologyManager() {
        return mngr;
    }
}
