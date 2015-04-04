package org.coode.outlinetree.model;

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

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.RemoveAxiom;

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

    private OWLObjectHierarchyProvider<OWLClass> subsumptionHierarchyProvider;

    private Set<OWLPropertyExpression> props;
    private Set<OWLOntology> onts;
    private OWLOntologyManager mngr;
    private int min = 1;

    private boolean showInheritedChildrenAllNodes = false;

    private boolean showAssertedChildrenAllNodes = false;


    /**
     *
     * @param mngr
     * @param onts
     * @param subsumptionHierarchyProvider used for inheritance of restrictions
     * @param comparator
     */
    public OutlineTreeModel(OWLOntologyManager mngr,
                            Set<OWLOntology> onts,
                            OWLObjectHierarchyProvider<OWLClass> subsumptionHierarchyProvider,
                            Comparator<OutlineNode> comparator) {
        this.mngr = mngr;
        this.onts = onts;
        this.subsumptionHierarchyProvider = subsumptionHierarchyProvider;
        this.comparator = comparator;
    }


    public void setShowInheritedChildrenAllNodes(boolean show){
        this.showInheritedChildrenAllNodes = show;
    }


    public void setShowAssertedChildrenAllNodes(boolean show) {
        this.showAssertedChildrenAllNodes = show;
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
        if (root != null){
            setRoot(root.getUserObject()); // regenerate the root object
        }
    }

    public <T extends OutlineNode> T createNode(OWLObject object, OutlineNode parent) {
        T node;
        if (object instanceof OWLLiteral ||
                object instanceof OWLDataRange ||
                object instanceof OWLIndividual){
            node = (T)new OutlineLeafNode(object, this);
        }
        else if (object instanceof OWLPropertyExpression){
            node = (T)new OWLPropertyNode((OWLPropertyExpression)object, this);
        }
        else if (object instanceof OWLClass){
            node = (T)new OWLClassNode((OWLClass)object, this);
        }
        else {
            node = (T)new OWLClassExpressionNode((OWLClassExpression)object, this);
        }
        node.setParent(parent);
        return node;
    }

    public List<OWLOntologyChange> add(OutlineNode child, OutlineNode parent, OWLOntology ont) {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

        final OWLDataFactory df = mngr.getOWLDataFactory();

        if (parent instanceof OWLPropertyNode && child instanceof OWLClassNode){
            OWLClassExpression cls = ((OWLPropertyNode)parent).getParent().getUserObject();
            OWLObjectProperty p = (OWLObjectProperty)((OWLPropertyNode)parent).getRenderedObject();
            OWLClassExpression filler = ((OWLClassNode)child).getUserObject();
            OWLClassExpression restr = df.getOWLObjectSomeValuesFrom(p, filler);

            // if it exists, overwrite the default p some Thing with the more specific restriction
            OWLObjectSomeValuesFrom pSomeThing = df.getOWLObjectSomeValuesFrom(p, df.getOWLThing());
            OWLAxiom clsSubPSomeThing = df.getOWLSubClassOfAxiom(cls, pSomeThing);
            if (ont.containsAxiom(clsSubPSomeThing)){
                changes.add(new RemoveAxiom(ont, clsSubPSomeThing));
            }

            changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cls, restr)));
        }
        else if (parent instanceof OWLClassNode && child instanceof OWLPropertyNode){
            // "adding a property to a class" = adding subClassOf(cls, someValuesFrom(p, owlThing))
            OWLClassExpression cls = ((OWLClassNode)parent).getUserObject();
            OWLObjectProperty p = (OWLObjectProperty)((OWLPropertyNode)child).getRenderedObject();
            OWLClassExpression restr = df.getOWLObjectSomeValuesFrom(p, df.getOWLThing());
            changes.add(new AddAxiom(ont, df.getOWLSubClassOfAxiom(cls, restr)));
        }
        
        return changes;
    }

    /**
     * As we carry a list of axioms for each node, we can work out which to remove/alter.
     * @param node
     * @return
     * @throws OWLOntologyChangeException
     */
    public List<OWLOntologyChange> delete(OutlineNode node) throws OWLOntologyChangeException {
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
//        final Set<OWLAxiom> axioms = node.getAxioms(); // god knows why intelliJ is getting the generics f***d up on this
//        for (OWLAxiom ax : axioms){
//        TODO below will only remove the whole path for the current node - need to handle truncating arbitrary descriptions
//            if (isSubjectOfAxiom(getRoot().getUserObject(), ax)){
//                for (OWLOntology ont : onts){
//                    if (ont.containsAxiom(ax)){
//                        changes.add(new RemoveAxiom(ont, ax));
//                    }
//                }
//            }
      //  }
        return changes;
    }


    public OWLOntologyManager getOWLOntologyManager() {
        return mngr;
    }


    public OWLObjectHierarchyProvider<OWLClass> getClassHierarchyProvider() {
        return subsumptionHierarchyProvider;
    }

    public boolean getShowInheritedChildrenAllNodes() {
        return showInheritedChildrenAllNodes;
    }


    public boolean getShowAssertedChildrenAllNodes() {
        return showAssertedChildrenAllNodes;
    }
}
