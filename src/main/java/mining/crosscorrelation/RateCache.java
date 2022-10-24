package mining.crosscorrelation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import util.Config;

public class RateCache {
	private String cacheBase;

	public RateCache() {
		String target = Config.getExperimentIndex();
		cacheBase = Config.getWorkDir() + "/cache/" + target;
		File file = new File(cacheBase);
		if (!file.exists())
			file.mkdirs();

		if (!file.isDirectory())
			throw new RuntimeException("File Path: '" + cacheBase + "' exists and it is not a directory");

	}

	public void saveRate(Map<String, Map<String, Double>> rate) {
		Path cacheFilePath = Paths.get(cacheBase + "/CrossCorrelation-Rates.json");
		try {
			BufferedWriter fileWriter = Files.newBufferedWriter(cacheFilePath, Charset.forName("UTF-8"));
			fileWriter.write(JSONObject.toJSONString(rate));
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, Map<String, Double>> getRate() {
		String cacheFilePath = cacheBase + "/CrossCorrelation-Rates.json";
		if ((new File(cacheFilePath)).exists()) {
			Map<String, Map<String, Double>> rtv = new HashMap<>();
			Feature[] parseFeatures = { Feature.AllowComment, Feature.AutoCloseSource };
			try {
				JSONObject jo = (JSONObject) JSONObject.parseObject(Files.newInputStream(Paths.get(cacheFilePath)),
						JSONObject.class, parseFeatures);
				Set<String> keySet1 = jo.keySet();
				for (String k1 : keySet1) {
					HashMap<String, Double> rate = new HashMap<String, Double>();
					rtv.put(k1, rate);
					JSONObject jsonObject = jo.getJSONObject(k1);
					Set<String> keySet2 = jsonObject.keySet();
					for (String k2 : keySet2) {
						rate.put(k2, jsonObject.getDouble(k2));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return rtv;
		} else
			return null;
	}
}
