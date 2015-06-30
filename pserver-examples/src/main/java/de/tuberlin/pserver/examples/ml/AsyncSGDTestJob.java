package de.tuberlin.pserver.examples.ml;

import com.google.common.collect.Lists;
import de.tuberlin.pserver.app.PServerJob;
import de.tuberlin.pserver.client.PServerExecutor;
import de.tuberlin.pserver.math.*;
import de.tuberlin.pserver.ml.models.GeneralLinearModel;
import de.tuberlin.pserver.ml.optimization.*;
import de.tuberlin.pserver.ml.optimization.SGD.SGDOptimizer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public final class AsyncSGDTestJob extends PServerJob {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private final Observer observer = (epoch, model, gradientSum) -> {

    };

    private GeneralLinearModel model = new GeneralLinearModel("model1", 15);

    private Matrix gradientMtx;

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    @Override
    public void prologue() {

        model.createModel(ctx);

        dataManager.loadAsMatrix("datasets/sparse_dataset.csv");

        gradientMtx = new MatrixBuilder()
                .dimension(5, 1000000)
                .format(Matrix.Format.SPARSE_MATRIX)
                .layout(Matrix.Layout.ROW_LAYOUT)
                .build();

                //new SMatrix(5, 1000000, Matrix.MemoryLayout.ROW_LAYOUT);
    }

    @Override
    public void compute() {

        final Matrix trainingData = dataManager.getObject("sparse_dataset.csv");

        final PredictionFunction predictionFunction = new PredictionFunction.LinearPredictionFunction();

        final PartialLossFunction partialLossFunction = new PartialLossFunction.SquareLoss();

        final Optimizer optimizer = new SGDOptimizer(ctx, SGDOptimizer.TYPE.SGD_SIMPLE, false)
                .setNumberOfIterations(150000)
                .setLearningRate(0.0005)
                .setLossFunction(new LossFunction.GenericLossFunction(predictionFunction, partialLossFunction))
                .setGradientStepFunction(new GradientStepFunction.SimpleGradientStep())
                .setLearningRateDecayFunction(null)
                .setWeightsObserver(observer, 100, false);

        optimizer.register();
        optimizer.optimize(model, trainingData.rowIterator());
        optimizer.unregister();

        result(model.getWeights());
    }

    // ---------------------------------------------------
    // Private Methods.
    // ---------------------------------------------------

    private List<Pair<Integer,Double>> gradientUpdate(final Vector gradient, final Vector gradientSums, final double updateThreshold) {
        final List<Pair<Integer,Double>> gradientUpdates = new ArrayList<>();
        for (int i = 0; i < gradient.length(); ++i) {
            boolean updatedGradient = Math.abs((gradientSums.get(i) - gradient.get(i)) / gradient.get(i)) > updateThreshold;
            if (updatedGradient) {
                gradientUpdates.add(Pair.of(i, gradientSums.get(i) + gradient.get(i)));
                gradientSums.set(i, gradient.get(i));
            } else
                gradientSums.set(i, gradientSums.get(i) + gradient.get(i));
        }
        return gradientUpdates;
    }


    // ---------------------------------------------------
    // Entry Point.
    // ---------------------------------------------------

    public static void main(final String[] args) {

        final List<List<Serializable>> res = Lists.newArrayList();

        PServerExecutor.LOCAL
                .run(AsyncSGDTestJob.class)
                .results(res)
                .done();

        final DecimalFormat numberFormat = new DecimalFormat("0.000");
        res.forEach(
                r -> r.forEach(
                        w -> {
                            for (double weight : ((Vector)w).toArray())
                                System.out.print(numberFormat.format(weight) + "\t | ");
                            System.out.println();
                        }
                )
        );
    }
}