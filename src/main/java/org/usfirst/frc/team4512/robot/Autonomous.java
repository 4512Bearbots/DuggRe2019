package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.Timer;

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
		double angle = (Input.gyro.getAngle()%360);
		switch(Autonomous.command) {
		case "Test":
			if(aSchedule(0,2)){
				Input.gyro.calibrate();
			}else{
				MotorBase.dSpeed = 0.3;
				double diff = 180 - Math.abs(Math.abs(-angle) - 180);
				if(diff<10) turn = 0;
				else if((angle)<(360-angle)){

				}
			}
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
