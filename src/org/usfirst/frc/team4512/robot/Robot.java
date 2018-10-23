/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.Encoder;
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
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DigitalInput;

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
	
	/* Sensors */
	private Encoder dEncoderL;
	private Encoder dEncoderR;
	private Encoder liftEncoder;
	private BuiltInAccelerometer accel;
	private DigitalInput sUp;
	private DigitalInput sDown;
	
	/** Values */
	private static double driveSpeed = 0.5;
	private static double forward;
	private static double turn;
	private static int lState;
	private int liftZero = -1373; //Set it to an outlandish value so the min function for recalibrating this works initially
	private int liftTop = 4632; //Same here
	private boolean liftUp = false;
	
	
	private String autoSelected;
	
	
	private XboxController xbox; // more buttons :)
	private Debouncer uDebouncer; // d-pad doesn't return values lightning fast
	private Debouncer dDebouncer; // define buttons to only return every period
	private static Hand left = GenericHID.Hand.kLeft;
	private static Hand right = GenericHID.Hand.kRight;
	
	@Override
	public void robotInit() {
		/* Controller, debouncers */
		xbox = new XboxController(0);
		uDebouncer = new Debouncer(xbox, 0f, 0.2);
		dDebouncer = new Debouncer(xbox, 180f, 0.2);
		lState = 0;
		
		/* Sensor assignment */
		dEncoderL = new Encoder(4, 5);
		dEncoderR = new Encoder(2, 3);
		liftEncoder = new Encoder(6, 7);
		accel = new BuiltInAccelerometer();
		sUp = new DigitalInput(1);
		sDown = new DigitalInput(8);
		
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
		
		driveSpeed = 0.5;
	}
	
	@Override
	public void autonomousInit() {
		//autoForwardTime(1000, 0.35);
		//autoTurnTime(500, 0.35);
		
		//Add in stuff to recalibrate the lift here
		
		autoForwardEncoder(10.0, 0.35);
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
		//System.out.println("X: "+accel.getX()+"  Y: "+accel.getY()+"  Z: "+accel.getZ());
		System.out.println("d: "+sDown.get()+"  u: "+sUp.get()+"   LiftE: "+liftEncoder.get() + "   DriveL: " + dEncoderL.get() + "   DriveR: " + dEncoderR.get());
		/* Gamepad processing */
		forward = deadband(xbox.getY(left))*driveSpeed;
		turn = deadband(xbox.getX(right))*driveSpeed;

		/* Basic Arcade Drive using PercentOutput along with Arbitrary FeedForward supplied by turn */
		_rightR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_rightL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_leftR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		_leftL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		
		/* Constantly recalibrate the lift encoder bottom value */
		if(!sDown.get())
		{
			liftZero = Math.min(liftZero, liftEncoder.get()); //Henry did a good so this is pretty much always -1373
			//System.out.println(liftZero);
		}
		
		/* Constantly recalibrates the lift encoder top value */
		if(sUp.get()) 
		{
			liftTop = Math.min(liftTop, liftEncoder.get()); //Same here, it's 4632
			//System.out.println(liftTop);
		}
		
		/* Lift */ 
		if(xbox.getBumper(right)){
			liftF.set(1.0);
			liftB.set(1.0);
		}
		else if(xbox.getBumper(left)){
			liftF.set(-.65);
			liftB.set(-.65);
		}
		else if(!sDown.get()){
			liftF.set(0.11);
			liftB.set(0.11);
		}
		else{
			liftF.set(0);
			liftB.set(0);
		}
	//	
	/*	if(xbox.getAButton()) lState = 1;
		switch(lState) {
		case 1:
			liftF.set(-.2);
			liftB.set(-.2);
			if(sDown.get()) {
				lState = 0;
				liftF.set(0);
				liftB.set(0);
			}
			break;
		default:break;
		}
		*/
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
			armR.set(-.18);
			armL.set(.18);
		}
		
		/*if(liftEncoder.get() > (liftZero + liftTop) / 4) //If the lift is over halfway up
		{
			driveSpeed = Math.min(0.3, driveSpeed);
			liftUp = true;
		} else
		{
			liftUp = false;
		}*/
		
		if(xbox.getAButton())
		{
			driveSpeed = 0.2;
		} else if(xbox.getXButton())
		{
			driveSpeed = 0.3;
		} else if(xbox.getYButton() /*&& !liftUp*/) //You can't switch to these if the lift is up a certain amount
		{
			driveSpeed = 0.5;
		} else if(xbox.getBButton() /*&& !liftUp*/)
		{
			driveSpeed = 1.0;
		}
	
		/* D-Pad debouncers(doesnt activate 3 times per click) */
		/*if((driveSpeed+0.25) < 1 && uDebouncer.get()) {
			driveSpeed = Math.nextUp(driveSpeed+0.25);
			//System.out.println("Drivespeed: "+driveSpeed);
		}
		else if((driveSpeed-0.25) > 0 && dDebouncer.get()) {
			driveSpeed = Math.nextDown(driveSpeed-0.25);
			//System.out.println("Drivespeed: "+driveSpeed);
		}*/
	}          /* ^^ tele-op ^^ */
			
		

	/** deadband ? percent, used on the gamepad */
	double deadband(double value) {
		double deadzone = 0.175;
		
		/* Inside deadband */
		if ((value >= +deadzone)||(value <= -deadzone)) 
			return value;
		
		/* Outside deadband */
		return 0;
	}
	
	
	//Quick and dirty methods for making a time-based auto easily
	void autoForwardTime(int time, double speed) //Time in milliseconds and value you want to pass to the motors
	{
		long start = System.currentTimeMillis();
		
		while(start + time > System.currentTimeMillis())
		{
			_rightR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, 0);
			_rightL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, 0);
			_leftR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, 0);
			_leftL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, 0);
		}
	}
	
	//Exactly the same but turning
	void autoTurnTime(int time, double turn) //Positive is right and negative is left (I think)
	{
		long start = System.currentTimeMillis();
		
		while(start + time > System.currentTimeMillis())
		{
			_rightR.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, +turn);
			_rightL.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, +turn);
			_leftR.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, -turn);
			_leftL.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, -turn);
		}
	}
	
	
	void autoForwardEncoder(double feet, double power)
	{
		double value = 360 / (6 * Math.PI) * 12 * feet - (19.5 / 12);
		
		while (dEncoderL.get() < value)
		{
			_rightR.set(ControlMode.PercentOutput, -power, DemandType.ArbitraryFeedForward, 0);
			_rightL.set(ControlMode.PercentOutput, -power, DemandType.ArbitraryFeedForward, 0);
			_leftR.set(ControlMode.PercentOutput, -power, DemandType.ArbitraryFeedForward, 0);
			_leftL.set(ControlMode.PercentOutput, -power, DemandType.ArbitraryFeedForward, 0);
		}
		
		_rightR.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, 0);
		_rightL.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, 0);
		_leftR.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, 0);
		_leftL.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, 0);
	}
}