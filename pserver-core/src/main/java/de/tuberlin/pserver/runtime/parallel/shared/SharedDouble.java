package de.tuberlin.pserver.runtime.parallel.shared;


import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;
import de.tuberlin.pserver.runtime.driver.ProgramContext;

public class SharedDouble {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private final SharedVar<AtomicDouble> sharedDouble;

    // ---------------------------------------------------
    // Constructors.
    // ---------------------------------------------------

    public SharedDouble(final ProgramContext pc, final double value) throws Exception {
        this.sharedDouble = new SharedVar<>(Preconditions.checkNotNull(pc), new AtomicDouble(value));
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public double inc() { return sharedDouble.get().addAndGet(1.0); }

    public double dec() { return sharedDouble.get().addAndGet(-1.0); }

    public double add(final double value) { return sharedDouble.get().addAndGet(value); }

    public double sub(final double value) { return sharedDouble.get().addAndGet(-value); }

    public double get() { return sharedDouble.get().get(); }

    public void set(final double value) { sharedDouble.get().set(value); }

    public AtomicDouble acquire() throws Exception { return sharedDouble.acquire(); }

    public SharedDouble done() throws Exception { sharedDouble.done(); return this; }
}
