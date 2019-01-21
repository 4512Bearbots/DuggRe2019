package org.usfirst.frc.team4512.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Autonomous {

	private static double forward = 0;
	private static double turn = 0;
	private static double kTurnP = 0.1;//proportional constant affecting turn rate
	private static double kTurnF = 0.05;//base constant used to overcome friction
	private static String command;
	private static Timer time;
	private static Timer timeTotal;

	public static void autoInit() {
		MotorBase.setNeutral(NeutralMode.Coast);
		MotorBase.shift(0.5);
		time = new Timer();
		timeTotal = new Timer();
		switch(Autonomous.command){
			case "test":
				time.reset();
				timeTotal.reset();
				time.start();
				break;
			default:
				break;
		}
	}

	public static void autoPeriodic() {
		switch(Autonomous.command) {
		case "test":
			if(time.get()<4)setHeading(90);
			else if(time.get()<8)setHeading(180);
			else if(time.get()<12)setHeading(91);
			else if(time.get()<16)setHeading(270);
			else if(time.get()<20)setHeading(220);
			else {
				setHeading(0);
				time.stop();
			}
			break;
		case "vision":
			double tx = Input.getTx();
			double angleError = 0;
			if(tx>0.5){
				angleError = kTurnP*(tx) + kTurnF;
			} else if(tx<-0.5){
				angleError = kTurnP*(-tx) - kTurnF;
			}
			turn = angleError;
			break;
		default:
			setHeading(0);
			break;
		}
		MotorBase.setDrive(forward, turn);
	}

	public static void setHeading(double heading){
		double angle = (Input.getAngle());
		double bHeading = Input.constrainAngle(heading+180);
		double diff = 90 * (Math.cos(Input.toRadians(angle-bHeading)))+90;//https://www.desmos.com/calculator/x3yql5nknh
		double diffD = (Math.sin(Input.toRadians(angle-bHeading)));
		//diffD should return positive when left(negative turn), negative right
		SmartDashboard.putNumber("AutoDiff", diff);
		if(diff<0.3) {
			turn = 0;
			timeTotal.start();
			MotorBase.setArms(0);
		} else {
			//angle increases clockwise
			//negative turn goes left
			MotorBase.setArms(0);
			if(diffD<0){//to the right of heading
				turn = (0.6*(-diff/180)-0.4);
			} else {
				turn = (0.6*(diff/180)+0.4);
			}
			
		}
		SmartDashboard.putNumber("AutoTurn", turn);
	}
	
	public static void setCommand(String str){
		Autonomous.command = str;
	}
}
