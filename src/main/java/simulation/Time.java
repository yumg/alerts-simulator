package simulation;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.Config;

public class Time {
	static private Logger logger = LoggerFactory.getLogger(Time.class);

	private Date base;
	private long baseSecs; // seconds
	private long timer; // seconds
	private long aliveTime; // seconds

	// Wed Jan 29 20:46:01 CST 2020
	// 30 Days
	public static final Time clock = new Time(new Date(1580301961000L), 60 * 60 * 24 * Config.getExperimentDuration());

	private Time(Date base, long aliveTime) {
		this.base = base;
		this.aliveTime = aliveTime;
		this.timer = 0;
		this.baseSecs = base.getTime() / 1000;
	}

	public void dida(IRound round) {
		int dot = 10000, dotLine = 100 * dot;
		System.out.println("Start ^_^");
		while (timer++ < aliveTime) {
			logger.debug(String.valueOf(timer));
			round.run();
			if (timer % dot == 0)
				System.out.print(".");
			if (timer % dotLine == 0)
				System.out.print("\n");
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String from = simpleDateFormat.format(base);
		String to = simpleDateFormat.format(new Date(base.getTime() + aliveTime * 1000));
		System.out.println("\nComplete! From " + from + " to " + to + ". Total "
				+ NumberFormat.getNumberInstance(Locale.US).format(aliveTime) + " rounds.");
	}

	public Date timestamp() {
		return new Date((this.baseSecs + this.timer) * 1000);
	}

	public long timerSecs() {
		return timer;
	}

	public static void main(String[] args) {
		Time time = new Time(new Date(1580301961000L), 60);
		System.out.println(time.base);
	}

}
