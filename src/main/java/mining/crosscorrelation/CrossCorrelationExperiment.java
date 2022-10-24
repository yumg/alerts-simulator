package mining.crosscorrelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import mining.AbstractEvaluation;
import mining.pearson.EventVector;
import util.TreeSet2;

public class CrossCorrelationExperiment {
	private EventVector eventVector = new EventVector(10);
	private List<String> allEventTypes;
	private Map<String, Map<String, Double>> rate;
	private Map<String, Set<String>> correlations = new HashMap<>();
	private List<TreeSet2> result;

	private RateCache rateCache;

	public CrossCorrelationExperiment() {
		eventVector = new EventVector(10);
		allEventTypes = eventVector.getAllEventTypes();

		rate = new HashMap<>();
		for (String et : allEventTypes)
			rate.put(et, new HashMap<String, Double>());

		result = new ArrayList<>();
		rateCache = new RateCache();
	}

	private void resovlePairwises() {
		CrossCorrelation cc = new CrossCorrelation();
		double[][] vectors = eventVector.getVectors();
		for (int i = 0; i < allEventTypes.size(); i++) {
			String x = allEventTypes.get(i);
			for (int j = 0; j < allEventTypes.size(); j++) {
				String y = allEventTypes.get(j);
				if (!x.equals(y)) {
					double[] px = vectors[i];
					double[] py = vectors[j];
					double correlation = cc.correlation(px, py);
					if (!Double.isNaN(correlation))
						rate.get(x).put(y, correlation);
//					System.out.println("(" + x + ":" + y + ") :\t" + correlation);
				}
			}
		}
		rateCache.saveRate(this.rate);
	}

	private void resolveCorGroup() {
		for (int i = 0; i < allEventTypes.size(); i++) {
			String x = allEventTypes.get(i);
			Map<String, Double> xRate = rate.get(x);
//			List<String> labelName = OutlierDetect.labelName(xRate);
			List<String> labelName = outlierDetect(xRate);
			TreeSet2 ts = new TreeSet2();
			ts.addAll(labelName);
			this.correlations.put(x, ts);
		}

		this.correlations.forEach((k, s) -> {
			if (s.size() > 1) {
				TreeSet2 r = new TreeSet2();
				r.add(k);
				r.addAll(s);
				this.result.add(r);
			}
		});
	}

	private List<String> outlierDetect(Map<String, Double> xRate) {
		DescriptiveStatistics stats = new DescriptiveStatistics();

		Collection<Double> values = xRate.values();
		double[] dd = new double[values.size()];
		Iterator<Double> iterator = values.iterator();
		for (int i = 0; iterator.hasNext(); i++) {
			dd[i] = iterator.next();
		}

		for (int i = 0; i < dd.length; i++)
			stats.addValue(dd[i]);
		double iqr = stats.getPercentile(75) - stats.getPercentile(25);
		iqr *= 0.7413;

		ArrayList<String> rtv = new ArrayList<String>();
		if (iqr > 14) {
			Percentile percentile = new Percentile();
			double p80 = percentile.evaluate(dd, 93);

			xRate.forEach((k, v) -> {
				if (v > p80)
					rtv.add(k);
			});
		}
		return rtv;
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
		Map<String, Map<String, Double>> rCache = rateCache.getRate();
		if (rCache == null)
			this.resovlePairwises();
		else
			this.rate = rCache;
		this.resolveCorGroup();
		this.evaluate();
	}

	public static void main(String[] args) {
		CrossCorrelationExperiment crossCorrelationExperiment = new CrossCorrelationExperiment();
		crossCorrelationExperiment.run();

	}

}
