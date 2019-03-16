package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;

public class VelcroHatch {

    private DigitalInput velcroHatchLimitSwitch;
    // motor object
    private final VictorSP winch;
    // pneumatics objects
    private Solenoid pusher;
    private DoubleSolenoid doublePusher;

    private boolean usingDoubleSolenoids;

    public VelcroHatch(int winchPort, int pusherPort, int limitSwitchPort) {
        winch = new VictorSP(winchPort);
        // Positive raises winch
        winch.setInverted(true);
        pusher = new Solenoid(pusherPort);

        velcroHatchLimitSwitch = new DigitalInput(limitSwitchPort);
        usingDoubleSolenoids = false;
    }

    public VelcroHatch(int winchPort, int pusherForwardChannel, int pusherReverseChannel, int limitSwitchPort) {
        winch = new VictorSP(winchPort);
        // Positive raises winch
        winch.setInverted(true);
        doublePusher = new DoubleSolenoid(pusherForwardChannel, pusherReverseChannel);

        velcroHatchLimitSwitch = new DigitalInput(limitSwitchPort);
        usingDoubleSolenoids = true;
    }

    public void rotate(double power) {
        winch.set(power);
    }

    public void deploy(boolean activate) {
        if (usingDoubleSolenoids) {
            doublePusher.set((activate) ? Value.kForward : Value.kReverse);
        } else {
            pusher.set(activate);
        }
    }

    public boolean armIsUp() {
        return !velcroHatchLimitSwitch.get();
    }
}
