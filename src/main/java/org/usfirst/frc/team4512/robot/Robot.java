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
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DigitalInput;

import org.usfirst.frc.team4512.robot.*;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

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
		SmartDashboard.putBoolean("Top", motorBase.sUp.get());
		SmartDashboard.putBoolean("Down", motorBase.sDown.get());
		SmartDashboard.putBoolean("Enabled",RobotState.isEnabled());
		SmartDashboard.putNumber("DriveSpeed", motorBase.DSPEED);
		SmartDashboard.putNumber("LeftSpeed", motorBase.dLeftF.getMotorOutputPercent());
		SmartDashboard.putNumber("RightSpeed", motorBase.dRightF.getMotorOutputPercent());
		SmartDashboard.putNumber("LiftSpeed", motorBase.liftF.get());
		SmartDashboard.putNumber("ArmRSpeed", motorBase.armR.get());
		SmartDashboard.putNumber("ArmLSpeed", motorBase.armL.get());		
		SmartDashboard.putNumber("RightDriveEncoder", motorBase.dEncoderR.get());
		SmartDashboard.putNumber("LeftDriveEncoder", motorBase.dEncoderL.get());	
		SmartDashboard.putNumber("LiftEncoder", motorBase.liftEncoder.get());
		SmartDashboard.putNumber("MaxLift", motorBase.MAXLIFT);
		SmartDashboard.putNumber("TimeTotal", Timer.getFPGATimestamp());
		SmartDashboard.putNumber("TimeLeft", Timer.getMatchTime());
		SmartDashboard.putNumber("GyroX", motorBase.gyro.getX());
		SmartDashboard.putNumber("GyroY", motorBase.gyro.getY());
		SmartDashboard.putNumber("GyroZ", motorBase.gyro.getZ());
	}
	
	@Override
	public void autonomousInit() {//runs upon auto startup
		/* Live Window */
		autoCommand = autoChoose.getSelected();//what option is selected in dashboard?   
		auto = new Autonomous(autoCommand);
	}
	
	@Override
	public void autonomousPeriodic() {//iteratively run while auto is active
		auto.autoPeriodic();
		motorBase.setDrive(auto.FORWARD, auto.TURN);
	}
	
	@Override
	public void teleopInit(){//runs upon tele-op startup
		motorBase.driveInit();
	}
	
	@Override
	public void teleopPeriodic() {//iteratively runs while tele-op is active	
		motorBase.drivePeriodic();
	}
}