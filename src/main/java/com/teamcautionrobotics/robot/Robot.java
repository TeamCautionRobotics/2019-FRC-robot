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

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Timer;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    /*
     * RoboRIO ports:
     * 
     * PWM: - 0 - Left drive - 1 - Right drive - 2 - Hatch / Winch - 3 - Cargo
     * mechanism
     * 
     * DIO: - 0 - left drive A - 1 - Left drive B - 2 - right drive A - 3 - right
     * drive B - 4, 5, 6 - line following (left, right, and back, respectively) - 7
     * - limit switch for Velcro hatch mechanism
     * 
     * Relay: - 0 - horizontal light - 1 - downward light
     * 
     * Pneumatic Control Module: - 0 - Left jack - 1 - Right jack - 2 - Velcro hatch
     * deployer - 3 - Expander hatch reacher (away from the robot) - 4 - Expander
     * hatch grabber (applies friction on the hatch) - 5 - Cargo Exit Flap - 6 -
     * Cargo Funnel Deployer (deploys the nice-to-have wheels)
     *
     * 
     * Driver controls:
     * 
     * Left joystick, basic tank drive: - 1 - Cargo Deploy Exit Flap - 2 - Aiming
     * Lights
     * 
     * Right Joystick: - 1 - Line following
     * 
     * Gamepad: - Left thumbstick - Rotate hatch arm - A - Deploy funnel roller
     * (cargo mechanism extender) - B - Deploy hatch (velcro mech) - X - Jack for
     * HAB - Right trigger - Cargo - Left trigger - Cargo reverse - Right bumper -
     * Expand expander hatch mech - Lift bumper - Extend expander hatch mech past
     * bumper zone
     *
     * All pneumatics are toggles except for the velcro hatch deployer and the cargo
     * exit flap
     */

    EnhancedJoystick driverLeft, driverRight;
    Gamepad manipulator;

    DriveBase driveBase;
    VelcroHatch velcroHatch;
    ExpanderHatch expanderHatch;
    Cargo cargo;
    HABJack habJack;

    AimingLights aimingLights;
    Timer timer;

    double armPower;
    double driveLeftCommand;
    double driveRightCommand;

    // This is for the VelcroHatch mechanism.
    boolean deployButtonPressed = false;

    // These are for the Expander Hatch mechanism.
    boolean reacherButtonPressed = false;
    boolean grabberButtonPressed = false;

    boolean deployedFunnelRoller = false;
    boolean aimingLightsButtonPressed = false;

    boolean jackButtonPressed = false;

    private final boolean usingVelcroHatch = true;
    private final double VELCRO_HATCH_ARM_PASSIVE_POWER = 0.05;
    private final double VELCRO_HATCH_ARM_UP_POWER = 1.0;
    private final double VELCRO_HATCH_ARM_DOWN_POWER = -0.25;

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
        velcroHatch = new VelcroHatch(2, 2);
        expanderHatch = new ExpanderHatch(3, 4);
        cargo = new Cargo(3, 5, 6);
        habJack = new HABJack(0, 1);

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
        driveLeftCommand = driverLeft.getY();
        driveRightCommand = driverRight.getY();

        if (usingVelcroHatch) {
            double velcroArmScalingFactor = (manipulator.getAxis(Axis.LEFT_Y) >= 0) ? VELCRO_HATCH_ARM_UP_POWER
                    : VELCRO_HATCH_ARM_DOWN_POWER;
            armPower = VELCRO_HATCH_ARM_PASSIVE_POWER + velcroArmScalingFactor * manipulator.getAxis(Axis.LEFT_TRIGGER);
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
            if (timer.get() > 0 && timer.get() < 0.25) {
                driveLeftCommand = -1;
                driveRightCommand = -1;
            }
        } else {
            if (reacherButtonPressed != manipulator.getButton(Button.LEFT_BUMPER)
                    && manipulator.getButton(Button.LEFT_BUMPER)) {
                expanderHatch.switchReacherState();
            }
            reacherButtonPressed = manipulator.getButton(Button.LEFT_BUMPER);

            if (grabberButtonPressed != manipulator.getButton(Button.RIGHT_BUMPER)
                    && manipulator.getButton(Button.RIGHT_BUMPER)) {
                expanderHatch.switchGrabberState();
            }
            grabberButtonPressed = manipulator.getButton(Button.B);
        }

        driveBase.drive(driveLeftCommand, driveRightCommand);

        if (driverLeft.getRawButton(2) != aimingLightsButtonPressed && driverLeft.getRawButton(2)) {
            aimingLights.changeState();
        }

        if (manipulator.getAxis(Axis.RIGHT_TRIGGER) > 0.5) {
            cargo.intake(CargoMoverSetting.THROUGH);
        } else if (manipulator.getAxis(Axis.LEFT_TRIGGER) > 0.5) {
            cargo.intake(CargoMoverSetting.BACK);
        } else {
            cargo.intake(CargoMoverSetting.STOP);
        }

        if (deployedFunnelRoller != manipulator.getButton(Button.A) && manipulator.getButton(Button.A)) {
            cargo.toggleFunnelRoller();
        }
        deployedFunnelRoller = manipulator.getButton(Button.A);

        cargo.deployExitFlap(driverLeft.getTrigger());

        if (jackButtonPressed != manipulator.getButton(Button.X) && manipulator.getButton(Button.X)) {
            habJack.switchState();
        }
        jackButtonPressed = manipulator.getButton(Button.X);

        aimingLightsButtonPressed = driverLeft.getRawButton(2);
    }

    @Override
    public void autonomousInit() {
    }

    /**
     * This function is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic() {
    }

    /**
     * This function is called periodically during operator control.
     */
    @Override
    public void teleopPeriodic() {
    }

    /**
     * This function is called periodically during test mode.
     */
    @Override
    public void testPeriodic() {
    }
}
