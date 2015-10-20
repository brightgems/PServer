package de.tuberlin.pserver.dsl.state.annotations;

import de.tuberlin.pserver.dsl.state.properties.Scope;
import de.tuberlin.pserver.math.matrix.Format;
import de.tuberlin.pserver.runtime.filesystem.record.IRecordIteratorProducer;
import de.tuberlin.pserver.runtime.filesystem.record.RowColValRecordIteratorProducer;
import de.tuberlin.pserver.runtime.state.partitioner.IMatrixPartitioner;
import de.tuberlin.pserver.runtime.state.partitioner.RowPartitioner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface State {;

    Scope scope() default Scope.REPLICATED;

    String at() default "";

    Class<? extends IMatrixPartitioner> partitioner() default RowPartitioner.class;

    long rows() default 0;

    long cols() default 0;

    Format format() default Format.DENSE_FORMAT;

    Class<? extends IRecordIteratorProducer> recordFormat() default RowColValRecordIteratorProducer.class;

    String path() default "";
}