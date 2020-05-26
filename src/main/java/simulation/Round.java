package simulation;

public class Round implements IRound {
	private Domain domain;

	public Round(Domain domain) {
		this.domain = domain;
	}

	@Override
	public void run(Time time) {
		// TODO Auto-generated method stub
		domain.getEventSourcesIdx();

	}

}
