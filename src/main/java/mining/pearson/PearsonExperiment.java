package mining.pearson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import mining.AbstractEvaluation;
import util.OutlierDetect;
import util.TreeSet2;

public class PearsonExperiment {

	private Map<String, Map<String, Double>> rate;
	private EventVector eventVector;
	private List<String> allEventTypes;

	private Set<Set<String>> pairwises;
	private Map<String, Set<String>> correlations = new HashMap<>();
	private List<TreeSet2> result;

	public PearsonExperiment() {
		rate = new HashMap<>();
		eventVector = new EventVector(1);
		allEventTypes = eventVector.getAllEventTypes();

		for (String et : allEventTypes)
			rate.put(et, new HashMap<String, Double>());

		pairwises = new HashSet<Set<String>>();
		result = new ArrayList<>();
	}

	private void resovlePairwises() {
		PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
		double[][] vectors = eventVector.getVectors();
		for (int i = 0; i < allEventTypes.size(); i++) {
			String x = allEventTypes.get(i);

			for (int j = 0; j < allEventTypes.size(); j++) {
				String y = allEventTypes.get(j);
				if (!x.equals(y)) {
					double[] px = vectors[i];
					double[] py = vectors[j];
					double correlation = pearsonsCorrelation.correlation(px, py);
//					System.out.println("(" + x + ":" + y + ") :\t" + correlation);
					rate.get(x).put(y, correlation);
				}
			}
			Map<String, Double> xRate = rate.get(x);
			List<String> labelName = OutlierDetect.labelName(xRate);
			for (String l : labelName) {
				HashSet<String> pairwise = new HashSet<String>();
				pairwise.add(x);
				pairwise.add(l);
				this.pairwises.add(pairwise);
			}
			TreeSet2 ts = new TreeSet2();
			ts.addAll(labelName);
			this.correlations.put(x, ts);
//			System.out.println(x + ": " + JSONObject.toJSONString(ts));
		}

	}

	private void resolveCorGroup() {
		this.correlations.forEach((k, s) -> {
			if (s.size() > 1) {
				TreeSet2 r = new TreeSet2();
				r.add(k);
				r.addAll(s);
				this.result.add(r);
			}
		});
	}

	private void evaluate() {
		AbstractEvaluation eval = new AbstractEvaluation() {
			@Override
			protected List<TreeSet2> resolveResult() {
				return result;
			}
		};
		eval.evaluate();
		System.out.println("=========");
		System.out.println(eval.f1Score());
	}

	public void run() {
		this.resovlePairwises();
		this.resolveCorGroup();
		this.evaluate();
//		System.out.println(JSONObject.toJSONString(evaluate, true));
	}

	public static void main(String[] args) {
		PearsonExperiment experiment = new PearsonExperiment();
		experiment.run();

	}
}
