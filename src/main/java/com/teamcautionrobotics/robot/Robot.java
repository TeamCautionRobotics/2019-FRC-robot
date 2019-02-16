/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.teamcautionrobotics.robot;

import com.teamcautionrobotics.misc2019.EnhancedJoystick;
import com.teamcautionrobotics.misc2019.Gamepad;
import com.teamcautionrobotics.misc2019.Gamepad.Axis;
import com.teamcautionrobotics.misc2019.Gamepad.Button;
import com.teamcautionrobotics.robot.Cargo.CargoMoverSetting;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;

public class Robot extends TimedRobot {

    EnhancedJoystick driverLeft, driverRight;
    Gamepad manipulator;

    DriveBase driveBase;
    VelcroHatch velcroHatch;
    ExpanderHatch expanderHatch;
    Cargo cargo;
    HABJack habJack;

    AimingLights aimingLights;
    Timer timer;

    // This is for the VelcroHatch mechanism.
    boolean deployButtonPressed = false;

    // These are for the Expander Hatch mechanism.
    boolean reacherButtonPressed = false;
    boolean grabberButtonPressed = false;

    boolean jackButtonPressed = false;

    boolean deployedFunnelRoller = false;
    boolean aimingLightsButtonPressed = false;

    private final boolean USING_VELCRO_HATCH = true;

    // Time the robot should drive backwards for after deploying the hatch
    private final double HATCH_DEPLOY_DRIVEBACK_TIME = 0.25;

    /**
     * This function is run when the robot is first started up and should be used
     * for any initialization code.
     */
    @Override
    public void robotInit() {
        driverLeft = new EnhancedJoystick(0);
        driverRight = new EnhancedJoystick(1);
        manipulator = new Gamepad(2);

        // pneumatic ports are not finalized
        driveBase = new DriveBase(0, 1, 0, 1, 2, 3);
        velcroHatch = new VelcroHatch(2, 1);
        expanderHatch = new ExpanderHatch(4, 5);
        cargo = new Cargo(3, 2, 3);
        habJack = new HABJack(6, 7);

        aimingLights = new AimingLights(0, 1);
        timer = new Timer();

        CameraServer.getInstance().startAutomaticCapture(0);
        CameraServer.getInstance().startAutomaticCapture(1);
    }

    /**
     * This function is called every robot packet, no matter the mode. Use this for
     * items like diagnostics that you want ran during disabled, autonomous,
     * teleoperated and test.
     *
     * <p>
     * This runs after the mode specific periodic functions, but before LiveWindow
     * and SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        double driveLeftCommand = driverLeft.getY();
        double driveRightCommand = driverRight.getY();

        if (USING_VELCRO_HATCH) {
            double armPower = .5 + .5 * manipulator.getAxis(Axis.LEFT_TRIGGER);
            velcroHatch.rotate(armPower);

            if (manipulator.getButton(Button.B)) {
                velcroHatch.deploy(manipulator.getButton(Button.B));
                deployButtonPressed = true;
            } else {
                if (deployButtonPressed) {
                    deployButtonPressed = false;

                    timer.reset();
                    timer.start();
                }
                velcroHatch.deploy(false);
            }
            // Counts for .25 of a second
            if (timer.get() > 0 && timer.get() < HATCH_DEPLOY_DRIVEBACK_TIME) {
                driveLeftCommand = -1;
                driveRightCommand = -1;
            }
        } else {
            if (manipulator.getButton(Button.A) != reacherButtonPressed) {
                reacherButtonPressed = manipulator.getButton(Button.A);
                expanderHatch.reach(reacherButtonPressed);
            }

            if (manipulator.getButton(Button.B) != grabberButtonPressed) {
                grabberButtonPressed = manipulator.getButton(Button.B);
                expanderHatch.grab(grabberButtonPressed);
            }
        }

        driveBase.drive(driveLeftCommand, driveRightCommand);

        if (driverLeft.getRawButton(2) != aimingLightsButtonPressed && driverLeft.getRawButton(2)) {
            aimingLights.toggleState();
        }

        if (manipulator.getAxis(Axis.RIGHT_TRIGGER) > 0.5) {
            cargo.intake(CargoMoverSetting.THROUGH);
        } else if (manipulator.getAxis(Axis.LEFT_TRIGGER) > 0.5) {
            cargo.intake(CargoMoverSetting.BACK);
        } else {
            cargo.intake(CargoMoverSetting.STOP);
        }

        if (deployedFunnelRoller != driverRight.getTrigger() && driverRight.getTrigger()) {
            cargo.toggleFunnelRoller();
        }
        deployedFunnelRoller = driverRight.getTrigger();

        cargo.deployExitFlap(driverLeft.getTrigger());

        if (jackButtonPressed != manipulator.getButton(Button.X) && manipulator.getButton(Button.X)) {
            habJack.switchState();
        }
        jackButtonPressed = manipulator.getButton(Button.X);

        aimingLightsButtonPressed = driverLeft.getRawButton(2);
    }

    // Empty methods to keep the robot's runtime from emitting messages about
    // unoverridden methods.
    @Override
    public void autonomousInit() {}

    @Override
    public void autonomousPeriodic() {}

    @Override
    public void teleopPeriodic() {}

    @Override
    public void testPeriodic() {}
}
