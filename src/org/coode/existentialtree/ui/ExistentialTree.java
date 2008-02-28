package org.coode.existentialtree.ui;

import org.coode.existentialtree.model2.OutlineNode;
import org.coode.existentialtree.model2.OutlineTreeModel;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLObject;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
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
public class ExistentialTree extends JTree {

    public ExistentialTree(OutlineTreeModel model, OWLEditorKit eKit) {
        super(model);
        setShowsRootHandles(true);
        setRowHeight(-1); // forces the renderer to be asked for row height (needed for text-wrapped expressions)
        setCellRenderer(new ExistentialTreeRenderer(eKit));
    }

    public String getToolTipText(MouseEvent event) {
        OWLObject obj = getOWLObjectAtMousePosition(event);
        if (obj instanceof OWLEntity) {
            return ((OWLEntity) obj).getURI().toString();
        }
        return null;
    }

    protected OWLObject getOWLObjectAtMousePosition(MouseEvent event){
        Point pt = event.getPoint();
        TreePath path = getPathForLocation(pt.x, pt.y);
        if (path == null) {
            return null;
        }
        OutlineNode node = (OutlineNode) path.getLastPathComponent();
        return node.getUserObject();
    }
}
