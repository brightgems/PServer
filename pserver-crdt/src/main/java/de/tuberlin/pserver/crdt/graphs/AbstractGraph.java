package de.tuberlin.pserver.crdt.graphs;

import de.tuberlin.pserver.crdt.AbstractCRDT;
import de.tuberlin.pserver.crdt.sets.GSet;
import de.tuberlin.pserver.runtime.driver.ProgramContext;

import java.util.Set;

public abstract class AbstractGraph<T> extends AbstractCRDT implements Graph<T> {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    protected final GSet<Vertex<T>> vertices;

    protected final GSet<Edge> edges;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    public AbstractGraph(String id, int noOfReplicas, ProgramContext programContext) {

        super(id, noOfReplicas, programContext);

        this.vertices = new GSet<>("vertices", noOfReplicas, programContext);

        this.edges = new GSet<>("edges", noOfReplicas, programContext);

    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public Set<Vertex<T>> getVertices() {

        return vertices.getSet();

    }

    public Set<Edge> getEdges() {

        return edges.getSet();

    }
}
