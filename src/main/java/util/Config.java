package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

public class Config {
	static private Properties props;
	static private String postfix;
	static private JSONArray structureRaw;
	static {
		InputStream resourceAsStream = Config.class.getClassLoader().getResourceAsStream("config.properties");
		props = new Properties();
		try {
			props.load(resourceAsStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		postfix = "@" + simpleDateFormat.format(new Date(System.currentTimeMillis()));

		String structureFilePath = Config.getWorkDir() + "/input/" + Config.getStructureFile();
		Feature[] parseFeatures = { Feature.AllowComment, Feature.AutoCloseSource };
		try {
			structureRaw = (JSONArray) JSONObject.parseObject(Files.newInputStream(Paths.get(structureFilePath)),
					JSONArray.class, parseFeatures);
		} catch (IOException e) {
			e.printStackTrace();
		}

		StringBuilder info = new StringBuilder();
		info.append("Work dir: ").append(getWorkDir());
		info.append("\nInputs from: ").append(Config.getWorkDir()).append("/input/");
		info.append("\nOutputs to: ").append(Config.getWorkDir()).append("/out/").append(getRuntimeIndexName());
		info.append("\nExperiment index source: http://").append(Config.getElasticsearchHost()).append("/")
				.append(Config.getExperimentIndex());
		System.out.println(info + "\n");
	}

	static public String getWorkDir() {
		return props.getProperty("work.dir");
	}

	static public String getStaticIndexName() {
		return props.getProperty("simulation.out.index.name");
	}

	static public Long getSimulationTimeBase() {
		return Long.valueOf(props.getProperty("simulation.time.base"));
	}

	static public String getRuntimeIndexName() {
		return getStaticIndexName() + postfix;
	}

	static public String getStructureFile() {
		return props.getProperty("work.input.structure-file");
	}

	static public String getRuntimePostfix() {
		return postfix;
	}

	static public JSONArray getStructureDefination() {
		return structureRaw;
	}

	static public String getExperimentIndex() {
		return props.getProperty("experiment.index.name");
	}

	static public String getElasticsearchHost() {
		return props.getProperty("experiment.es.host");
	}

	static public Long getExperimentTimeWindow() {
		return Long.valueOf(props.getProperty("experiment.timewindow"));
	}

	static public Float getExperimentTimeWindowSplit() {
		return Float.valueOf(props.getProperty("experiment.timewindow.split"));
	}

	static public Double getExperimentOutlierThreshold() {
		return Double.valueOf(props.getProperty("experiment.outlier.threshold"));
	}

	static public Integer getExperimentOutlierKn() {
		return Integer.valueOf(props.getProperty("experiment.outlier.kn"));
	}

	static public Integer getExperimentDuration() {
		return Integer.valueOf(props.getProperty("simulation.duration"));
	}

}
