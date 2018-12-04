package org.usfirst.frc.team4512.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;

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
		MotorBase.dLeftF.setNeutralMode(NeutralMode.Coast);
		MotorBase.dLeftB.setNeutralMode(NeutralMode.Coast);
		MotorBase.dRightF.setNeutralMode(NeutralMode.Coast);
		MotorBase.dRightB.setNeutralMode(NeutralMode.Coast);
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
			SmartDashboard.putNumber("AutoTurn", turn);
			MotorBase.setArms(turn);
			break;
		default:
			break;
		}
	}

	public static boolean aSchedule(double start, double end) {
		return (Timer.getFPGATimestamp()>=aTime+start&&Timer.getFPGATimestamp()<=aTime+end)?true:false;
	}
}
