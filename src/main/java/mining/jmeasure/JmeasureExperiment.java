package mining.jmeasure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mining.AbstractEvaluation;
import util.JMeasureUtil;
import util.OutlierDetect;
import util.TreeSet2;

public class JmeasureExperiment {
	private TumblingWindows tumblingWindows;
	private List<String> allEventTypes;
	private Map<String, Map<String, Double>> rate;
	private Map<String, Set<String>> correlations = new HashMap<>();
	private List<TreeSet2> result;

	public JmeasureExperiment() {
		this.tumblingWindows = new TumblingWindows();
		this.allEventTypes = tumblingWindows.getAllEventTypes();
		rate = new HashMap<>();
		for (String et : allEventTypes)
			rate.put(et, new HashMap<String, Double>());

		result = new ArrayList<>();
	}

	private void resovlePairwises() {
		double[] indepProb = tumblingWindows.getIndepProb();
		double[][] depProb = tumblingWindows.getDepProb();

		for (int i = 0; i < allEventTypes.size(); i++) {
			String x = allEventTypes.get(i);
			for (int j = 0; j < allEventTypes.size(); j++) {
				String y = allEventTypes.get(j);
				if (!x.equals(y)) {
					double px = indepProb[i];
					double py = indepProb[j];
					double pxy = depProb[i][j];
					double jmeasureXy = JMeasureUtil.jmeasureXy(px, py, pxy);
					if (!Double.isNaN(jmeasureXy))
						rate.get(x).put(y, jmeasureXy);
//					System.out.println("(" + x + ":" + y + ") :\t" + jmeasureXy);
				}
			}

			Map<String, Double> xRate = rate.get(x);
			List<String> labelName = OutlierDetect.labelName(xRate);
			TreeSet2 ts = new TreeSet2();
			ts.addAll(labelName);
			this.correlations.put(x, ts);
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
	}

	public static void main(String[] args) {
		JmeasureExperiment experiment = new JmeasureExperiment();
		experiment.run();
	}
}
