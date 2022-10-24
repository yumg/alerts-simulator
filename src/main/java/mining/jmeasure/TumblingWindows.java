package mining.jmeasure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import util.Config;
import util.EsUtil;

public class TumblingWindows {
	private List<String> allEventTypes;
	private long base;
	private long limit;
	private long window;

	private double[][] depProb; // [i][j] = prob(i|j)
	private double[] indepProb;
	private List<List<JSONObject>> windows;
	private Map<String, Integer> count;
	private Map<String, Set<Set<String>>> windowsIdx;

	public double[] getIndepProb() {
		return indepProb;
	}

	public List<String> getAllEventTypes() {
		return allEventTypes;
	}

	public double[][] getDepProb() {
		return depProb;
	}

	public List<List<JSONObject>> getWindows() {
		return windows;
	}

	public Map<String, Integer> getCount() {
		return count;
	}

	public TumblingWindows() {
		base = Config.getSimulationTimeBase();
		limit = base + Config.getExperimentDuration() * 24 * 60 * 60 * 1000;
//		window = Config.getExperimentTimeWindow() * 1000;
		window = 200* 1000;

		Map<String, Integer> agg = EsUtil.agg(Config.getElasticsearchHost(), Config.getExperimentIndex(), "source");
		allEventTypes = Lists.newArrayList(agg.keySet());
		allEventTypes.sort(Comparator.naturalOrder());

		count = new HashMap<String, Integer>();
		for (String srcType : allEventTypes)
			count.put(srcType, 0);

		windowsIdx = new HashMap<>();
		for (String srcType : allEventTypes)
			windowsIdx.put(srcType, new HashSet<>());

		depProb = new double[allEventTypes.size()][];
		for (int i = 0; i < depProb.length; i++)
			depProb[i] = new double[allEventTypes.size()];

		indepProb = new double[allEventTypes.size()];

		windows = new ArrayList<>();
		initWindows(base, limit, window);
	}

	private void initWindows(long base, long limit, long window) {
		long t1 = base;
		long t2 = base + window;
		do {
			List<JSONObject> res = EsUtil.search(Config.getElasticsearchHost(), Config.getExperimentIndex(),
					"@timestamp:[" + t1 + " TO " + t2 + " ]");
			windows.add(res);
			count(res);
			idx(res);
			t1 = t2;
			t2 = t2 + window;
		} while (t2 < limit);
		calcuProb();
	}

	private void count(List<JSONObject> window) {
		window.forEach(event -> {
			String src = event.getString("source");
			count.put(src, count.get(src) + 1);
		});
	}

	private void idx(List<JSONObject> window) {
		Set<String> w = new HashSet<>();
		window.forEach(event -> {
			String src = event.getString("source");
			w.add(src);
			windowsIdx.get(src).add(w);
		});
	}

	private void calcuProb() {
		for (int i = 0; i < allEventTypes.size(); i++) {
			String e1 = allEventTypes.get(i);
			for (int j = 0; j < allEventTypes.size(); j++) {
				String e2 = allEventTypes.get(j);
				int e2_count = windowsIdx.get(e2).size();
				int e1_under_e2_count = 0;
				for (Set<String> s : windowsIdx.get(e2)) {
					if (s.contains(e1))
						e1_under_e2_count++;
				}
				depProb[i][j] = (double) e1_under_e2_count / (double) e2_count;
			}
		}

		for (int i = 0; i < allEventTypes.size(); i++) {
			String e = allEventTypes.get(i);
			Integer c = count.get(e);
			indepProb[i] = (double) c / (double) windows.size();
		}
	}

	public static void main(String[] args) {
//		TumblingWindows tumblingWindows = new TumblingWindows();
//		System.out.println(123);
	}
}
