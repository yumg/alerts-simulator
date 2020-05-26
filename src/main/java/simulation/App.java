package simulation;

public class App {
	public static void main(String[] args) {
		Domain domain = new Domain();
		Time time = new Time(null, 1L);
		time.dida(new Round(domain));
	}
}
