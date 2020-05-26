package simulation;

import java.util.Date;

public class Time {
	private Date base;
	private long timer;
	private long aliveTime;

	public Time(Date base, long aliveTime) {
		this.base = base;
		this.aliveTime = aliveTime;
		this.timer = 0;
	}

	public void dida(IRound round) {
		while (timer++ < aliveTime) {
			round.run(this);
		}
	}

	public static void main(String[] args) {
		Time time = new Time(new Date(1580301961000L), 60);
		System.out.println(time.base);
	}

}
