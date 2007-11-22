package org.coode.existentialtree.view;

import org.coode.existentialtree.model2.ExistentialNode;
import org.coode.existentialtree.model2.OWLExistentialTreeModel;
import org.coode.existentialtree.ui.ExistentialTree;
import org.coode.existentialtree.util.ExistentialNodeComparator;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.renderer.OWLEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRenderer;
import org.protege.editor.owl.ui.view.AbstractOWLClassViewComponent;
import org.semanticweb.owl.model.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
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
public class ExistentialTreeView2 extends AbstractOWLClassViewComponent {

    private OWLExistentialTreeModel model;
    private ExistentialTree tree;

    private static final String ALL_PROPERTIES = "all properties";

    private String propertyLabel = ALL_PROPERTIES;

    private boolean ignoreUpdateView = false;

    private boolean requiresRefresh = false;

    private TreeSelectionListener treeSelListener = new TreeSelectionListener(){
        public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
            ExistentialNode node = (ExistentialNode)treeSelectionEvent.getPath().getLastPathComponent();
            OWLObject owlObject = node.getUserObject();
            if (owlObject instanceof OWLClass){
                ignoreUpdateView = true;
                getOWLWorkspace().getOWLSelectionModel().setSelectedEntity((OWLEntity)owlObject);
            }
        }
    };

    private OWLOntologyChangeListener ontListener = new OWLOntologyChangeListener(){
        public void ontologiesChanged(java.util.List<? extends OWLOntologyChange> changes) throws OWLException {
            refresh();
        }
    };

    private HierarchyListener hListener = new HierarchyListener(){
        public void hierarchyChanged(HierarchyEvent hierarchyEvent) {
            if (requiresRefresh){
                refresh();
            }
        }
    };

    private DisposableAction selectObjectPropertyAction = new DisposableAction("Select Object Property", OWLIcons.getIcon("property.object.png")){
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            OWLProperty prop = new UIHelper(getOWLEditorKit()).pickOWLObjectProperty();
            if (prop != null){
                propertyLabel = getOWLModelManager().getOWLEntityRenderer().render(prop);
                clearPropertyAction.setEnabled(true);
                model.setProperties(new HashSet<OWLPropertyExpression>(generateAllDescendants(prop)));
                refresh();
                updateHeader(getSelectedOWLClass());
            }
        }
    };

    private DisposableAction selectDataPropertyAction = new DisposableAction("Select Data Property", OWLIcons.getIcon("property.data.png")){
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            OWLProperty prop = new UIHelper(getOWLEditorKit()).pickOWLDataProperty();
            if (prop != null){
                propertyLabel = getOWLModelManager().getOWLEntityRenderer().render(prop);
                clearPropertyAction.setEnabled(true);
                model.setProperties(new HashSet<OWLPropertyExpression>(generateAllDescendants(prop)));
                refresh();
                updateHeader(getSelectedOWLClass());
                // change clearProp
            }
        }
    };

    private DisposableAction clearPropertyAction = new DisposableAction("Clear Property",
                                                                        OWLIcons.getIcon("property.object.delete.png")){
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            propertyLabel = ALL_PROPERTIES;
            model.setProperties(null);
            refresh();
            updateHeader(getSelectedOWLClass());
            setEnabled(false);
        }
    };

    private DisposableAction showMinZeroAction = new DisposableAction("Show min 0", null){
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            if (model.getMin() == 0){
                model.setMin(1);
            }
            else{
                model.setMin(0);
            }
        }
    };

    private void refresh() {
        if (isShowing()){
            OWLClass selectedOWLClass = getSelectedOWLClass();
            if (selectedOWLClass != null){
                model.setRoot(selectedOWLClass);
                if (tree == null){
                    tree = new ExistentialTree(model, getOWLEditorKit());
                    tree.getSelectionModel().addTreeSelectionListener(treeSelListener);

                    add(new JScrollPane(tree), BorderLayout.CENTER);
                }

                expandFirstChildren();
            }
            requiresRefresh = false;
        }
        else{
            requiresRefresh = true;
        }
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

    public void initialiseClassView() throws Exception {
        setLayout(new BorderLayout());

        OWLModelManager mngr = getOWLModelManager();
        model = new OWLExistentialTreeModel(mngr.getOWLOntologyManager(),
                                            mngr.getActiveOntologies(),
                                            new ExistentialNodeComparator(mngr));

        getOWLModelManager().addOntologyChangeListener(ontListener);

        getOWLWorkspace().addHierarchyListener(hListener);

        addAction(selectObjectPropertyAction, "A", "A");
        addAction(selectDataPropertyAction, "A", "B");
        addAction(clearPropertyAction, "A", "C");
        addAction(showMinZeroAction, "B", "A");

        clearPropertyAction.setEnabled(false);
    }

    protected OWLClass updateView(OWLClass selectedClass) {
        if (!ignoreUpdateView){
            refresh();
        }
        ignoreUpdateView = false;
        return selectedClass;
    }

    protected void updateHeader(OWLObject object) {
        String str = "(" + propertyLabel + ")";
        if (object != null){
            final OWLObjectRenderer owlObjectRenderer = getOWLModelManager().getOWLObjectRenderer();
            final OWLEntityRenderer owlEntityRenderer = getOWLModelManager().getOWLEntityRenderer();
            str += " " + owlObjectRenderer.render(object, owlEntityRenderer);
        }
        getView().setHeaderText(str);
    }

    public void disposeView() {
        tree.getSelectionModel().removeTreeSelectionListener(treeSelListener);
        getOWLModelManager().removeOntologyChangeListener(ontListener);
        getOWLWorkspace().removeHierarchyListener(hListener);

        model = null;
        tree = null;
    }

    private Set<OWLProperty> generateAllDescendants(OWLProperty prop) {
        Set<OWLProperty> propAndDescendants = null;
        if (prop != null){
            if (prop instanceof OWLObjectProperty){
                OWLObjectHierarchyProvider<OWLObjectProperty> ohp = getOWLModelManager().getOWLObjectPropertyHierarchyProvider();
                propAndDescendants = new HashSet<OWLProperty>(ohp.getDescendants((OWLObjectProperty)prop));
                propAndDescendants.add(prop);
            }
            else if (prop instanceof OWLDataProperty){
                OWLObjectHierarchyProvider<OWLDataProperty> dhp = getOWLModelManager().getOWLDataPropertyHierarchyProvider();
                propAndDescendants = new HashSet<OWLProperty>(dhp.getDescendants((OWLDataProperty)prop));
                propAndDescendants.add(prop);
            }
        }
        return propAndDescendants;
    }
}
