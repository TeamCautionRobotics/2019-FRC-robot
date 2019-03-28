package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class ExpanderHatch {

    // pneumatics objects
    // reacher extends away from the robot
    // grabber piston applies friction to the hatch by expanding
    private final Solenoid reacher;
    private final Solenoid grabber;

    private final DoubleSolenoid doubleReacher;
    private final DoubleSolenoid doubleGrabber;

    private final boolean usingDoubleSolenoids;

    // true is out
    private boolean reacherState;
    private boolean grabberState;

    public ExpanderHatch(int reacherPort, int grabberPort) {
        reacher = new Solenoid(reacherPort);
        grabber = new Solenoid(grabberPort);

        doubleGrabber = null;
        doubleReacher = null;
        usingDoubleSolenoids = false;
    }

    public ExpanderHatch(int reacherForwardChannel, int reacherBackwardChannel, int grabberForwardChannel,
            int grabberBackwardChannel) {
        reacher = null;
        grabber = null;

        doubleReacher = new DoubleSolenoid(reacherForwardChannel, reacherBackwardChannel);
        doubleGrabber = new DoubleSolenoid(grabberForwardChannel, grabberBackwardChannel);
        usingDoubleSolenoids = true;
    }

    public void reach(boolean out) {
        if (usingDoubleSolenoids) {
            doubleReacher.set((out) ? Value.kForward : Value.kReverse);
        } else {
            reacher.set(out);
        }
        reacherState = out;
    }

    public void toggleReacher() {
        reach(!reacherState);
    }

    public void grab(boolean out) {
        if (usingDoubleSolenoids) {
            doubleGrabber.set((out) ? Value.kForward : Value.kReverse);
        } else {
            grabber.set(out);
        }
        grabberState = out;
    }

    public void toggleGrabber() {
        grab(!grabberState);
    }
}
