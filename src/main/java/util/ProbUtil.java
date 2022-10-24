package util;

public class ProbUtil {
	
	public static boolean hasProbality(double prob) {
		double r = Math.random();
		if (r < prob)
			return true;
		else
			return false;
	}
	
}
