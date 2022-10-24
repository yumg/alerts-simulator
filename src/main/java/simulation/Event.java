package simulation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.alibaba.fastjson.JSONObject;

public class Event {
	private JSONObject internal;

	public Event(Date timestamp, String source, String cause) {
		internal = new JSONObject();
		internal.put("@timestamp", timestamp);
		internal.put("source", source);
		internal.put("cause", cause);
	}

	public String toString() {
		return internal.toJSONString();
	}
	
	
	public String toString2() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
		internal.put("localtime", simpleDateFormat.format((Date)internal.get("@timestamp")));
		return internal.toJSONString();
	}
}
