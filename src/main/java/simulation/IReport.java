package simulation;

public interface IReport {
	void record(Event event);
	void close();
}
