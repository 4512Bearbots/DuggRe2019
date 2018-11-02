package org.usfirst.frc.team4512.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.BuiltInAccelerometer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
public class MotorBase{
    /** Hardware */
	/* Right Talons */
	static TalonSRX dRightF = new TalonSRX(1);
	static TalonSRX dRightB = new TalonSRX(2);

	/* Left Talons */
	static TalonSRX dLeftF = new TalonSRX(3);
	static TalonSRX dLeftB = new TalonSRX(4);

	/* Lift Victors */
	static VictorSP liftF = new VictorSP(0);
	static VictorSP liftB = new VictorSP(1);

	/* Intake Victors */
	static Spark armR = new Spark(4);
	static Spark armL = new Spark(5);

    /* Sensors */
	public static Encoder dEncoderL;
	public static Encoder dEncoderR;
	public static Encoder liftEncoder;
	public static BuiltInAccelerometer gyro;
	public static DigitalInput sUp;
	public static DigitalInput sDown;

    /* Constants */
	public static double DSPEED;//overall speed affecting robots actions
	public static double DGTIME;//warming for stopping acceleration jerk
	public static double DSTIME;//stopped
	public static double LUTIME;//warming for lift - up
	public static double LDTIME;//down
	public static double LHIGH;//last non-zero lift power 
	public static double FORWARD;//value affecting forward speed in feedforward
	public static double FORWARDH;//last non-zero FORWARD value
	public static double TURN;//value affecting turning in feedforward
	public static int LSTATE;//determine state for executing lift commands
	public static int MAXLIFT;//top of the lift in counts
	
	/* Controls */
	public static XboxController xbox; //object for controller --more buttons :)
	public static Debouncer uDebouncer; //d-pad doesn't return values lightning fast
	public static Debouncer dDebouncer; //define buttons to only return every period
    private static Hand KLEFT = GenericHID.Hand.kLeft; //constant referring to
    private static Hand KRIGHT = GenericHID.Hand.kRight;//the side of controller
    
    public MotorBase(){
        /* Controls' assignment*/
		MotorBase.xbox = new XboxController(0);
		MotorBase.uDebouncer = new Debouncer(xbox, 0f, 0.25);
        MotorBase.dDebouncer = new Debouncer(xbox, 180f, 0.25);
        
        /* Sensor assignment *///code matches electrical
		MotorBase.dEncoderL = new Encoder(4, 5);
		MotorBase.dEncoderR = new Encoder(2, 3);
		MotorBase.liftEncoder = new Encoder(6, 7);
		MotorBase.gyro = new BuiltInAccelerometer();
		MotorBase.sUp = new DigitalInput(1);
        MotorBase.sDown = new DigitalInput(8);
        
        
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
		MotorBase.DSPEED = 0.5;
		MotorBase.LSTATE = 0;
		MotorBase.MAXLIFT = 4300;//actual ~4400
    }
    public static void driveInit(){
		FORWARD=FORWARDH=TURN=DGTIME=DSTIME=LUTIME=LDTIME = 0;
        setDrive(0,0);
        setLift(0);
		liftEncoder.reset();
		dEncoderL.reset();
		dEncoderR.reset();
		System.out.println("--Feed Forward Teleop--");
    }

    public static void drivePeriodic(){
        /* Controller interface => Motors */
		FORWARD = deadband(xbox.getY(KLEFT))*DSPEED;//apply the math to joysticks
		TURN = deadband(xbox.getX(KRIGHT))*DSPEED;

		/* Drive Base */
		setDrive(FORWARD,TURN);
		
		/* Lift */ 
		//stop the lift if bumpers are not pressed
		if(LSTATE!=1 && LSTATE!=2)LSTATE=0;
		//check input
		LSTATE=(xbox.getBumper(KRIGHT))? 3:LSTATE;
		LSTATE=(xbox.getBumper(KLEFT))? 4:LSTATE;
		//reset if pressing switches
		LSTATE=(sUp.get()&&(LSTATE==1||LSTATE==3))?0:LSTATE;
		LSTATE=(sDown.get()&&(LSTATE==2||LSTATE==4))?0:LSTATE;
		if(sDown.get())onDown();//call methods when switches are pressed
		if(sUp.get())onUp();

		if(uDebouncer.get()) LSTATE = 1;//pressing d-pad will automatically
		if(dDebouncer.get()) LSTATE = 2;//move the lift to top or bottom
        
		double liftPercent = (liftEncoder.get()/(double)MAXLIFT)-0.05;
		SmartDashboard.putNumber("LiftPercent", liftPercent);
		switch(LSTATE) {//different states dictate lift speed
		case 1://if up d-pad is pressed, automatically go up
			setLift(encoderMath(liftPercent, 0.9));
			break;
		case 2://if down d-pad is pressed, automatically go down
			//setLift(-encoderMath(liftPercent*Math.min((Math.pow(liftPercent,2)*8+0.4),1), 0.6));
			setLift(-encoderMath(liftPercent,0.6)*interpolate(0.3,3,liftPercent));
			break;
		case 3://if right bumper, lift goes up
			setLift(encoderMath(liftPercent, 1));
			break;
		case 4://if left bumper, lift goes down
			//setLift(-encoderMath(liftPercent*Math.min((Math.pow(liftPercent,2)*8+0.4),1), 0.7));
			setLift(-encoderMath(liftPercent,0.7)*interpolate(0.4,3,liftPercent));
			break;
		default://keep lift still
			if(!sDown.get()) {
				setLift(0.11);//backpressure
				if(liftEncoder.get()<400) LSTATE = 2;//if the lift is low, auto push down
			}else {
				setLift(0);//dont break things if not suspended
			}
			break;
		}
		
		/* Intake */
		double rTrigg = xbox.getTriggerAxis(KRIGHT);
		double lTrigg = xbox.getTriggerAxis(KLEFT);
		if(rTrigg > 0) {
            setArms(rTrigg);
		}
		else if(lTrigg > 0) {
            setArms(-lTrigg);
		}
		else {
			setArms(0.18);
		}
		
		//change speed if buttons are pressed
		DSPEED=(xbox.getAButton())?0.2:DSPEED;
		DSPEED=(xbox.getXButton())?0.3:DSPEED;
		DSPEED=(xbox.getYButton())?0.5:DSPEED;
		DSPEED=(xbox.getBButton())?1.0:DSPEED;
		DSPEED=(liftPercent>0.5 && DSPEED>0.2)?0.2:DSPEED;//if the lift is high up make sure speed is low

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
	static double deadband(double value) {
		double deadzone = 0.15;//smallest amount you can recognize from the controller
		
		/* Inside deadband */
		if ((value >= +deadzone)||(value <= -deadzone)) {
			DSTIME = Timer.getFPGATimestamp();
			return value;
		}else{/* Outside deadband */
			DGTIME = Timer.getFPGATimestamp();
			return 0;
		}
	}
	
	/* Basic Arcade Drive using PercentOutput along with Arbitrary FeedForward supplied by turn */
		//given a forward value and a turn value, will automatically do all the math and appropriately send signals
	public static void setDrive(double forward, double turn){
		if(forward==0){
			forward = interpolate(FORWARDH,0,Timer.getFPGATimestamp()-DSTIME/2.0);
			//forward *= Math.min(1,(Timer.getFPGATimestamp()-DGTIME) + 0.1);
		}else{
			FORWARDH=forward;
			forward *= interpolate(0.1,1,Timer.getFPGATimestamp()-DGTIME/2.0);//for the first ~0.5 seconds after first issuing a movement the drivebase is slowed
			//forward *= Math.min(1,(Timer.getFPGATimestamp()-DGTIME) + 0.1);
		}
		dRightF.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, turn);
		dRightB.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, turn);
		dLeftF.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		dLeftB.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
	}

	public static void setLift(double power){
		if(LSTATE==0&&!sDown.get()&&!sUp.get()){//if the lift wasnt moving
			LUTIME = Timer.getFPGATimestamp();
			power = interpolate(LHIGH,power,(Timer.getFPGATimestamp()-LDTIME)/2.0);	
		}else if(!sDown.get()&&!sUp.get()){
			LDTIME = Timer.getFPGATimestamp();
			power *= interpolate(0.1,1,(Timer.getFPGATimestamp()-LUTIME)/2.0);
			//power *= Math.min(1,(Timer.getFPGATimestamp()-LUTIME) + 0.1);//for the first ~0.5 seconds after first issuing a movement the lift is slowed
			LHIGH = power;
		}
		liftF.set(power);
		liftB.set(power);
    }
    
    public static void setArms(double power){
        armR.set(power);
        armL.set(power);
	}
	
	private static double encoderMath(double x, double n) {//give a lift speed based on how high the lift is
		double k = 0.25;//minimum speed when lift at bottom/top
		//n = Math.min((15*Math.pow((n-0.5),3))+1,1); //lift speed changes with DSPEED
		double y = -(1000*(1-k))*0.25*n*Math.pow((x-0.5), 8)+((1-k)*n)+k;//big equation(slow down on bottom/top of lift) https://www.desmos.com/calculator/mqlbagskqz
		y = Math.max(y, k); //negatives are bad kids
		return y;
	}

	private static double interpolate(double a, double b, double x){//given x as a fraction between a and b
		double math = a+(x*(b-a));
		math = Math.min(math,Math.max(math,0));
		return math;
	}

	//whenever lift switches are pressed, run these methods
	public static void onDown() {
		liftEncoder.reset();//make sure bottoming out sets the encoder to 0
	}
	public static void onUp() {
		//MAXLIFT = liftEncoder.get()-150;
		//System.out.println("Top triggered on: "+liftEncoder.get());
	}
}