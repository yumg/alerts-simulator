package detection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.google.common.collect.Lists;

import mining.EventSeq;
import util.Config;

public class DataBase {
	public final Logger logger = LoggerFactory.getLogger(DataBase.class);

	private ArrayList<String> allEventTypes;

	private long timeWindowWidth;
	private float timeWindowSplit;
	private long zero;
	private long leftLen;
	private long rightLen;
	private long base;

	private List<String> dbKeys = new ArrayList<String>();
	private Map<String, String> positiveDbs = new HashMap<>();
	private Map<String, String> negtiveDbs = new HashMap<>();
	private Map<String, String> mergeDbs = new HashMap<>();
	private Map<String, String> mergeEqDbs = new HashMap<>();

	public DataBase() {
		EventSeq eventSeq = new EventSeq();
		allEventTypes = Lists.newArrayList(eventSeq.allEventTypes());
		allEventTypes.sort(Comparator.naturalOrder());
		timeWindowWidth = Config.getExperimentTimeWindow() * 1000;
		timeWindowSplit = Config.getExperimentTimeWindowSplit();
		zero = (long) (timeWindowWidth * timeWindowSplit);
		leftLen = zero;
		rightLen = timeWindowWidth - zero;
		base = Math.max(leftLen, rightLen);
	}
	
	public List<String> getDbKeys() {
		return dbKeys;
	}

	public Map<String, String> getPositiveDbs() {
		return positiveDbs;
	}

	public Map<String, String> getNegtiveDbs() {
		return negtiveDbs;
	}

	public Map<String, String> getMergeDbs() {
		return mergeDbs;
	}

	public Map<String, String> getMergeEqDbs() {
		return mergeEqDbs;
	}

	public void init() {
		String workDir = Config.getWorkDir();
		String summaryResult = workDir + "/out/" + Config.getExperimentIndex() + "/summary-result.json";
		Feature[] parseFeatures = { Feature.AllowComment, Feature.AutoCloseSource };
		try {
			JSONObject result = (JSONObject) JSONObject.parseObject(Files.newInputStream(Paths.get(summaryResult)),
					JSONObject.class, parseFeatures);

			JSONObject correlations = result.getJSONObject("correlations");
			Set<String> correlationKeys = correlations.keySet();

			for (String key : correlationKeys) {

				dbKeys.add(key);

				JSONArray cc = correlations.getJSONArray(key);
				HashSet<String> correlation = new HashSet<String>();
				correlation.add(key);
				for (int i = 0; i < cc.size(); i++)
					correlation.add(cc.getString(i));

				String cacheFileName = key + "-" + timeWindowWidth / 1000 + "-" + timeWindowSplit + "-episodes.json";
				String cachePath = workDir + "/cache/" + Config.getExperimentIndex() + "/" + cacheFileName;

				JSONArray cacheArray = (JSONArray) JSONObject.parseObject(Files.newInputStream(Paths.get(cachePath)),
						JSONArray.class, parseFeatures);

				JSONArray positiveSamples = new JSONArray();
				JSONArray negtiveSamples = new JSONArray();

				List<String> positiveCsvLines = new ArrayList<>();
				List<String> negtiveCsvLines = new ArrayList<>();

				for (int i = 0; i < cacheArray.size(); i++) {
					JSONObject episode = cacheArray.getJSONObject(i);
					Set<String> epKeys = episode.keySet();
					StringBuilder csvLine = new StringBuilder();

					allEventTypes.forEach(event -> {
						Long t = episode.getLong(event);
						if (t != null) {
//							Long t1 = timeWindowWidth - t;
//							double t2 = (double) (t1 - zero) / (double) base;
//							csvLine.append(t2 + ",");
							csvLine.append("1,");
						} else
							csvLine.append("0,");
					});

					if (epKeys.containsAll(correlation)) {
						positiveSamples.add(episode);
						csvLine.append("1");
						positiveCsvLines.add(csvLine.toString());
					} else {
						negtiveSamples.add(episode);
						csvLine.append("0");
						negtiveCsvLines.add(csvLine.toString());
					}
				}
				saveFiles(key, positiveCsvLines, negtiveCsvLines);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void saveFiles(String key, List<String> positiveLines, List<String> negtiveLines) {
		String outBase = Config.getWorkDir() + "/cache/" + Config.getExperimentIndex();
		String prefix = key + "-" + timeWindowWidth / 1000 + "-" + timeWindowSplit;

		String positiveCsv = prefix + "-episodes.positive.csv";
		String negtiveCsv = prefix + "-episodes.negtive.csv";
		String mergeCsv = prefix + "-episodes.merge.csv";
		String mergeEqCsv = prefix + "-episodes.eq.csv";

		Path positiveCsvPath = Paths.get(outBase + "/" + positiveCsv);
		try {
			BufferedWriter positiveCsvWriter = Files.newBufferedWriter(positiveCsvPath, Charset.forName("UTF-8"));
			for (String line : positiveLines) {
				positiveCsvWriter.write(line);
				positiveCsvWriter.newLine();
			}
			positiveCsvWriter.close();
			logger.info("Save {}.", positiveCsvPath);
			this.positiveDbs.put(key, positiveCsvPath.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path negtiveCsvPath = Paths.get(outBase + "/" + negtiveCsv);
		try {
			BufferedWriter negtiveCsvWriter = Files.newBufferedWriter(negtiveCsvPath, Charset.forName("UTF-8"));
			for (String line : negtiveLines) {
				negtiveCsvWriter.write(line);
				negtiveCsvWriter.newLine();
			}
			negtiveCsvWriter.close();
			logger.info("Save {}.", negtiveCsvPath);
			this.negtiveDbs.put(key, negtiveCsvPath.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path mergeCsvPath = Paths.get(outBase + "/" + mergeCsv);
		try {
			BufferedWriter mergeCsvWriter = Files.newBufferedWriter(mergeCsvPath, Charset.forName("UTF-8"));
			for (String line : positiveLines) {
				mergeCsvWriter.write(line);
				mergeCsvWriter.newLine();
			}

			for (String line : negtiveLines) {
				mergeCsvWriter.write(line);
				mergeCsvWriter.newLine();
			}
			mergeCsvWriter.close();
			logger.info("Save {}.", mergeCsvPath);
			this.mergeDbs.put(key, mergeCsvPath.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		Path mergeEqCsvPath = Paths.get(outBase + "/" + mergeEqCsv);
		try {
			BufferedWriter negtiveEqCsvWriter = Files.newBufferedWriter(mergeEqCsvPath, Charset.forName("UTF-8"));
			int min = Math.min(positiveLines.size(), negtiveLines.size());
			for (int i = 0; i < min; i++) {
				negtiveEqCsvWriter.write(positiveLines.get(i));
				negtiveEqCsvWriter.newLine();
				negtiveEqCsvWriter.write(negtiveLines.get(i));
				negtiveEqCsvWriter.newLine();
			}
			negtiveEqCsvWriter.close();
			logger.info("Save {}.", mergeEqCsvPath);
			this.mergeEqDbs.put(key, mergeEqCsvPath.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		DataBase dataProcessor = new DataBase();
		dataProcessor.init();
	}

}
