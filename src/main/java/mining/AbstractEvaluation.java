package mining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import simulation.Domain;
import simulation.Element;
import util.Config;
import util.TreeSet2;

abstract public class AbstractEvaluation {
	private List<Set<String>> truth;
	private List<TreeSet2> result;

	private double tp = 0;
	private double fp = 0;
	private double fn = 0;
	private double tn = 0;

	public AbstractEvaluation() {
		this.truth = truth();
		this.result = resolveResult();
	}

	abstract protected List<TreeSet2> resolveResult();

	private List<Set<String>> truth() {
		Map<String, Set<String>> idx = new LinkedHashMap<String, Set<String>>();
		Domain domain = new Domain();
		for (Element element : domain.elementsList()) {
			String name = element.name();
			Set<String> correlations = new TreeSet<String>();
			element.ins().forEach(s -> correlations.add(s.name()));
			element.unionIns().forEach(s -> correlations.add(s.name()));
			element.outs().forEach(s -> correlations.add(s.name()));
			Iterator<String> iterator = correlations.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				i++;
				String s = iterator.next();
				if (idx.containsKey(s)) {
					idx.get(s).addAll(correlations);
					idx.put(name, idx.get(s));
					break;
				}
			}
			if (i >= correlations.size())
				idx.put(name, correlations);
		}

		List<Set<String>> rtv = new ArrayList<Set<String>>();
		idx.forEach((key, value) -> {
			if (value.size() > 0 && !rtv.contains(value)) {
				rtv.add(value);
			}
		});

		return rtv;
	}

	@SuppressWarnings("unused")
	public Map<String, Object> evaluate() {
		int total = this.result.size();

		int[] flag = new int[truth.size()];

		for (Iterator<TreeSet2> itr = result.iterator(); itr.hasNext();) {
			TreeSet2 res = itr.next();
			int i = 0;
			for (; i < truth.size(); i++) {
				Set<String> tru = truth.get(i);
				if (tru.containsAll(res)) {
					if (res.containsAll(tru))
						flag[i] = 1;
					tp++;
					break;
				}
			}
			if (i == truth.size())
				fp++;
		}

		List<Set<String>> fnList = new ArrayList<Set<String>>();
		for (int i = 0; i < flag.length; i++)
			if (flag[i] == 0) {
				fn++;
				fnList.add(truth.get(i));
			}

		Map<String, Object> m = new LinkedHashMap<>();

		System.out.println("TRUTH:");
		System.out.println(truth);
		System.out.println("\nRESULT:");
		System.out.println(result);

		System.out.print("\n");

		System.out.println("Truth  size:\t" + truth.size());
		System.out.println("TP in  truth:\t" + (truth.size() - fn));
		System.out.println("Result size:\t" + total);
		System.out.println("TP in result:\t" + tp);

		System.out.println("\nFN list :");
		System.out.println(fnList);

		m.put("TRUTH", truth);
		m.put("RESULT", result);
		m.put("Truth size", truth.size());
		m.put("TP in truth", truth.size() - fn);
		m.put("Result size", total);
		m.put("TP in result", tp);
		m.put("FN list", fnList);
		m.put("Outlier Kn", Config.getExperimentOutlierKn());
		m.put("Outlier Threshold", Config.getExperimentOutlierThreshold());
		return m;
	}

	public double precision() {
		return tp / (tp + fp);
	}

	public double recall() {
		return tp / (tp + fn);
	}

	public double f1Score() {
		double precision = this.precision();
		double recall = this.recall();
		return 2 * precision * recall / (precision + recall);
	}

	public static void main(String[] args) {
		Evaluation evaluate = new Evaluation();
		evaluate.evaluate();
	}

}
