package mining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import util.Config;

public class Reports {
	private String outBase;
	private BufferedWriter occurNumbersOutFile;
	private BufferedWriter centralRatesOutFile;
	private BufferedWriter integrationRatesOutFile;
	private BufferedWriter summaryOutFile;

	public Reports() {
		String target = Config.getExperimentIndex();
		outBase = Config.getWorkDir() + "/out/" + target;
		File file = new File(outBase);
		if (!file.exists())
			file.mkdirs();

		if (!file.isDirectory())
			throw new RuntimeException("File Path: '" + outBase + "' exists and it is not a directory");

		Path occurNumbersPath = Paths.get(outBase + "/occurNumbers.csv");
		try {
			occurNumbersOutFile = Files.newBufferedWriter(occurNumbersPath, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path centralRatesPath = Paths.get(outBase + "/centralRates.csv");
		try {
			centralRatesOutFile = Files.newBufferedWriter(centralRatesPath, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path integrationRatesPath = Paths.get(outBase + "/integrationRates.csv");
		try {
			integrationRatesOutFile = Files.newBufferedWriter(integrationRatesPath, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path summaryResultPath = Paths.get(outBase + "/summary-result.json");
		try {
			summaryOutFile = Files.newBufferedWriter(summaryResultPath, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reportSummaryResult(Map<String, Set<String>> correlations, Map<String, Object> params) {
		JSONObject result = new JSONObject();
		result.put("params", params);
		result.put("correlations", correlations);
		try {
			this.summaryOutFile.write(result.toString(SerializerFeature.PrettyFormat));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reportEvaluateResult(Map<String, Object> er) {
		Path evaluatesPath = Paths.get(outBase + "/evaluates." + (new Date()).getTime());
		try {
			BufferedWriter evaluatesFile = Files.newBufferedWriter(evaluatesPath, Charset.forName("UTF-8"));
			for (Iterator<Entry<String, Object>> iterator = er.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, Object> next = iterator.next();
				evaluatesFile.write(next.getKey() + ":\t");
				evaluatesFile.write(next.getValue().toString() + "\n");
			}
			evaluatesFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void report0(BufferedWriter fileWriter, Map<String, double[]> metrics, String[] eventsTypes) {
		String header = StringUtils.join(eventsTypes, ",");
		try {
			fileWriter.write("#," + header + "\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		metrics.forEach((k, v) -> {
			try {
				fileWriter.write(k + "," + StringUtils.join(v, ',') + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		try {
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reportCentralRates(Map<String, double[]> centralRates, String[] eventsTypes) {
		report0(centralRatesOutFile, centralRates, eventsTypes);
	}

	public void reportOccurNumbers(Map<String, double[]> occurNumbers, String[] eventsTypes) {
		report0(occurNumbersOutFile, occurNumbers, eventsTypes);
	}

	public void reportIntegrationRates(Map<String, double[]> integrationRates, String[] eventsTypes) {
		report0(integrationRatesOutFile, integrationRates, eventsTypes);
	}

	public void close() {
		try {
			this.occurNumbersOutFile.close();
			this.centralRatesOutFile.close();
			this.integrationRatesOutFile.close();
			this.summaryOutFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
