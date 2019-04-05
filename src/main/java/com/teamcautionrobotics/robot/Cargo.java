package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;

public class Cargo {
    public enum CargoMoverSetting {
        THROUGH(1), BACK(-1), STOP(0);

        public final double power;

        private CargoMoverSetting(double power) {
            this.power = power;
        }
    }

    // motor object
    private final VictorSP cargoMover;
    // pneumatics object

    // Attached to mechanum wheels in front of the robot that allow for more driver
    // error.
    private final Solenoid funnelRollerDeployer;
    private final Solenoid exitFlapDeployer;
    private final DoubleSolenoid doubleExitFlapDeployer;

    private final boolean usingDoubleSolenoids;

    // true if out, false if in.
    private boolean currentExitFlapState;
    private boolean currentFunnelRollerState;

    public Cargo(int funnelRollerPort, int exitFlapDeployerPort, int funnelRollerDeployerPort) {
        cargoMover = new VictorSP(funnelRollerPort);
        // Positive moves ball up (THROUGH)
        cargoMover.setInverted(true);
        exitFlapDeployer = new Solenoid(exitFlapDeployerPort);
        doubleExitFlapDeployer = null;
        funnelRollerDeployer = new Solenoid(funnelRollerDeployerPort);
        usingDoubleSolenoids = false;
        currentFunnelRollerState = false;
        currentExitFlapState = false;
    }

    public Cargo(int funnelRollerPort, int exitFlapDeployerForwardChannel, int exitFlapDeployerBackwardChannel,
            int funnelRollerDeployerPort) {
        cargoMover = new VictorSP(funnelRollerPort);
        // Positive moves ball up (THROUGH)
        cargoMover.setInverted(true);
        exitFlapDeployer = null;
        doubleExitFlapDeployer = new DoubleSolenoid(exitFlapDeployerForwardChannel, exitFlapDeployerBackwardChannel);
        funnelRollerDeployer = new Solenoid(funnelRollerDeployerPort);
        usingDoubleSolenoids = true;
        currentFunnelRollerState = true;
        currentExitFlapState = false;
    }

    public void intake(double power) {
        cargoMover.set(power);
    }

    public void intake(CargoMoverSetting funnelRollerSetting) {
        intake(funnelRollerSetting.power);
    }

    public void deployExitFlap(boolean goingUp) {
        if (usingDoubleSolenoids) {
            doubleExitFlapDeployer.set(goingUp ? Value.kForward : Value.kReverse);
        } else {
            exitFlapDeployer.set(goingUp);
        }
        currentExitFlapState = goingUp;
    }

    public void toggleExitFlap()
    {
        deployExitFlap(!currentExitFlapState);
    }

    public void setFunnelRollerDeployer(boolean out) {
        funnelRollerDeployer.set(out);
    }

    public void toggleFunnelRoller() {
        setFunnelRollerDeployer(!currentFunnelRollerState);
    }
}