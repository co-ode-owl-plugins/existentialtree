package org.coode.outlinetree;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;/*
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
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: May 7, 2008<br><br>
 */
public class OutlineTreePreferences {

    private static final String SHOW_INHERITED_CHILDREN_ALL = "show.children.inherited.all";
    private static final String SHOW_ASSERTED_CHILDREN_ALL = "show.children.asserted.all";
    private static final String SHOW_MIN_ZERO = "show.children.min0";

    private boolean showInheritedChildrenAllNodes;
    private boolean showAssertedChildrenAllNodes;
    private boolean showMinZero;

    private static OutlineTreePreferences instance;


    public static OutlineTreePreferences getInstance(){
        if (instance == null){
            instance = new OutlineTreePreferences();
        }
        return instance;
    }


    private OutlineTreePreferences() {
        load();
    }

    private Preferences getPreferences() {
        return PreferencesManager.getInstance().getApplicationPreferences(getClass());
    }

    private void load() {
        Preferences p = getPreferences();
        showInheritedChildrenAllNodes = p.getBoolean(SHOW_INHERITED_CHILDREN_ALL, false);
        showAssertedChildrenAllNodes = p.getBoolean(SHOW_ASSERTED_CHILDREN_ALL, false);
        showMinZero = p.getBoolean(SHOW_ASSERTED_CHILDREN_ALL, false);
    }

    public boolean getShowInheritedChildrenAllNodes(){
        return showInheritedChildrenAllNodes;
    }

    public void setShowInheritedChildrenAllNodes(boolean filter){
        showInheritedChildrenAllNodes = filter;
        getPreferences().putBoolean(SHOW_INHERITED_CHILDREN_ALL, filter);
    }

    public boolean getShowAssertedChildrenAllNodes() {
        return showAssertedChildrenAllNodes;
    }

    public void setShowAssertedChildrenAllNodes(boolean filter){
        showAssertedChildrenAllNodes = filter;
        getPreferences().putBoolean(SHOW_ASSERTED_CHILDREN_ALL, filter);
    }

    public boolean getShowMinZero() {
        return showMinZero;
    }

    public void setShowMinZero(boolean show){
        showMinZero = show;
        getPreferences().putBoolean(SHOW_MIN_ZERO, show);
    }
}
