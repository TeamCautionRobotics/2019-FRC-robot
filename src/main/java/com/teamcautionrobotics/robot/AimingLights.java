package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.Relay;

public class AimingLights {

   private final Relay flashlightRelay1;
   private final Relay flashlightRelay2;

   private boolean currentSetting;

   public AimingLights(int port1, int port2) {
      flashlightRelay1 = new Relay(port1);
      flashlightRelay2 = new Relay(port2);

      currentSetting = false;
   }

   public void set(boolean on) {
      if (on) {
         flashlightRelay1.set(Relay.Value.kOn);
         flashlightRelay2.set(Relay.Value.kOn);
      } else {
         flashlightRelay1.set(Relay.Value.kOff);
         flashlightRelay2.set(Relay.Value.kOff);
      }
      currentSetting = on;
   }

   public void changeState() {
      set(!currentSetting);
   }
}
