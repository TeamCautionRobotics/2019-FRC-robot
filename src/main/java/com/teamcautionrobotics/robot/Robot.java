/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.teamcautionrobotics.robot;

import com.teamcautionrobotics.misc2019.ButtonToggleRunner;
import com.teamcautionrobotics.misc2019.EnhancedJoystick;
import com.teamcautionrobotics.misc2019.Gamepad;
import com.teamcautionrobotics.misc2019.Gamepad.Axis;
import com.teamcautionrobotics.misc2019.Gamepad.Button;
import com.teamcautionrobotics.robot.Cargo.CargoMoverSetting;

import edu.wpi.first.cameraserver.CameraServer;
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
     * Pneumatic Control Module: 0, Cargo Exit Flap; 1, Jack; 2, Velcro hatch
     * deployer; 3, Cargo Funnel Deployer (deploys the nice-to-have wheels) 4,
     * Expander hatch reacher; 2, Expander hatch grabber
     * 
     * Driver controls:
     * 
     * Left joystick: X axis, robot turn control; Button 1, Jack for HAB;
     * Button 2, Toggle aiming lights
     * 
     * Right Joystick: Y axis, robot forward and backward control; Button 2, smooth
     * deriving toggle; Button 3, precision turning mode
     * 
     * Gamepad: Left thumbstick, Rotate hatch arm; A, Deploy funnel roller (cargo
     * mechanism extender); B, Deploy hatch (velcro mech); X, Cargo deploy exit flap; Right
     * trigger, Cargo; Left trigger, Cargo reverse; Right bumper, Expand expander
     * hatch mech; Left bumper, Extend expander hatch mech past bumper zone
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

    // This is for the VelcroHatch mechanism.
    boolean deployButtonPressed = false;

    // These are for the Expander Hatch mechanism.
    private ButtonToggleRunner grabberToggleRunner;
    private ButtonToggleRunner reacherToggleRunner;

    private ButtonToggleRunner exitFlapToggleRunner;

    private ButtonToggleRunner funnelRollerToggleRunner;
    private ButtonToggleRunner aimingLightsToggleRunner;

    private boolean smoothDerivingEnabled = true;
    private boolean smoothDerivingButtonPressed = false;

    private final boolean USING_VELCRO_HATCH = false;
    private final boolean USING_DOUBLE_SOLENOIDS = false;

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
        habJack = new HABJack(1);
        cargo = USING_DOUBLE_SOLENOIDS ? new Cargo(3, 6, 5, 7) : new Cargo(3, 0, 3);

        if (USING_VELCRO_HATCH) {
            velcroHatch = USING_DOUBLE_SOLENOIDS ? new VelcroHatch(2, 4, 3, 0) : new VelcroHatch(2, 2, 0);
        } else {
            expanderHatch = USING_DOUBLE_SOLENOIDS ? new ExpanderHatch(4, 3, 2, 1) : new ExpanderHatch(4, 2);
        }

        aimingLights = new AimingLights(0, 1);
        velcroHatchTimer = new Timer();

        jerkTimer = new Timer();
        jerkTimer.reset();
        jerkTimer.start();
        lastPower = 0;

        if (!USING_VELCRO_HATCH) {
            reacherToggleRunner = new ButtonToggleRunner(() -> manipulator.getButton(Button.LEFT_BUMPER),
                    expanderHatch::toggleReacher);
            grabberToggleRunner = new ButtonToggleRunner(() -> manipulator.getButton(Button.RIGHT_BUMPER),
                    expanderHatch::toggleGrabber);
        }

        funnelRollerToggleRunner = new ButtonToggleRunner(() -> manipulator.getButton(Button.A),
                cargo::toggleFunnelRoller);
        exitFlapToggleRunner = new ButtonToggleRunner(() -> manipulator.getButton(Button.X), cargo::toggleExitFlap);
        aimingLightsToggleRunner = new ButtonToggleRunner(() -> driverLeft.getRawButton(2), aimingLights::toggleState);

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

            // returns true if not pressed - pressed when up
            if (velcroHatch.armIsUp()) {
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
            reacherToggleRunner.update();
            grabberToggleRunner.update();
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

        if (!smoothDerivingButtonPressed && driverRight.getRawButton(2)) {
            smoothDerivingEnabled = !smoothDerivingEnabled;
        }
        smoothDerivingButtonPressed = driverRight.getRawButton(2);
        SmartDashboard.putBoolean("Smooth deriving enabled", smoothDerivingEnabled);

        double driveLeftCommand;
        double driveRightCommand;
        if (smoothDerivingEnabled) {
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

        CargoMoverSetting cargoCommand = CargoMoverSetting.STOP;

        if (manipulator.getAxisAsButton(Axis.RIGHT_TRIGGER) || driverLeft.getTrigger()) {
            cargoCommand = CargoMoverSetting.THROUGH;
        } else if (manipulator.getAxisAsButton(Axis.LEFT_TRIGGER)) {
            cargoCommand = CargoMoverSetting.BACK;
        }
        cargo.intake(cargoCommand);

        funnelRollerToggleRunner.update();
        exitFlapToggleRunner.update();
        aimingLightsToggleRunner.update();

        habJack.setJack(driverRight.getTrigger());

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
