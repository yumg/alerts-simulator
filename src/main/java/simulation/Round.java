package simulation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Round implements IRound {
	static private Logger logger = LoggerFactory.getLogger(Round.class);

	private Domain domain;
	private IReport report;

	public Round(Domain domain, IReport report) {
		this.domain = domain;
		this.report = report;
	}

	@Override
	public void run() {
		List<Element> elements = domain.elementsList();
		for (Element e : elements)
			e.tryRecover();

		for (Element e : elements)
			e.hitBySelf();

		for (Element e : elements)
			e.hitByCorrelations();

		for (Element e : elements) {
			long timerSecs = Time.clock.timerSecs();
			if(e.isFault() && !e.isAlert())
				if (timerSecs % e.getCheckingInterval() == 0 /* && e.isFault() && !e.isAlert() */) {
				report.record(new Event(Time.clock.timestamp(), e.name(), e.faultBy()));
				e.setAlert();
			}
			e.clearRoundCheck();
		}
		logger.debug("Fault elements: " + domain.faultElementsQuality());
	}
}
