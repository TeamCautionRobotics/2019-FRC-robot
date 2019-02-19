package com.teamcautionrobotics.misc2019;

import java.util.function.BooleanSupplier;

/**
 * ButtonPressRunner
 */
public class ButtonPressRunner {
    private final BooleanSupplier getButton;
    private final Runnable runnable;
    boolean lastButtonState;

    /**
     * Calls runnable when the button value, accessed by getButton
     * goes from false to true.
     * 
     * @param getButton returns the state of the button controlling the runnable
     * @param runnable called when the button is pressed
     */
    public ButtonPressRunner(BooleanSupplier getButton, Runnable runnable) {
        this.getButton = getButton;
        this.runnable = runnable;
    }

    /**
     * Call this method to trigger the button to be checked.
     * This probably belongs in robotPeriodic() or teleopPeriodic()
     */
    public void update() {
        if (!lastButtonState && getButton.getAsBoolean()) {
            runnable.run();
        }
        lastButtonState = getButton.getAsBoolean();
    }
}