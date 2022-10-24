package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import util.Config;

public class Domain {
	private List<Element> elements = new ArrayList<Element>();
	private Map<String, Element> idx = new HashMap<String, Element>();

	public Domain() {
		try {
			JSONArray structureRaw = Config.getStructureDefination();
			for (int i = 0; i < structureRaw.size(); i++) {
				JSONObject es = structureRaw.getJSONObject(i);
				String name = es.getString("name");
				Element eventSource = new Element(name);
				elements.add(eventSource);
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

	public List<Element> elementsList() {
		return elements;
	}

	public Map<String, Element> elementsIdx() {
		return idx;
	}

	public void printOuts() {
		elements.forEach(e -> {
			System.out.print(e.name());
			System.out.println(e.outs());
		});
	}

	public void printIns() {
		elements.forEach(e -> {
			System.out.print(e.ins());
			System.out.println(e.name());
		});
	}

	public void printUnionIns() {
		elements.forEach(e -> {
			System.out.print(e.unionIns());
			System.out.println(e.name());
		});
	}

	public int faultElementsQuality() {
		int total = 0;
		for (Element e : elements) {
			if (e.isFault())
				total++;
		}
		return total;
	}
}
