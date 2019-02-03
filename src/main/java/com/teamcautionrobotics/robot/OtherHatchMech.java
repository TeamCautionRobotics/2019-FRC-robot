
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.*;

public class OtherHatch {
    
    // pneumatics object push out
    private Solenoid push;
    // pneumatics object pull back
    private Solenoid pull;
    public OtherHatch(int pushPort, int pullPort) {
        push = new Solenoid(pushPort);
        pull = new Solenoid(pullPort);
    }

    public void deploy(boolean activate) {
        push.set(activate);

    }
    public void deploy(boolean back) {
        pull.set(back);
    }

}
