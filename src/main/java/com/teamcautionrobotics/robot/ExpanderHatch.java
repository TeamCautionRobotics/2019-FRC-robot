package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class ExpanderHatch {

    // pneumatics objects
    // reacher extends away from the robot
    // grabber piston applies friction to the hatch by expanding
    private Solenoid reacher;
    private Solenoid grabber;

    private DoubleSolenoid doubleReacher;
    private DoubleSolenoid doubleGrabber;

    private boolean usingDoubleSolenoids;

    // true is out
    private boolean reacherState;
    private boolean grabberState;

    public ExpanderHatch(int reacherPort, int grabberPort) {
        reacher = new Solenoid(reacherPort);
        grabber = new Solenoid(grabberPort);
        usingDoubleSolenoids = false;
    }

    public ExpanderHatch(int reacherForwardChannel, int reacherBackwardChannel, int grabberForwardChannel, int grabberBackwardChannel) {
        doubleReacher = new DoubleSolenoid(reacherForwardChannel, reacherBackwardChannel);
        doubleGrabber = new DoubleSolenoid(grabberForwardChannel, grabberBackwardChannel);
        usingDoubleSolenoids = true;
    }

    public void reach(boolean out) {
        if (usingDoubleSolenoids) {
            doubleReacher.set((out) ? Value.kForward : Value.kOff);
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
            doubleGrabber.set((out) ? Value.kForward : Value.kOff);
        } else {
            grabber.set(out);
        }
        grabberState = out;
    }

    public void toggleGrabber() {
        grab(!grabberState);
    }
}
