package simulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.viz.ColorImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.viz.Color;
import util.Config;

public class Reports implements IReport {
	private String outBase;
	private BufferedWriter rawEventsOutFile;
	private BufferedWriter structureOutFile;
	private BufferedWriter gexfOutFile;

	public Reports() {
		String target = Config.getStaticIndexName() + Config.getRuntimePostfix();
		outBase = Config.getWorkDir() + "/out/" + target;
		File file = new File(outBase);
		if (!file.exists())
			file.mkdirs();

		if (!file.isDirectory())
			throw new RuntimeException("File Path: '" + outBase + "' exists and it is not a directory");

		Path bulkEventsPath = Paths.get(outBase + "/bulk-events.json");
		try {
			rawEventsOutFile = Files.newBufferedWriter(bulkEventsPath, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path structurePath = Paths.get(outBase + "/structure.json");
		try {
			structureOutFile = Files.newBufferedWriter(structurePath, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path gexfPath = Paths.get(outBase + "/structure.gexf");
		try {
			gexfOutFile = Files.newBufferedWriter(gexfPath, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void record(Event event) {
		StringBuilder out = new StringBuilder();
		out.append("{ \"index\" : { \"_index\" : \"" + Config.getRuntimeIndexName() + "\", \"_type\" : \"doc\" } }\n");
		out.append(event).append("\n");
		try {
			rawEventsOutFile.write(out.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		try {
			JSONArray.writeJSONString(this.structureOutFile, Config.getStructureDefination(),
					new SerializerFeature[] { SerializerFeature.PrettyFormat });

			outGexf();
			this.rawEventsOutFile.close();
			this.structureOutFile.close();
			this.gexfOutFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void outGexf() throws IOException {
		Gexf gexf = new GexfImpl();
		Calendar date = Calendar.getInstance();
		gexf.getMetadata().setLastModified(date.getTime()).setCreator("YuMG")
				.setDescription(Config.getStaticIndexName() + Config.getRuntimePostfix());
		gexf.setVisualization(true);
		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);

		JSONArray structureDefination = Config.getStructureDefination();
		for (int i = 0; i < structureDefination.size(); i++) {
			JSONObject nodeJo = structureDefination.getJSONObject(i);
			String nodeName = nodeJo.getString("name");
			Node node = graph.createNode(nodeName);
			node.setLabel(nodeName);
			node.setSize(50);
			node.setColor(new ColorImpl(0, 0, 0));
		}

		for (int i = 0; i < structureDefination.size(); i++) {
			JSONObject nodeJo = structureDefination.getJSONObject(i);
			String selfName = nodeJo.getString("name");
			Node selfNode = graph.getNode(selfName);
			JSONObject props = nodeJo.getJSONObject("properties");
			JSONArray ins = props.getJSONArray("in");
			for (int j = 0; j < ins.size(); j++) {
				JSONObject in = ins.getJSONObject(j);
				String inName = in.getString("name");
				Float prob = in.getFloat("prob");
				Node node = graph.getNode(inName);
				Edge edge = node.connectTo(selfNode);
				edge.setColor(normalColor());
				edge.setWeight(20);
				edge.setLabel(String.valueOf(prob));
				edge.setThickness(10);
				edge.setEdgeType(EdgeType.DIRECTED);
			}

			JSONArray unionIns = props.getJSONArray("union-in");
			for (int j = 0; j < unionIns.size(); j++) {
				String inName = unionIns.getString(j);
				Node node = graph.getNode(inName);
				Edge edge = node.connectTo(selfNode);
				edge.setColor(unionColor(0));
				edge.setWeight(20);
				edge.setThickness(10);
				edge.setEdgeType(EdgeType.DIRECTED);
			}
		}

		StaxGraphWriter graphWriter = new StaxGraphWriter();
		graphWriter.writeToStream(gexf, gexfOutFile, "UTF-8");
	}

	private Color normalColor() {
		return new ColorImpl(0, 0, 0);
	}

	private Color unionColor(int i) {
		Color blue = new ColorImpl(0, 0, 255);
		Color yellow = new ColorImpl(255, 255, 0);
		Color purple = new ColorImpl(160, 32, 240);
		Color[] colors = new Color[] { blue, yellow, purple };
		return colors[i];
	}

}
