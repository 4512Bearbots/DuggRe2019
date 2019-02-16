/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

@SuppressWarnings("unused")
public class Robot extends TimedRobot {
	/* Live Window */
	String autoCommand;
	SendableChooser<String> autoChoose;//give auto options
	
	@Override
	public void robotInit() {//commands run on code startup
		/* Live Window assingment */
		autoChoose = new SendableChooser<String>();
		autoChoose.setDefaultOption("Default", "default");
		autoChoose.addOption("Test", "test");
		autoChoose.addOption("Vision", "vision");
		SmartDashboard.putData("Auto Chooser", autoChoose);

		/* Controls */
		Input.init();
		Input.calibrate();
	}
	
	@Override
	public void robotPeriodic() {//this command is like auto or teleop periodic, but is ran regardless of mode, even disabled(after other periodics)
		Input.displayStats();
		MotorBase.displayStats();
	}
	
	@Override
	public void disabledInit() {
		MotorBase.driveDisable();
	}
	
	@Override
	public void autonomousInit() {//runs upon auto startup
		Autonomous.setCommand(autoChoose.getSelected());
		MotorBase.driveInit();
		Autonomous.autoInit();
	}
	
	@Override
	public void autonomousPeriodic() {//iteratively run while auto is active
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