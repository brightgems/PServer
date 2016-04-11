package de.tuberlin.pserver.test.core.programs;

import com.google.common.base.Preconditions;
import de.tuberlin.pserver.compiler.Program;
import de.tuberlin.pserver.dsl.transaction.aggregators.Aggregator;
import de.tuberlin.pserver.dsl.unit.annotations.Unit;
import de.tuberlin.pserver.dsl.unit.controlflow.lifecycle.Lifecycle;


public class ASymAggregatorTestJob extends Program {

    @Unit
    public void main(final Lifecycle lifecycle) {

        lifecycle.process(() -> {

            final int partialAgg = programContext.nodeID * 1000;

            final int globalAgg = new Aggregator<>(programContext, partialAgg, false)
                    .apply(pa -> pa.stream().mapToInt(Integer::intValue).sum());

            Preconditions.checkState(globalAgg == 6000);
        });
    }
}
