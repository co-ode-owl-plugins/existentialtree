package org.coode.existentialtree;

import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.renderer.OWLEntityRenderer;
import org.protege.editor.owl.ui.renderer.OWLObjectRenderer;
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
 * Date: Sep 13, 2007<br><br>
 */
public class RelationsTreeView extends AbstractOWLIndividualHierarchyViewComponent {

    private static final String ALL_PROPERTIES = "all properties";

    private OWLRelationHierarchyProvider treeProvider;

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
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            OWLObjectProperty prop = new UIHelper(getOWLEditorKit()).pickOWLObjectProperty();
            if (prop != null){
                propertyLabel = getOWLModelManager().getOWLEntityRenderer().render(prop);
                clearPropertyAction.setEnabled(true);
                treeProvider.setProp(prop);
                refresh();
                updateHeader(getSelectedOWLIndividual());
            }
        }
    };

    private DisposableAction clearPropertyAction = new DisposableAction("Clear Property", OWLIcons.getIcon("property.object.delete.png")){
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            propertyLabel = ALL_PROPERTIES;
            treeProvider.setProp(null);
            refresh();
            updateHeader(getSelectedOWLIndividual());
            setEnabled(false);
        }
    };

    protected void performExtraInitialisation() throws Exception {

        getOWLModelManager().addOntologyChangeListener(ontListener);

        getOWLWorkspace().addHierarchyListener(hListener);

        addAction(selectPropertyAction, "A", "A");
        addAction(clearPropertyAction, "A", "B");

        clearPropertyAction.setEnabled(false);
    }

    // overload to prevent selection changing as we click on classes in the hierarchy
    protected void transmitSelection() {
        ignoreUpdateView = true;
        super.transmitSelection();
    }

    protected OWLObjectHierarchyProvider<OWLIndividual> getOWLIndividualHierarchyProvider() {
        if (treeProvider == null){
            treeProvider = new OWLRelationHierarchyProvider(getOWLModelManager());
        }
        return treeProvider;
    }

    protected OWLIndividual updateView(OWLIndividual individual) {
        if (!ignoreUpdateView){
            if (treeProvider != null){
                treeProvider.setRoot(individual);
            }

            refresh();

            super.updateView(individual);
        }

        ignoreUpdateView = false;

        return individual;
    }

    private void refresh() {
        if (isShowing()){
            getTree().reload();
            getTree().expandRow(0);
            requiresRefresh = false;
        }
        else{
            requiresRefresh = true;
        }
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
        getOWLModelManager().removeOntologyChangeListener(ontListener);
        getOWLWorkspace().removeHierarchyListener(hListener);
        ontListener = null;
        hListener = null;
        treeProvider = null;
        super.disposeView();
    }
}
