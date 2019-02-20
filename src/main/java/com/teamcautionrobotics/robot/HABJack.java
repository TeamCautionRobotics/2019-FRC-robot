package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;

public class HABJack {

    private final Solenoid jack;

    // true if propping up
    private boolean currentState;

    public HABJack(int jackPort) {
        jack = new Solenoid(jackPort);
    }

    public void setJack(boolean up) {
        jack.set(up);
        currentState = up;
    }

    public void toggleJack() {
        setJack(!currentState);
    }
}