package de.tuberlin.pserver.dsl.transaction;


import com.google.common.base.Preconditions;
import de.tuberlin.pserver.commons.utils.ParseUtils;
import de.tuberlin.pserver.compiler.TransactionDescriptor;
import de.tuberlin.pserver.dsl.transaction.annotations.TransactionType;
import de.tuberlin.pserver.runtime.driver.ProgramContext;

public final class TransactionBuilder {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private final ProgramContext programContext;

    // ---------------------------------------------------

    public String srcStateObjectNames;

    public String dstStateObjectNames;

    public TransactionType type;

    public String at;

    public boolean cache;

    public long observerPeriod;

    // ---------------------------------------------------
    // Constructor.
    // ---------------------------------------------------

    public TransactionBuilder(final ProgramContext programContext) {
        this.programContext = Preconditions.checkNotNull(programContext);
        clear();
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    public TransactionBuilder state(final String stateObjectNames) { this.srcStateObjectNames = stateObjectNames; return this; }

    public TransactionBuilder type(final TransactionType type) { this.type = type; return this; }

    public TransactionBuilder at(final String at) { this.at = at; return this; }

    public TransactionBuilder cache(final boolean cache) { this.cache = cache; return this; }

    public TransactionBuilder observerPeriod(final long observerPeriod) { this.observerPeriod = observerPeriod; return this; }

    // ---------------------------------------------------

    public TransactionDefinition build(final String transactionName, final TransactionDefinition definition) {
        final TransactionDescriptor descriptor = new TransactionDescriptor(
                transactionName,
                ParseUtils.parseStateList(srcStateObjectNames),
                ParseUtils.parseStateList(dstStateObjectNames),
                definition,
                type,
                cache,
                observerPeriod,
                programContext.nodeID,
                programContext.programTable
        );
        return programContext.runtimeContext.runtimeManager.createTransaction(programContext, descriptor);
    }

    // ---------------------------------------------------

    public void clear() {
        this.srcStateObjectNames = "";
        this.dstStateObjectNames = "";
        this.type = TransactionType.PUSH;
        this.at = "";
        this.cache = false;
    }
}
