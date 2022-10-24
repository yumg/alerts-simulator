package mining.crosscorrelation;

import com.alibaba.fastjson.JSONObject;

public class CrossCorrelation {
	public double correlation(double[] x, double[] y) {
		double[] product = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			double[] x1 = circleSlide(x, i);
			product[i] = dotProduct(x1, y);
		}
		return filterMax(product);
	}

	private double dotProduct(double[] x, double[] y) {
		double sum = 0;
		for (int i = 0; i < x.length; i++)
			sum += x[i] * y[i];
		return sum;
	}

	private double[] circleSlide(double[] x, int dis) {
		double[] rtv = new double[x.length];
		int i = 0;
		for (; i < dis; i++)
			rtv[i] = x[x.length - dis + i];

		int j = 0;
		for (; i < rtv.length; i++)
			rtv[i] = x[j++];
		return rtv;
	}

	private double filterMax(double[] d) {
		double max = Double.MIN_VALUE;
		for (double dd : d)
			max = Math.max(max, dd);
		return max;
	}

	public static void main(String[] args) {
		CrossCorrelation crossCorrelation = new CrossCorrelation();
		double[] d = new double[] { 1, 2, 3 };
		System.out.println(JSONObject.toJSONString(d));
		System.out.println(JSONObject.toJSONString(crossCorrelation.circleSlide(d, 3)));

	}
}
