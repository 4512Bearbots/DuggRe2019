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
		MotorBase.dSpeed = 0.5;
		aTime = Timer.getFPGATimestamp();
	}

	public static void autoPeriodic() {
		switch(Autonomous.command) {
		case "test":
			if(aSchedule(0,4))setHeading(90);
			else if(aSchedule(4,8))setHeading(180);
			else if(aSchedule(8,12))setHeading(91);
			else if(aSchedule(12,16))setHeading(270);
			else if(aSchedule(16,20))setHeading(220);
			else setHeading(0);
			break;
		default:
			setHeading(0);
			break;
		}
	}

	public static boolean aSchedule(double start, double end) {
		return (Timer.getFPGATimestamp()>=start && Timer.getFPGATimestamp()<=end)?true:false;
	}

	public static void setHeading(double heading){
		double angle = (Input.getAngle());
		double bHeading = Input.constrainAngle(heading+180);
		double diff = 90 * Input.toDegrees(Math.cos(Input.toRadians(angle-bHeading)))+90;//https://www.desmos.com/calculator/x3yql5nknh
		double diffD = Input.toRadians(Math.sin(Input.toRadians(angle-bHeading)));
		//diffD should return positive when left(negative turn), negative right
		SmartDashboard.putNumber("AutoDiff", diff);
		if(diff<5) {
			turn = 0;
			last = Timer.getFPGATimestamp();
			MotorBase.setArms(0);
		} else {
			if(aSchedule(last, last+1)){
				MotorBase.setArms(1);
			} else {
				//angle increases clockwise
				//negative turn goes left
				MotorBase.setArms(0);
				if(diffD>0){//to the right of heading
					turn = (0.3*(-diff/180)-0.7);
				} else {
					turn = (0.3*(diff/180)+0.7);
				}
			}
			
		}
		SmartDashboard.putNumber("AutoTurn", turn);
		MotorBase.setDrive(forward, turn);
	}
}
