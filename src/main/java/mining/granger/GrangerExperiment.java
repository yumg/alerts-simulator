package mining.granger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mining.AbstractEvaluation;
import mining.pearson.EventVector;
import util.TreeSet2;

public class GrangerExperiment {

	private List<String> allEventTypes;
	private Map<String, Map<String, Double>> rate;
	private Map<String, Set<String>> correlations = new HashMap<>();
	private List<TreeSet2> result;

	private EventVector eventVector;

	public GrangerExperiment() {
		eventVector = new EventVector(5);
		allEventTypes = eventVector.getAllEventTypes();

		rate = new HashMap<>();
		for (String et : allEventTypes)
			rate.put(et, new HashMap<String, Double>());

		result = new ArrayList<>();
	}

	private void resovlePairwises() {
		double[][] vectors = eventVector.getVectors();
		for (int i = 0; i < allEventTypes.size(); i++) {
			String x = allEventTypes.get(i);
			for (int j = 0; j < allEventTypes.size(); j++) {
				String y = allEventTypes.get(j);
				if (!x.equals(y)) {
					double[] px = vectors[i];
					double[] py = vectors[j];
					double granger = GrangerTest.granger(px, py, 2);
					if (!Double.isNaN(granger))
						rate.get(x).put(y, granger);
//					res.add("(" + x + ":" + y + ") :" + granger);
				}
			}

			Map<String, Double> xRate = rate.get(x);

//			List<String> labelName = OutlierDetect.labelName(xRate);
			List<String> labelName = outlierDetect(xRate);
			TreeSet2 ts = new TreeSet2();
			ts.addAll(labelName);
			this.correlations.put(x, ts);
		}
	}

	private List<String> outlierDetect(Map<String, Double> xRate) {
		ArrayList<String> rtv = new ArrayList<String>();

		xRate.forEach((k, v) -> {
			if (v == 0)
				rtv.add(k);
		});

		return rtv;
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
	}

	public static void main(String[] args) {
		GrangerExperiment grangerExperiment = new GrangerExperiment();
		grangerExperiment.run();
	}
}
