package de.tuberlin.pserver.runtime.memory;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.commons.compression.Compressor;
import de.tuberlin.pserver.commons.unsafe.UnsafeOp;

import java.io.Serializable;

public class ManagedBuffer implements Serializable {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    protected byte[] data;

    protected Compressor compressor;

    protected final int decompressedLength;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    public ManagedBuffer(final int length) {
        this(new byte[length], length, Compressor.CompressionType.NO_COMPRESSION);
    }

    public ManagedBuffer(final byte[] data) {
        this(data, data.length, Compressor.CompressionType.NO_COMPRESSION);
    }

    public ManagedBuffer(final byte[] data,
                         final int decompressedLength,
                         final Compressor.CompressionType type) {

        Preconditions.checkNotNull(decompressedLength > 0);
        this.data = Preconditions.checkNotNull(data);
        this.decompressedLength = decompressedLength;
        this.compressor = Compressor.Factory.create(Preconditions.checkNotNull(type));
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public int size() { return data.length; }

    public byte[] getRawData() { return data; }

    public void setRawData(final byte[] data) { this.data = data; }

    // ---------------------------------------------------

    public void compress() { compressor.compress(data); }

    public void decompress() { data = compressor.decompress(data, decompressedLength); }

    // ---------------------------------------------------

    public void putShort(final int offset, final short value) { UnsafeOp.unsafe.putShort(data, (long)(UnsafeOp.BYTE_ARRAY_OFFSET + offset), value); }

    public void putInt(final int offset, final int value) { UnsafeOp.unsafe.putInt(data, (long)(UnsafeOp.BYTE_ARRAY_OFFSET + offset), value); }

    public void putLong(final int offset, final long value) { UnsafeOp.unsafe.putLong(data, (long)(UnsafeOp.BYTE_ARRAY_OFFSET + offset), value); }

    public void putFloat(final int offset, final float value) { UnsafeOp.unsafe.putFloat(data, (long)(UnsafeOp.BYTE_ARRAY_OFFSET + offset), value); }

    public void putDouble(final int offset, final double value) { UnsafeOp.unsafe.putDouble(data, (long)(UnsafeOp.BYTE_ARRAY_OFFSET + offset), value); }

    // ---------------------------------------------------

    public short getShort(final int offset) { return UnsafeOp.unsafe.getShort(data, (long)(UnsafeOp.BYTE_ARRAY_OFFSET + offset)); }

    public int getInt(final int offset) { return UnsafeOp.unsafe.getInt(data, (long)(UnsafeOp.BYTE_ARRAY_OFFSET + offset)); }

    public long getLong(final int offset) { return UnsafeOp.unsafe.getLong(data, (long) (UnsafeOp.BYTE_ARRAY_OFFSET + offset)); }

    public float getFloat(final int offset) { return UnsafeOp.unsafe.getFloat(data, (long)(UnsafeOp.BYTE_ARRAY_OFFSET + offset)); }

    public double getDouble(final int offset) { return UnsafeOp.unsafe.getDouble(data, (long)(UnsafeOp.BYTE_ARRAY_OFFSET + offset)); }
}