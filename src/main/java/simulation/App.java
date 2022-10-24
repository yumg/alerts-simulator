package simulation;

public class App {
	public static void main(String[] args) {
		Domain domain = new Domain();
		IReport reports = new Reports();
		Time.clock.dida(new Round(domain, reports));
		reports.close();
	}
}
