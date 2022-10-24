package mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

import util.Config;
import util.EsUtil;

public class EventSeq implements IEventSeq {
	EventSeqCache cache = new EventSeqCache();

	@Override
	public Set<String> allEventTypes() {
		Map<String, Integer> agg = EsUtil.agg(Config.getElasticsearchHost(), Config.getExperimentIndex(), "source");
		return agg.keySet();
	}

	public List<Map<String, Long>> eventWindows(String eventType, long windowSize, float split) {
		List<Map<String, Long>> episode = cache.getEpisode(eventType, windowSize, split);
		if (episode != null)
			return episode;
		else {
			List<Map<String, Long>> rtv = new ArrayList<>();
			List<JSONObject> baseEvents = EsUtil.search(Config.getElasticsearchHost(), Config.getExperimentIndex(),
					"source:" + eventType);
			baseEvents.forEach(e -> {
				Long t0 = e.getLong("@timestamp");
				Long t1 = t0 - (long) (windowSize * split * 1000);
				Long t2 = t0 + (long) (windowSize * (1 - split) * 1000);
				List<JSONObject> window = EsUtil.search(Config.getElasticsearchHost(), Config.getExperimentIndex(),
						"@timestamp:[" + t1 + " TO " + t2 + " ]");
				Map<String, Long> m = new HashMap<>();
				window.forEach(event -> {
					String src = event.getString("source");
					Long ts = event.getLong("@timestamp");
					if (m.containsKey(src)) {
						Long ts0 = m.get(src);
						if (Math.abs(ts0 - t0) >= Math.abs(ts - t0))
							m.put(src, t2 - ts);
					} else
						m.put(src, t2 - ts);
				});
				rtv.add(m);
			});
			cache.saveEpisode(eventType, windowSize, split, rtv);
			return rtv;
		}
	}

	public EventSeq() {

	}

}
