package de.tuberlin.pserver.runtime.filesystem.hdfs;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.runtime.core.config.IConfig;
import de.tuberlin.pserver.runtime.core.infra.InfrastructureManager;
import de.tuberlin.pserver.runtime.core.network.MachineDescriptor;
import de.tuberlin.pserver.runtime.core.network.NetEvent;
import de.tuberlin.pserver.runtime.core.network.NetManager;
import de.tuberlin.pserver.runtime.core.network.RPCManager;
import de.tuberlin.pserver.runtime.filesystem.FileDataIterator;
import de.tuberlin.pserver.runtime.filesystem.FileFormat;
import de.tuberlin.pserver.runtime.filesystem.FileSystemManager;
import de.tuberlin.pserver.runtime.filesystem.records.Record;
import de.tuberlin.pserver.runtime.state.partitioner.MatrixPartitioner;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class HDFSFileSystemManagerServer implements FileSystemManager, InputSplitProvider {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    //private static final Logger LOG = LoggerFactory.getLogger(HDFSFileSystemManagerServer.class);

    private final IConfig config;

    private final InfrastructureManager infraManager;

    private final NetManager netManager;

    private final Map<UUID, InputSplitAssigner> inputSplitAssignerMap;

    private final Map<String,List<FileDataIterator<? extends Record>>> registeredIteratorMap;

    private final Map<String,HDFSInputFile> inputFileMap;

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
        this.registeredIteratorMap = new HashMap<>();
        this.inputFileMap = new HashMap<>();

        rpcManager.registerRPCProtocol(this, InputSplitProvider.class);
    }

    public void clearContext() {

        inputSplitAssignerMap.clear();

        registeredIteratorMap.clear();

        inputFileMap.clear();
    }

    @Override
    public void deactivate() {
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    @SuppressWarnings("unchecked")
    @Override
    public void computeInputSplitsForRegisteredFiles() {
        inputFileMap.forEach( (k,v) -> {
            try {
                final InputSplit[] inputSplits = v.createInputSplits(infraManager.getMachines().size());
                final InputSplitAssigner inputSplitAssigner = new LocatableInputSplitAssigner((FileInputSplit[]) inputSplits);
                for (final MachineDescriptor md : infraManager.getMachines()) {
                    inputSplitAssignerMap.put(md.machineID, inputSplitAssigner);
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });

        netManager.broadcastEvent(new NetEvent(PSERVER_LFSM_COMPUTED_FILE_SPLITS, true));

        registeredIteratorMap.forEach(
                (k, v) -> v.forEach(FileDataIterator::initialize)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Record> FileDataIterator<T> createFileIterator(final String filePath,
                                                                     final FileFormat fileFormat,
                                                                     final MatrixPartitioner partitioner) {
        HDFSInputFile inputFile = inputFileMap.get(Preconditions.checkNotNull(filePath));
        if (inputFile == null) {
            inputFile = new HDFSInputFile(config, netManager, filePath, fileFormat);
            final Configuration conf = new Configuration();
            conf.set("fs.defaultFS", config.getString("filesystem.hdfs.url"));
            inputFile.configure(conf);
            inputFileMap.put(filePath, inputFile);
            registeredIteratorMap.put(filePath, new ArrayList<>());
        }
        final FileDataIterator<T> fileIterator = (FileDataIterator<T>)inputFile.iterator(this);
        registeredIteratorMap.get(filePath).add(fileIterator);
        return fileIterator;
    }

    @Override
    public InputSplit getNextInputSplit(final MachineDescriptor md) {
        Preconditions.checkNotNull(md);
        final InputSplitAssigner inputSplitAssigner = inputSplitAssignerMap.get(md.machineID);
        return inputSplitAssigner.getNextInputSplit(md);
    }
}
