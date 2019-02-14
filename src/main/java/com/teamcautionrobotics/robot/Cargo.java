
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.*;

public class Cargo {
    //motor object
    private final VictorSP funnelRoller;
    //pneumatics object
    Solenoid deployFunnelRoller;
    Solenoid deployExitFlap;

    boolean deployedFunnelRoller = false;

    public Cargo(int funnelRollerPort, int backRollerConveyorPort, int deployFunnelRollerPort, int deployExitFlapPort) {
        funnelRoller = new VictorSP(funnelRollerPort);
        deployFunnelRoller = new Solenoid(deployFunnelRollerPort);
        deployExitFlap = new Solenoid(deployExitFlapPort);
    }

    public void intake(double power) {
        funnelRoller.set(power);
    }

    }

    public void deployFunnelRoller(boolean toggleFunnelRoller) {
        if((lastToggleFunnelRoller == false) && (toggleFunnelRoller == true)){
            deployedFunnelRoller = !deployedFunnelRoller;
        }
        deployFunnelRoller.set(deployedFunnelRoller);
        boolean lastToggleFunnelRoller = toggleFunnelRoller;
    }

    public void deployExitFlap(boolean exitFlapStatus){
        deployExitFlap.set(exitFlapStatus)
    }
}
}