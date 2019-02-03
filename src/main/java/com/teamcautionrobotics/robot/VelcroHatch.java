
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.*;

public class VelcroHatch {
    // motor object
    VictorSP winch;
    // pneumatics object
    Solenoid pusher;

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
