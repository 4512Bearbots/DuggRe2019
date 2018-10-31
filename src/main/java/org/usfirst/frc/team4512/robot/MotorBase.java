package org.usfirst.frc.team4512.robot;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
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
public class MotorBase{
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

    /* Sensors */
	public Encoder dEncoderL;
	public Encoder dEncoderR;
	public Encoder liftEncoder;
	public BuiltInAccelerometer gyro;
	public DigitalInput sUp;
	public DigitalInput sDown;

    /* Constants */
	public double DSPEED;//overall speed affecting robots actions
	public double WTIME;//warming for stopping acceleration jerk
	public double FORWARD; //value affecting forward speed in feedforward
	public double TURN; //value affecting turning in feedforward
	public int LSTATE;//determine state for executing lift commands
	public int MAXLIFT;//top of the lift in counts
	
	/* Controls */
	public XboxController xbox; //object for controller --more buttons :)
	public Debouncer uDebouncer; //d-pad doesn't return values lightning fast
	public Debouncer dDebouncer; //define buttons to only return every period
    private Hand KLEFT = GenericHID.Hand.kLeft; //constant referring to
    private Hand KRIGHT = GenericHID.Hand.kRight;//the side of controller
    
    public MotorBase(){
        /* Controls' assignment*/
		this.xbox = new XboxController(0);
		this.uDebouncer = new Debouncer(xbox, 0f, 0.25);
        this.dDebouncer = new Debouncer(xbox, 180f, 0.25);
        
        /* Sensor assignment *///code matches electrical
		this.dEncoderL = new Encoder(4, 5);
		this.dEncoderR = new Encoder(2, 3);
		this.liftEncoder = new Encoder(6, 7);
		this.gyro = new BuiltInAccelerometer();
		this.sUp = new DigitalInput(1);
        this.sDown = new DigitalInput(8);
        
        
		/* Disable motor controllers */
		setDrive(0, 0);
		
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
		this.DSPEED = 0.5;
		this.LSTATE = 0;
		this.MAXLIFT = 4200;
    }
    public void driveInit(){
        FORWARD=TURN=WTIME = 0;
        setDrive(0,0);
        setLift(0);
		liftEncoder.reset();
		dEncoderL.reset();
		dEncoderR.reset();
		System.out.println("--Feed Forward Teleop--");
    }

    public void drivePeriodic(){
        /* Controller interface => Motors */
		FORWARD = deadband(xbox.getY(KLEFT))*DSPEED*warming();//apply the math to joysticks
		TURN = deadband(xbox.getX(KRIGHT))*DSPEED;

		/* Drive Base */
		setDrive(FORWARD,TURN);
		
		/* Lift */ 
		//persisting lift motion, or reset when not pressed
		if(LSTATE!=1 && LSTATE!=2)LSTATE=0;
		//check input
		LSTATE=(xbox.getBumper(KRIGHT) && !sUp.get())? 3:0;
		LSTATE=(xbox.getBumper(KLEFT) && !sDown.get())? 4:0;
		
		if(sDown.get())onDown();//call methods when switches are pressed
		if(sUp.get())onUp();

		if(uDebouncer.get()) LSTATE = 1;//pressing d-pad will automatically
		if(dDebouncer.get()) LSTATE = 2;//move the lift to top or bottom
        
        double liftPercent = ((double)liftEncoder.get()-100/MAXLIFT);
		switch(LSTATE) {
		case 1:
			setLift(encoderMath(liftPercent, 0.9));
			LSTATE=(sUp.get())? 0:LSTATE;
			break;
		case 2:
			setLift(-encoderMath(liftPercent, 0.6));
			LSTATE=(sDown.get())? 0:LSTATE;
			break;
		case 3:
			setLift(encoderMath(liftPercent, 1));
			break;
		case 4:
			setLift(-encoderMath(liftPercent, 0.7));
			break;
		default:
			if(!sDown.get()) {
				setLift(0.11);
				if(liftEncoder.get()<400) LSTATE = 2;
			}else {
				setLift(0);
			}
			break;
		}
		
		/* Intake */
		if(xbox.getTriggerAxis(KRIGHT) > 0) {
            setArms(xbox.getTriggerAxis(KRIGHT));
		}
		else if(xbox.getTriggerAxis(KLEFT) > 0) {
            setArms(-xbox.getTriggerAxis(KLEFT));
		}
		else {
			setArms(0.18);
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
    }

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
	
	public double warming() {
		SmartDashboard.putNumber("Warming", Timer.getFPGATimestamp()-WTIME+0.1);
		return (double)((WTIME==0)?1:Math.min(1, ((Timer.getFPGATimestamp()-WTIME)+0.1)));
	}
	
	/* Basic Arcade Drive using PercentOutput along with Arbitrary FeedForward supplied by turn */
		//given a forward value and a turn value, will automatically do all the math and appropriately send signals
	public void setDrive(double forward, double turn){
		dRightF.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, turn);
		dRightB.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, turn);
		dLeftF.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		dLeftB.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
	}

	public void setLift(double power){
		liftF.set(power);
		liftB.set(power);
    }
    
    public void setArms(double power){
        armR.set(power);
        armL.set(power);
    }
	//return new value after applying to curve
	public static double encoderMath(double x, double n) {
		double k = 0.25;//minimum speed when bottom/top
		//n = Math.min((15*Math.pow((n-0.5),3))+1,1); //lift speed changes with DSPEED
		double y = -(1000*(1-k))*0.25*n*Math.pow((x-0.5), 8)+((1-k)*n)+k;//big equation(slow down on bottom/top of lift)
		y = Math.max(y, 0.25);
		return y;
	}
	
	public void onDown() {
		liftEncoder.reset();
	}
	public void onUp() {
		//MAXLIFT = liftEncoder.get()-150;
		//System.out.println("Top triggered on: "+liftEncoder.get());
	}
}