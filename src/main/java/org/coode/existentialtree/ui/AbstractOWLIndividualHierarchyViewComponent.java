package org.coode.existentialtree.ui;

import java.awt.BorderLayout;
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

import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.action.OWLObjectHierarchyDeleter;
import org.protege.editor.owl.ui.tree.OWLModelManagerTree;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeCellRenderer;
import org.protege.editor.owl.ui.view.ChangeListenerMediator;
import org.protege.editor.owl.ui.view.Findable;
import org.protege.editor.owl.ui.view.individual.AbstractOWLIndividualViewComponent;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.OWLEntitySetProvider;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Sep 11, 2007<br><br>
 *
 * Variant of AbstractOWLClassHierarchyViewComponent allowing OWLClassExpression nodes
 *
 */
public abstract class AbstractOWLIndividualHierarchyViewComponent extends AbstractOWLIndividualViewComponent
        implements Findable<OWLNamedIndividual> { //, Deleteable {
    private static final long serialVersionUID = 1L;

    protected OWLModelManagerTree<OWLNamedIndividual> tree;

    private TreeSelectionListener listener;

    private OWLObjectHierarchyDeleter<OWLNamedIndividual> hierarchyDeleter;

    @Override
    final public void initialiseIndividualsView() throws Exception {

        setLayout(new BorderLayout(7, 7));

        tree = new OWLModelManagerTree<OWLNamedIndividual>(getOWLEditorKit(),
                                                      getOWLIndividualHierarchyProvider());

        tree.setCellRenderer(new OWLObjectTreeCellRenderer(getOWLEditorKit()));

        initSelectionManagement();
        add(ComponentFactory.createScrollPane(tree));
        performExtraInitialisation();
        OWLNamedIndividual individual = getSelectedOWLIndividual();
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
            @Override
            public void mouseReleased(MouseEvent e) {
                transmitSelection();
            }
        });

        hierarchyDeleter = new OWLObjectHierarchyDeleter<OWLNamedIndividual>(getOWLEditorKit(),
                                                                        getOWLIndividualHierarchyProvider(),
                                                                        new OWLEntitySetProvider<OWLNamedIndividual>() {
                                                                            public Set<OWLNamedIndividual> getEntities() {
                                                                                return new HashSet<OWLNamedIndividual>(tree.getSelectedOWLObjects());
                                                                            }
                                                                        },
                                                                        "individuals");

    }

    protected void ensureSelection() {
        OWLNamedIndividual ind = getSelectedOWLIndividual();
        if (ind != null) {
            OWLNamedIndividual treeSel = tree.getSelectedOWLObject();
            if (treeSel == null || !treeSel.equals(ind)) {
                tree.setSelectedOWLObject(ind);
            }
        }
    }


    @Override
    public boolean requestFocusInWindow() {
        return tree.requestFocusInWindow();
    }


    protected OWLObjectTree<OWLNamedIndividual> getTree() {
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
            OWLNamedIndividual individual = tree.getSelectedOWLObject();
            if (individual != null) {
                setGlobalSelection(individual);
            }
            else {
                // Update from OWL selection model
                updateViewContentAndHeader();
            }
        }
    }


    @Override
    public OWLNamedIndividual updateView(OWLNamedIndividual individual) {
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


    protected abstract OWLObjectHierarchyProvider<OWLNamedIndividual> getOWLIndividualHierarchyProvider();


    @Override
    public void disposeView() {
        // Dispose of the tree selection listener
        if (tree != null) {
            tree.removeTreeSelectionListener(listener);
            tree.dispose();
        }
    }


    @Override
    protected OWLObject getObjectToCopy() {
        return tree.getSelectedOWLObject();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    //
    // Implementation of Deleteable
    //
    /////////////////////////////////////////////////////////////////////////////////////

    private ChangeListenerMediator deletableChangeListenerMediator = new ChangeListenerMediator();


    public void addChangeListener(ChangeListener l) {
        deletableChangeListenerMediator.addChangeListener(l);
    }


    public void removeChangeListener(ChangeListener l) {
        deletableChangeListenerMediator.removeChangeListener(l);
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


    public java.util.List<OWLNamedIndividual> find(String match) {
        return new ArrayList<OWLNamedIndividual>(getOWLModelManager().getOWLEntityFinder().getMatchingOWLIndividuals(match));
    }

    public void show(OWLNamedIndividual individual) {
        getTree().setSelectedOWLObject(individual);
    }
}
