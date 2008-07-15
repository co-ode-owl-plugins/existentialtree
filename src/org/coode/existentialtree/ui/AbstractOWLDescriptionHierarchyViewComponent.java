package org.coode.existentialtree.ui;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeCellRenderer;
import org.protege.editor.owl.ui.view.AbstractOWLClassViewComponent;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owl.model.*;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
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
 * Date: Sep 11, 2007<br><br>
 *
 * Variant of AbstractOWLClassHierarchyViewComponent allowing OWLDescription nodes
 *
 */
public abstract class AbstractOWLDescriptionHierarchyViewComponent extends AbstractOWLClassViewComponent
        implements Findable<OWLClass> { //, Deleteable {

        private OWLModelManagerTree<OWLDescription> tree;

        private TreeSelectionListener listener;

        final public void initialiseClassView() throws Exception {

            setLayout(new BorderLayout(7, 7));

            tree = new OWLModelManagerTree<OWLDescription>(getOWLEditorKit(),
                                                           getHierarchyProvider()){
                public String getToolTipText(MouseEvent event) {
                    OWLObject obj = getOWLObjectAtMousePosition(event);
                    if (obj == null){
                        return null;
                    }
                    else if (obj instanceof OWLEntity){
                        return super.getToolTipText(event);
                    }
                    else{
                        return getOWLModelManager().getRendering(obj);
                    }
                }
            };

            final Comparator<OWLDescription> comp = getOWLModelManager().getOWLObjectComparator();
            tree.setOWLObjectComparator(comp);

            tree.setCellRenderer(new OWLObjectTreeCellRenderer(getOWLEditorKit()){

                protected String getRendering(Object object) {
                    if (object instanceof OWLObjectIntersectionOf){
                        for (OWLDescription op : ((OWLObjectIntersectionOf)object).getOperands()){
                            if (op instanceof OWLClass){
                                return super.getRendering(op) + "...";
                            }
                        }
                    }
                    return super.getRendering(object);
                }

            });
//            tree.getCellRenderer().setPreferredWidth(tree.getPreferredSize().width);

            initSelectionManagement();
            add(ComponentFactory.createScrollPane(tree));
            performExtraInitialisation();
            OWLClass cls = getSelectedOWLClass();
            if (cls != null) {
                tree.setSelectedOWLObject(cls);
                if (tree.getSelectionPath() != null) {
                    tree.scrollPathToVisible(tree.getSelectionPath());
                }
            }
            tree.getModel().addTreeModelListener(new TreeModelListener() {
                public void treeNodesChanged(TreeModelEvent e) {
                }


                public void treeNodesInserted(TreeModelEvent e) {
                    ensureSelection();
                }


                public void treeNodesRemoved(TreeModelEvent e) {
                    ensureSelection();
                }


                public void treeStructureChanged(TreeModelEvent e) {
                    ensureSelection();
                }
            });
            tree.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    transmitSelection();
                }
            });
        }

        private void ensureSelection() {
            OWLClass cls = getSelectedOWLClass();
            if (cls != null) {
                OWLDescription treeSel = tree.getSelectedOWLObject();
                if (treeSel == null || !treeSel.equals(cls)) {
                    tree.setSelectedOWLObject(cls);
                }
            }
        }


        public boolean requestFocusInWindow() {
            return tree.requestFocusInWindow();
        }


        protected OWLObjectTree<OWLDescription> getTree() {
            return tree;
        }


        protected abstract void performExtraInitialisation() throws Exception;


        private void initSelectionManagement() {
            // Hook up a selection listener so that we can transmit our
            // selection to the main selection model

            listener = new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    transmitSelection();
                }
            };
            tree.addTreeSelectionListener(listener);
        }


        protected void transmitSelection() {
//            deletableChangeListenerMediator.fireStateChanged(this);
            if (!isPinned()) {
                OWLDescription selCls = tree.getSelectedOWLObject();
                if (selCls != null && selCls instanceof OWLClass) {
                    setSelectedEntity((OWLClass)selCls);
                }
                else {
                    // Update from OWL selection model
                    updateViewContentAndHeader();
                }
            }
        }


        protected OWLClass updateView(OWLClass selectedClass) {
            if (tree.getSelectedOWLObject() == null) {
                if (selectedClass != null) {
                    tree.setSelectedOWLObject(selectedClass);
                }
                else {
                    // Don't need to do anything - both null
                }
            }
            else {
                if (!tree.getSelectedOWLObject().equals(selectedClass)) {
                    tree.setSelectedOWLObject(selectedClass);
                }
            }

            return selectedClass;
        }


        protected abstract OWLObjectHierarchyProvider<OWLDescription> getHierarchyProvider();


        public void disposeView() {
            // Dispose of the tree selection listener
            if (tree != null) {
                tree.removeTreeSelectionListener(listener);
                tree.dispose();
            }
        }


        protected OWLObject getObjectToCopy() {
            return tree.getSelectedOWLObject();
        }

        //////////////////////////////////////////////////////////////////////////////////////
        //
        // Implementation of Deleteable
        //
        /////////////////////////////////////////////////////////////////////////////////////

//        private ChangeListenerMediator deletableChangeListenerMediator = new ChangeListenerMediator();
//
//
//        public void addChangeListener(ChangeListener listener) {
//            deletableChangeListenerMediator.addChangeListener(listener);
//        }
//
//
//        public void removeChangeListener(ChangeListener listener) {
//            deletableChangeListenerMediator.removeChangeListener(listener);
//        }
//
//
//        public void handleDelete() {
//            //hierarchyDeleter.performDeletion();
//        }
//
//
//        public boolean canDelete() {
//            return !tree.getSelectedOWLObjects().isEmpty();
//        }

        //////////////////////////////////////////////////////////////////////////////////////
        //
        // Implementation of Findable
        //
        /////////////////////////////////////////////////////////////////////////////////////


        public java.util.List<OWLClass> find(String match) {
            // Here we should just find classes
            return getOWLModelManager().getMatchingOWLClasses(match);
        }

        public void show(OWLClass cls) {
            getTree().setSelectedOWLObject(cls);
        }
}
