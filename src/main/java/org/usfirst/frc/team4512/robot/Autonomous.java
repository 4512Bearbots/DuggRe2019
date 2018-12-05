package org.usfirst.frc.team4512.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous {

	public static double forward = 0;
	public static double turn = 0;
	public static double lift = 0;
	public static double aTime;
	public static double last = 0;
	public static String command;

	public static void autoInit() {
		MotorBase.dLeftF.setNeutralMode(NeutralMode.Coast);
		MotorBase.dLeftB.setNeutralMode(NeutralMode.Coast);
		MotorBase.dRightF.setNeutralMode(NeutralMode.Coast);
		MotorBase.dRightB.setNeutralMode(NeutralMode.Coast);
		aTime = Timer.getFPGATimestamp();
	}

	public static void autoPeriodic() {
		switch(Autonomous.command) {
		case "test":
			testAuto(0);
			break;
		default:
			break;
		}
	}

	public static boolean aSchedule(double start, double end) {
		return (Timer.getFPGATimestamp()>=start && Timer.getFPGATimestamp()<=end)?true:false;
	}

	public static void testAuto(double heading){
		heading = Input.constrainAngle(heading+180);
		double angle = (Input.getAngle());
		double diff = 180 - Math.abs(angle - heading);
		diff = Input.constrainAngle(diff);
		SmartDashboard.putNumber("AutoDiff", diff);
		if(diff<3) {
			turn = 0;
			last = Timer.getFPGATimestamp();
			MotorBase.setArms(0);
		} else {
			if(aSchedule(last, last+2)){
				MotorBase.setArms(1);
			} else {
				MotorBase.setArms(0);
				if(angle<180){
					turn = (0.4*(-diff/180)-0.6);
				} else {
					turn = (0.4*(diff/180)+0.6);
				}
			}
			
		}
		SmartDashboard.putNumber("AutoTurn", turn);
		MotorBase.setDrive(forward, turn);
	}
}
