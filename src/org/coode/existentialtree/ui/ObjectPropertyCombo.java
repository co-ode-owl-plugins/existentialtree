package org.coode.existentialtree.ui;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.util.*;

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
 * http://www.cs.man.ac.uk/~drummond<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Apr 26, 2007<br><br>
 * <p/>
 */
public class ObjectPropertyCombo extends JComboBox {

    private OWLModelManager mngr;

    private boolean changesMade = false;


    public static final String ALL_PROPERTIES = "All Object Properties";

    private OWLOntologyChangeListener ontologyChangeListener = new OWLOntologyChangeListener() {
        public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
            changesMade = true; // @@TODO should only respond to changes that add or remove properties
        }
    };

    // listen to whether the currently active ontology has changed
    private OWLModelManagerListener activeOntologyListener = new OWLModelManagerListener() {
        public void handleChange(OWLModelManagerChangeEvent event) {
            if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED ||
                event.getType() == EventType.ONTOLOGY_VISIBILITY_CHANGED) {
                reload();
            }
        }
    };

    public ObjectPropertyCombo (OWLEditorKit eKit) {
        super();

        mngr = eKit.getModelManager();

        setRenderer(new OWLCellRenderer(eKit));

        load();

        setSelectedItem(ALL_PROPERTIES);
        repaint();

        // deal with adding and removing object properties
        mngr.addOntologyChangeListener(ontologyChangeListener);

        mngr.addListener(activeOntologyListener);
    }

    public void setPopupVisible(boolean v) {
        if (changesMade) {
            reload();
            changesMade = false;
        }
        super.setPopupVisible(v);
    }

    private void reload() {
        removeAllItems();
        load();
    }

    private void load() {
        Set<OWLProperty> props = new HashSet<OWLProperty>();
        for (OWLOntology ont: mngr.getOWLOntologyManager().getImportsClosure(mngr.getActiveOntology())){
            props.addAll(ont.getReferencedObjectProperties());
        }
        ArrayList<OWLProperty> orderedProps = new ArrayList<OWLProperty>(props);
        Collections.sort(orderedProps, mngr.getOWLObjectComparator());
        addItem(ALL_PROPERTIES);
        addItems(orderedProps);
    }

    private void addItems(List<? extends OWLProperty> properties) {
        for (OWLProperty prop : properties) {
            addItem(prop);
        }
    }

    protected void finalize() throws Throwable {
        mngr.removeOntologyChangeListener(ontologyChangeListener);
        mngr.removeListener(activeOntologyListener);
        mngr = null;
        super.finalize();
    }
}
