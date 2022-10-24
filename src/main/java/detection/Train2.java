package detection;

import java.io.File;

import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.Nadam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Train2 {

	private static Logger log = LoggerFactory.getLogger(Train2.class);

	public static void main(String[] args) throws Exception {

		// First: get the dataset using the record reader. CSVRecordReader handles
		// loading/parsing
		int numLinesToSkip = 0;
		char delimiter = ',';
		RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
//        recordReader.initialize(new FileSplit(new File(DownloaderUtility.IRISDATA.Download(),"iris.txt")));
		recordReader.initialize(new FileSplit(
				new File("C:\\Usr\\dev\\eclipse-ws\\Simulation\\work\\cache\\alert11-2@2020-06-21_14-53-14", "A copy.csv")));

		// Second: the RecordReaderDataSetIterator handles conversion to DataSet
		// objects, ready for use in neural network
		int labelIndex = 52; // 53 values in each row of the iris.txt CSV: 52 input features followed by an
							// integer label (class) index. Labels are the 5th value (index 4) in each row
		int numClasses = 2; // 3 classes (types of iris flowers) in the iris data set. Classes have integer
							// values 0, 1 or 2
		int batchSize = 280; // Iris data set: 150 examples total. We are loading all of them into one
								// DataSet (not recommended for large data sets)

		DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numClasses);
		DataSet allData = iterator.next();
		allData.shuffle();
		SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65); // Use 65% of data for training

		DataSet trainingData = testAndTrain.getTrain();
		DataSet testData = testAndTrain.getTest();

		// We need to normalize our data. We'll use NormalizeStandardize (which gives us
		// mean 0, unit variance):
//		DataNormalization normalizer = new NormalizerStandardize();
//		normalizer.fit(trainingData); // Collect the statistics (mean/stdev) from the training data. This does not
//										// modify the input data
//		normalizer.transform(trainingData); // Apply normalization to the training data
//		normalizer.transform(testData); // Apply normalization to the test data. This is using statistics calculated
										// from the *training* set

		final int numInputs = 52;
		int outputNum = 2;
		long seed = 6;
		int numHiddenNodes = 52;
		double learningRate = 0.01;

		log.info("Build model....");
		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				.seed(seed)
				.activation(Activation.RELU)
				.weightInit(WeightInit.XAVIER)
//				.updater(new Sgd(0.1))
				.updater(new Nadam())
//				.l2(1e-4)
				.l2(learningRate * 0.005) 
				.list()
				.layer(new DenseLayer.Builder().nIn(numInputs).nOut(500).build())
				.layer(new DenseLayer.Builder().nIn(500).nOut(100).build())
				.layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
						.activation(Activation.SOFTMAX) // Override the global TANH activation with softmax for this
						.nIn(100).nOut(outputNum).build())
				.build();
		
		
//	    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
//                .seed(seed)
//                .weightInit(WeightInit.XAVIER)
////                .updater(new Nesterovs(learningRate, 0.9))
//                .updater(new Sgd(0.1))
//                .list()
//                .layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
//                        .activation(Activation.RELU)
//                        .build())
//                .layer(new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
//                        .activation(Activation.SOFTMAX)
//                        .nIn(numHiddenNodes).nOut(outputNum).build())
//                .build();


		// run the model
		MultiLayerNetwork model = new MultiLayerNetwork(conf);
		model.init();
		// record score once every 100 iterations
		model.setListeners(new ScoreIterationListener(100));

		for (int i = 0; i < 1000; i++) {
			model.fit(trainingData);
		}

		// evaluate the model on the test set
//		Evaluation eval = new Evaluation(2);
//		INDArray output = model.output(trainingData.getFeatures());
//		eval.eval(trainingData.getLabels(), output);
//		log.info(eval.stats());
		
		Evaluation eval2 = new Evaluation(2);
		INDArray output2 = model.output(testData.getFeatures());
		eval2.eval(testData.getLabels(), output2);
		log.info(eval2.stats());

	}

}
