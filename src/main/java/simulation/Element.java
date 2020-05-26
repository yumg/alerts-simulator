package simulation;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Element {
	final static int[] checkingIntervals = new int[] { 1, 5, 10, 1 * 60, 5 * 60, 10 * 60 };
	final static int[] recoveryTimes = new int[] { 3 * 60, 5 * 60, 10 * 60, 15 * 60 };

	private String name;
	private int checkingInterval;
	private float pf;
	private int fault;
	private int faultTime;
	private int recoveryTime;

	private List<Element> out = new ArrayList<>();
	private List<Element> in = new ArrayList<>();
	private List<Element> unionIn = new ArrayList<>();

	public List<Element> getOut() {
		return out;
	}

	public List<Element> getIn() {
		return in;
	}

	public List<Element> getUnionIn() {
		return unionIn;
	}

	public int status() {
		return fault;
	}

	public void trigger() {

	}

	public Element(String name) {
		this.name = name;
		this.checkingInterval = checkingIntervals[(int) Math.round(Math.random() * 10 % 5)];
		this.pf = (float) ((Math.random() * 100 % 12 + 3) / 100);
		this.recoveryTime = recoveryTimes[(int) Math.round(Math.random() * 10 % 3)];
	}

	public String name() {
		return name;
	}

	public void postConstruct(JSONObject properties, Domain domain) {
		JSONArray ins = properties.getJSONArray("in");
		JSONArray outs = properties.getJSONArray("out");
		JSONArray unionIn = properties.getJSONArray("union-in");

		for (int i = 0; i < ins.size(); i++) {
			String n = ins.getString(i);
			this.in.add(domain.getEventSourcesIdx().get(n));
		}

		for (int i = 0; i < outs.size(); i++) {
			String n = outs.getString(i);
			this.out.add(domain.getEventSourcesIdx().get(n));
		}

		for (int i = 0; i < unionIn.size(); i++) {
			String n = unionIn.getString(i);
			this.unionIn.add(domain.getEventSourcesIdx().get(n));
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{name: ").append(name).append(", pf: ").append(String.format("%.2f", this.pf).toString())
				.append(", rt: ").append(this.recoveryTime).append(", ci: ").append(this.checkingInterval).append("}");
		return sb.toString();
	}

	public static void main(String[] args) {
		for (int i = 0; i < 100; i++) {
			System.out.println((Math.random() * 100 % 12 + 3) / 100);
		}
	}

}
