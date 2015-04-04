package org.coode.existentialtree.view;

import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
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

import org.coode.existentialtree.model.AbstractHierarchyProvider;
import org.coode.existentialtree.ui.AbstractOWLClassExpressionHierarchyViewComponent;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 29, 2007<br><br>
 */
public abstract class AbstractTreeView<O extends OWLObject> extends AbstractOWLClassExpressionHierarchyViewComponent {
    private static final long serialVersionUID = 1L;

    private static final String ALL_PROPERTIES = "all properties";

    private String propertyLabel = ALL_PROPERTIES;

    private boolean ignoreUpdateView = false;

    protected boolean requiresRefresh = false;

    private OWLOntologyChangeListener ontListener = new OWLOntologyChangeListener(){
        public void ontologiesChanged(java.util.List<? extends OWLOntologyChange> changes) {
            refresh();
        }
    };

    private HierarchyListener hListener = new HierarchyListener(){
        public void hierarchyChanged(HierarchyEvent hierarchyEvent) {
            if (requiresRefresh && isShowing()){
                refresh();
            }
        }
    };

    private DisposableAction selectPropertyAction = new DisposableAction("Select Property", OWLIcons.getIcon("property.object.png")){
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent actionEvent) {
            handleSelectProperty();
        }
        @Override
        public void dispose() {
        }
    };

    private DisposableAction clearPropertyAction = new DisposableAction("Clear Property", OWLIcons.getIcon("property.object.delete.png")){
        private static final long serialVersionUID = 1L;
        public void actionPerformed(ActionEvent actionEvent) {
            handleClearProperty();
        }
        @Override
        public void dispose() {
        }
    };

    private OWLModelManagerListener mngrListener = new OWLModelManagerListener(){

        public void handleChange(OWLModelManagerChangeEvent event) {
            if (event.getType().equals(EventType.ACTIVE_ONTOLOGY_CHANGED)){
                getHierarchyProvider().setOntologies(getOWLModelManager().getActiveOntologies());
            }
        }
    };


    protected void handleAddNode() {
    }

    @Override
    protected void performExtraInitialisation() {

        getOWLModelManager().addOntologyChangeListener(ontListener);

        getOWLModelManager().addListener(mngrListener);

        getOWLWorkspace().addHierarchyListener(hListener);

//        addAction(addNodeAction, "A", "A");
        addAction(selectPropertyAction, "B", "A");
        addAction(clearPropertyAction, "B", "B");

        clearPropertyAction.setEnabled(false);
    }

    @Override
    public void disposeView() {
        getOWLModelManager().removeOntologyChangeListener(ontListener);
        getOWLModelManager().removeListener(mngrListener);
        getOWLWorkspace().removeHierarchyListener(hListener);
        ontListener = null;
        hListener = null;
        mngrListener = null;
        super.disposeView();
    }


    // overload to prevent selection changing as we click on classes in the hierarchy
    @Override
    protected void transmitSelection() {
        ignoreUpdateView = true;
        super.transmitSelection();
    }

    @Override
    protected abstract AbstractHierarchyProvider getHierarchyProvider();

    @Override
    protected OWLClass updateView(OWLClass selectedClass) {
        if (!ignoreUpdateView){

            getHierarchyProvider().setRoot(selectedClass);

            refresh();

            super.updateView(selectedClass);
        }

        ignoreUpdateView = false;

        return selectedClass;
    }

    protected void refresh() {
        if (isShowing()){
            getTree().reload();
            getTree().expandAll();
            requiresRefresh = false;
        }
        else{
            requiresRefresh = true;
        }
    }

    @Override
    protected void updateHeader(OWLObject object) {
        String str = "(" + propertyLabel + ")";
        if (object != null){
            str += " " + getOWLModelManager().getRendering(object);
        }
        getView().setHeaderText(str);
    }

    protected void handleSelectProperty() {
        OWLObjectProperty prop = new UIHelper(getOWLEditorKit()).pickOWLObjectProperty();
        if (prop != null){
            propertyLabel = getOWLModelManager().getRendering(prop);
            clearPropertyAction.setEnabled(true);
            getHierarchyProvider().setProp(prop);
            refresh();
            updateHeader(getSelectedOWLClass());
        }
    }

    protected void handleClearProperty() {
        propertyLabel = ALL_PROPERTIES;
        getHierarchyProvider().setProp(null);
        refresh();
        updateHeader(getSelectedOWLClass());
        setEnabled(false);
    }
}
