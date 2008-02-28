package org.coode.existentialtree.model2;

import org.semanticweb.owl.model.OWLObject;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Feb 28, 2008<br><br>
 */
public abstract  class AbstractFillerNode<O extends OWLObject> implements OutlineNode<O, OWLPropertyNode>{

    private OWLPropertyNode parent;

    public final void setParent(OWLPropertyNode parent){
        if (parent != this.parent){
            this.parent = parent;
            clear();
        }
    }

    public final OWLPropertyNode getParent(){
        return parent;
    }

    protected abstract void clear();
}
