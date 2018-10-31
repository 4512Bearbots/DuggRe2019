package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.Timer;

public class Autonomous {

	public double FORWARD;
	public double TURN;
	public double LIFT;
	public double ATIME;
	public String command;
	
	public Autonomous(String command) {
		this.command = command;
		this.ATIME = Timer.getFPGATimestamp();
		this.FORWARD = this.TURN = this.LIFT = 0;
	}
	
	public void autoPeriodic() {
		switch(this.command) {
		default:
			break;
		}
	}

	public boolean aSchedule(double start, double end) {
		return (Timer.getFPGATimestamp()>=this.ATIME+start&&Timer.getFPGATimestamp()<=this.ATIME+end)?true:false;
	}

	//Quick and dirty methods for making a time-based auto easily
	private void forwardTime(double time, double power) //Time in milliseconds and value you want to pass to the motors
	{
		double start = Timer.getFPGATimestamp();
	}
	private void forwardTime(double time, double power, double feet){
		double counts = 360 / (6 * Math.PI) * 12 * feet - (19.5 / 12);
	}
	//Exactly the same but turning
	private void turnTime(double time, double turn) //Positive is right and negative is left (I think)
	{
		double start = Timer.getFPGATimestamp();
	}
}
