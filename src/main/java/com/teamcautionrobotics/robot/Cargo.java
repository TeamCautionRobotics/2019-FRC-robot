
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.VictorSP;

public class Cargo {

    public enum FunnelRollerSetting {
        THROUGH(1), BACK(-1), STOP(0);

        public final double power;

        private FunnelRollerSetting(double power) {
            this.power = power;
        }
    }

    // motor object
    private final VictorSP funnelRoller;
    private final VictorSP conveyorRoller;
    // pneumatics object
    private final Solenoid deployFunnelRoller;
    private final Solenoid deployExitFlap;

    public Cargo(int funnelRollerPort, int conveyorRollerPort, int deployFunnelRollerPort, int deployExitFlapPort) {
        funnelRoller = new VictorSP(funnelRollerPort);
        conveyorRoller = new VictorSP(conveyorRollerPort);
        deployFunnelRoller = new Solenoid(deployFunnelRollerPort);
        deployExitFlap = new Solenoid(deployExitFlapPort);
    }

    public void intake(double power) {
        funnelRoller.set(power);
        conveyorRoller.set(power);
    }

    public void intake(FunnelRollerSetting funnelRollerSetting) {
        intake(funnelRollerSetting.power);
    }

    public void deployFunnelRoller(boolean out) {
        deployFunnelRoller.set(out);
    }

    public void deployExitFlap(boolean exitFlapStatus) {
        deployExitFlap.set(exitFlapStatus);
    }
}