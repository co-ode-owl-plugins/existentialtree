package org.coode.existentialtree.model2;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;

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
public class OWLExistentialTreeModel implements TreeModel {

    private OWLDescriptionNode root;
    private Comparator<ExistentialNode> comparator;

    private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

    private Set<OWLObjectProperty> props;
    private Set<OWLOntology> onts;

    public OWLExistentialTreeModel(Set<OWLOntology> onts, Comparator<ExistentialNode> comparator) {
        this.onts = onts;
        this.comparator = comparator;
    }

    public void setRoot(OWLClass cls){
        OWLDescriptionNode oldRoot = root;
        this.root = new OWLDescriptionNode(cls, this);
        for (TreeModelListener l : listeners){
            l.treeStructureChanged(new TreeModelEvent(this, new Object[]{oldRoot}));
        }
    }

    public ExistentialNode getRoot() {
        return root;
    }

    public ExistentialNode getChild(Object object, int i) {
        return (ExistentialNode) ((ExistentialNode)object).getChildren().get(i);
    }

    public int getChildCount(Object object) {
        return ((ExistentialNode)object).getChildren().size();
    }

    public boolean isLeaf(Object object) {
        return getChildCount(object) == 0;
    }

    public void valueForPathChanged(TreePath treePath, Object object) {
        //@@TODO implement
    }

    public int getIndexOfChild(Object object, Object object1) {
        return ((ExistentialNode)object).getChildren().indexOf(object1);
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

    public Comparator<ExistentialNode> getComparator() {
        return comparator;
    }

    public void setProperties(Set<OWLObjectProperty> props) {
        this.props = props;
        for (TreeModelListener l : listeners){
            l.treeStructureChanged(new TreeModelEvent(this, new Object[]{root}));
        }
    }

    public Set<OWLObjectProperty> getProperties(){
        return props;
    }
}
