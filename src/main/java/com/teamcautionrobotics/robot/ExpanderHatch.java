
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.*;

public class ExpanderHatch{
    
    // pneumatics object push out
    private final Solenoid extender;
    // pneumatics object pull back
    private final Solenoid expander;

    public ExpanderHatch(int extenderPort, int expanderPort) {
        extender = new Solenoid(extenderPort);
        expander = new Solenoid(expanderPort);
    }

    public void deploy(boolean extend) {
        extender.set(extend);
    }
    public void pullback(boolean out) {
        expander.set(out);
    }
}
