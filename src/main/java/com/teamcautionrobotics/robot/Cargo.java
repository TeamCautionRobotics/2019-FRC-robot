
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.*;

public class Cargo {
    //motor object
    VictorSP funnelRoller;
    Victor backRollerConveyor;
    //pneumatics object
    Solenoid deployFunnelRoller;
    Solenoid deployExitFlap;

    boolean deployedFunnelRoller = false;

    public Cargo(int funnelRollerPort, int backRollerConveyorPort, int deployFunnelRollerPort, int deployExitFlapPort) {
        funnelRoller = new VictorSP(funnelRollerPort);
        backRollerConveyor = new VictorSP(backRollerConveyorPort);
        deployFunnelRoller = new Solenoid(deployFunnelRollerPort);
        deployExitFlap = new Solenoid(deployExitFlapPort);
    }

    public void intake(boolean intake, boolean spitout) {
        if(intake){
            funnelRoller.set(127);
            if(deployedFunnelRoller){
                backRollerConveyor.set(127);
            }
        }
        else if(spitout){
            funnelRoller.set(-127);
                if(deployedFunnelRoller){
                backRollerConveyor.set(-127);
            }        }
        else{
            funnelRoller.set(0);
                if(deployedFunnelRoller){
                backRollerConveyor.set(0);
            }        }

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