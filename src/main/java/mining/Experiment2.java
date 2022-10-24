package mining;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.alibaba.fastjson.JSONObject;

import util.HttpKit;

public class Experiment2 {
	private static String url = "http://localhost:9200/alert20@2020-06-13_13-57-21/_count";

	public static void main(String[] args) throws UnsupportedEncodingException {
		count(new String[] { "H", "G0", "G"}, new String[] { "G" });

		System.out.println("================================");
		count(new String[] { "I", "H", "G", "J" }, new String[] { "J" });

		System.out.println("================================");
		count(new String[] { "D", "E", "F" }, new String[] { "F" });

	}

	public static void count(String[] abc, String[] d) throws UnsupportedEncodingException {
		for (String s : abc) {
			String param1 = "q=" + URLEncoder.encode("source:" + s, "UTF-8");
			String resp = HttpKit.get(url, new String[] { param1 });

			JSONObject jo = JSONObject.parseObject(resp);
			Integer count = jo.getInteger("count");
			System.out.println("Count " + s + ":" + count);
		}

		for (String s : d) {
			String param1 = "q=" + URLEncoder.encode("source:" + s + " AND cause:correlations", "UTF-8");
			String resp = HttpKit.get(url, new String[] { param1 });

			JSONObject jo = JSONObject.parseObject(resp);
			Integer count = jo.getInteger("count");
			System.out.println("Count " + s + " with cause-correlations:" + count);
		}

	}

	public static void countABC() throws UnsupportedEncodingException {
		String param1 = "q=" + URLEncoder.encode("source:D", "UTF-8");
		String resp = HttpKit.get(url, new String[] { param1 });

		JSONObject jo = JSONObject.parseObject(resp);
		Integer count = jo.getInteger("count");
		System.out.println("Count D:" + count);

		param1 = "q=" + URLEncoder.encode("source:E", "UTF-8");
		resp = HttpKit.get(url, new String[] { param1 });

		jo = JSONObject.parseObject(resp);
		count = jo.getInteger("count");
		System.out.println("Count E:" + count);

		param1 = "q=" + URLEncoder.encode("source:F", "UTF-8");
		resp = HttpKit.get(url, new String[] { param1 });

		jo = JSONObject.parseObject(resp);
		count = jo.getInteger("count");
		System.out.println("Count F:" + count);

		param1 = "q=" + URLEncoder.encode("source:E AND cause:correlations", "UTF-8");
		resp = HttpKit.get(url, new String[] { param1 });

		jo = JSONObject.parseObject(resp);
		count = jo.getInteger("count");
		System.out.println("Count E with cause-correlations:" + count);

	}

}
