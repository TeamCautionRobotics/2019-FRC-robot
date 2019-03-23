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
    private Solenoid exitFlapDeployer;
    private DoubleSolenoid doubleExitFlapDeployer;

    private boolean usingDoubleSolenoids;

    // true if out, false if in.
    private boolean currentFunnelRollerState;

    public Cargo(int funnelRollerPort, int exitFlapDeployerPort, int funnelRollerDeployerPort) {
        cargoMover = new VictorSP(funnelRollerPort);
        // Positive moves ball up (THROUGH)
        cargoMover.setInverted(true);
        exitFlapDeployer = new Solenoid(exitFlapDeployerPort);
        funnelRollerDeployer = new Solenoid(funnelRollerDeployerPort);
        usingDoubleSolenoids = false;
        currentFunnelRollerState = false;
    }

    public Cargo(int funnelRollerPort, int exitFlapDeployerForwardChannel, int exitFlapDeployerBackwardChannel,
            int funnelRollerDeployerPort) {
        cargoMover = new VictorSP(funnelRollerPort);
        // Positive moves ball up (THROUGH)
        cargoMover.setInverted(true);
        doubleExitFlapDeployer = new DoubleSolenoid(exitFlapDeployerForwardChannel, exitFlapDeployerBackwardChannel);
        funnelRollerDeployer = new Solenoid(funnelRollerDeployerPort);
        usingDoubleSolenoids = true;
        currentFunnelRollerState = true;
    }

    public void intake(double power) {
        cargoMover.set(power);
    }

    public void intake(CargoMoverSetting funnelRollerSetting) {
        intake(funnelRollerSetting.power);
    }

    public void deployExitFlap(boolean goingUp) {
        if (usingDoubleSolenoids) {
            doubleExitFlapDeployer.set((goingUp) ? Value.kForward : Value.kReverse);
        } else {
            exitFlapDeployer.set(goingUp);
        }
    }

    public void setFunnelRollerDeployer(boolean out) {
        funnelRollerDeployer.set(out);
    }

    public void toggleFunnelRoller() {
        setFunnelRollerDeployer(!currentFunnelRollerState);
    }
}