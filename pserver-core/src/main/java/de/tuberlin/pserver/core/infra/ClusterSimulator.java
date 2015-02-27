package de.tuberlin.pserver.core.infra;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.server.NIOServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClusterSimulator {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private static final Logger LOG = LoggerFactory.getLogger(ClusterSimulator.class);

    private final List<ProcessExecutor> peList;

    private final ZooKeeperServer zookeeperServer;

    private final NIOServerCnxnFactory zookeeperCNXNFactory;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    public ClusterSimulator(final Class<?> mainClass,
                            final boolean startupZookeeper,
                            final int numNodes) {
        Preconditions.checkNotNull(mainClass);

        final int tickTime = 1;
        final int numConnections = 50;
        final String zkServer = "localhost";
        final int zkPort = 2181;

        // sanity check.
        //ZookeeperClient.checkConnectionString(zkServer);
        if (numNodes < 1)
            throw new IllegalArgumentException("numNodes < 1");

        this.peList = new ArrayList<>();

        // ------- bootstrap zookeeper server -------

        if (startupZookeeper) {
            final File dir = new File(System.getProperty("java.io.tmpdir"), "zookeeper").getAbsoluteFile();
            if (dir.exists()) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            try {
                this.zookeeperServer = new ZooKeeperServer(dir, dir, tickTime);
                this.zookeeperServer.setMaxSessionTimeout(10000000);
                this.zookeeperServer.setMinSessionTimeout(10000000);
                this.zookeeperCNXNFactory = new NIOServerCnxnFactory();
                this.zookeeperCNXNFactory.configure(new InetSocketAddress(zkPort), numConnections);
                this.zookeeperCNXNFactory.startup(zookeeperServer);
            } catch (IOException | InterruptedException e) {
                throw new IllegalStateException(e);
            }
        } else {
            zookeeperServer = null;
            zookeeperCNXNFactory = null;
        }

        // ------- bootstrap cluster -------

        //try {
            String[] jvmOpts = new String[] { };
            for (int i = 0; i < numNodes; ++i) {
                peList.add(new ProcessExecutor(mainClass).execute(jvmOpts));
                //Thread.sleep(100);
            }
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public void shutdown() {
        for (final ProcessExecutor pe : peList) {
            pe.destroy();
        }
        this.zookeeperCNXNFactory.closeAll();
        this.zookeeperServer.shutdown();
    }

    // ---------------------------------------------------
    // Inner Classes.
    // ---------------------------------------------------

    public static final class ProcessExecutor {

        // ---------------------------------------------------
        // Fields.
        // ---------------------------------------------------

        private final Class<?> executableClazz;

        private Process process;

        // ---------------------------------------------------
        // Constructors.
        // ---------------------------------------------------

        public ProcessExecutor(final Class<?> executableClazz) {
            Preconditions.checkNotNull(executableClazz);
            try {
                executableClazz.getMethod("main", String[].class);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new IllegalArgumentException(e);
            }
            this.executableClazz = executableClazz;
            this.process = null;
        }

        // ---------------------------------------------------
        // Public.
        // ---------------------------------------------------

        public ProcessExecutor execute(String[] jvmOpts, String... params) {
            final String javaRuntime = System.getProperty("java.home") + "/bin/java";
            final String classpath =
                    System.getProperty("java.class.path") + ":"
                            + executableClazz.getProtectionDomain().getCodeSource().getLocation().getPath();
            final String canonicalName = executableClazz.getCanonicalName();
            try {
                final List<String> commandList = new ArrayList<String>();
                commandList.add(javaRuntime);
                commandList.add("-cp");
                commandList.add(classpath);
                commandList.addAll(Arrays.asList(jvmOpts));
                commandList.add(canonicalName);
                commandList.addAll(Arrays.asList(params));
                final ProcessBuilder builder = new ProcessBuilder(commandList);
                builder.redirectErrorStream(true);
                builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                process = builder.start();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return this;
        }

        public void destroy() {
            Preconditions.checkNotNull(process);
            process.destroy();
        }
    }
}
