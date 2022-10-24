package mining;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IEventSeq {
	public Set<String> allEventTypes();

	/**
	 * 
	 * @param eventType
	 * @param windowSize
	 * @param split
	 * @return
	 * 
	 * List[window-1,window-2,...] 
	 * <br>
	 * window-n-> 
	 * <p>
	 * <pre>
	 * {
	 *    "source-1":ts0,
	 *    "source-2":ts1,
	 *    "source-3":ts2,
	 *    ...
	 * }
	 * </pre>
	 * </p>
	 */
	public List<Map<String, Long>> eventWindows(String eventType, long windowSize, float split);

}
