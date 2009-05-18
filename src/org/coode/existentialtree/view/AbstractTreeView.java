package org.coode.existentialtree.view;

import org.coode.existentialtree.model.AbstractHierarchyProvider;
import org.coode.existentialtree.ui.AbstractOWLDescriptionHierarchyViewComponent;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.semanticweb.owl.model.*;

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

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Oct 29, 2007<br><br>
 */
public abstract class AbstractTreeView<O extends OWLObject> extends AbstractOWLDescriptionHierarchyViewComponent {

    private static final String ALL_PROPERTIES = "all properties";

    private String propertyLabel = ALL_PROPERTIES;

    private boolean ignoreUpdateView = false;

    private boolean requiresRefresh = false;

    private OWLOntologyChangeListener ontListener = new OWLOntologyChangeListener(){
        public void ontologiesChanged(java.util.List<? extends OWLOntologyChange> changes) throws OWLException {
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
        public void actionPerformed(ActionEvent actionEvent) {
            handleSelectProperty();
        }
        public void dispose() {
        }
    };

    private DisposableAction clearPropertyAction = new DisposableAction("Clear Property", OWLIcons.getIcon("property.object.delete.png")){
        public void actionPerformed(ActionEvent actionEvent) {
            handleClearProperty();
        }
        public void dispose() {
        }
    };

    private DisposableAction addNodeAction = new DisposableAction("Add Node", OWLIcons.getIcon("class.add.sub.png")){
        public void dispose() {
            handleAddNode();
        }
        public void actionPerformed(ActionEvent actionEvent) {
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void performExtraInitialisation() throws Exception {

        getOWLModelManager().addOntologyChangeListener(ontListener);

        getOWLModelManager().addListener(mngrListener);

        getOWLWorkspace().addHierarchyListener(hListener);

//        addAction(addNodeAction, "A", "A");
        addAction(selectPropertyAction, "B", "A");
        addAction(clearPropertyAction, "B", "B");

        clearPropertyAction.setEnabled(false);
    }

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
    protected void transmitSelection() {
        ignoreUpdateView = true;
        super.transmitSelection();
    }

    protected abstract AbstractHierarchyProvider getHierarchyProvider();

    protected OWLClass updateView(OWLClass selectedClass) {
        if (!ignoreUpdateView){

            getHierarchyProvider().setRoot(selectedClass);

            refresh();

            super.updateView(selectedClass);
        }

        ignoreUpdateView = false;

        return selectedClass;
    }

    private void refresh() {
        if (isShowing()){
            getTree().reload();
            getTree().expandAll();
            requiresRefresh = false;
        }
        else{
            requiresRefresh = true;
        }
    }

    protected void updateHeader(OWLObject object) {
        String str = "(" + propertyLabel + ")";
        if (object != null){
            str += " " + getOWLModelManager().getRendering(object);
        }
        getView().setHeaderText(str);
    }

    private void handleSelectProperty() {
        OWLObjectProperty prop = new UIHelper(getOWLEditorKit()).pickOWLObjectProperty();
        if (prop != null){
            propertyLabel = getOWLModelManager().getRendering(prop);
            clearPropertyAction.setEnabled(true);
            getHierarchyProvider().setProp(prop);
            refresh();
            updateHeader(getSelectedOWLClass());
        }
    }

    private void handleClearProperty() {
        propertyLabel = ALL_PROPERTIES;
        getHierarchyProvider().setProp(null);
        refresh();
        updateHeader(getSelectedOWLClass());
        setEnabled(false);
    }
}
