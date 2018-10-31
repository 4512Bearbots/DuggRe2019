package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.Timer;

public class Autonomous {

	public double FORWARD;
	public double TURN;
	public double ATIME;
	public String command;
	
	public Autonomous(String command) {
		// TODO Auto-generated constructor stub
		this.command = command;
		this.ATIME = Timer.getFPGATimestamp();
	}
	
	public void calc() {
		switch(this.command) {
		default:
			break;
		}
	}

	public boolean aSchedule(double time) {
		return (Timer.getFPGATimestamp()<=this.ATIME+time)?true:false;
	}
}
