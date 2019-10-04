package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;

public class HABJack {

    private final Solenoid frontJack;
    private final Solenoid backJack;

    // true if propping up
    private boolean currentFrontState;
    private boolean currentBackState;

    public HABJack(int frontJackPort, int backJackPort) {
        frontJack = new Solenoid(frontJackPort);
        backJack = new Solenoid(backJackPort);
        currentFrontState = false;
        currentBackState = false;
    }

    public void setFrontJack(boolean up) {
        frontJack.set(up);
        currentFrontState = up;
    }

    public void setBackJack(boolean up) {
        backJack.set(up);
        currentBackState = up;
    }

    public void toggleFrontJack() {
        setFrontJack(!currentFrontState);
    }

    public void toggleBackJack() {
        setBackJack(!currentBackState);
    }
}