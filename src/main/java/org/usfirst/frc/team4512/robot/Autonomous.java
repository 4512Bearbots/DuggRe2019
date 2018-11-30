package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous {

	public static double forward = 0;
	public static double turn = 0;
	public static double lift = 0;
	public static double aTime;
	public static String command;

	public static void autoInit() {
		Input.reset();
	}

	public static void autoPeriodic() {
		double angle = (Input.getAngle());
		switch(Autonomous.command) {
		case "test":
			double diff = 180 - Math.abs(angle - 180);
			SmartDashboard.putNumber("AutoDiff", diff);
			if(diff<15) turn = 0;
			else {
				if(diff<180){
					turn = (0.3*(-diff/180)-0.5);
				} else {
					turn = (0.3*(diff/180)+0.5);
				}
			}
			if(Input.getRightX()!=0) turn = Input.getRightX();
			SmartDashboard.putNumber("AutoTurn", turn);
			MotorBase.setDrive(forward, turn);
			break;
		default:
			break;
		}
	}

	public static boolean aSchedule(double start, double end) {
		return (Timer.getFPGATimestamp()>=aTime+start&&Timer.getFPGATimestamp()<=aTime+end)?true:false;
	}
}
