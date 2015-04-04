package org.coode.outlinetree.ui;

import java.util.HashSet;
import java.util.List;
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

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.coode.outlinetree.model.OutlineNode;

/**
 * Author: drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Mar 19, 2008<br><br>
 */
public class TextOutlineView extends AbstractOutlineView {
    private static final long serialVersionUID = 1L;

    private JTextArea editor;

    private Set<OutlineNode> renderedNodes = new HashSet<OutlineNode>();

    public int indent = 0;

    private TreeModelListener treeModelListener = new TreeModelListener(){

        public void treeNodesChanged(TreeModelEvent treeModelEvent) {
            refresh();
        }

        public void treeNodesInserted(TreeModelEvent treeModelEvent) {
            refresh();
        }

        public void treeNodesRemoved(TreeModelEvent treeModelEvent) {
            refresh();
        }

        public void treeStructureChanged(TreeModelEvent treeModelEvent) {
            refresh();
        }
    };

    @Override
    public void initialiseClassView() {
        super.initialiseClassView();
    }

    @Override
    protected void refreshGUI() {
        model.addTreeModelListener(treeModelListener);
        
        if (editor == null){
            editor = new JTextArea();
            editor.setTabSize(2);
            add(new JScrollPane(editor));
        }
        else{
            editor.setText("");
        }

        renderedNodes.clear();

        OutlineNode root = model.getRoot();
        renderNode(root, indent);
    }

    private void renderNode(OutlineNode node, int indent) {
        String str = "";
        for(int i=0; i<indent; i++){
            str += "\t";
        }

        str += getOWLModelManager().getRendering(node.getRenderedObject());
        str += "\n";

        editor.append(str);
        if (!renderedNodes.contains(node)){
            renderedNodes.add(node);

            final List<OutlineNode> children = node.getChildren(); //weird generics problem in intelliJ
            for (OutlineNode child : children){
                renderNode(child, indent+1);
            }
        }
    }
}
