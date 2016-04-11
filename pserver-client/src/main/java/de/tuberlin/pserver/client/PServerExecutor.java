package de.tuberlin.pserver.client;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.compiler.Program;
import de.tuberlin.pserver.node.PServerMain;
import de.tuberlin.pserver.node.PServerNode;
import de.tuberlin.pserver.node.PServerNodeFactory;
import de.tuberlin.pserver.commons.config.Config;
import de.tuberlin.pserver.commons.config.ConfigLoader;
import de.tuberlin.pserver.runtime.core.infra.ClusterSimulator;
import org.apache.log4j.ConsoleAppender;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public enum PServerExecutor {

    LOCAL (true,     false),

    LOCAL_DEBUG (true,     true),

    DISTRIBUTED (false,    false),

    DISTRIBUTED_DEBUG (false,    true); // Not used at the moment.

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private final List<PServerNode> nodes;

    private final boolean isLocal;

    private final boolean isDebug;

    private ClusterSimulator simulator;

    private PServerClient client;

    private UUID currentJob = null;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    PServerExecutor(final boolean isLocal, final boolean isDebug) {
        org.apache.log4j.Logger.getRootLogger().addAppender(new ConsoleAppender());
        this.nodes = new ArrayList<>();
        this.isLocal = isLocal;
        this.isDebug = isDebug;
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public PServerExecutor run(final Class<? extends Program> programClass) { return run(ConfigLoader.loadResource("local.conf"), programClass); }
    public PServerExecutor run(final Config config, final Class<? extends Program> programClass) {
        if (isLocal) {
            simulator = new ClusterSimulator(
                    config,
                    isDebug,
                    PServerMain.class
            );

            if (isDebug) {
                for (int i = 0; i < config.getInt("global.simNodes"); ++i) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    new Thread(() -> nodes.add(PServerNodeFactory.createNode(config))).start();
                }
            }
        } else
            simulator = null;

        client = PServerClientFactory.createPServerClient(config);
        currentJob = client.execute(Preconditions.checkNotNull(programClass));
        return this;
    }

    public PServerExecutor results(final List<List<Serializable>> results) {
        Preconditions.checkState(currentJob != null);
        for (int i = 0; i < client.getNumberOfWorkers(); ++i)
            results.add(client.getResultsFromWorker(currentJob, i));
        return this;
    }

    public void done() {
        client.deactivate();
        if (simulator != null) {
            if (isDebug) {
                for (final PServerNode node : nodes) {
                    System.out.println("Shutdown " + node);
                    node.deactivate();
                }
            }
            simulator.deactivate();
            System.exit(0); // TODO: change this.
        }
    }
}