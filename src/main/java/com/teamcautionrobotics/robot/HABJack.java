package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;

public class HABJack {

    private final Solenoid leftJack;
    private final Solenoid rightJack;

    // true if propping up
    private boolean currentState;

    public HABJack(int leftJackPort, int rightJackPort) {
        leftJack = new Solenoid(leftJackPort);
        rightJack = new Solenoid(rightJackPort);
    }

    public void jack(boolean up) {
        leftJack.set(up);
        rightJack.set(up);
        currentState = up;
    }

    public void switchState() {
        jack(!currentState);
    }
}