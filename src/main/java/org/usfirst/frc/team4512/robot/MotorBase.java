package org.usfirst.frc.team4512.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
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

    /* Constants */
	public static double dSpeed;//overall speed affecting robots actions
	public static double dgTime;//warming for stopping acceleration jerk
	public static double dsTime;//stopped
	public static double tgTime;//same for turn
	public static double tsTime;
	public static double luTime;//warming for lift - up
	public static double ldTime;//down
	public static double lHigh;//last non-zero lift power 
	public static double dForward;//value affecting forward speed in feedforward
	public static double dForwardH;//last non-zero FORWARD value
	public static double dTurn;//value affecting turning in feedforward
	public static double dTurnH;//last non-zero TURN value
	public static int lState;//determine state for executing lift commands
	public static final int MAXLIFT = 4300;//top of the lift in counts(actual ~4400)
	    
    public MotorBase(){        
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
		MotorBase.dSpeed = 0.5;
		MotorBase.lState = 0;
    }
    public static void driveInit(){
		dForward=dForwardH=dTurn=dgTime=dsTime=luTime=ldTime = 0;
        setDrive(0,0);
		setLift(0);
		Input.init();
		System.out.println("--Feed Forward Teleop--");
    }

    public static void drivePeriodic(){
        /* Controller interface => Motors */
		dForward = deadband(Input.getLeftY());//apply the math to joysticks
		dTurn = deadband(Input.getRightX());

		/* Drive Base */
		setDrive(dForward,dTurn);
		
		/* Lift */ 
		//stop the lift if bumpers are not pressed
		if(lState!=1 && lState!=2)lState=0;
		//check input
		lState=(Input.getRightBumper())? 3:lState;
		lState=(Input.getLeftBumper())? 4:lState;
		//reset if pressing switches
		if(Input.sDown.get())onDown();//call methods when switches are pressed
		if(Input.sUp.get())onUp();

		if(Input.uDebouncer.get()) lState = 1;//pressing d-pad will automatically
		if(Input.dDebouncer.get()) lState = 2;//move the lift to top or bottom

		lState=(Input.sUp.get()&&(lState==1||lState==3))? 0:lState;
		lState=(Input.sDown.get()&&(lState==2||lState==4))? 0:lState;
        
		double liftPercent = (Input.getLift()/(double)MAXLIFT)-0.025;
		SmartDashboard.putNumber("LiftPercent", liftPercent);
		switch(lState) {//different states dictate lift speed
		case 1://if up d-pad is pressed, automatically go up
			setLift(encoderMath(liftPercent, 1));
			break;
		case 2://if down d-pad is pressed, automatically go down
			setLift(-encoderMath(liftPercent,0.75)*interpolate(0.3,5,liftPercent));
			break;
		case 3://if right bumper, lift goes up
			setLift(encoderMath(liftPercent, 1));
			break;
		case 4://if left bumper, lift goes down
			setLift(-encoderMath(liftPercent,0.75)*interpolate(0.3,5,liftPercent));
			break;
		default://keep lift still
			if(!Input.sDown.get()) {
				setLift(0.11);//backpressure
				if(Input.getLift()<400) lState = 2;//if the lift is low, auto push down
			}else {
				setLift(0);//dont break things if not suspended
			}
			break;
		}
		
		/* Intake */
		double rTrigg = Input.getRightTrigger();
		double lTrigg = Input.getLeftTrigger();
		if(rTrigg > 0) setArms(rTrigg);
		else if(lTrigg > 0) setArms(-lTrigg);
		else setArms(0.18);
		
		/* Drive <-> Lift */
		//change speed if buttons are pressed
		//dSpeed=(Input.getAButton())?0.2:dSpeed;
		dSpeed = (Input.getXButton())? 0.3:dSpeed;
		dSpeed = (Input.getYButton())? 0.5:dSpeed;
		dSpeed = (Input.getBButton())? 1.0:dSpeed;
		if(liftPercent>0.4){//slow speed when lift high
			dSpeed=interpolate(0.25,0.4,1-liftPercent);
		}else if(liftPercent<0.4){
			dSpeed=(dSpeed<0.3)? 0.3:dSpeed;
		}
		dSpeed=Math.round(dSpeed*100)/100.0;
		/* D-Pad debouncers(doesnt activate 3 times per click) */
		/*if((dSpeed+0.25) < 1 && Input.uDebouncer.get()) {
			dSpeed = Math.nextUp(dSpeed+0.25);
			//System.out.println("dSpeed: "+dSpeed);
		}
		else if((dSpeed-0.25) > 0 && Input.dDebouncer.get()) {
			dSpeed = Math.nextDown(dSpeed-0.25);
			//System.out.println("dSpeed: "+dSpeed);
		}*/
    }

    /** deadband ? percent, used on the gamepad */
	private static double deadband(double value) {
		double deadzone = 0.15;//smallest amount you can recognize from the controller
		
		/* Inside deadband */
		if ((value >= +deadzone)||(value <= -deadzone)) {
			return value;
		}else{/* Outside deadband */
			return 0;
		}
	}
	
	/* Basic Arcade Drive using PercentOutput along with Arbitrary FeedForward supplied by turn */
		//given a forward value and a turn value, will automatically do all the math and appropriately send signals
	public static void setDrive(double forward, double turn){
		double warmMult = interpolate(.3,.8,dSpeed)*10;
		if(forward==0){
			dgTime = Timer.getFPGATimestamp();
			forward = interpolate(0,dForwardH,((dsTime+1)-Timer.getFPGATimestamp())*warmMult);
		}else{
			dForwardH=forward;
			dsTime = Timer.getFPGATimestamp();
			forward *= interpolate(0.1,1,(Timer.getFPGATimestamp()-dgTime)*warmMult);//for the first ~0.5 seconds after first issuing a movement the drivebase is slowed
		}
		if(turn==0){
			tgTime = Timer.getFPGATimestamp();
			turn = interpolate(0,dTurnH,((tsTime+1)-Timer.getFPGATimestamp())*warmMult);
		}else{
			dTurnH = turn;
			tsTime = Timer.getFPGATimestamp();
			turn *= interpolate(0.1,1,(Timer.getFPGATimestamp()-tgTime)*warmMult);//for the first ~0.5 seconds after first issuing a movement the drivebase is slowed
		}
		forward*=dSpeed;
		turn*=dSpeed;
		SmartDashboard.putNumber("Forward", forward);
		SmartDashboard.putNumber("Turn", turn);
		SmartDashboard.putNumber("DMult", warmMult);
		dRightF.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, turn);
		dRightB.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, turn);
		dLeftF.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
		dLeftB.set(ControlMode.PercentOutput, forward, DemandType.ArbitraryFeedForward, -turn);
	}

	public static void setLift(double power){
		if(!Input.sUp.get() && !Input.sDown.get()){
			if(lState==0){
				luTime = Timer.getFPGATimestamp();//reference time for starting
				//power = interpolate(lHigh,power,(Timer.getFPGATimestamp()-ldTime));
				power = interpolate(power,lHigh,(ldTime+1)-Timer.getFPGATimestamp());	
			}else{
				ldTime = Timer.getFPGATimestamp();//reference time for stopping
				power *= interpolate(0.1,1,(Timer.getFPGATimestamp()-luTime));
				lHigh = power;//if the lift stops it slows down from this speed(not max)
			}
		}
		power = Math.round(power*1000)/1000.0;
		SmartDashboard.putNumber("LiftSpeed", power);
		liftF.set(power);
		liftB.set(power);
    }
    
    public static void setArms(double power){
        armR.set(power);
        armL.set(power);
	}
	
	private static double encoderMath(double x, double n) {//give a lift speed based on how high the lift is
		double k = 0.25;//minimum speed when lift at bottom/top
		double y = -(1000*(1-k))*0.25*n*Math.pow((x-0.5), 8)+((1-k)*n)+k;//big equation(slow down on bottom/top of lift) https://www.desmos.com/calculator/mqlbagskqz
		y = Math.max(y, k); //negatives are bad kids
		return y;
	}

	private static double interpolate(double a, double b, double x){//given x as a fraction between a and b
		double math = a+(x*(b-a));
		if(a>b){
			double hold = a;
			a = b;
			b = hold;
		}
		math = limit(limit(0,1,a),limit(0,1,b),math);
		return math;
	}

	private static double limit(double min, double max, double x){//limit
		return (x>max)?max:Math.max(x,min);
	}

	//whenever lift switches are pressed, run these methods
	public static void onDown() {
		Input.liftEncoder.reset();//make sure bottoming out sets the encoder to 0
	}
	public static void onUp() {
		//MAXLIFT = Input.getLift()-150;
		//System.out.println("Top triggered on: "+Input.getLift());
	}
}