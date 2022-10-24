package util;

import java.util.TreeSet;

public class TreeSet2 extends TreeSet<String> implements Comparable<Object> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean equals(Object o) {
		if (o instanceof TreeSet2) {
			TreeSet2 t = (TreeSet2) o;
			String o_str = String.join(",", t);
			String this_str = String.join(",", this);
			return o_str.equals(this_str);
		} else
			return super.equals(o);
	}

	@Override
	public int compareTo(Object o) {
		if (o instanceof TreeSet2) {
			TreeSet2 t = (TreeSet2) o;
			String o_str = String.join(",", t);
			String this_str = String.join(",", this);
			return o_str.equals(this_str) ? 0 : 1;
		} else
			return 1;
	}

}