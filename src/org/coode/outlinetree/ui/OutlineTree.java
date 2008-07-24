package org.coode.outlinetree.ui;

import org.coode.outlinetree.model.OutlineNode;
import org.coode.outlinetree.model.OutlineTreeModel;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLObject;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
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
public class OutlineTree extends JTree {
    
    private OWLEditorKit eKit;

    public OutlineTree(OutlineTreeModel model, OWLEditorKit eKit) {
        super(model);
        this.eKit = eKit;
        setShowsRootHandles(true);
        setRowHeight(-1); // forces the renderer to be asked for row height (needed for text-wrapped expressions)
        setCellRenderer(new OutlineTreeRenderer(eKit));
    }

    public String getToolTipText(MouseEvent event) {
        OutlineNode node = getNodeAtMousePosition(event);
        String text = "";
        if (node != null){

            text += "<html><body>";
            text += "<b><font size='12pt'>" + eKit.getModelManager().getRendering(node.getUserObject()) + "</font></b><br>";

//            text += "<b>" + node.getClass().getSimpleName() + "</b><br>";
//            text += "<b>" + objRen.render(node.getUserObject(), entRen) + "</b><br>";

            Set<OWLAxiom> axioms = node.getAxioms();
            for (OWLAxiom ax : axioms){
                text += eKit.getModelManager().getRendering(ax) + "<br>";
            }
            text += "</body></html>";
//        OWLObject obj = getOWLObjectAtMousePosition(event);

//        if (obj instanceof OWLEntity) {
//            return ((OWLEntity) obj).getURI().toString();
//        }
        }
        return text;
    }

    protected OWLObject getOWLObjectAtMousePosition(MouseEvent event){
        return getNodeAtMousePosition(event).getUserObject();
    }

    protected OutlineNode getNodeAtMousePosition(MouseEvent event){
        Point pt = event.getPoint();
        TreePath path = getPathForLocation(pt.x, pt.y);
        if (path == null) {
            return null;
        }
        return (OutlineNode) path.getLastPathComponent();
    }
}
