package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 离群点分析
 * 
 * @author Wilson 算法：基于密度的局部离群点检测（lof算法） 输入：样本集合D，正整数K（用于计算第K距离） 输出：各样本点的局部离群点因子
 *         过程： 1）计算每个对象与其他对象的欧几里得距离 2）对欧几里得距离进行排序，计算第k距离以及第K领域 3）计算每个对象的可达密度
 *         4）计算每个对象的局部离群点因子 5）对每个点的局部离群点因子进行排序，输出。
 **/
public class OutlierDetect {

	private int INT_K = 4;// 正整数K

	public void setK(int int_k) {
		this.INT_K = int_k;
	}

	// 1.找到给定点与其他点的欧几里得距离
	// 2.对欧几里得距离进行排序，找到前5位的点，并同时记下k距离
	// 3.计算每个点的可达密度
	// 4.计算每个点的局部离群点因子
	// 5.对每个点的局部离群点因子进行排序，输出。
	public List<DataObject> getOutlierNode(List<DataObject> allNodes) {

		List<DataObject> kdAndKnList = getKDAndKN(allNodes);
		calReachDis(kdAndKnList);
		calReachDensity(kdAndKnList);
		calLof(kdAndKnList);
		// 降序排序
		Collections.sort(kdAndKnList, new LofComparator());

		return kdAndKnList;
	}

	/**
	 * 计算每个点的局部离群点因子
	 * 
	 * @param kdAndKnList
	 */
	private void calLof(List<DataObject> kdAndKnList) {
		for (DataObject node : kdAndKnList) {
			List<DataObject> tempNodes = node.kNeighbor;
			double sum = 0.0;
			for (DataObject tempNode : tempNodes) {
				double rd = getRD(tempNode.name, kdAndKnList);
				sum = rd / node.reachDensity + sum;
			}
			sum = sum / (double) INT_K;
			node.lof = sum;
		}
	}

	/**
	 * 计算每个点的可达距离
	 * 
	 * @param kdAndKnList
	 */
	private void calReachDensity(List<DataObject> kdAndKnList) {
		for (DataObject node : kdAndKnList) {
			List<DataObject> tempNodes = node.kNeighbor;
			double sum = 0.0;
			double rd = 0.0;
			for (DataObject tempNode : tempNodes) {
				sum = tempNode.reachDis + sum;
			}
			rd = (double) INT_K / sum;
			node.reachDensity = rd;
		}
	}

	/**
	 * 计算每个点的可达密度,reachdis(p,o)=max{ k-distance(o),d(p,o)}
	 * 
	 * @param kdAndKnList
	 */
	private void calReachDis(List<DataObject> kdAndKnList) {
		for (DataObject node : kdAndKnList) {
			List<DataObject> tempNodes = node.kNeighbor;
			for (DataObject tempNode : tempNodes) {
				// 获取tempNode点的k-距离
				double kDis = getKDis(tempNode.name, kdAndKnList);
				// reachdis(p,o)=max{ k-distance(o),d(p,o)}
				if (kDis < tempNode.distance) {
					tempNode.reachDis = tempNode.distance;
				} else {
					tempNode.reachDis = kDis;
				}
			}
		}
	}

	/**
	 * 获取某个点的k-距离（kDistance）
	 * 
	 * @param nodeName
	 * @param nodeList
	 * @return
	 */
	private double getKDis(String nodeName, List<DataObject> nodeList) {
		double kDis = 0;
		for (DataObject node : nodeList) {
			if (nodeName.trim().equals(node.name.trim())) {
				kDis = node.distance;
				break;
			}
		}
		return kDis;

	}

	/**
	 * 获取某个点的可达距离
	 * 
	 * @param nodeName
	 * @param nodeList
	 * @return
	 */
	private double getRD(String nodeName, List<DataObject> nodeList) {
		double kDis = 0;
		for (DataObject node : nodeList) {
			if (nodeName.trim().equals(node.name.trim())) {
				kDis = node.reachDensity;
				break;
			}
		}
		return kDis;

	}

	/**
	 * 计算给定点NodeA与其他点NodeB的欧几里得距离（distance）,并找到NodeA点的前5位NodeB，然后记录到NodeA的k-领域（kNeighbor）变量。
	 * 同时找到NodeA的k距离，然后记录到NodeA的k-距离（kDistance）变量中。 处理步骤如下：
	 * 1,计算给定点NodeA与其他点NodeB的欧几里得距离，并记录在NodeB点的distance变量中。
	 * 2,对所有NodeB点中的distance进行升序排序。 3,找到NodeB点的前5位的欧几里得距离点，并记录到到NodeA的kNeighbor变量中。
	 * 4,找到NodeB点的第5位距离，并记录到NodeA点的kDistance变量中。
	 * 
	 * @param allNodes
	 * @return List<Node>
	 */
	private List<DataObject> getKDAndKN(List<DataObject> allNodes) {
		List<DataObject> kdAndKnList = new ArrayList<DataObject>();
		for (int i = 0; i < allNodes.size(); i++) {
			List<DataObject> tempNodeList = new ArrayList<DataObject>();
			DataObject nodeA = new DataObject(allNodes.get(i).index, allNodes.get(i).name, allNodes.get(i).dimension);
			// 1,找到给定点NodeA与其他点NodeB的欧几里得距离，并记录在NodeB点的distance变量中。
			for (int j = 0; j < allNodes.size(); j++) {
				DataObject nodeB = new DataObject(allNodes.get(j).index, allNodes.get(j).name,
						allNodes.get(j).dimension);
				// 计算NodeA与NodeB的欧几里得距离(distance)
				double tempDis = getDis(nodeA, nodeB);
				nodeB.distance = tempDis;
				tempNodeList.add(nodeB);
			}

			// 2,对所有NodeB点中的欧几里得距离（distance）进行升序排序。
			Collections.sort(tempNodeList, new DistComparator());
			for (int k = 1; k < INT_K; k++) {
				// 3,找到NodeB点的前5位的欧几里得距离点，并记录到到NodeA的kNeighbor变量中。
				nodeA.kNeighbor.add(tempNodeList.get(k));
				if (k == INT_K - 1) {
					// 4,找到NodeB点的第5位距离，并记录到NodeA点的kDistance变量中。
					nodeA.kDistance = tempNodeList.get(k).distance;
				}
			}
			kdAndKnList.add(nodeA);
		}

		return kdAndKnList;
	}

	/**
	 * 计算给定点A与其他点B之间的欧几里得距离。 欧氏距离的公式： d=sqrt( ∑(xi1-xi2)^2 ) 这里i=1,2..n
	 * xi1表示第一个点的第i维坐标,xi2表示第二个点的第i维坐标 n维欧氏空间是一个点集,它的每个点可以表示为(x(1),x(2),...x(n)),
	 * 其中x(i)(i=1,2...n)是实数,称为x的第i个坐标,两个点x和y=(y(1),y(2)...y(n))之间的距离d(x,y)定义为上面的公式.
	 * 
	 * @param A
	 * @param B
	 * @return
	 */
	private double getDis(DataObject A, DataObject B) {
		double dis = 0.0;
		double[] dimA = A.dimension;
		double[] dimB = B.dimension;
		if (dimA.length == dimB.length) {
			for (int i = 0; i < dimA.length; i++) {
				double temp = Math.pow(dimA[i] - dimB[i], 2);
				dis = dis + temp;
			}
			dis = Math.pow(dis, 0.5);
		}
		return dis;
	}

	/**
	 * 升序排序
	 * 
	 */
	class DistComparator implements Comparator<DataObject> {
		public int compare(DataObject A, DataObject B) {
			// return A.getDistance() - B.getDistance() < 0 ? -1 : 1;

//			if ((A.distance - B.distance) < 0)
//				return -1;
//			else if ((A.distance - B.distance) > 0)
//				return 1;
//			else
//				return 0;

			if (A.distance < B.distance)
				return -1;
			else if (A.distance > B.distance)
				return 1;
			else
				return 0;

		}
	}

	/**
	 * 降序排序
	 * 
	 */
	class LofComparator implements Comparator<DataObject> {
		public int compare(DataObject A, DataObject B) {
			// return A.getLof() - B.getLof() < 0 ? 1 : -1;
			if ((A.lof - B.lof) < 0)
				return 1;
			else if ((A.lof - B.lof) > 0)
				return -1;
			else
				return 0;
		}
	}

	public static void main(String[] args) {
		OutlierDetect outlierDetect = new OutlierDetect();

		double[] i = new double[] { 33.934440651479015, 28.655615079847614, 32.87177279679312, 35.44522987508627,
				30.923568545955423, 44.40555501095897, 61.48476258155192, 32.96997524559368, 34.53164557616153,
				36.57672937525179, 34.23710062931264, 29.101240284539266, 22.243400356204162, 1451.6743660864508,
				25.285620489251542, 31.05160781308729, 35.31474896073644, 28.029405393666536 };
		outlierDetect.setK(i.length);
		int[] label = outlierDetect.label(i, 3);
		System.out.println(label);
	}

	public int[] label(double[] input, double threshold) {
		List<DataObject> inputList = new ArrayList<DataObject>();
		for (int i = 0; i < input.length; i++) {
			DataObject dataNode = new DataObject(i, new double[] { input[i] });
			inputList.add(dataNode);
		}
		List<DataObject> outlierNode = getOutlierNode(inputList);
		int[] label = new int[input.length];
		outlierNode.forEach(node -> {
			if (node.lof > threshold)
				label[node.index] = 1;
		});
		return label;
	}

	static public int[] label(double[] input) {
		OutlierDetect outlierNodeDetect = new OutlierDetect();
		return outlierNodeDetect.label(input, 2);
	}

	static public List<Integer> labelIdx(double[] input) {
		OutlierDetect outlierDetect = new OutlierDetect();
		if (Config.getExperimentOutlierKn() != -1)
			outlierDetect.setK(Config.getExperimentOutlierKn());
		else
			outlierDetect.setK(input.length);
		int[] label = outlierDetect.label(input, Config.getExperimentOutlierThreshold());
		List<Integer> rtv = new ArrayList<Integer>();
		for (int i = 0; i < label.length; i++) {
			if (label[i] == 1)
				rtv.add(i);
		}
		return rtv;
	}

	static public List<String> labelName(Map<String, Double> input) {
		OutlierDetect outlierDetect = new OutlierDetect();
		List<DataObject> listInput = new ArrayList<DataObject>();
		input.forEach((name, value) -> {
			listInput.add(outlierDetect.new DataObject(0, name, new double[] { value }));
		});
		List<DataObject> outlierNode = outlierDetect.getOutlierNode(listInput);
		ArrayList<String> rtv = new ArrayList<String>();
		Iterator<DataObject> itr = outlierNode.iterator();
		while (itr.hasNext()) {
			DataObject next = itr.next();
			if (next.lof > Config.getExperimentOutlierThreshold())
				rtv.add(next.name);

		}
		return rtv;
	}

	class DataObject {
		private String name; // 样本点名
		private int index;
		private double[] dimension; // 样本点的维度
		@SuppressWarnings("unused")
		private double kDistance; // k-距离
		private List<DataObject> kNeighbor = new ArrayList<DataObject>();// k-领域
		private double distance; // 到给定点的欧几里得距离
		private double reachDensity;// 可达密度
		private double reachDis;// 可达距离

		private double lof;// 局部离群因子

		public DataObject(int index, double[] dimension) {
			this.index = index;
			this.name = String.valueOf(index);
			this.dimension = dimension;
		}

		public DataObject(int index, String name, double[] dimension) {
			this.index = index;
			this.name = name;
			this.dimension = dimension;
		}

	}

}
