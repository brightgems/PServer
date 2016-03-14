package de.tuberlin.pserver.radt.list;

import de.tuberlin.pserver.radt.AbstractRADT;
import de.tuberlin.pserver.radt.S4Vector;
import de.tuberlin.pserver.runtime.driver.ProgramContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractLinkedList<T> extends AbstractRADT<T> implements ILinkedList<T> {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    protected final Map<S4Vector, Node<T>> svi; // S4Vector Index

    private Node<T> head;

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public synchronized Node<T> getHead() {

        return head;

    }

    // ---------------------------------------------------
    // Protected Methods.
    // ---------------------------------------------------

    protected AbstractLinkedList(String id, int noOfReplicas, ProgramContext programContext) {

        super(id, noOfReplicas, programContext);

        this.svi = Collections.synchronizedMap(new HashMap<>());

        this.head = null;

    }



    protected synchronized Node<T> getSVIEntry(S4Vector s4) {

        return svi.get(s4);

    }

    protected synchronized void setSVIEntry(Node<T> node) {

        svi.put(node.getS4Vector(), node);

    }

    protected synchronized void setHead(Node<T> node) {

        this.head = node;

    }

}
