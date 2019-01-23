package frc.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;

@SuppressWarnings("unused")
public class Debouncer{

    XboxController _xbox;
    int buttonnum;
    double latest;
    double debounce_period;
    float pov;

    public Debouncer(XboxController _xbox, int buttonnum){
        this._xbox = _xbox;
        this.buttonnum = buttonnum;
        this.latest = 0;
        this.debounce_period = .5;
        this.pov = -1.0f;
    }
    public Debouncer(XboxController _xbox, int buttonnum, double d){
        this._xbox = _xbox;
        this.buttonnum = buttonnum;
        this.latest = 0;
        this.debounce_period = d;
        this.pov = -1.0f;
    }
    public Debouncer(XboxController _xbox, float pov, double d){
        this._xbox = _xbox;
        this.latest = 0;
        this.debounce_period = d;
        this.pov = pov;
    }

    public void setDebouncePeriod(float period){
        this.debounce_period = period;
    }

    public boolean get(){
        double now = Timer.getFPGATimestamp();
        if(((int)pov) != -1.0f && _xbox.getPOV() == ((int)pov)) {
        	if((now-latest) > debounce_period){
                latest = now;
                return true;
            }
        }
        else if(((int)pov) == -1.0f && _xbox.getRawButton(buttonnum)){
            if((now-latest) > debounce_period){
                latest = now;
                return true;
            }
        }
        return false;
    }
}
