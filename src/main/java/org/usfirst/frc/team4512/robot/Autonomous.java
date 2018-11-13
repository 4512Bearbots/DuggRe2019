package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.Timer;

public class Autonomous {

	public static double FORWARD;
	public static double TURN;
	public static double LIFT;
	public static double ATIME;
	public static String command;
	
	public Autonomous(String command) {
		Autonomous.command = command;
		Autonomous.ATIME = Timer.getFPGATimestamp();
		Autonomous.FORWARD = Autonomous.TURN = Autonomous.LIFT = 0;
		Input.init();
	}
	
	public static void autoPeriodic() {
		switch(Autonomous.command) {
		case "Test":
			break;
		default:
			break;
		}
	}

	public static boolean aSchedule(double start, double end) {
		return (Timer.getFPGATimestamp()>=ATIME+start&&Timer.getFPGATimestamp()<=ATIME+end)?true:false;
	}
}
