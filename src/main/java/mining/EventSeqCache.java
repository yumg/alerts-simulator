package mining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import util.Config;

public class EventSeqCache {
	private String cacheBase;

	public EventSeqCache() {
		String target = Config.getExperimentIndex();
		cacheBase = Config.getWorkDir() + "/cache/" + target;
		File file = new File(cacheBase);
		if (!file.exists())
			file.mkdirs();

		if (!file.isDirectory())
			throw new RuntimeException("File Path: '" + cacheBase + "' exists and it is not a directory");

	}

	public void saveEpisode(String type, long windowSize, float split, List<Map<String, Long>> episodes) {
		Path cacheFilePath = Paths.get(cacheBase + "/" + type + "-" + windowSize + "-" + split + "-episodes.json");
		try {
			BufferedWriter fileWriter = Files.newBufferedWriter(cacheFilePath, Charset.forName("UTF-8"));
			fileWriter.write(JSONObject.toJSONString(episodes));
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Map<String, Long>> getEpisode(String type, long windowSize, float split) {
		String cacheFilePath = cacheBase + "/" + type + "-" + windowSize + "-" + split + "-episodes.json";
		if ((new File(cacheFilePath)).exists()) {
			Feature[] parseFeatures = { Feature.AllowComment, Feature.AutoCloseSource };
			List<Map<String, Long>> rtv = null;
			try {
				JSONArray ja = (JSONArray) JSONObject.parseObject(Files.newInputStream(Paths.get(cacheFilePath)),
						JSONArray.class, parseFeatures);
				rtv = new ArrayList<>(ja.size());
				for (int i = 0; i < ja.size(); i++) {
					JSONObject jo = ja.getJSONObject(i);
					Map<String, Long> map = new HashMap<String, Long>();
					for (Iterator<String> itr = jo.keySet().iterator(); itr.hasNext();) {
						String key = itr.next();
						map.put(key, jo.getLong(key));
					}
					rtv.add(map);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return rtv;
		} else
			return null;
	}
}
