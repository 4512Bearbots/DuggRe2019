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
	TalonSRX dRightF = new TalonSRX(1);
	TalonSRX dRightB = new TalonSRX(2);

	/* Left Talons */
	TalonSRX dLeftF = new TalonSRX(3);
	TalonSRX dLeftB = new TalonSRX(4);
	
	/* Lift Victors */
	VictorSP liftF = new VictorSP(0);
	VictorSP liftB = new VictorSP(1);
	
	/* Intake Victors */
	Spark armR = new Spark(4);
	Spark armL = new Spark(5);
	
	/** Software */
	/* Live Window */
	String autoCommand;
	SendableChooser<String> autoChoose;
	
	/* Sensors */
	private Encoder dEncoderB;
	private Encoder dEncoderF;
	private Encoder liftEncoder;
	private BuiltInAccelerometer gyro;
	private DigitalInput sUp;
	private DigitalInput sDown;
	
	/* Constants */
	private static double DSPEED;//overall speed affecting robots actions
	private static double WTIME;//warming for stopping acceleration jerk
	private static double FORWARD; //value affecting forward speed in feedforward
	private static double TURN; //value affecting turning in feedforward
	private static int LSTATE;//determine state for executing lift commands
	private static int MAXLIFT;//top of the lift in counts
	private static Hand KLEFT = GenericHID.Hand.kLeft; //constant referring to
	private static Hand KRIGHT = GenericHID.Hand.kRight;//the side of controller
	
	private Autonomous auto;
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
		uDebouncer = new Debouncer(xbox, 0f, 0.25);
		dDebouncer = new Debouncer(xbox, 180f, 0.25);
		
		
		/* Sensor assignment *///code matches electrical
		dEncoderB = new Encoder(4, 5);
		dEncoderF = new Encoder(2, 3);
		liftEncoder = new Encoder(6, 7);
		gyro = new BuiltInAccelerometer();
		sUp = new DigitalInput(1);
		sDown = new DigitalInput(8);
		
		/* Live Window assingment */
		autoChoose = new SendableChooser<String>();
		autoChoose.addDefault("Default", "default");
		autoChoose.addObject("Test", "test");
		SmartDashboard.putData("Auto Chooser", autoChoose);
		
		/* Disable motor controllers */
		dRightF.set(ControlMode.PercentOutput, 0);
		dRightB.set(ControlMode.PercentOutput, 0);
		dLeftF.set(ControlMode.PercentOutput, 0);
		dLeftB.set(ControlMode.PercentOutput, 0);
		
		/* Set Neutral mode *///motor behavior
		dRightF.setNeutralMode(NeutralMode.Brake);
		dRightB.setNeutralMode(NeutralMode.Brake);
		dLeftF.setNeutralMode(NeutralMode.Brake);
		dLeftB.setNeutralMode(NeutralMode.Brake);

		/* Configure output direction */
		dRightF.setInverted(false);
		dRightB.setInverted(false);
		dLeftF.setInverted(true);
		dLeftB.setInverted(true);
		armR.setInverted(true);
		armL.setInverted(false);
		
		/* Constant assignment */
		DSPEED = 0.5;
		LSTATE = 0;
		MAXLIFT = 4250;
	}
	
	@Override
	public void robotPeriodic() {//this command is like auto or teleop periodic, but is ran regardless of mode, even disabled(after other periodics)
		SmartDashboard.putNumber("DriveSpeed", DSPEED);
		SmartDashboard.putNumber("LeftSpeed", dLeftF.getMotorOutputPercent());
		SmartDashboard.putNumber("RightSpeed", dRightF.getMotorOutputPercent());
		SmartDashboard.putNumber("LiftSpeed", liftF.get());
		SmartDashboard.putNumber("ArmRSpeed", armR.get());
		SmartDashboard.putNumber("ArmLSpeed", armL.get());		
		SmartDashboard.putBoolean("Top", sUp.get());
		SmartDashboard.putBoolean("Down", sDown.get());
		double[] dArray = {(double)dEncoderB.get(), (double)dEncoderF.get()};
		SmartDashboard.putNumberArray("DriveEncoders", dArray);
		SmartDashboard.putNumber("LiftEncoder", liftEncoder.get());
		SmartDashboard.putNumber("MaxLift", MAXLIFT);
		SmartDashboard.putNumber("TimeTotal", Timer.getFPGATimestamp());
		SmartDashboard.putNumber("TimeLeft", Timer.getMatchTime());
		double[] gArray = {gyro.getX(),gyro.getY(),gyro.getZ()};
		SmartDashboard.putNumberArray("Gyro", gArray);
	}
	
	@Override
	public void autonomousInit() {//runs upon auto startup
		/* Live Window */
		autoCommand = autoChoose.getSelected();//what option is selected in dashboard?   
		
		auto = new Autonomous(autoCommand);
		//autoForwardTime(1000, 0.35);
		//autoTurnTime(500, 0.35);
		
		//Add in stuff to recalibrate the lift here
		
		autoForwardEncoder(10.0, 0.35);
	}
	
	@Override
	public void autonomousPeriodic() {//iteratively run while auto is active
		FORWARD = auto.FORWARD;
		TURN = auto.TURN;
		dRightF.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, +TURN);
		dRightB.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, +TURN);
		dLeftF.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, -TURN);
		dLeftB.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, -TURN);
	}
	
	@Override
	public void teleopInit(){//runs upon tele-op startup
		FORWARD = 0;
		TURN = 0;
		WTIME = 0;
		liftEncoder.reset();
		dEncoderB.reset();
		dEncoderF.reset();
		System.out.println("--Feed Forward Teleop--");
	}
	
	          /* vv tele-op vv */
	@Override
	public void teleopPeriodic() {//iteratively runs while tele-op is active	
		/* Controller interface => Motors */
		FORWARD = deadband(xbox.getY(KLEFT))*DSPEED*warming();//apply the math to joysticks
		TURN = deadband(xbox.getX(KRIGHT))*DSPEED;

		/* Basic Arcade Drive using PercentOutput along with Arbitrary FeedForward supplied by turn */
		//given a forward value and a turn value, will automatically do all the math and appropriately send signals
		dRightF.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, +TURN);
		dRightB.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, +TURN);
		dLeftF.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, -TURN);
		dLeftB.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, -TURN);
		
		
		/* Lift */ 
		if(sDown.get())onDown();//call methods when switches are pressed
		if(sUp.get())onUp();
			
		if(LSTATE!=1 && LSTATE!=2)LSTATE=0;
		if(xbox.getBumper(KRIGHT)){//upon button press, do this
			LSTATE = 3;
		}
		else if(xbox.getBumper(KLEFT)){
			LSTATE = 4;
		}
		
		if(uDebouncer.get()) LSTATE = 1;//pressing d-pad will automatically
		if(dDebouncer.get()) LSTATE = 2;//move the lift to top or bottom
		
		switch(LSTATE) {
		case 1:
			liftF.set(0.9*encoderMath((double)liftEncoder.get()/MAXLIFT, DSPEED));
			liftB.set(0.9*encoderMath((double)liftEncoder.get()/MAXLIFT, DSPEED));
			if(sUp.get()) {
				LSTATE = 0;
			}
			break;
		case 2:
			liftF.set(-0.7*encoderMath((double)liftEncoder.get()/MAXLIFT, DSPEED));
			liftB.set(-0.7*encoderMath((double)liftEncoder.get()/MAXLIFT, DSPEED));
			if(sDown.get()) {
				LSTATE = 0;
			}
			break;
		case 3:
			liftF.set(encoderMath((double)liftEncoder.get()/MAXLIFT, DSPEED));
			liftB.set(encoderMath((double)liftEncoder.get()/MAXLIFT, DSPEED));
			break;
		case 4:
			liftF.set(-0.8*encoderMath((double)liftEncoder.get()/MAXLIFT, DSPEED));
			liftB.set(-0.8*encoderMath((double)liftEncoder.get()/MAXLIFT, DSPEED));
			break;
		default:
			if(!sDown.get()) {
				liftF.set(0.11);
				liftB.set(0.11);
				if(liftEncoder.get()<400) LSTATE = 2;
			}else {
				liftF.set(0);
				liftB.set(0);
			}
			break;
		}
		
		/* Intake */
		if(xbox.getTriggerAxis(KRIGHT) > 0) {
			armR.set(1*xbox.getTriggerAxis(KRIGHT));
			armL.set(1*xbox.getTriggerAxis(KRIGHT));
		}
		else if(xbox.getTriggerAxis(KLEFT) > 0) {
			armR.set(1*(-xbox.getTriggerAxis(KLEFT)));
			armL.set(1*(-xbox.getTriggerAxis(KLEFT)));
		}
		else {
			armR.set(.18);
			armL.set(.18);
		}
		
		/*if(liftEncoder.get() > (liftZero + liftTop) / 4) //If the lift is over halfway up
		{
			DSPEED = Math.min(0.3, DSPEED);
			liftUp = true;
		} else
		{
			liftUp = false;
		}*/
		
		if(xbox.getAButton())DSPEED=0.2;
		if(xbox.getXButton())DSPEED=0.3;
		if(xbox.getYButton()&&(liftEncoder.get()/MAXLIFT < 0.7))DSPEED=0.5;
		if(xbox.getBButton()&&(liftEncoder.get()/MAXLIFT < 0.7))DSPEED=1.0;
	
		/* D-Pad debouncers(doesnt activate 3 times per click) */
		/*if((DSPEED+0.25) < 1 && uDebouncer.get()) {
			DSPEED = Math.nextUp(DSPEED+0.25);
			//System.out.println("DSPEED: "+DSPEED);
		}
		else if((DSPEED-0.25) > 0 && dDebouncer.get()) {
			DSPEED = Math.nextDown(DSPEED-0.25);
			//System.out.println("DSPEED: "+DSPEED);
		}*/
	}          /* ^^ tele-op ^^ */
			
		

	/** deadband ? percent, used on the gamepad */
	double deadband(double value) {
		double deadzone = 0.15;
		
		/* Inside deadband */
		if ((value >= +deadzone)||(value <= -deadzone)) {
			if(WTIME==0)WTIME=Timer.getFPGATimestamp();
			return value;
		}
		
		/* Outside deadband */
		WTIME=0;
		return 0;
	}
	
	public static double warming() {
		SmartDashboard.putNumber("Warming", Timer.getFPGATimestamp()-WTIME);
		return (double)((WTIME==0)?1:Math.min(1, (Timer.getFPGATimestamp()-WTIME)+0.1));
	}
	
	//return new value after applying to curve
	public static double encoderMath(double x, double n) {
		double k = 0.25;//minimum speed when bottom/top
		//n = Math.min((15*Math.pow((n-0.5),3))+1,1); //lift speed changes with DSPEED
		n=1;
		double y = -(1000*(1-k))*n*Math.pow((x-0.5), 10)+((1-k)*n)+k;//big equation(slow down on bottom/top of lift)
		y = Math.max(y, 0.25);
		return y;
	}
	
	public void onDown() {
		liftEncoder.reset();
	}
	public void onUp() {
		//MAXLIFT = liftEncoder.get()-150;
		System.out.println("Top triggered on: "+liftEncoder.get());
	}
	//Quick and dirty methods for making a time-based auto easily
	private void autoForwardTime(int time, double speed) //Time in milliseconds and value you want to pass to the motors
	{
		long start = System.currentTimeMillis();
		
		while(start + time > System.currentTimeMillis())
		{
			dRightF.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, 0);
			dRightB.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, 0);
			dLeftF.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, 0);
			dLeftB.set(ControlMode.PercentOutput, FORWARD, DemandType.ArbitraryFeedForward, 0);
		}
	}
	
	//Exactly the same but turning
	private void autoTurnTime(int time, double turn) //Positive is right and negative is left (I think)
	{
		long start = System.currentTimeMillis();
		
		while(start + time > System.currentTimeMillis())
		{
			dRightF.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, +turn);
			dRightB.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, +turn);
			dLeftF.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, -turn);
			dLeftB.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, -turn);
		}
	}
	
	
	private void autoForwardEncoder(double feet, double power)
	{
		double value = 360 / (6 * Math.PI) * 12 * feet - (19.5 / 12);
		
		while (dEncoderB.get() < value)
		{
			dRightF.set(ControlMode.PercentOutput, -power, DemandType.ArbitraryFeedForward, 0);
			dRightB.set(ControlMode.PercentOutput, -power, DemandType.ArbitraryFeedForward, 0);
			dLeftF.set(ControlMode.PercentOutput, -power, DemandType.ArbitraryFeedForward, 0);
			dLeftB.set(ControlMode.PercentOutput, -power, DemandType.ArbitraryFeedForward, 0);
		}
		
		dRightF.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, 0);
		dRightB.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, 0);
		dLeftF.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, 0);
		dLeftB.set(ControlMode.PercentOutput, 0, DemandType.ArbitraryFeedForward, 0);
	}
}