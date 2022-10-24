package simulation;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import util.ProbUtil;

public class Element {
//	final static int[] checkingIntervals = new int[] { 1, 5, 10, 60 /* , 5 * 60, 10 * 60 */ };
	final static int[] checkingIntervals = new int[] { 20, 30, 50, 70 /* , 5 * 60, 10 * 60 */ };
	final static int[] recoveryTimes = new int[] { 3 * 60, 5 * 60, 10 * 60, 15 * 60 };

	// Id
	private String name;
	// Interval to check fault
	private int checkingInterval;
	// Probability of encountering fault
	private float faultProbability;
	// Flag of fault
	private int fault;
	// The Timer Seconds when fault
	private long faultTime;
	// Time needed by recovery
	private long recoveryTime;

	private String faultBy;

	private boolean correlationCheck = false;
	private boolean recoverCheck = false;
	private boolean alert = false;

	private List<Element> out = new ArrayList<>();
	private List<Element> in = new ArrayList<>();
	private List<Double> inProb = new ArrayList<>();
	private List<Element> unionIn = new ArrayList<>();

	public Element(String name) {
		this.name = name;
		this.checkingInterval = checkingIntervals[(int) Math.round(Math.random() * 10 % 3)];
//		this.checkingInterval = 60;
//		this.faultProbability = 0.02592*(float) (((Math.random() * 100) % 3) / 3000);
		this.recoveryTime = recoveryTimes[(int) Math.round(Math.random() * 10 % 3)];
//		this.recoveryTime = 300;
//		this.recoveryTime = 1;
		this.faultTime = -1;
	}

	public void postConstruct(JSONObject properties, Domain domain) {
		JSONArray ins = properties.getJSONArray("in");
		JSONArray outs = properties.getJSONArray("out");
		JSONArray unionIn = properties.getJSONArray("union-in");
		float faultProbability = properties.getFloatValue("faultProbability");

		this.faultProbability = faultProbability;

		for (int i = 0; i < ins.size(); i++) {
			String n = ins.getJSONObject(i).getString("name");
			this.in.add(domain.elementsIdx().get(n));
			Double prob = ins.getJSONObject(i).getDouble("prob");
			this.inProb.add(prob);
		}

		for (int i = 0; i < outs.size(); i++) {
			String n = outs.getString(i);
			this.out.add(domain.elementsIdx().get(n));
		}

		for (int i = 0; i < unionIn.size(); i++) {
			String n = unionIn.getString(i);
			this.unionIn.add(domain.elementsIdx().get(n));
		}
	}

	public String name() {
		return name;
	}

	public List<Element> outs() {
		return out;
	}

	public List<Element> ins() {
		return in;
	}

	public List<Element> unionIns() {
		return unionIn;
	}

	public boolean isFault() {
		return fault != 0;
	}

	public boolean isAlert() {
		return alert;
	}

	public void setAlert() {
		this.alert = true;
	}

	public int getCheckingInterval() {
		return checkingInterval;
	}

	/**
	 * Fault because of itself
	 * 
	 * @param time
	 * @return if normal->fault then return <b>true</b> else <b>false</b>
	 */
	public void hitBySelf() {
		if (!this.isFault()) {
			double r = Math.random();
			if (r < this.faultProbability) {
				this.fault = 1;
				this.faultTime = Time.clock.timerSecs();
				this.faultBy = "self";
			}
		}
	}

	public void hitByCorrelations() {
		if (!correlationCheck) {
			this.correlationCheck = true;
			if (!this.isFault()) {
				if (anyDirectElementFault() || anyUnionElementsFault()) {
					this.fault = 1;
					this.faultTime = Time.clock.timerSecs();
					this.faultBy = "correlations";
				}
			}
		}
	}

	public void faultBy(String faultBy) {
		this.faultBy = faultBy;
	}

	public String faultBy() {
		return this.faultBy;
	}

	private boolean anyDirectElementFault() {
		if (in.size() > 0) {
			int i = 0;
			for (; i < in.size(); i++) {
				Element e = in.get(i);
				if (!e.correlationCheck)
					e.hitByCorrelations();
				if (e.isFault() && ProbUtil.hasProbality(inProb.get(i)))
					return true;
			}
			return false;
		}
		return false;
	}

	private boolean anyUnionElementsFault() {
		if (unionIn.size() > 0) {
			for (Element e : unionIn) {
				if (!e.correlationCheck)
					e.hitByCorrelations();
				if (!e.isFault())
					return false;
			}
			return true;
		}
		return false;
	}

	public void tryRecover() {
		if (!recoverCheck) {
			recoverCheck = true;
			if (this.isFault()) {
				long current = Time.clock.timerSecs();
				long duration = current - faultTime;
				if (duration >= recoveryTime) {
					in.forEach(e -> e.tryRecover());
					unionIn.forEach(e -> e.tryRecover());
					if (!hasPreFault())
						recover();
				}
			}
		}
	}

	private boolean hasPreFault() {
		if (in.size() > 0) {
			int i = 0;
			for (; i < in.size(); i++) {
				Element e = in.get(i);
				Double prob = inProb.get(i);
				if (e.isFault() && ProbUtil.hasProbality(prob))
					return true;
			}
		}
		if (unionIn.size() > 0) {
			for (Element e : unionIn) {
				if (!e.isFault())
					return false;
			}
			return true;
		}
		return false;
	}

	private void recover() {
		this.fault = 0;
		this.faultTime = -1;
		this.alert = false;
		this.faultBy = "";
	}

	public void clearRoundCheck() {
		this.recoverCheck = false;
		this.correlationCheck = false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name: ").append(name).append(", faultProbability: ")
				.append(String.format("%.2f", this.faultProbability).toString()).append(", recoveryTime: ")
				.append(this.recoveryTime).append(", checkingInterval: ").append(this.checkingInterval).append("}");
		return sb.toString();
	}

}
