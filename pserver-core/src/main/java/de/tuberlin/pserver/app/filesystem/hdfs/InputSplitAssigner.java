package de.tuberlin.pserver.app.filesystem.hdfs;

import de.tuberlin.pserver.core.infra.MachineDescriptor;

public interface InputSplitAssigner {

    public InputSplit getNextInputSplit(final MachineDescriptor md);
}