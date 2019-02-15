
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;

public class VelcroHatch {
    // motor object
    private VictorSP winch;
    // pneumatics object
    private Solenoid pusher;

    public VelcroHatch(int winchPort, int pusherPort) {
        winch = new VictorSP(winchPort);
        pusher = new Solenoid(pusherPort);
    }

    public void rotate(double power) {
        winch.set(power);
    }

    public void deploy(boolean activate) {
        pusher.set(activate);
    }
}
