package de.tuberlin.pserver.core.filesystem.hdfs;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.core.config.IConfig;
import de.tuberlin.pserver.core.filesystem.FileDataIterator;
import de.tuberlin.pserver.core.filesystem.FileSystemManager;
import de.tuberlin.pserver.core.filesystem.hdfs.in.CSVInputFormat;
import de.tuberlin.pserver.core.filesystem.hdfs.in.InputFormat;
import de.tuberlin.pserver.core.infra.InfrastructureManager;
import de.tuberlin.pserver.core.infra.MachineDescriptor;
import de.tuberlin.pserver.core.net.NetManager;
import de.tuberlin.pserver.core.net.RPCManager;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HDFSFileSystemManagerServer implements FileSystemManager, InputSplitProvider {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private static final Logger LOG = LoggerFactory.getLogger(HDFSFileSystemManagerServer.class);

    private final IConfig config;

    private final InfrastructureManager infraManager;

    private final NetManager netManager;

    private final Map<Pair<UUID,String>, InputSplitAssigner> inputSplitAssignerMap;

    private final List<Pair<String, Class<?>[]>> registeredSources;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    public HDFSFileSystemManagerServer(final IConfig config,
                                       final InfrastructureManager infraManager,
                                       final NetManager netManager,
                                       final RPCManager rpcManager) {

        Preconditions.checkNotNull(rpcManager);

        this.config         = Preconditions.checkNotNull(config);
        this.infraManager   = Preconditions.checkNotNull(infraManager);
        this.netManager     = Preconditions.checkNotNull(netManager);

        this.inputSplitAssignerMap = new ConcurrentHashMap<>();
        this.registeredSources = new ArrayList<>();

        rpcManager.registerRPCProtocol(this, InputSplitProvider.class);
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public void computeInputSplitsForRegisteredFiles() {
        try {
            for (final Pair<String, Class<?>[]> source : registeredSources) {
                final Path path = new Path(source.getLeft());
                final InputFormat inputFormat = new CSVInputFormat(path, source.getRight());
                final Configuration conf = new Configuration();
                conf.set("fs.defaultFS", config.getString("filesystem.hdfs.url"));
                inputFormat.configure(conf);
                final InputSplit[] inputSplits = inputFormat.createInputSplits(infraManager.getMachines().size());
                final InputSplitAssigner inputSplitAssigner = new LocatableInputSplitAssigner((FileInputSplit[]) inputSplits);
                for (final MachineDescriptor md : infraManager.getMachines()) {
                    inputSplitAssignerMap.put(Pair.of(md.machineID, source.getLeft()), inputSplitAssigner);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> FileDataIterator<T> createFileIterator(final String filePath, final Class<T> recordType) {
        return (FileDataIterator<T>) new HDFSFileDataIterator(config, netManager.getMachineDescriptor(), this, filePath, null);
    }

    @Override
    public InputSplit getNextInputSplit(final MachineDescriptor md) {
        Preconditions.checkNotNull(md);
        final InputSplitAssigner inputSplitAssigner = inputSplitAssignerMap.get(md.machineID);
        return inputSplitAssigner.getNextInputSplit(md);
    }
}