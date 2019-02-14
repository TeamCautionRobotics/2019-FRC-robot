
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.*;

public class Cargo {

    public enum FunnelRollerSetting {
        THROUGH(1), BACK(-1), STOP(0);

        public final double power;

        private FunnelRollerSetting(double power) {
            this.power = power;
        }
    }

    //motor object
    private final VictorSP funnelRoller;
    //pneumatics object
    private final Solenoid deployFunnelRoller;
    private final Solenoid deployExitFlap;

    boolean deployedFunnelRoller = false;
    boolean lastToggleFunnelRoller = false;

    public Cargo(int funnelRollerPort, int backRollerConveyorPort, int deployFunnelRollerPort, int deployExitFlapPort) {
        funnelRoller = new VictorSP(funnelRollerPort);
        deployFunnelRoller = new Solenoid(deployFunnelRollerPort);
        deployExitFlap = new Solenoid(deployExitFlapPort);
    }

    public void intake(double power) {
        funnelRoller.set(power);
    }

    public void intake(FunnelRollerSetting funnelRollerSetting) {
        intake(funnelRollerSetting.power);
    }

    public void deployFunnelRoller(boolean toggleFunnelRoller) {
        if(!lastToggleFunnelRoller && toggleFunnelRoller){
            deployedFunnelRoller = !deployedFunnelRoller;
        }
        deployFunnelRoller.set(deployedFunnelRoller);
        lastToggleFunnelRoller = toggleFunnelRoller;
    }

    public void deployExitFlap(boolean exitFlapStatus){
        deployExitFlap.set(exitFlapStatus);
    }
}