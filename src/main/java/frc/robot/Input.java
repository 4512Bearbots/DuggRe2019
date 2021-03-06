package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.SPI.Port;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.networktables.*;
public class Input{
    /* Sensors */
    private static BuiltInAccelerometer accel;
    private static ADXRS450_Gyro gyro;
	private static DigitalInput sUp;
    private static DigitalInput sDown;
    private static Encoder dEncoderL;
	private static Encoder dEncoderR;
    private static Encoder liftEncoder;
    private static NetworkTable table;
    private static NetworkTableEntry tx;
    private static NetworkTableEntry ty;
    private static NetworkTableEntry ta;
    
    /* Controls */
	private static XboxController xbox; //object for controller --more buttons :)
	public static Debouncer uDebouncer; //d-pad doesn't return values lightning fast
    public static Debouncer dDebouncer; //define buttons to only return every period
    public static Debouncer lDebouncer;
    public static Debouncer rDebouncer;
    public static Debouncer backDebouncer;
    public static Debouncer startDebouncer;
    private static Hand KLEFT = GenericHID.Hand.kLeft; //constant referring to
    private static Hand KRIGHT = GenericHID.Hand.kRight;//the side of controller

    public static void init(){
        /* Sensor assignment *///code matches electrical
		dEncoderL = new Encoder(4, 5);
		dEncoderR = new Encoder(2, 3);
		liftEncoder = new Encoder(6, 7);
        accel = new BuiltInAccelerometer();
        gyro = new ADXRS450_Gyro(Port.kOnboardCS0);
		sUp = new DigitalInput(1);
        sDown = new DigitalInput(8);
        table = NetworkTableInstance.getDefault().getTable("limelight");
        tx = table.getEntry("tx");
        ty = table.getEntry("ty");
        ta = table.getEntry("ta");

        /* Controls' assignment*/
		xbox = new XboxController(0);
		uDebouncer = new Debouncer(xbox, 0f, 0.25);
        dDebouncer = new Debouncer(xbox, 180f, 0.25);
        lDebouncer = new Debouncer(xbox, 90f, 0.25);
        rDebouncer = new Debouncer(xbox, 270f, 0.25);
        backDebouncer = new Debouncer(xbox, 7, 0.25);
        startDebouncer = new Debouncer(xbox, 8, 0.25);
    }

    public static void reset(){
        liftEncoder.reset();
		dEncoderL.reset();
        dEncoderR.reset();
        gyro.reset();
    }
    public static void resetLift(){
        liftEncoder.reset();
    }

    public static void calibrate(){
        Input.gyro.calibrate();
    }

    /** deadband ? percent, used on the gamepad */
	private static double deadband(double value) {
		double deadzone = 0.15;//smallest amount you can recognize from the controller
		if ((value >= +deadzone)||(value <= -deadzone)) {
			return value;//outside deadband
		}else{
			return 0;//inside deadband
		}
    }

    public static double toRadians(double degrees){
        return degrees * (Math.PI/180);
    }

    public static double toDegrees(double radians){
        return radians * (180/Math.PI);
    }

    public static double constrainAngle(double x){
        while(x<0){//constrain angles 0 - 360
            x += 360;
        } if(x>360){
            x = x % 360;
        }
        return x;
    }
    
    public static void toggleLight(){
        double lightMode = table.getEntry("ledMode").getDouble(0);
        table.getEntry("ledMode").setNumber((lightMode==3)? 1:lightMode+1);
    }
    public static void shiftPipe(){
        table.getEntry("pipeline").setNumber((table.getEntry("pipeline").getDouble(0)==0)? 1:0);
    }
    public static double getTx(){
        return tx.getDouble(0.0);
    }
    public static double getTy(){
        return ty.getDouble(0.0);
    }
    public static double getTa(){
        return ta.getDouble(0.0);
    }
    public static double getAngleRate(){
        return gyro.getRate();
    }
    public static double getAngle(){
        return constrainAngle(gyro.getAngle());
    }
    public static double getLeftY(){//forward+, backward-
        double joy = -deadband(xbox.getY(KLEFT));
        SmartDashboard.putNumber("LJoyY", joy);
        return joy;
    }
    public static double getLeftX(){//right+, left-
        double joy = deadband(xbox.getX(KLEFT));
        SmartDashboard.putNumber("LJoyX", joy);
        return joy;
    }
    public static double getRightY(){
        double joy = -deadband(xbox.getY(KRIGHT));
        SmartDashboard.putNumber("RJoyY", joy);
        return joy;
    }
    public static double getRightX(){
        double joy = deadband(xbox.getX(KRIGHT));
        SmartDashboard.putNumber("RJoyX", joy);
        return joy;
    }
    public static boolean getRightBumper(){
        return xbox.getBumper(KRIGHT);
    }
    public static boolean getLeftBumper(){
        return xbox.getBumper(KLEFT);
    }
    public static boolean getAButton(){
        return xbox.getAButton();
    }
    public static boolean getXButton(){
        return xbox.getXButton();
    }
    public static boolean getYButton(){
        return xbox.getYButton();
    }
    public static boolean getBButton(){
        return xbox.getBButton();
    }
    public static boolean getButton(int button){
        return xbox.getRawButton(button);
    }
    public static double getRightTrigger(){
        return xbox.getTriggerAxis(KRIGHT);
    }
    public static double getLeftTrigger(){
        return xbox.getTriggerAxis(KLEFT);
    }
    public static boolean getLeftStick(){
        return xbox.getStickButton(KLEFT);
    }
    public static boolean getRightStick(){
        return xbox.getStickButton(KRIGHT);
    }
    public static boolean getRightStickReleased(){
        return xbox.getStickButtonReleased(KRIGHT);
    }
    public static boolean getBackButton(){
        return backDebouncer.get();
    }
    public static boolean getStartButton(){
        return startDebouncer.get();
    }
    public static int getLift(){
        return liftEncoder.get();
    }
    public static int getLeftDrive(){
        return dEncoderL.get();
    }
    public static int getRightDrive(){
        return dEncoderR.get();
    }
    public static boolean getDown(){
        return sDown.get();
    }
    public static boolean getUp(){
        return sUp.get();
    }

    public static void displayStats(){
        SmartDashboard.putBoolean("Top", Input.sUp.get());
		SmartDashboard.putBoolean("Down", Input.sDown.get());
		SmartDashboard.putNumber("RightDriveEncoder", Input.dEncoderR.get());
		SmartDashboard.putNumber("LeftDriveEncoder", Input.dEncoderL.get());
		SmartDashboard.putNumber("LiftEncoder", Input.liftEncoder.get());
		SmartDashboard.putNumber("TimeTotal", Timer.getFPGATimestamp());
		SmartDashboard.putNumber("TimeLeft", Timer.getMatchTime());
		SmartDashboard.putNumber("accelX", Input.accel.getX());
		SmartDashboard.putNumber("accelY", Input.accel.getY());
		SmartDashboard.putNumber("accelZ", Input.accel.getZ());
		SmartDashboard.putNumber("Gyro", Input.getAngle());
        SmartDashboard.putNumber("GyroR", Input.getAngleRate());
        SmartDashboard.putNumber("tx", getTx());
        SmartDashboard.putNumber("ty", getTy());
        SmartDashboard.putNumber("ta", getTa());
    }
}