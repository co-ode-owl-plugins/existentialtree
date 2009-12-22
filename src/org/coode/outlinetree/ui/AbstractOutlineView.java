package org.coode.outlinetree.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.coode.outlinetree.OutlineTreePreferences;
import org.coode.outlinetree.model.OutlineNode;
import org.coode.outlinetree.model.OutlineNodeComparator;
import org.coode.outlinetree.model.OutlineTreeModel;
import org.protege.editor.core.ui.view.DisposableAction;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.hierarchy.OWLObjectHierarchyProvider;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.selector.OWLDataPropertySelectorPanel;
import org.protege.editor.owl.ui.selector.OWLObjectPropertySelectorPanel;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Mar 19, 2008<br><br>
 */
public abstract class AbstractOutlineView extends AbstractOWLClassViewComponent {

    protected OutlineTreeModel model;

    protected OutlineNode currentSelection; // @@TODO or shall we just ask the tree for this when needed?

    private static final String ALL_PROPERTIES = "all properties";

    protected String propertyLabel = ALL_PROPERTIES;

    protected boolean ignoreUpdateView = false;

    private boolean requiresRefresh = false;

    private Set<OWLPropertyExpression> propertyFilter = new HashSet<OWLPropertyExpression>();


    private OWLOntologyChangeListener ontListener = new OWLOntologyChangeListener(){
        public void ontologiesChanged(java.util.List<? extends OWLOntologyChange> changes) throws OWLException {
            refresh();
        }
    };

    private OWLModelManagerListener mngrListener = new OWLModelManagerListener(){
        public void handleChange(OWLModelManagerChangeEvent event) {
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

    protected DisposableAction addNodeAction = new DisposableAction("Add Some Restriction", OWLIcons.getIcon("class.add.sub.png")){
        public void actionPerformed(ActionEvent actionEvent) {
            handleAddNode();
        }
        public void dispose() {
        }
    };

    private DisposableAction filterPropertiesAction = new DisposableAction("Filter properties", OWLIcons.getIcon("property.object.png")){
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            handleFilterProperties();
        }
    };

    private DisposableAction clearFiltersAction = new DisposableAction("Clear filters (Follow all properties)", OWLIcons.getIcon("property.object.delete.png")){
        public void dispose() {
        }

        public void actionPerformed(ActionEvent actionEvent) {
            handleClearProperty();
        }
    };

    private OWLObjectPropertySelectorPanel objPropSel;
    private OWLDataPropertySelectorPanel dataPropSel;

    private JCheckBox selectAllObjectPropsCheckbox;

    private JCheckBox selectAllDataPropsCheckbox;

    private ActionListener propCheckboxListener = new ActionListener(){
        public void actionPerformed(ActionEvent actionEvent) {
            objPropSel.setEnabled(selectAllObjectPropsCheckbox.isSelected());
            dataPropSel.setEnabled(selectAllDataPropsCheckbox.isSelected());
        }
    };

    private JCheckBox minZeroCB;
    private JCheckBox showInheritedCB;
    private JCheckBox showAssertedCB;

    private ActionListener minZeroChangeListener = new ActionListener(){
        public void actionPerformed(ActionEvent event) {
            OutlineTreePreferences.getInstance().setShowMinZero(minZeroCB.isSelected());
            refresh();
        }
    };

    private ActionListener showInheritedChangeLister = new ActionListener(){
        public void actionPerformed(ActionEvent event) {
            OutlineTreePreferences.getInstance().setShowInheritedChildrenAllNodes(showInheritedCB.isSelected());
            refresh();
        }
    };

        private ActionListener showAssertedChangeLister = new ActionListener(){
        public void actionPerformed(ActionEvent event) {
            OutlineTreePreferences.getInstance().setShowAssertedChildrenAllNodes(showAssertedCB.isSelected());
            refresh();
        }
    };

    public void initialiseClassView() throws Exception {
        setLayout(new BorderLayout());

        getOWLModelManager().addOntologyChangeListener(ontListener);
        getOWLModelManager().addListener(mngrListener);

        getOWLWorkspace().addHierarchyListener(hListener);

        addAction(addNodeAction, "A", "A");
        addAction(filterPropertiesAction, "B", "A");
        addAction(clearFiltersAction, "B", "B");

        minZeroCB = new JCheckBox("Show min 0");
        minZeroCB.addActionListener(minZeroChangeListener);

        showInheritedCB = new JCheckBox("Show inherited");
        showInheritedCB.addActionListener(showInheritedChangeLister);

        showAssertedCB = new JCheckBox("Show asserted (for all nodes)");
        showAssertedCB.addActionListener(showAssertedChangeLister);

        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
        tools.add(minZeroCB);
        tools.add(showInheritedCB);
        tools.add(showAssertedCB);
        add(tools, BorderLayout.NORTH);

        clearFiltersAction.setEnabled(false);
    }

    public void disposeView() {
        getOWLModelManager().removeOntologyChangeListener(ontListener);
        getOWLModelManager().removeListener(mngrListener);
        getOWLWorkspace().removeHierarchyListener(hListener);

        model = null;
    }

    protected void refresh() {
        if (isShowing()){
            OWLClass selectedOWLClass = getSelectedOWLClass();
            if (selectedOWLClass != null){
                createModel(selectedOWLClass);

                refreshGUI();

                refreshPreferences();
            }
            requiresRefresh = false;
        }
        else{
            requiresRefresh = true;
        }
    }


    protected abstract void refreshGUI();


    private void refreshPreferences() {
        minZeroCB.setSelected(OutlineTreePreferences.getInstance().getShowMinZero());
        showAssertedCB.setSelected(OutlineTreePreferences.getInstance().getShowAssertedChildrenAllNodes());
        showInheritedCB.setSelected(OutlineTreePreferences.getInstance().getShowInheritedChildrenAllNodes());
    }

    private void createModel(OWLClass owlClass) {
        OWLModelManager mngr = getOWLModelManager();
        final OWLObjectHierarchyProvider<OWLClass> hp = getHierarchyProvider();

        model = new OutlineTreeModel(mngr.getOWLOntologyManager(),
                mngr.getActiveOntologies(), hp,
                new OutlineNodeComparator(mngr));
        model.setShowInheritedChildrenAllNodes(OutlineTreePreferences.getInstance().getShowInheritedChildrenAllNodes());
        model.setShowAssertedChildrenAllNodes(OutlineTreePreferences.getInstance().getShowAssertedChildrenAllNodes());
        model.setMin(OutlineTreePreferences.getInstance().getShowMinZero() ? 0 : 1);
        model.setRoot(owlClass);

        if (!propertyFilter.isEmpty()){
            model.setProperties(propertyFilter);
        }
    }

    private OWLObjectHierarchyProvider<OWLClass> getHierarchyProvider() {
        OWLModelManager mngr = getOWLModelManager();
        if (mngr.getOWLReasonerManager().isClassified()){
            return mngr.getOWLHierarchyManager().getInferredOWLClassHierarchyProvider();
        }
        return mngr.getOWLHierarchyManager().getOWLClassHierarchyProvider();
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

    private Set<OWLProperty> generateAllDescendants(OWLProperty prop) {
        Set<OWLProperty> propAndDescendants = null;
        if (prop != null){
            if (prop instanceof OWLObjectProperty){
                OWLObjectHierarchyProvider<OWLObjectProperty> ohp =
                        getOWLModelManager().getOWLHierarchyManager().getOWLObjectPropertyHierarchyProvider();
                propAndDescendants = new HashSet<OWLProperty>(ohp.getDescendants((OWLObjectProperty)prop));
                propAndDescendants.add(prop);
            }
            else if (prop instanceof OWLDataProperty){
                OWLObjectHierarchyProvider<OWLDataProperty> dhp =
                        getOWLModelManager().getOWLHierarchyManager().getOWLDataPropertyHierarchyProvider();
                propAndDescendants = new HashSet<OWLProperty>(dhp.getDescendants((OWLDataProperty)prop));
                propAndDescendants.add(prop);
            }
        }
        return propAndDescendants;
    }


    private void handleAddNode() {
        String errMessage = null;
        if (currentSelection != null && currentSelection.isEditable()){
            OWLObject value = getValue(currentSelection.getTypeOfChild());

            if (value != null){
                OutlineNode newNode = model.createNode(value, currentSelection);
                java.util.List<OWLOntologyChange> changes = model.add(newNode, currentSelection, getOWLModelManager().getActiveOntology());
                if (!changes.isEmpty()){
                    getOWLModelManager().applyChanges(changes);
                }
            }
        }
        else{
            errMessage = "Please select a property or class in the tree first";
        }

        if (errMessage != null){
            new UIHelper(getOWLEditorKit()).showDialog(errMessage, null);
        }
    }

    private <T extends OWLObject> T getValue(Class<T> typeOfChild) {
        if (typeOfChild.equals(OWLClass.class)){
            return (T)new UIHelper(getOWLEditorKit()).pickOWLClass();
        }
        else if (typeOfChild.equals(OWLProperty.class)){
            return (T)new UIHelper(getOWLEditorKit()).pickOWLObjectProperty(); //@@TODO also data properties
        }
        return null;
    }


    private void handleFilterProperties() {
        Set<OWLProperty> props = getFilterProperties();
        if (!props.isEmpty()){
            propertyLabel = "";
            propertyFilter.clear();
            for (OWLProperty p : props){
                propertyLabel += getOWLModelManager().getRendering(p) + ", ";
                propertyFilter.addAll(generateAllDescendants(p));
            }
            clearFiltersAction.setEnabled(true);
            refresh();
            updateHeader(getSelectedOWLClass());
            // change clearProp
        }
    }

    private Set<OWLProperty> getFilterProperties() {
        Set<OWLProperty> props = new HashSet<OWLProperty>();
        if (objPropSel == null){
            objPropSel = new OWLObjectPropertySelectorPanel(getOWLEditorKit());
            dataPropSel = new OWLDataPropertySelectorPanel(getOWLEditorKit());
            selectAllObjectPropsCheckbox = new JCheckBox("Select all object properties");
            selectAllDataPropsCheckbox = new JCheckBox("Select all data properties");
        }

        JPanel objPanel = new JPanel(new BorderLayout());
        objPanel.add(selectAllObjectPropsCheckbox, BorderLayout.NORTH);
        objPanel.add(objPropSel, BorderLayout.CENTER);

        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(selectAllDataPropsCheckbox, BorderLayout.NORTH);
        dataPanel.add(dataPropSel, BorderLayout.CENTER);

        JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, objPanel, dataPanel);

        selectAllObjectPropsCheckbox.addActionListener(propCheckboxListener);
        selectAllDataPropsCheckbox.addActionListener(propCheckboxListener);

        if (new UIHelper(getOWLEditorKit()).showDialog("Select filter properties", splitter) == JOptionPane.OK_OPTION){
            if (selectAllObjectPropsCheckbox.isSelected()){
                props.addAll(getAllObjectProperties());
            }
            else{
                props.addAll(objPropSel.getSelectedObjects());
            }
            if (selectAllDataPropsCheckbox.isSelected()){
                props.addAll(getAllDataProperties());
            }
            else{
                props.addAll(dataPropSel.getSelectedObjects());
            }
        }
        return props;
    }


    private Set<OWLObjectProperty> getAllObjectProperties() {
        Set<OWLObjectProperty> props = new HashSet<OWLObjectProperty>();
        for (OWLOntology ont : model.getOntologies()){
            props.addAll(ont.getReferencedObjectProperties());
        }
        return props;
    }


        private Set<OWLDataProperty> getAllDataProperties() {
        Set<OWLDataProperty> props = new HashSet<OWLDataProperty>();
        for (OWLOntology ont : model.getOntologies()){
            props.addAll(ont.getReferencedDataProperties());
        }
        return props;
    }


    private void handleClearProperty() {
        propertyLabel = ALL_PROPERTIES;
        propertyFilter.clear();
        refresh();
        updateHeader(getSelectedOWLClass());
        setEnabled(false);
    }
}
