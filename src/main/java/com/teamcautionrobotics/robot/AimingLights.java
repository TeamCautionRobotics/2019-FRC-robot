
package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.*;

public class AimingLights {
   private final Relay FlashlightRelay1;
   private final Relay FlashlightRelay2;

   public AimingLights(int port1, int port2) {
      FlashlightRelay1 = new Relay(port1);
      FlashlightRelay2 = new Relay(port2);
   }

   public void set(boolean on) {
      if (on) {
         FlashlightRelay1.set(Relay.Value.kOn);
         FlashlightRelay2.set(Relay.Value.kOn);
      } else {
         FlashlightRelay1.set(Relay.Value.kOff);
         FlashlightRelay2.set(Relay.Value.kOff);
      }
   }
}
