package org.coode.existentialtree.ui;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.OWLEntityComparator;
import org.protege.editor.owl.ui.action.OWLObjectHierarchyDeleter;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeCellRenderer;
import org.protege.editor.owl.ui.view.AbstractOWLIndividualViewComponent;
import org.protege.editor.owl.ui.view.ChangeListenerMediator;
import org.protege.editor.owl.ui.view.Findable;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.util.OWLEntitySetProvider;

import javax.swing.event.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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
 * Date: Sep 11, 2007<br><br>
 *
 * Variant of AbstractOWLClassHierarchyViewComponent allowing OWLDescription nodes
 *
 */
public abstract class AbstractOWLIndividualHierarchyViewComponent extends AbstractOWLIndividualViewComponent
        implements Findable<OWLIndividual> { //, Deleteable {

    private OWLModelManagerTree<OWLIndividual> tree;

    private TreeSelectionListener listener;

    private OWLObjectHierarchyDeleter<OWLIndividual> hierarchyDeleter;

    final public void initialiseIndividualsView() throws Exception {

        setLayout(new BorderLayout(7, 7));

        tree = new OWLModelManagerTree<OWLIndividual>(getOWLEditorKit(),
                                                      getOWLIndividualHierarchyProvider());

        tree.setOWLObjectComparator(new OWLEntityComparator<OWLIndividual>(getOWLModelManager()));

        tree.setCellRenderer(new OWLObjectTreeCellRenderer(getOWLEditorKit()));

        initSelectionManagement();
        add(ComponentFactory.createScrollPane(tree));
        performExtraInitialisation();
        OWLIndividual individual = getSelectedOWLIndividual();
        if (individual != null) {
            tree.setSelectedOWLObject(individual);
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

        hierarchyDeleter = new OWLObjectHierarchyDeleter<OWLIndividual>(getOWLEditorKit(),
                                                                        getOWLIndividualHierarchyProvider(),
                                                                        new OWLEntitySetProvider<OWLIndividual>() {
                                                                            public Set<OWLIndividual> getEntities() {
                                                                                return new HashSet<OWLIndividual>(tree.getSelectedOWLObjects());
                                                                            }
                                                                        },
                                                                        "individuals");

    }

    private void ensureSelection() {
        OWLIndividual ind = getSelectedOWLIndividual();
        if (ind != null) {
            OWLIndividual treeSel = tree.getSelectedOWLObject();
            if (treeSel == null || !treeSel.equals(ind)) {
                tree.setSelectedOWLObject(ind);
            }
        }
    }


    public boolean requestFocusInWindow() {
        return tree.requestFocusInWindow();
    }


    protected OWLObjectTree<OWLIndividual> getTree() {
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
            OWLIndividual individual = tree.getSelectedOWLObject();
            if (individual != null && individual instanceof OWLClass) {
                setSelectedEntity(individual);
            }
            else {
                // Update from OWL selection model
                updateViewContentAndHeader();
            }
        }
    }


    protected OWLIndividual updateView(OWLIndividual individual) {
        if (tree.getSelectedOWLObject() == null) {
            if (individual != null) {
                tree.setSelectedOWLObject(individual);
            }
            else {
                // Don't need to do anything - both null
            }
        }
        else {
            if (!tree.getSelectedOWLObject().equals(individual)) {
                tree.setSelectedOWLObject(individual);
            }
        }

        return individual;
    }


    protected abstract OWLObjectHierarchyProvider<OWLIndividual> getOWLIndividualHierarchyProvider();


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

    private ChangeListenerMediator deletableChangeListenerMediator = new ChangeListenerMediator();


    public void addChangeListener(ChangeListener listener) {
        deletableChangeListenerMediator.addChangeListener(listener);
    }


    public void removeChangeListener(ChangeListener listener) {
        deletableChangeListenerMediator.removeChangeListener(listener);
    }


    public void handleDelete() {
        hierarchyDeleter.performDeletion();
    }


    public boolean canDelete() {
        return !tree.getSelectedOWLObjects().isEmpty();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //
    // Implementation of Findable
    //
    /////////////////////////////////////////////////////////////////////////////////////


    public java.util.List<OWLIndividual> find(String match) {
        // Here we should just find individuals
        return new ArrayList<OWLIndividual>(getOWLModelManager().getEntityFinder().getMatchingOWLIndividuals(match));
    }

    public void show(OWLIndividual individual) {
        getTree().setSelectedOWLObject(individual);
    }
}
