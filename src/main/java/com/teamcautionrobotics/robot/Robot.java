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

import edu.wpi.first.wpilibj.TimedRobot;
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
    VelcroHatch hatch;
    double armPower;
    EnhancedJoystick driverLeft, driverRight;
    Gamepad manipulator;
    DriveBase driveBase;

    double driveLeftCommand;
    double driveRightCommand;

    Timer timer;

    boolean deployButtonPressed = false;

    /**
     * This function is run when the robot is first started up and should be used
     * for any initialization code.
     */
    @Override
    public void robotInit() {
        hatch = new VelcroHatch(2, 1);
        driverLeft = new EnhancedJoystick(0);
        driverRight = new EnhancedJoystick(1);
        manipulator = new Gamepad(2);
        driveBase = new DriveBase(0, 1, 0, 1, 2, 3);

        timer = new Timer();
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
        armPower = .5 + .5 * manipulator.getAxis(Axis.LEFT_TRIGGER);
        hatch.rotate(armPower);

        // if B is pressed, deploy hatch pneumatics

        driveLeftCommand = driverLeft.getY();
        driveRightCommand = driverRight.getY();

        if (manipulator.getButton(Button.B)) {
            hatch.deploy(manipulator.getButton(Button.B));
            deployButtonPressed = true;
        } else {
            if (deployButtonPressed) {
                deployButtonPressed = false;

                timer.reset();
                timer.start();
            }
            hatch.deploy(false);
        }
        // Counts for .25 of a second
        if (timer.get() > 0 && timer.get() < 0.25) {
            driveLeftCommand = -1;
            driveRightCommand = -1;
        }
        driveBase.drive(driveLeftCommand, driveRightCommand);
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
