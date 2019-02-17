package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;

public class VelcroHatch {

    // motor object
    private final VictorSP winch;
    // pneumatics objects
    private Solenoid pusher1;
    private Solenoid pusher2;
    private Solenoid pusher3;

    public VelcroHatch(int winchPort, int pusherPort1, int pusherPort2, int pusherPort3) {
        winch = new VictorSP(winchPort);
        pusher1 = new Solenoid(pusherPort1);
        pusher2 = new Solenoid(pusherPort2);
        pusher3 = new Solenoid(pusherPort3);
    }

    public void rotate(double power) {
        winch.set(power);
    }

    public void deploy(boolean activate) {
        pusher1.set(activate);
        pusher2.set(activate);
        pusher3.set(activate);
    }
}
