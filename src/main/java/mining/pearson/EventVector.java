package mining.pearson;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import util.Config;
import util.EsUtil;

public class EventVector {
	private List<String> allEventTypes;
	private long base;

	private int len;
	private double[][] vectors;

	private int interval = 1;

	public EventVector(int interval) {
		this.interval = interval;
		
		len = Config.getExperimentDuration() * 24 * 60 / this.interval; // every 10 mins
		base = Config.getSimulationTimeBase() / 1000 / 60;

		Map<String, Integer> agg = EsUtil.agg(Config.getElasticsearchHost(), Config.getExperimentIndex(), "source");
		allEventTypes = Lists.newArrayList(agg.keySet());
		allEventTypes.sort(Comparator.naturalOrder());

		int eventTypeSize = allEventTypes.size();
		vectors = new double[eventTypeSize][];

		for (int i = 0; i < eventTypeSize; i++) {
			String source = allEventTypes.get(i);
			vectors[i] = new double[len];

			List<JSONObject> res = EsUtil.search(Config.getElasticsearchHost(), Config.getExperimentIndex(),
					"source:" + source);

			for (JSONObject event : res) {
				Long ts = event.getLong("@timestamp");
				long min = ts / 1000 / 60 - base;
				vectors[i][(int) (min / this.interval)] = 1;
			}
		}
	}

	public double[][] getVectors() {
		return vectors;
	}

	public List<String> getAllEventTypes() {
		return allEventTypes;
	}

	public static void main(String[] args) {
		EventVector eventVector = new EventVector(10);
		System.out.println(eventVector.len);
	}
}
