package de.tuberlin.pserver.runtime.state.matrix;

import de.tuberlin.pserver.runtime.driver.ProgramContext;
import de.tuberlin.pserver.runtime.filesystem.AbstractFileIterator;
import de.tuberlin.pserver.runtime.filesystem.FileSystemManager;
import de.tuberlin.pserver.runtime.filesystem.records.RecordEntry32F;
import de.tuberlin.pserver.runtime.filesystem.records.Record;
import de.tuberlin.pserver.types.matrix.implementation.Matrix32F;
import de.tuberlin.pserver.types.matrix.implementation.matrix32f.sparse.CSRMatrix32F;
import de.tuberlin.pserver.types.typeinfo.DistributedTypeInfo;
import gnu.trove.map.hash.TIntFloatHashMap;

import java.util.ArrayList;
import java.util.List;

public final class MatrixLoader {

    // ---------------------------------------------------
    // Inner Classes.
    // ---------------------------------------------------

    private static abstract class MatrixLoaderStrategy {

        protected final Matrix32F matrix;

        public MatrixLoaderStrategy(Matrix32F matrix)  { this.matrix = matrix; }

        public void done(Matrix32F dataMatrix) {}

        abstract public void putRecord(Record record, Matrix32F dataMatrix, Matrix32F labelMatrix);

        public static MatrixLoaderStrategy createLoader(DistributedTypeInfo state) {
            if (CSRMatrix32F.class.isAssignableFrom(state.type()))
                return new CSRMatrix32LoaderStrategy((Matrix32F) state);
            if (Matrix32F.class.isAssignableFrom(state.type()))
                return new Matrix32LoaderStrategy((Matrix32F) state);
            throw new IllegalStateException();
        }
    }

    // ---------------------------------------------------

    private final static class Matrix32LoaderStrategy extends MatrixLoaderStrategy {

        public Matrix32LoaderStrategy(Matrix32F matrix)  { super(matrix); }

        @Override
        public void putRecord(Record record, Matrix32F dataMatrix, Matrix32F labelMatrix) {
            while (record.hasNext()) {
                final RecordEntry32F entry = record.next();
                if (labelMatrix != null && entry.getCol() == 0)
                    labelMatrix.set(record.getRow(), entry.getCol(), record.getLabel());
                else
                    dataMatrix.set(entry.getRow(), entry.getCol() - ((labelMatrix != null) ? 1 : 0), entry.getValue());
            }
        }
    }

    private final static class CSRMatrix32LoaderStrategy extends MatrixLoaderStrategy {

        private final TIntFloatHashMap rowData = new TIntFloatHashMap();

        public CSRMatrix32LoaderStrategy(Matrix32F matrix)  { super(matrix); }

        @Override
        public void putRecord(Record record, Matrix32F dataMatrix, Matrix32F labelMatrix) {
            while (record.hasNext()) {
                final RecordEntry32F entry = record.next();
                if (labelMatrix != null && entry.getCol() == 0)
                    labelMatrix.set(record.getRow(), entry.getCol(), record.getLabel()); // TODO: VERIFY THAT!!!!!!!!!!!!!!!!!!!!
                else
                    rowData.put((int) entry.getCol() - ((labelMatrix != null) ? 1 : 0), entry.getValue());
            }
            ((CSRMatrix32F) dataMatrix).addRow(rowData);
            rowData.clear();
        }

        @Override
        public void done(Matrix32F dataMatrix) {
            ((CSRMatrix32F) dataMatrix).build();
        }
    }

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private final ProgramContext programContext;

    private final FileSystemManager fileManager;

    private final List<DistributedTypeInfo> loadingTasks;

    // ---------------------------------------------------
    // Constructor.
    // ---------------------------------------------------

    public MatrixLoader(ProgramContext programContext) {
        this.programContext = programContext;
        this.fileManager    = programContext.runtimeContext.fileManager;
        this.loadingTasks   = new ArrayList<>();
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public void add(DistributedTypeInfo typeInfo) {
        fileManager.registerFile(typeInfo);
        loadingTasks.add(typeInfo);
    }

    public void load() {
        programContext.runtimeContext.fileManager.buildPartitions();
        for (DistributedTypeInfo typeInfo : loadingTasks) {
            Matrix32F dataMatrix = (Matrix32F)typeInfo;
            AbstractFileIterator fileIterator = fileManager.getFileIterator(typeInfo);
            Matrix32F labelMatrix = null;
            if (!"".equals(typeInfo.input().labels())) {
                labelMatrix = programContext.runtimeContext.runtimeManager.getDHT(typeInfo.input().labels());
            }
            final MatrixLoaderStrategy loader = MatrixLoaderStrategy.createLoader(typeInfo);
            while (fileIterator.hasNext())
                loader.putRecord(fileIterator.next(), dataMatrix, labelMatrix);
            loader.done(dataMatrix);
        }
    }
}