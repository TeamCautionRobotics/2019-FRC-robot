package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;

public class ExpanderHatch {

    // pneumatics objects
    // reacher extends away from the robot
    // grabber piston applies friction to the hatch by expanding
    private final Solenoid reacher;
    private final Solenoid grabber;

    public ExpanderHatch(int reacherPort, int grabberPort) {
        reacher = new Solenoid(reacherPort);
        grabber = new Solenoid(grabberPort);
    }

    public void reach(boolean out) {
        reacher.set(out);
    }

    public void grab(boolean out) {
        grabber.set(out);
    }
}
