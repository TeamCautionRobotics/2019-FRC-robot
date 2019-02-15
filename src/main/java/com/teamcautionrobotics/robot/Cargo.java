
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;

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

    // true if out, false if in.
    private boolean currentFunnelRollerState;

    public Cargo(int funnelRollerPort, int deployFunnelRollerPort, int deployExitFlapPort) {
        cargoMover = new VictorSP(funnelRollerPort);
        funnelRollerDeployer = new Solenoid(deployFunnelRollerPort);
        exitFlapDeployer = new Solenoid(deployExitFlapPort);
        currentFunnelRollerState = false;
    }

    public void intake(double power) {
        cargoMover.set(power);
    }

    public void intake(CargoMoverSetting funnelRollerSetting) {
        intake(funnelRollerSetting.power);
    }

    public void toggleFunnelRoller() {
        funnelRollerDeployer.set(!currentFunnelRollerState);
    }

    public void deployExitFlap(boolean goingUp) {
        exitFlapDeployer.set(goingUp);
    }
}