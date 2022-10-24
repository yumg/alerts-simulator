package mining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import util.Config;
import util.TreeSet2;

public class Evaluation extends AbstractEvaluation {
	private JSONObject report;

	@Override
	protected List<TreeSet2> resolveResult() {
		List<TreeSet2> result = new ArrayList<TreeSet2>();

		String resultPath = Config.getWorkDir() + "/out/" + Config.getExperimentIndex() + "/summary-result.json";
		Feature[] parseFeatures = { Feature.AllowComment, Feature.AutoCloseSource };
		try {
			report = (JSONObject) JSONObject.parseObject(Files.newInputStream(Paths.get(resultPath)), JSONObject.class,
					parseFeatures);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject correlations = report.getJSONObject("correlations");
		Iterator<String> itr = correlations.keySet().iterator();
		while (itr.hasNext()) {
			String head = itr.next();
			JSONArray arr = correlations.getJSONArray(head);
			TreeSet2 set = new TreeSet2();
			set.add(head);
			for (int i = 0; i < arr.size(); i++)
				set.add(arr.getString(i));
			if (!result.contains(set))
				result.add(set);
		}

		return result;
	}

}
