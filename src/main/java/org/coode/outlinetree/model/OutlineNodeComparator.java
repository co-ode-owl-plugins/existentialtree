package org.coode.outlinetree.model;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLObject;

import java.util.Comparator;
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
public class OutlineNodeComparator implements Comparator<OutlineNode> {

    private Comparator<OWLObject> owlComparator;

    public OutlineNodeComparator(OWLModelManager mngr){
        owlComparator = mngr.getOWLObjectComparator();
    }

    public int compare(OutlineNode existentialNode, OutlineNode existentialNode1) {
        // first check to see if this is the same object
        final Object o = existentialNode.getUserObject();
        final Object o1 = existentialNode1.getUserObject();
        if (o instanceof OWLObject && o1 instanceof OWLObject){
        return owlComparator.compare((OWLObject)o, (OWLObject)o1);
        }
        else if (o instanceof Comparable && o1 instanceof Comparable){
            return ((Comparable)o).compareTo(((Comparable)o1));
        }
        else if (o.hashCode() != o1.hashCode()){
            return (o.hashCode() > o1.hashCode()) ? 1 : -1;
        }
        return 0;
    }
}
