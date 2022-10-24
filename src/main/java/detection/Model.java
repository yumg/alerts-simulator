package detection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.Config;

public class Model {
	public final Logger logger = LoggerFactory.getLogger(Model.class);

	private int labelIndex = 52;
	private int numClasses = 2;
	private int batchSize = 1000;

	private Map<String, MultiLayerNetwork> models = new HashMap<>();

	public void build(DataBase dataBase) throws IOException, InterruptedException {
		List<String> dbKeys = dataBase.getDbKeys();

		Map<String, String> mergeEqDbs = dataBase.getMergeEqDbs();

		for (String dbKey : dbKeys) {
			String dbPath = mergeEqDbs.get(dbKey);
			int numLinesToSkip = 0;
			char delimiter = ',';
			RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
			recordReader.initialize(new FileSplit(new File(dbPath)));

			DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numClasses);
			DataSet allData = iterator.next();
			allData.shuffle();
			SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65); // Use 65% of data for training

			DataSet trainingData = testAndTrain.getTrain();
			DataSet testData = testAndTrain.getTest();

			final int numInputs = 52;
			int outputNum = 2;
			long seed = 6;
			int numHiddenNodes = 20;
			double learningRate = 0.01;

			MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed).weightInit(WeightInit.XAVIER)
//					.updater(new Nesterovs(learningRate, 0.9))
					.updater(new Sgd(learningRate)).list()
					.layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.RELU)
							.build())
					.layer(new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD).activation(Activation.SOFTMAX)
							.nIn(numHiddenNodes).nOut(outputNum).build())
					.build();

			MultiLayerNetwork model = new MultiLayerNetwork(conf);
			model.init();
			// record score once every 100 iterations
			model.setListeners(new ScoreIterationListener(100));

			for (int i = 0; i < 1000; i++) {
				model.fit(trainingData);
			}

			// evaluate the model on the test set
			Evaluation eval = new Evaluation(2);
			INDArray output = model.output(testData.getFeatures());
			eval.eval(testData.getLabels(), output);

			System.out.println(eval.stats());
			models.put(dbKey, model);
		}
	}

	public void save() {
		String outBase = Config.getWorkDir() + "/cache/" + Config.getExperimentIndex();
		models.forEach((key, model) -> {
			try {
				model.save(new File(outBase + "/" + key + ".model"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public void load(DataBase dataBase) {
		String outBase = Config.getWorkDir() + "/cache/" + Config.getExperimentIndex();
		List<String> dbKeys = dataBase.getDbKeys();
		dbKeys.forEach((key) -> {
			try {
				this.models.put(key, MultiLayerNetwork.load(new File(outBase + "/" + key + ".model"), true));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public void test(DataBase dataBase) throws IOException, InterruptedException {
		List<String> dbKeys = dataBase.getDbKeys();

		Map<String, String> mergeEqDbs = dataBase.getMergeEqDbs();

		for (String dbKey : dbKeys) {
			String dbPath = mergeEqDbs.get(dbKey);
			int numLinesToSkip = 0;
			char delimiter = ',';
			RecordReader recordReader = new CSVRecordReader(numLinesToSkip, delimiter);
			recordReader.initialize(new FileSplit(new File(dbPath)));

			DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex, numClasses);
			DataSet allData = iterator.next();
			allData.shuffle();
			SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.35); // Use 65% of data for training

			DataSet testData = testAndTrain.getTest();

			MultiLayerNetwork model = this.models.get(dbKey);
			// evaluate the model on the test set
			Evaluation eval = new Evaluation(2);
			INDArray output = model.output(testData.getFeatures());
			eval.eval(testData.getLabels(), output);

			System.out.println(eval.stats());
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		DataBase dataBase = new DataBase();
		dataBase.init();

		Model model = new Model();
		model.load(dataBase);
		model.test(dataBase);
//		try {
//			model.build(dataBase);
//			model.saveModel();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
