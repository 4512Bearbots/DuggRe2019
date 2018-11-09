package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
public class Input{
    /* Sensors */
    public static BuiltInAccelerometer gyro;
	public static DigitalInput sUp;
    public static DigitalInput sDown;
    public static Encoder dEncoderL;
	public static Encoder dEncoderR;
    public static Encoder liftEncoder;
    
    /* Controls */
	public static XboxController xbox; //object for controller --more buttons :)
	public static Debouncer uDebouncer; //d-pad doesn't return values lightning fast
	public static Debouncer dDebouncer; //define buttons to only return every period
    private static Hand KLEFT = GenericHID.Hand.kLeft; //constant referring to
    private static Hand KRIGHT = GenericHID.Hand.kRight;//the side of controller

    public Input(){
        /* Controls' assignment*/
		Input.xbox = new XboxController(0);
		Input.uDebouncer = new Debouncer(xbox, 0f, 0.25);
        Input.dDebouncer = new Debouncer(xbox, 180f, 0.25);
        
        /* Sensor assignment *///code matches electrical
		Input.dEncoderL = new Encoder(4, 5);
		Input.dEncoderR = new Encoder(2, 3);
		Input.liftEncoder = new Encoder(6, 7);
		Input.gyro = new BuiltInAccelerometer();
		Input.sUp = new DigitalInput(1);
        Input.sDown = new DigitalInput(8);        
    }

    public static void init(){
        liftEncoder.reset();
		dEncoderL.reset();
		dEncoderR.reset();
    }
}