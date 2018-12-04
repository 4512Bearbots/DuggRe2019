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
	/* Live Window */
	String autoCommand;
	SendableChooser<String> autoChoose;//give auto options
	private String autoSelected;//pick which auto to run
	
	@Override
	public void robotInit() {//commands run on code startup
		/* Live Window assingment */
		autoChoose = new SendableChooser<String>();
		autoChoose.addDefault("Default", "default");
		autoChoose.addObject("Test", "test");
		SmartDashboard.putData("Auto Chooser", autoChoose);

		/* Controls */
		Input.init();
		Input.gyro.calibrate();
	}
	
	@Override
	public void robotPeriodic() {//this command is like auto or teleop periodic, but is ran regardless of mode, even disabled(after other periodics)
		SmartDashboard.putBoolean("Top", Input.sUp.get());
		SmartDashboard.putBoolean("Down", Input.sDown.get());
		SmartDashboard.putNumber("DriveSpeed", MotorBase.dSpeed);
		SmartDashboard.putNumber("LeftSpeed", MotorBase.dLeftF.getMotorOutputPercent());
		SmartDashboard.putNumber("RightSpeed", MotorBase.dRightF.getMotorOutputPercent());
		SmartDashboard.putNumber("ArmRSpeed", MotorBase.armR.get());
		SmartDashboard.putNumber("ArmLSpeed", MotorBase.armL.get());		
		SmartDashboard.putNumber("RightDriveEncoder", Input.dEncoderR.get());
		SmartDashboard.putNumber("LeftDriveEncoder", Input.dEncoderL.get());
		SmartDashboard.putNumber("LiftEncoder", Input.liftEncoder.get());
		SmartDashboard.putNumber("MaxLift", MotorBase.MAXLIFT);
		SmartDashboard.putNumber("TimeTotal", Timer.getFPGATimestamp());
		SmartDashboard.putNumber("TimeLeft", Timer.getMatchTime());
		SmartDashboard.putNumber("accelX", Input.accel.getX());
		SmartDashboard.putNumber("accelY", Input.accel.getY());
		SmartDashboard.putNumber("accelZ", Input.accel.getZ());
		SmartDashboard.putNumber("Gyro", Input.getAngle());
		SmartDashboard.putNumber("GyroR", Input.getAngleRate());
	}
	
	@Override
	public void autonomousInit() {//runs upon auto startup
		Autonomous.aTime = Timer.getFPGATimestamp();
		Input.gyro.calibrate();
		MotorBase.driveInit();
		Autonomous.autoInit();
	}
	
	@Override
	public void autonomousPeriodic() {//iteratively run while auto is active
		Autonomous.command = autoChoose.getSelected();
		Autonomous.autoPeriodic();
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