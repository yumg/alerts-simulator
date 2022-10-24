package detection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;

import mining.EventSeq;
import util.Config;

public class Training {
	public static JSONObject result;

	public static void main(String[] args) {
		String workDir = Config.getWorkDir();
		String summaryResult = workDir + "/out/" + Config.getExperimentIndex() + "/summary-result.json";
		System.out.println(summaryResult);

		Feature[] parseFeatures = { Feature.AllowComment, Feature.AutoCloseSource };
		try {
			result = (JSONObject) JSONObject.parseObject(Files.newInputStream(Paths.get(summaryResult)),
					JSONObject.class, parseFeatures);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println(result);

		String name = "A";
		JSONArray jsonArray = result.getJSONObject("correlations").getJSONArray(name);

		String cache = workDir + "/cache/" + Config.getExperimentIndex() + "/A-900-0.7-episodes.json";

		JSONArray cacheArray = new JSONArray();

		HashSet<String> col = new HashSet<String>();
		col.add("A");
		col.add("B");
		col.add("C");
		try {
			cacheArray = (JSONArray) JSONObject.parseObject(Files.newInputStream(Paths.get(cache)), JSONArray.class,
					parseFeatures);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONArray rightSamples = new JSONArray();
		JSONArray wrongSamples = new JSONArray();

		JSONArray rightSamples2 = new JSONArray();
		Map<Long, Integer> bm = new HashMap<>();
		Map<Long, Integer> cm = new HashMap<>();

		for (int i = 0; i < cacheArray.size(); i++) {
			JSONObject jsonObject = cacheArray.getJSONObject(i);
			Set<String> keySet = jsonObject.keySet();
			if (keySet.containsAll(col)) {
				rightSamples.add(jsonObject);

				JSONObject jo = new JSONObject();
				Long bv = jsonObject.getLong("B");
				Long cv = jsonObject.getLong("C");
				jo.put("B", bv);
				jo.put("C", cv);

				rightSamples2.add(jo);

				if (bm.containsKey(bv))
					bm.put(bv, bm.get(bv) + 1);
				else
					bm.put(bv, 1);

				if (cm.containsKey(cv))
					cm.put(cv, cm.get(cv) + 1);
				else
					cm.put(cv, 1);
			} else {
				wrongSamples.add(jsonObject);
			}
		}

		System.out.println(rightSamples2.toString(SerializerFeature.PrettyFormat));
		System.out.println(rightSamples.size());
		System.out.println(cacheArray.size());

		System.out.println(bm);
		System.out.println(cm);

		long width = 900 * 1000;
		float split = 0.7f;
		long zero = (long) (width * split); // 0 point

		long left_len = zero;
		long right_len = width - zero;

		System.out.println(zero);
		System.out.println(left_len);
		System.out.println(right_len);

		long base = Math.max(left_len, right_len);

		EventSeq eventSeq = new EventSeq();
		ArrayList<String> allEventTypes = Lists.newArrayList(eventSeq.allEventTypes());
		allEventTypes.sort(Comparator.naturalOrder());
		System.out.println(allEventTypes);

		String csv = "";
		for (int i = 0; i < rightSamples.size(); i++) {
			JSONObject s = rightSamples.getJSONObject(i);
			StringBuilder sb = new StringBuilder();
			allEventTypes.forEach(et -> {
				Long long1 = s.getLong(et);
				if (long1 != null) {
					Long v1 = width - long1;
					double v2 = (double) (v1 - zero) / (double) base;
//					sb.append(v2 + ",");
					sb.append("1,");
				} else
					sb.append("0,");
			});
//			sb.deleteCharAt(sb.length() - 1);
			sb.append("1");
			csv = csv + sb.toString() + "\n";
		}

		for (int i = 0; i < wrongSamples.size(); i++) {
			JSONObject s = wrongSamples.getJSONObject(i);
			StringBuilder sb = new StringBuilder();
			allEventTypes.forEach(et -> {
				Long long1 = s.getLong(et);
				if (long1 != null) {
					Long v1 = width - long1;
					double v2 = (double) (v1 - zero) / (double) base;
//					sb.append(v2 + ",");
					sb.append("1,");
				} else
					sb.append("0,");
			});
//			sb.deleteCharAt(sb.length() - 1);
			sb.append("0");
			csv = csv + sb.toString() + "\n";
		}

		System.out.println(csv);
	}
}
