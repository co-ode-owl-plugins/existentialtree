package org.coode.outlinetree.model;

import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Nick Drummond<br>
 * http://www.cs.man.ac.uk/~drummond/<br><br>
 * <p/>
 * The University Of Manchester<br>
 * Bio Health Informatics Group<br>
 * Date: Feb 28, 2008<br><br>
 */
abstract class AbstractOutlineNode<O extends OWLObject, P extends OutlineNode> implements OutlineNode<O, P>{

    private P parent;

    private OutlineTreeModel model;

    private boolean editable = true;

    private Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
    

    public AbstractOutlineNode(OutlineTreeModel model) {
        this.model = model;
    }

    public final void setParent(P parent){
        if (parent != this.parent){
            this.parent = parent;
            if (!parent.isEditable()){
                editable = false;
            }
            clear();
        }
    }

    public final void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void addAxioms(Set<OWLAxiom> axioms){
        this.axioms.addAll(axioms);
    }

    public final boolean isEditable() {
        return editable;
    }

    public final P getParent(){
        return parent;
    }

    public final Set<OWLAxiom> getAxioms(){
        return axioms;
    }

    protected final OutlineTreeModel getModel(){
        return model;
    }

    /**
     * Called after the parent has been set to clear out any cached children
     */
    protected abstract void clear();
}
