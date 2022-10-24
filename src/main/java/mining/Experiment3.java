package mining;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import simulation.Event;
import util.HttpKit;

public class Experiment3 {
	private static String url = "http://localhost:9200/alert20@2020-06-13_13-57-21/_search";

	public static void main(String[] args) throws UnsupportedEncodingException {
//		itrUnCorrelations("B");
		itrCorrelations("C");
	}

	public static void itrCorrelations(String source) throws UnsupportedEncodingException {
		String param1 = "q=" + URLEncoder.encode("source:" + source + " AND cause:correlations", "UTF-8");
		String param2 = "size=" + URLEncoder.encode("500", "UTF-8");
		String resp = HttpKit.get(url, new String[] { param1, param2 });

		JSONObject jo = JSONObject.parseObject(resp);
		JSONArray hits = jo.getJSONObject("hits").getJSONArray("hits");
		System.out.println("correlations count:" + hits.size());

		for (int i = 0; i < hits.size(); i++) {
			JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");
			Long ts = hit.getLong("@timestamp");
			range(ts - 1000 * 60 * 5, ts);
		}
	}

	public static void itrUnCorrelations(String source) throws UnsupportedEncodingException {
		String param1 = "q=" + URLEncoder.encode("(NOT cause:correlations) AND source:" + source , "UTF-8");
		String param2 = "size=" + URLEncoder.encode("500", "UTF-8");
		String resp = HttpKit.get(url, new String[] { param1, param2 });

		JSONObject jo = JSONObject.parseObject(resp);
		JSONArray hits = jo.getJSONObject("hits").getJSONArray("hits");
		System.out.println("un-correlations count:" + hits.size());

		for (int i = 0; i < hits.size(); i++) {
			JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");
			Long ts = hit.getLong("@timestamp");
			range(ts - 1000 * 60 * 5, ts);
		}

		System.out.println("confusingFault: " + confusingFault);
	}

	public static int confusingFault = 0;

	public static void range(long t0, long t1) throws UnsupportedEncodingException {
		String param = "q=" + URLEncoder.encode("@timestamp:[ " + t0 + " TO " + t1 + " ]", "UTF-8");
		String resp = HttpKit.get(url, new String[] { param });

		JSONObject jo = JSONObject.parseObject(resp);
		JSONArray hits = jo.getJSONObject("hits").getJSONArray("hits");

		if (hits.size() > 1) {
			System.out.println("==================");
			confusingFault++;
			for (int i = 0; i < hits.size(); i++) {
				JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");
				String source = hit.getString("source");
				Long ts = hit.getLong("@timestamp");
				String cause = hit.getString("cause");
				Event event = new Event(new Date(ts), source, cause);
				System.out.println(event.toString2());
			}
		}

	}

}
