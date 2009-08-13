package org.coode.outlinetree.ui;

import org.coode.outlinetree.model.OutlineNode;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashSet;
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
public class OutlineView extends AbstractOutlineView {

    private OutlineTree tree;

    private TreeSelectionListener treeSelListener = new TreeSelectionListener(){
        public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
            OutlineNode node = (OutlineNode)treeSelectionEvent.getPath().getLastPathComponent();
            handleNodeSelection(node);
        }
    };

    public void disposeView() {
        if (tree != null){
            tree.getSelectionModel().removeTreeSelectionListener(treeSelListener);
        }
        super.disposeView();
    }

    protected void refreshGUI() {
        if (tree == null){
            tree = new OutlineTree(model, getOWLEditorKit());
            tree.getSelectionModel().addTreeSelectionListener(treeSelListener);

            add(new JScrollPane(tree), BorderLayout.CENTER);
        }
        else{
            tree.setModel(model);
        }
        expandFirstChildren();
    }


    private void expandFirstChildren() {
        for (TreePath path : getFirstChildrenPaths()){
            tree.expandPath(path);
        }
    }

    private Set<TreePath> getFirstChildrenPaths() {
        Set<TreePath> paths = new HashSet<TreePath>();
        for (int i=tree.getRowCount()-1; i>0; i--){
            paths.add(tree.getPathForRow(i));
        }
        return paths;
    }

    protected OWLClass updateView(OWLClass selectedClass) {
        if (isSynchronizing()){
            if (!ignoreUpdateView){
                refresh();
                return (model == null || model.getRoot() == null) ? null : (OWLClass)model.getRoot().getUserObject();
            }
            ignoreUpdateView = false;
        }
        return selectedClass;
    }

    protected void updateHeader(OWLObject object) {
        String str = "(" + propertyLabel + ")";
        if (object != null){
            str += " " + getOWLModelManager().getRendering(object);
        }
        getView().setHeaderText(str);
    }


    private void handleNodeSelection(OutlineNode node) {
        this.currentSelection = node;
        OWLObject owlObject = node.getRenderedObject();
        if (owlObject instanceof OWLClass){
            ignoreUpdateView = true;
            getOWLWorkspace().getOWLSelectionModel().setSelectedEntity((OWLEntity)owlObject);
        }

        addNodeAction.setEnabled(node.isEditable());
    }
}
