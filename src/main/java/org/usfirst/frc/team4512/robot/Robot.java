/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

@SuppressWarnings("unused")
public class Robot extends IterativeRobot {
	/** Hardware */
	MotorBase motorBase;
	
	/** Software */
	/* Live Window */
	String autoCommand;
	SendableChooser<String> autoChoose;
	private String autoSelected; //determine state for executing autonomous
								 //changes which auto will be run
	private Autonomous auto;
	
	@Override
	public void robotInit() {//commands run on code startup
		motorBase = new MotorBase();
		/* Live Window assingment */
		autoChoose = new SendableChooser<String>();
		autoChoose.addDefault("Default", "default");
		autoChoose.addObject("Test", "test");
		SmartDashboard.putData("Auto Chooser", autoChoose);
	}
	
	@Override
	public void robotPeriodic() {//this command is like auto or teleop periodic, but is ran regardless of mode, even disabled(after other periodics)
		SmartDashboard.putBoolean("Top", MotorBase.sUp.get());
		SmartDashboard.putBoolean("Down", MotorBase.sDown.get());
		SmartDashboard.putNumber("DriveSpeed", MotorBase.DSPEED);
		SmartDashboard.putNumber("LeftSpeed", MotorBase.dLeftF.getMotorOutputPercent());
		SmartDashboard.putNumber("RightSpeed", MotorBase.dRightF.getMotorOutputPercent());
		SmartDashboard.putNumber("LiftSpeed", MotorBase.liftF.get());
		SmartDashboard.putNumber("ArmRSpeed", MotorBase.armR.get());
		SmartDashboard.putNumber("ArmLSpeed", MotorBase.armL.get());		
		SmartDashboard.putNumber("RightDriveEncoder", MotorBase.dEncoderR.get());
		SmartDashboard.putNumber("LeftDriveEncoder", MotorBase.dEncoderL.get());
		SmartDashboard.putNumber("LiftEncoder", MotorBase.liftEncoder.get());
		SmartDashboard.putNumber("MaxLift", MotorBase.MAXLIFT);
		SmartDashboard.putNumber("TimeTotal", Timer.getFPGATimestamp());
		SmartDashboard.putNumber("TimeLeft", Timer.getMatchTime());
		SmartDashboard.putNumber("GyroX", MotorBase.gyro.getX());
		SmartDashboard.putNumber("GyroY", MotorBase.gyro.getY());
		SmartDashboard.putNumber("GyroZ", MotorBase.gyro.getZ());
	}
	
	@Override
	public void autonomousInit() {//runs upon auto startup
		/* Live Window */
		autoCommand = autoChoose.getSelected();//what option is selected in dashboard?   
		auto = new Autonomous(autoCommand);
	}
	
	@Override
	public void autonomousPeriodic() {//iteratively run while auto is active
		Autonomous.autoPeriodic();
		MotorBase.setDrive(Autonomous.FORWARD, Autonomous.TURN);
	}
	
	@Override
	public void teleopInit(){//runs upon tele-op startup
		MotorBase.driveInit();
	}
	
	@Override
	public void teleopPeriodic() {//iteratively runs while tele-op is active	
		MotorBase.drivePeriodic();
	}
}