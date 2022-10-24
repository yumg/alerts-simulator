package util;

public class ArrayUtil {
	static public Long[][] transposeMartrix(Long[][] arg) {
		if (arg.length == 0)
			return new Long[0][];
		int l1 = arg.length;
		int l2 = arg[0].length;
		Long[][] rtv = new Long[l2][l1];
		for (int i = 0; i < l2; i++)
			rtv[i] = new Long[l1];

		for (int i = 0; i < l1; i++)
			for (int j = 0; j < l2; j++)
				rtv[j][i] = arg[i][j];

		return rtv;
	}
}
