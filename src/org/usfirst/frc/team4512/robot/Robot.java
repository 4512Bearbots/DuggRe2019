/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;

import org.usfirst.frc.team4512.robot.Debouncer;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

@SuppressWarnings("unused")
public class Robot extends IterativeRobot {
	/** Hardware */
	/* Right Talons */
	TalonSRX _rightR = new TalonSRX(1);
	TalonSRX _rightL = new TalonSRX(2);

	/* Left Talons */
	TalonSRX _leftR = new TalonSRX(3);
	TalonSRX _leftL = new TalonSRX(4);
	
	/* Lift Victors */
	VictorSP liftF = new VictorSP(0);
	VictorSP liftB = new VictorSP(1);
	
	/* Intake Victors */
	Spark armR = new Spark(4);
	Spark armL = new Spark(5);
	
	/** Values */
	private static double driveSpeed;
	private static double forward;
	private static double turn;
	
	private String autoSelected;
	
	
	public XboxController xbox; // more buttons :)
	public Debouncer uDebouncer; // d-pad doesn't return values lightning fast
	public Debouncer dDebouncer; // define buttons to only return every period
	public static Hand left = GenericHID.Hand.kLeft;
	public static Hand right = GenericHID.Hand.kRight;
	
	@Override
	public void robotInit() {
		/* Don't use this for now */ //you cant tell me what to do
		
		xbox = new XboxController(0);
		
		uDebouncer = new Debouncer(xbox, 0f, 0.2);
		dDebouncer = new Debouncer(xbox, 180f, 0.2);
		
		/* Disable motor controllers */
		_rightR.set(ControlMode.PercentOutput, 0);
		_rightL.set(ControlMode.PercentOutput, 0);
		_leftR.set(ControlMode.PercentOutput, 0);
		_leftL.set(ControlMode.PercentOutput, 0);
		
		/* Set Neutral mode */
		_rightR.setNeutralMode(NeutralMode.Brake);
		_rightL.setNeutralMode(NeutralMode.Brake);
		_leftR.setNeutralMode(NeutralMode.Brake);
		_leftL.setNeutralMode(NeutralMode.Brake);

		/* Configure output direction */
		_rightR.setInverted(false);
		_rightL.setInverted(false);
		_leftR.setInverted(true);
		_leftL.setInverted(true);
		
		driveSpeed = 0.4;
	}
	
	@Override
	public void autonomousInit() {
		
	}
	
	@Override
	public void autonomousPeriodic() {
		_rightR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_rightL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_leftR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		_leftL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
	}
	
	@Override
	public void teleopInit(){
		forward = 0;
		turn = 0;
		System.out.println("This is a basic arcade drive using Arbitrary Feed Forward.");
	}
	
	          /* vv tele-op vv */
	@Override
	public void teleopPeriodic() {		
		/* Gamepad processing */
		forward = Deadband(xbox.getY(left))*driveSpeed;
		turn = Deadband(xbox.getX(right))*driveSpeed;

		/* Basic Arcade Drive using PercentOutput along with Arbitrary FeedForward supplied by turn */
		_rightR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_rightL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_leftR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		_leftL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		
		/* Lift */ 
		if(xbox.getBumper(right)){
			liftF.set(1.0);
			liftB.set(1.0);
		}
		else if(xbox.getBumper(left)){
			liftF.set(-.5);
			liftB.set(-.5);
		}
		else{
			liftF.set(0);
			liftB.set(0);
		}
		
		/* Intake */
		if(xbox.getTriggerAxis(right) > 0) {
			armR.set(1*-xbox.getTriggerAxis(right));
			armL.set(1*xbox.getTriggerAxis(right));
		}
		else if(xbox.getTriggerAxis(left) > 0) {
			armR.set(1*(xbox.getTriggerAxis(left)));
			armL.set(1*(-xbox.getTriggerAxis(left)));
		}
		else {
			armR.set(0);
			armL.set(0);
		}
		
		/* D-Pad debouncers(doesnt activate 3 times per click) */
		if((driveSpeed+0.1) < 1 && uDebouncer.get()) {
			driveSpeed = Math.nextUp(driveSpeed+0.1);
			System.out.println("Drivespeed: "+driveSpeed);
		}
		else if((driveSpeed-0.1) > 0 && dDebouncer.get()) {
			driveSpeed = Math.nextDown(driveSpeed-0.1);
			System.out.println("Drivespeed: "+driveSpeed);
		}
	}          /* ^^ tele-op ^^ */
			
		

	/** Deadband ? percent, used on the gamepad */
	double Deadband(double value) {
		double deadzone = 0.175;
		
		/* Inside deadband */
		if ((value >= +deadzone)||(value <= -deadzone)) 
			return value;
		
		/* Outside deadband */
		return 0;
	}
}