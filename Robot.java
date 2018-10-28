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
import edu.wpi.first.wpilibj.command.Command;
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
	
	/** Software */
	String autoCommand;
	SendableChooser<String> autoChoose;
	
	/* Sensors */
	private Encoder dEncoderL;
	private Encoder dEncoderR;
	private Encoder liftEncoder;
	private BuiltInAccelerometer accel;
	private DigitalInput sUp;
	private DigitalInput sDown;
	
	/* Constants */
	private static double driveSpeed = 0.5;//overall speed affecting robots actions
	private static double forward; //value affecting forward speed in feedforward
	private static double turn; //value affecting turning in feedforward
	private static int lState;//determine state for executing lift commands
	private static int maxLift;//top of the lift in counts
	private static Hand left = GenericHID.Hand.kLeft; //constant referring to
	private static Hand right = GenericHID.Hand.kRight;//the side of controller
	
	private String autoSelected; //determine state for executing autonomous
								 //changes which auto will be run
	
	/* Controls */
	private XboxController xbox; //object for controller --more buttons :)
	private Debouncer uDebouncer; //d-pad doesn't return values lightning fast
	private Debouncer dDebouncer; //define buttons to only return every period
	
	@Override
	public void robotInit() {//commands run on code startup
		/* Controls' assignment*/
		xbox = new XboxController(0);
		uDebouncer = new Debouncer(xbox, 0f, 0.3);
		dDebouncer = new Debouncer(xbox, 180f, 0.3);
		
		
		/* Sensor assignment *///code matches electrical
		dEncoderL = new Encoder(4, 5);
		dEncoderR = new Encoder(2, 3);
		liftEncoder = new Encoder(6, 7);
		accel = new BuiltInAccelerometer();
		sUp = new DigitalInput(1);
		sDown = new DigitalInput(8);
		
		/* Live Window assingment */
		autoChoose = new SendableChooser<String>();
		autoChoose.addDefault("Default", "default");
		autoChoose.addObject("Test", "test");
		SmartDashboard.putData("Auto Chooser", autoChoose);
		
		/* Disable motor controllers */
		_rightR.set(ControlMode.PercentOutput, 0);
		_rightL.set(ControlMode.PercentOutput, 0);
		_leftR.set(ControlMode.PercentOutput, 0);
		_leftL.set(ControlMode.PercentOutput, 0);
		
		/* Set Neutral mode *///motor behavior
		_rightR.setNeutralMode(NeutralMode.Brake);
		_rightL.setNeutralMode(NeutralMode.Brake);
		_leftR.setNeutralMode(NeutralMode.Brake);
		_leftL.setNeutralMode(NeutralMode.Brake);

		/* Configure output direction */
		_rightR.setInverted(false);
		_rightL.setInverted(false);
		_leftR.setInverted(true);
		_leftL.setInverted(true);
		armR.setInverted(true);
		armL.setInverted(false);
		
		/* Constant assignment */
		driveSpeed = 0.5;
		lState = 0;
		maxLift = 6000;
	}
	
	@Override
	public void autonomousInit() {//runs upon auto startup
		/* Live Window */
		autoCommand = autoChoose.getSelected();//what option is selected in dashboard?   
		
		//autoForwardTime(1000, 0.35);
		//autoTurnTime(500, 0.35);
		
		//Add in stuff to recalibrate the lift here
		
		autoForwardEncoder(10.0, 0.35);
	}
	
	@Override
	public void autonomousPeriodic() {//iteratively run while auto is active
		_rightR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_rightL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_leftR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		_leftL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
	}
	
	@Override
	public void teleopInit(){//runs upon tele-op startup
		forward = 0;
		turn = 0;
		liftEncoder.reset();
		dEncoderL.reset();
		dEncoderR.reset();
		System.out.println("--Feed Forward Teleop--");
	}
	
	          /* vv tele-op vv */
	@Override
	public void teleopPeriodic() {//iteratively runs while tele-op is active
		//System.out.println("X: "+accel.getX()+"  Y: "+accel.getY()+"  Z: "+accel.getZ());
		//System.out.println("d: "+sDown.get()+"  u: "+sUp.get()+"   LiftE: "+liftEncoder.get() + "   DriveL: " + dEncoderL.get() + "   DriveR: " + dEncoderR.get());
		System.out.println("Max: "+maxLift);
		
		/* Controller interface => Motors */
		forward = deadband(xbox.getY(left))*driveSpeed;//apply the math to joysticks
		turn = deadband(xbox.getX(right))*driveSpeed;

		/* Basic Arcade Drive using PercentOutput along with Arbitrary FeedForward supplied by turn */
		//given a forward value and a turn value, will automatically do all the math and appropriately send signals
		_rightR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_rightL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, +turn);
		_leftR.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		_leftL.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		
		
		/* Lift */ 
		if(sDown.get())onDown();//call methods when switches are pressed
		if(sUp.get())onUp();
			
		if(lState!=1||lState!=2)lState=0;
		if(xbox.getBumper(right)){//upon button press, do this
			lState = 3;
		}
		else if(xbox.getBumper(left)){
			lState = 4;
		}
		
		if(uDebouncer.get()) lState = 1;//pressing d-pad will automatically
		if(dDebouncer.get()) lState = 2;//move the lift to top or bottom
		
		switch(lState) {
		case 1:
			liftF.set(encoderMath((double)liftEncoder.get()/maxLift, driveSpeed));
			liftB.set(encoderMath((double)liftEncoder.get()/maxLift, driveSpeed));
			if(sUp.get()) {
				lState = 0;
			}
			break;
		case 2:
			liftF.set(-0.7*encoderMath((double)liftEncoder.get()/maxLift, driveSpeed));
			liftB.set(-0.7*encoderMath((double)liftEncoder.get()/maxLift, driveSpeed));
			if(sDown.get()) {
				lState = 0;
			}
			break;
		case 3:
			liftF.set(encoderMath((double)liftEncoder.get()/maxLift, driveSpeed));
			liftB.set(encoderMath((double)liftEncoder.get()/maxLift, driveSpeed));
			break;
		case 4:
			liftF.set(-0.8*encoderMath((double)liftEncoder.get()/maxLift, driveSpeed));
			liftB.set(-0.8*encoderMath((double)liftEncoder.get()/maxLift, driveSpeed));
			break;
		default:
			if(!sDown.get()) {
				liftF.set(0.11);
				liftB.set(0.11);
				if(liftEncoder.get()<700) lState = 2;
			}else {
				liftF.set(0);
				liftB.set(0);
			}
			break;
		}
		
		/* Intake */
		if(xbox.getTriggerAxis(right) > 0) {
			armR.set(1*xbox.getTriggerAxis(right));
			armL.set(1*xbox.getTriggerAxis(right));
		}
		else if(xbox.getTriggerAxis(left) > 0) {
			armR.set(1*(-xbox.getTriggerAxis(left)));
			armL.set(1*(-xbox.getTriggerAxis(left)));
		}
		else {
			armR.set(.18);
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
	
	//return new value after applying to curve
	public static double encoderMath(double x, double n) {
		double k = 0.3;//minimum speed when bottom/top
		n = Math.min((15*Math.pow((n-0.5),3))+1,1); //lift speed changes with drivespeed
		double y = -(1000*(1-k))*n*Math.pow((x-0.5), 10)+((1-k)*n)+k;//big equation(slow down on bottom/top of lift)
		y = Math.max(y, 0.3);
		return y;
	}
	
	public void onDown() {
		liftEncoder.reset();
	}
	public void onUp() {
		maxLift = liftEncoder.get();
		System.out.println("Top triggered on: "+liftEncoder.get());
	}
	//Quick and dirty methods for making a time-based auto easily
	private void autoForwardTime(int time, double speed) //Time in milliseconds and value you want to pass to the motors
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
	private void autoTurnTime(int time, double turn) //Positive is right and negative is left (I think)
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
	
	
	private void autoForwardEncoder(double feet, double power)
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