package simulation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

public class Domain {
	private List<Element> eventSources = new ArrayList<Element>();
	private Map<String, Element> idx = new HashMap<String, Element>();

	public Domain() {
		try {
			InputStream structureIs = App.class.getClassLoader().getResourceAsStream("structure.json");
			Feature[] parseFeatures = { Feature.AllowComment, Feature.AutoCloseSource };
			JSONArray structureRaw = (JSONArray) JSONObject.parseObject(structureIs, JSONArray.class, parseFeatures);

			for (int i = 0; i < structureRaw.size(); i++) {
				JSONObject es = structureRaw.getJSONObject(i);
				String name = es.getString("name");
				Element eventSource = new Element(name);
				eventSources.add(eventSource);
				idx.put(name, eventSource);
			}

			for (int i = 0; i < structureRaw.size(); i++) {
				JSONObject es = structureRaw.getJSONObject(i);
				String name = es.getString("name");
				JSONObject properties = es.getJSONObject("properties");
				Element eventSource = idx.get(name);
				eventSource.postConstruct(properties, this);
			}
		} catch (Exception e) {
			System.out.println("Domain initiallizing error");
			e.printStackTrace();
		}
	}

	public List<Element> getEventSourcesList() {
		return eventSources;
	}

	public Map<String, Element> getEventSourcesIdx() {
		return idx;
	}

	public void printOuts() {
		eventSources.forEach(e -> {
			System.out.print(e.name());
			System.out.println(e.getOut());
		});
	}

	public void printIns() {
		eventSources.forEach(e -> {
			System.out.print(e.getIn());
			System.out.println(e.name());
		});
	}

	public void printUnionIns() {
		eventSources.forEach(e -> {
			System.out.print(e.getUnionIn());
			System.out.println(e.name());
		});
	}
}
