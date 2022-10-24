package util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author wilsonact
 * 
 */
public class DataNode {
	private String nodeName; // 样本点名
	private int nodeIdx;
	private double[] dimensioin; // 样本点的维度
	private double kDistance; // k-距离
	private List<DataNode> kNeighbor = new ArrayList<DataNode>();// k-领域
	private double distance; // 到给定点的欧几里得距离
	private double reachDensity;// 可达密度
	private double reachDis;// 可达距离

	private double lof;// 局部离群因子

	public DataNode(int nodeIndex, double[] dimension) {
		this.nodeIdx = nodeIndex;
		this.dimensioin = dimension;
		this.nodeName = String.valueOf(nodeIdx);
	}

	public DataNode(String nodeName, double[] dimension) {
		this.nodeName = nodeName;
		this.dimensioin = dimension;
	}

	public String getNodeName() {
		return nodeName;
	}

	public double[] getDimensioin() {
		return dimensioin;
	}

	public double getkDistance() {
		return kDistance;
	}

	public List<DataNode> getkNeighbor() {
		return kNeighbor;
	}

	public int getNodeIndex() {
		return nodeIdx;
	}

	public void setkNeighbor(List<DataNode> kNeighbor) {
		this.kNeighbor = kNeighbor;
	}

	public double getDistance() {
		return distance;
	}

	public void setkDistance(double kDistance) {
		this.kDistance = kDistance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getReachDensity() {
		return reachDensity;
	}

	public void setReachDensity(double reachDensity) {
		this.reachDensity = reachDensity;
	}

	public double getReachDis() {
		return reachDis;
	}

	public void setReachDis(double reachDis) {
		this.reachDis = reachDis;
	}

	public double getLof() {
		return lof;
	}

	public void setLof(double lof) {
		this.lof = lof;
	}

}
