/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.teamcautionrobotics.robot;

import com.teamcautionrobotics.misc2019.ButtonPressRunner;
import com.teamcautionrobotics.misc2019.EnhancedJoystick;
import com.teamcautionrobotics.misc2019.Gamepad;
import com.teamcautionrobotics.misc2019.Gamepad.Axis;
import com.teamcautionrobotics.misc2019.Gamepad.Button;
import com.teamcautionrobotics.robot.Cargo.CargoMoverSetting;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends TimedRobot {
    /*
     * RoboRIO ports:
     * 
     * PWM: 0, Left drive; 1, Right drive; 2, Winch, 3 Cargo mechanism
     * 
     * DIO: 0, limit switch for Velcro hatch mechanism (false when pressed); 1, 2,
     * 3, line following (left, right, and back, respectively) future allocation,
     * not currently connected
     * 
     * Relay: 0, both lights
     * 
     * Pneumatic Control Module: 0, Jack; 1, Cargo Exit Flap; 2, Velcro hatch
     * deployer; 3, Cargo Funnel Deployer (deploys the nice-to-have wheels) 4,
     * Expander hatch reacher; 5, Expander hatch grabber
     * 
     * Driver controls:
     * 
     * Left joystick: X axis, robot turn control; Button 1, Cargo deploy exit flap;
     * Button 2, Toggle aiming lights
     * 
     * Right Joystick: Y axis, robot forward and backward control; Button 2, smooth
     * driving toggle; Button 3, precision turning mode
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
    Timer velcroHatchTimer;
    Timer jerkTimer;

    private double lastPower;
    private double inputDerivative;

    // This value is the derivative of the input power, which is only proportional
    // to the actual jerk of the robot in m/s^3
    private double jerkLimit = 4.5;

    DigitalInput velcroHatchLimitSwitch;

    // This is for the VelcroHatch mechanism.
    boolean deployButtonPressed = false;

    // These are for the Expander Hatch mechanism.
    private ButtonPressRunner grabberButtonRunner;
    private ButtonPressRunner reacherButtonRunner;

    private ButtonPressRunner jackButtonRunner;

    private ButtonPressRunner funnelRollerButtonRunner;
    private ButtonPressRunner aimingLightsButtonRunner;

    private boolean smoothDrivingEnabled = true;
    private boolean smoothDrivingButtonPressed = false;

    private final boolean USING_VELCRO_HATCH = true;

    // Time the robot should drive backwards for after deploying the hatch
    private final double HATCH_DEPLOY_DRIVEBACK_TIME = 0.25;

    // Passive power to hold the velcro arm in position
    private final double VELCRO_HATCH_ARM_PASSIVE_POWER = 0.05;

    // Scaling factors for the arm power based on its direction of movement
    private final double VELCRO_HATCH_ARM_UP_COEFFICIENT = 1.0;
    private final double VELCRO_HATCH_ARM_DOWN_COEFFICIENT = 1.0;

    private final double PRECISION_TURNING_SCALING_FACTOR = 0.4;

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
        driveBase = new DriveBase(0, 1);
        habJack = new HABJack(0);
        cargo = new Cargo(3, 1, 3);

        if (USING_VELCRO_HATCH) {
            velcroHatch = new VelcroHatch(2, 2);
        } else {
            expanderHatch = new ExpanderHatch(4, 5);
        }

        aimingLights = new AimingLights(0, 1);
        velcroHatchTimer = new Timer();

        jerkTimer = new Timer();
        jerkTimer.reset();
        jerkTimer.start();

        velcroHatchLimitSwitch = new DigitalInput(0);

        if (!USING_VELCRO_HATCH) {
            reacherButtonRunner = new ButtonPressRunner(() -> manipulator.getButton(Button.LEFT_BUMPER),
                    expanderHatch::toggleReacher);
            grabberButtonRunner = new ButtonPressRunner(() -> manipulator.getButton(Button.RIGHT_BUMPER),
                    expanderHatch::toggleGrabber);
        }

        funnelRollerButtonRunner = new ButtonPressRunner(() -> manipulator.getButton(Button.A),
                cargo::toggleFunnelRoller);
        jackButtonRunner = new ButtonPressRunner(() -> manipulator.getButton(Button.X), habJack::toggleJack);
        aimingLightsButtonRunner = new ButtonPressRunner(() -> driverLeft.getRawButton(2), aimingLights::toggleState);

        CameraServer.getInstance().startAutomaticCapture(0);
        CameraServer.getInstance().startAutomaticCapture(1);

        SmartDashboard.putNumber("Jerk Limit", jerkLimit);
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
        boolean precisionTurningEngaged = driverRight.getRawButton(3);
        if (precisionTurningEngaged) {
            driverLeft.setDeadband(driverLeft.getDeadband() * PRECISION_TURNING_SCALING_FACTOR);
        } else {
            driverLeft.setDeadband(0.1);
        }

        double forwardCommand = -driverRight.getY();
        double turnCommand = driverLeft.getX();

        if (precisionTurningEngaged) {
            turnCommand *= PRECISION_TURNING_SCALING_FACTOR;
        }

        if (USING_VELCRO_HATCH) {
            double velcroArmScalingFactor = (-manipulator.getAxis(Axis.LEFT_Y) >= 0) ? VELCRO_HATCH_ARM_UP_COEFFICIENT
                    : VELCRO_HATCH_ARM_DOWN_COEFFICIENT;
            double armPower = VELCRO_HATCH_ARM_PASSIVE_POWER
                    + velcroArmScalingFactor * -manipulator.getAxis(Axis.LEFT_Y);

            if (!velcroHatchLimitSwitch.get()) {
                velcroHatch.rotate(Math.min(armPower, 0) + VELCRO_HATCH_ARM_PASSIVE_POWER);
            } else {
                velcroHatch.rotate(armPower);
            }

            // Start the driveback timer when the deploy button is released
            if (!manipulator.getButton(Button.B) && deployButtonPressed) {
                velcroHatchTimer.reset();
                velcroHatchTimer.start();
            }
            deployButtonPressed = manipulator.getButton(Button.B);
            velcroHatch.deploy(deployButtonPressed);

            // Drive backwards after the deploy button is released
            if (velcroHatchTimer.get() > 0 && velcroHatchTimer.get() < HATCH_DEPLOY_DRIVEBACK_TIME) {
                forwardCommand = -1;
                turnCommand = 0;
            }
        } else {
            reacherButtonRunner.update();
            grabberButtonRunner.update();
        }

        // change in time between RobotPeriodic() calls
        double dt = jerkTimer.get();
        jerkTimer.reset();
        jerkTimer.start();

        /*
         * Work showing that the jerk of the robot is proportional the the rate of
         * change of the driver input:
         * 
         * a = Fnet / m = (1/m)(Fa - Fs) da/dt = (1/m)(dFa/dt - dFs/dt) = (1/m)(kdi/dt -
         * 0) = (k/m) di/dt since the driver input is proportional to the torque applied
         * on the axle and the friction force is roughly constant (Get it? Roughly,
         * since it's friction).
         * 
         * This math is wrong, but it has a pun so it will stay.
         */

        if (!smoothDrivingButtonPressed && driverRight.getRawButton(2)) {
            smoothDrivingEnabled = !smoothDrivingEnabled;
        }
        smoothDrivingButtonPressed = driverRight.getRawButton(2);
        SmartDashboard.putBoolean("Smooth deriving enable", smoothDrivingEnabled);

        double driveLeftCommand;
        double driveRightCommand;
        if (smoothDrivingEnabled) {
            inputDerivative = (forwardCommand - lastPower) / dt;

            // limit jerk for each side if predicted jerk is too high
            if (Math.abs(inputDerivative) > jerkLimit) {
                // desired change in input
                double di = dt * jerkLimit;
                forwardCommand = lastPower + Math.signum(inputDerivative) * di;
            }
            driveLeftCommand = forwardCommand + turnCommand;
            driveRightCommand = forwardCommand - turnCommand;
            driveBase.drive(driveLeftCommand, driveRightCommand);
        } else {
            driveLeftCommand = forwardCommand + turnCommand;
            driveRightCommand = forwardCommand - turnCommand;
            driveBase.drive(driveLeftCommand, driveRightCommand);
        }

        lastPower = forwardCommand;

        if (manipulator.getAxisAsButton(Axis.LEFT_TRIGGER) || driverLeft.getTrigger()) {
            cargo.intake(CargoMoverSetting.THROUGH);
        } else if (manipulator.getAxisAsButton(Axis.RIGHT_TRIGGER)) {
            cargo.intake(CargoMoverSetting.BACK);
        } else if (!driverLeft.getTrigger()) {
            cargo.intake(CargoMoverSetting.STOP);
        }

        funnelRollerButtonRunner.update();
        jackButtonRunner.update();
        aimingLightsButtonRunner.update();

        cargo.deployExitFlap(driverRight.getTrigger());

        jerkLimit = SmartDashboard.getNumber("Jerk Limit", jerkLimit);
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
