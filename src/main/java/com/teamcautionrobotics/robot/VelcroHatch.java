package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;

public class VelcroHatch {

    private DigitalInput velcroHatchLimitSwitch;
    // motor object
    private final VictorSP winch;
    // pneumatics objects
    private Solenoid pusher;

    public VelcroHatch(int winchPort, int pusherPort, int limitSwitchPort) {
        winch = new VictorSP(winchPort);
        // Positive raises winch
        winch.setInverted(true);
        pusher = new Solenoid(pusherPort);

        velcroHatchLimitSwitch = new DigitalInput(limitSwitchPort);
    }

    public boolean armIsUp(){
        return !velcroHatchLimitSwitch.get();
    }

    public void rotate(double power) {
        winch.set(power);
    }

    public void deploy(boolean activate) {
        pusher.set(activate);
    }
}
