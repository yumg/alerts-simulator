package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class EsUtil {
	public static Map<String, Integer> agg(String url, String idx, String field) {
		Map<String, Integer> rtv = new HashMap<>();

		String body = "{ \"aggs\" : { \"agg\" : { \"terms\" : { \"size\" : 100,\"field\" : \"" + field + "\" } } } }";
		String resp = HttpKit.post("http://" + Config.getElasticsearchHost() + "/" + idx + "/_search",
				new String[] { "size=0" }, body);
		JSONObject respJsonObj = JSONObject.parseObject(resp);
		JSONArray buckets = respJsonObj.getJSONObject("aggregations").getJSONObject("agg").getJSONArray("buckets");
		for (int i = 0; i < buckets.size(); i++) {
			JSONObject bucket = buckets.getJSONObject(i);
			rtv.put(bucket.getString("key"), bucket.getInteger("doc_count"));
		}
		return rtv;
	}

	public static List<JSONObject> search(String url, String idx, String query) {
		List<JSONObject> rtv = new ArrayList<>();
		String body = "{\"query\":{\"bool\":{\"filter\":[{\"query_string\":{\"query\":\"" + query + "\"}}]}}}";
		String resp = HttpKit.post("http://" + Config.getElasticsearchHost() + "/" + idx + "/_search",
				new String[] { "size=2000" }, body);
		JSONObject respJsonObj = JSONObject.parseObject(resp);
		JSONObject hits = respJsonObj.getJSONObject("hits");
		int total = hits.getIntValue("total");
		if (total > 2000)
			throw new RuntimeException("total > 2000");
		JSONArray resList = hits.getJSONArray("hits");
		for (int i = 0; i < resList.size(); i++)
			rtv.add(resList.getJSONObject(i).getJSONObject("_source"));
		return rtv;
	}

	public static void main(String[] args) {
		List<JSONObject> search = search(Config.getElasticsearchHost(), Config.getExperimentIndex(),
				"@timestamp:[1580302711000 TO 1581129691000]");
		System.out.println(search);

	}
}
