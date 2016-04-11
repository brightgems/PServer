package de.tuberlin.pserver.runtime.parallel.shared;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.runtime.driver.ProgramContext;

import java.util.concurrent.atomic.AtomicInteger;


public class SharedInt {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private final SharedVar<AtomicInteger> sharedInt;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    public SharedInt(final ProgramContext pc, final int value) throws Exception {
        this.sharedInt = new SharedVar<>(Preconditions.checkNotNull(pc), new AtomicInteger(value));
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public int inc() { return sharedInt.get().addAndGet(1); }

    public int dec() { return sharedInt.get().addAndGet(-1); }

    public int add(final int value) { return sharedInt.get().addAndGet(value); }

    public int sub(final int value) { return sharedInt.get().addAndGet(-value); }

    public int get() { return sharedInt.get().get(); }

    public void set(final int value) { sharedInt.get().set(value); }

    public AtomicInteger acquire() throws Exception { return sharedInt.acquire(); }

    public SharedInt done() throws Exception { sharedInt.done(); return this; }
}
