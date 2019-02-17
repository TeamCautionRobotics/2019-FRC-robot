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
    /*
     * RoboRIO ports:
     * 
     * PWM: 0, Left drive; 1, Right drive; 2, Hatch / Winch, 3 Cargo mechanism
     * 
     * DIO: 0, left drive encoder A; 1, left drive encoder B; 2, right drive encoder
     * A; 3, right drive encoder B; 4, 5, 6, line following (left, right, and back,
     * respectively); 7, limit switch for Velcro hatch mechanism
     * 
     * Relay: 0, horizontal light; 1, downward light
     * 
     * Pneumatic Control Module: 0, Left jack; 1, Right jack; 2, Cargo Exit Flap; 3
     * Cargo Funnel Deployer (deploys the nice-to-have wheels); 4, Expander hatch
     * reacher (away from the robot); 5, Expander hatch grabber (applies friction on
     * the hatch); 4, 5, 6 Velcro hatch deployers (left, right, respectively)
     *
     * 
     * Driver controls:
     * 
     * Left joystick: X axis, robot turn control; Button 1, Cargo deploy exit flap;
     * Button 2, Toggle aiming lights
     * 
     * Right Joystick: Y axis, robot forward and backward control.
     * 
     * Gamepad: Left thumbstick, Rotate hatch arm; A, Deploy funnel roller (cargo
     * mechanism extender); B, Deploy hatch (velcro mech); X, Jack for HAB; Right
     * trigger, Cargo; Left trigger, Cargo reverse; Right bumper, Expand expander
     * hatch mech; Lfft bumper, Extend expander hatch mech past bumper zone
     *
     * All pneumatics are toggles except for the velcro hatch deployer and the cargo
     * exit flap.
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

    // Passive power to hold the velcro arm in position
    private final double VELCRO_HATCH_ARM_PASSIVE_POWER = 0.00;

    // Scaling factors for the arm power based on its direction of movement
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
        habJack = new HABJack(0, 1);
        cargo = new Cargo(3, 2, 3);
        if (USING_VELCRO_HATCH) {
            velcroHatch = new VelcroHatch(2, 4, 5, 6);
        } else {
            expanderHatch = new ExpanderHatch(4, 5);
        }

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
        double forwardCommand = -driverRight.getY();
        double turnCommand = driverLeft.getX();
        double driveLeftCommand = forwardCommand + turnCommand;
        double driveRightCommand = forwardCommand - turnCommand;

        if (USING_VELCRO_HATCH) {
            double velcroArmScalingFactor = (manipulator.getAxis(Axis.LEFT_Y) >= 0) ? VELCRO_HATCH_ARM_UP_POWER
                    : VELCRO_HATCH_ARM_DOWN_POWER;
            double armPower = VELCRO_HATCH_ARM_PASSIVE_POWER
                    + velcroArmScalingFactor * manipulator.getAxis(Axis.LEFT_TRIGGER);
            velcroHatch.rotate(armPower);

            // Start the driveback timer when the deploy button is released
            if (!manipulator.getButton(Button.B) && deployButtonPressed) {
                timer.reset();
                timer.start();
            }
            deployButtonPressed = manipulator.getButton(Button.B);
            velcroHatch.deploy(deployButtonPressed);

            // Drive backwards after the deploy button is released
            if (timer.get() > 0 && timer.get() < HATCH_DEPLOY_DRIVEBACK_TIME) {
                driveLeftCommand = -1;
                driveRightCommand = -1;
            }
        } else {
            if (!reacherButtonPressed && manipulator.getButton(Button.LEFT_BUMPER)) {
                expanderHatch.toggleReacher();
            }
            reacherButtonPressed = manipulator.getButton(Button.LEFT_BUMPER);

            if (!grabberButtonPressed && manipulator.getButton(Button.RIGHT_BUMPER)) {
                expanderHatch.toggleGrabber();
            }
            grabberButtonPressed = manipulator.getButton(Button.B);
        }

        driveBase.drive(driveLeftCommand, driveRightCommand);

        if (!driverLeft.getRawButton(2) && driverLeft.getRawButton(2)) {
            aimingLights.toggleState();
        }

        if (manipulator.getAxis(Axis.RIGHT_TRIGGER) > 0.5) {
            cargo.intake(CargoMoverSetting.THROUGH);
        } else if (manipulator.getAxis(Axis.LEFT_TRIGGER) > 0.5) {
            cargo.intake(CargoMoverSetting.BACK);
        } else {
            cargo.intake(CargoMoverSetting.STOP);
        }

        if (!deployedFunnelRoller && manipulator.getButton(Button.A)) {
            cargo.toggleFunnelRoller();
        }
        deployedFunnelRoller = manipulator.getButton(Button.A);

        cargo.deployExitFlap(driverLeft.getTrigger());

        if (!jackButtonPressed && manipulator.getButton(Button.X)) {
            habJack.toggleJack();
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
