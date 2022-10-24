package util;

public class JMeasureUtil {
	public static double jmeasureXy(double pX, double pY, double pXY) {
		double item1 = pXY * log2(pXY / pX);
		double item2 = 0;
		if ((1 - pXY) != 0)
			item2 = (1 - pXY) * log2((1 - pXY) / (1 - pX));
		return item1 + item2;
	}

	public static double jmeasureXy(double[] p) {
		return jmeasureXy(p[0], p[1], p[2]);
	}

	public static double JmeasureXy(double pX, double pY, double pXY) {
		return pY * jmeasureXy(pX, pY, pXY);
	}
	
	public static double JmeasureXy(double[] p) {
		return p[1] * jmeasureXy(p);
	}

	private static double log2(double d) {
		return Math.log(d) / Math.log((double) 2);
	}

	public static void main(String[] args) {

		double pXY = 0.875;
		double pX = 0.45;
		double pY = 0.4;
		System.out.println(jmeasureXy(pX, pY, pXY));
		System.out.println(JmeasureXy(pX, pY, pXY));
		
		pXY = 0.5285;
		pY = 0.7;
		System.out.println(jmeasureXy(pX, pY, pXY));
		System.out.println(JmeasureXy(pX, pY, pXY));
		
		pXY = 0.9;
		pY = 0.3;
		System.out.println(jmeasureXy(pX, pY, pXY));
		System.out.println(JmeasureXy(pX, pY, pXY));
		
		
		pXY = 0.75;
		pX = 0.55;
		pY = 0.4;
		System.out.println(jmeasureXy(pX, pY, pXY));
		System.out.println(JmeasureXy(pX, pY, pXY));
		
		pXY = 1.0;
		pX = 0.55;
		pY = 0.2;
		System.out.println(jmeasureXy(pX, pY, pXY));
		System.out.println(JmeasureXy(pX, pY, pXY));
		
		pXY = 0.5625;
		pX = 0.45;
		pY = 0.8;
		System.out.println(jmeasureXy(pX, pY, pXY));
		System.out.println(JmeasureXy(pX, pY, pXY));
	}
}
