
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.*;

public class ExpanderHatch{
    
    // pneumatics object push out
    private Solenoid extender;
    // pneumatics object pull back
    private Solenoid expander;

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
