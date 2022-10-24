package mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Lists;

import util.ArrayUtil;
import util.Config;
import util.OutlierDetect;

public class Experiment {

	private IEventSeq eventSeq;
	private long timeWindowSize;
	private float timeWinodwSplit;
	private Map<String, Long[][]> allEpisodeMatrix;
	private List<String> allEventTypes;
	private Reports reports = new Reports();

	private Map<String, double[]> occurNumbers = new HashMap<>();
	private Map<String, double[]> centralRates = new HashMap<>();
	private Map<String, double[]> integrationRates = new HashMap<>();
	private Map<String, Set<String>> correlations = new HashMap<>();

	public Experiment() {
		eventSeq = new EventSeq();
		allEpisodeMatrix = new LinkedHashMap<>();
		timeWindowSize = Config.getExperimentTimeWindow();
		timeWinodwSplit = Config.getExperimentTimeWindowSplit();
		allEventTypes = Lists.newArrayList(eventSeq.allEventTypes());
		allEventTypes.sort(Comparator.naturalOrder());
	}

	public void loadAllEpisodeMatrix() {
		System.out.println("Start loading all episodes ......");
		allEventTypes.forEach(type -> {
			System.out.print("Loading episodes on: [ " + type + " ] ...");
			List<Map<String, Long>> episodes = eventSeq.eventWindows(type, timeWindowSize, timeWinodwSplit);
			allEpisodeMatrix.put(type, episodesMatrix(episodes));
			System.out.println(" Completed!");
		});
		System.out.println("All Done!");
	}

	public void loadSomeEpisodeMatrix(String[] testRange) {
		Lists.newArrayList(testRange).forEach(type -> {
			List<Map<String, Long>> episodes = eventSeq.eventWindows(type, timeWindowSize, timeWinodwSplit);
			allEpisodeMatrix.put(type, episodesMatrix(episodes));
		});
	}

	/**
	 * 
	 * @param episodes
	 * @return
	 * 
	 *         Long[][] episodesMatrix:
	 *         <ul>
	 *         <li>The first dimension is all episodes <br>
	 *         </li>
	 *         <li>The second dimension is all event types</li>
	 *         </ul>
	 */
	private Long[][] episodesMatrix(List<Map<String, Long>> episodes) {
		Long[][] episodesMatrix = new Long[episodes.size()][];
		for (int i = 0; i < episodes.size(); i++) {
			Long[] episodeVector = new Long[allEventTypes.size()];
			Map<String, Long> episode = episodes.get(i);
			episode.forEach((source, t) -> episodeVector[allEventTypes.indexOf(source)] = t);
			episodesMatrix[i] = episodeVector;
		}
		return episodesMatrix;
	}

	public void test() {
		explore0("G", this.allEpisodeMatrix.get("G"));
		double[] ds = this.integrationRates.get("G");
		List<Integer> labelIdx = OutlierDetect.labelIdx(ds);
		System.out.print("G: ");
		labelIdx.forEach(i -> System.out.print(allEventTypes.get(i) + ","));
	}

	public void explore() {
		this.allEventTypes.forEach(eventType -> explore0(eventType, this.allEpisodeMatrix.get(eventType)));
		this.integrationRates.forEach((source, integrationRate) -> {
			List<Integer> labelIdx = OutlierDetect.labelIdx(integrationRate); // ignore NaN , If NaN, then must do not
																				// correlation
			Set<String> correlation = new TreeSet<String>();
			labelIdx.forEach(i -> correlation.add(allEventTypes.get(i)));
			this.correlations.put(source, correlation);
		});
		compress();
	}

	private void compress() {

		for (Iterator<Entry<String, Set<String>>> itr = correlations.entrySet().iterator(); itr.hasNext();) {
			Entry<String, Set<String>> source = itr.next();
			String s_name = source.getKey();
			Set<String> s_corre = source.getValue();
			s_corre.remove(s_name);
//			for (Iterator<String> itr2 = s_corre.iterator(); itr2.hasNext();) {
//				String other = itr2.next();
//				if (!correlations.containsKey(other) || !correlations.get(other).contains(s_name))
//					itr2.remove();
//			}
//
			if (s_corre.size() <= 1)
				itr.remove();
		}

	}

	public void outResult() {
		this.correlations.forEach((source, correlations) -> {
			System.out.println(source + ": {" + Arrays.toString(correlations.toArray()) + "}");
		});

		String[] eTypes = allEventTypes.toArray(new String[] {});
		reports.reportOccurNumbers(this.occurNumbers, eTypes);
		reports.reportCentralRates(this.centralRates, eTypes);
		reports.reportIntegrationRates(this.integrationRates, eTypes);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("Outlier Kn", Config.getExperimentOutlierKn());
		params.put("Outlier Threshold", Config.getExperimentOutlierThreshold());
		params.put("Events Number", allEventTypes.size());
		reports.reportSummaryResult(correlations, params);
		reports.close();
	}

	private int countOccur(Long[] v) {
		int rtv = 0;
		for (Long l : v)
			if (l != null)
				rtv++;
		return rtv;
	}

	// TODO: It is possible that return a NaN value because stats.n()==0
	private double evalCentral(Long[] v) {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for (int i = 0; i < v.length; i++)
			if (v[i] != null)
				stats.addValue(v[i]);
		double iqr = stats.getPercentile(75) - stats.getPercentile(25);
		iqr *= 0.7413;
		return iqr / stats.getMean();
	}

	private List<String> explore0(String source, Long[][] matrix) {
		List<String> rtv = new ArrayList<>();
		Long[][] transposeMartrix = ArrayUtil.transposeMartrix(matrix);
		int l = transposeMartrix.length;
		double[] occurNumber = new double[l];
		double[] centralRate = new double[l];
		double[] integration = new double[l];

		for (int i = 0; i < transposeMartrix.length; i++) {
			occurNumber[i] = countOccur(transposeMartrix[i]);
			centralRate[i] = evalCentral(transposeMartrix[i]); // may return NaN
			integration[i] = occurNumber[i] / (centralRate[i] + 1); // may got NaN
		}
		occurNumbers.put(source, occurNumber);
		centralRates.put(source, centralRate);
		integrationRates.put(source, integration);
		return rtv;
	}

	public void evaluate() {
		AbstractEvaluation evaluate = new Evaluation();
		reports.reportEvaluateResult(evaluate.evaluate());
		
		System.out.println("===========");
		System.out.println(evaluate.f1Score());
	}

	public static void main(String[] args) {
		Experiment experiment = new Experiment();

		experiment.loadAllEpisodeMatrix();
		experiment.explore();
		experiment.outResult();

//		experiment.loadSomeEpisodeMatrix(new String[] { "G" });
//		experiment.test();
		experiment.evaluate();
	}
}
