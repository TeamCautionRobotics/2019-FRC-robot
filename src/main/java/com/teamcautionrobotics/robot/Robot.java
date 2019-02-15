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
    VelcroHatch velcroHatch;
    ExpanderHatch expanderHatch;
    double armPower;
    EnhancedJoystick driverLeft, driverRight;
    Gamepad manipulator;
    DriveBase driveBase;
    AimingLights aimingLights;
    double driveLeftCommand;
    double driveRightCommand;

    Timer timer;

    boolean deployButtonPressed = false;

    // These are for the Expander Hatch mechanism
    boolean reacherButtonPressed = false;
    boolean grabberButtonPressed = false;

    private final boolean usingVelcroHatch = true;
    private static final String kDefaultAuto = "Default";
    private static final String kCustomAuto = "My Auto";
    private String m_autoSelected;
    private final SendableChooser<String> m_chooser = new SendableChooser<>();

    /**
     * This function is run when the robot is first started up and should be used
     * for any initialization code.
     */
    @Override
    public void robotInit() {
        // pneumatic ports are not finalized
        velcroHatch = new VelcroHatch(2, 1);
        expanderHatch = new ExpanderHatch(3, 4);
        driverLeft = new EnhancedJoystick(0);
        driverRight = new EnhancedJoystick(1);
        manipulator = new Gamepad(2);
        aimingLights = new AimingLights(0, 1);
        driveBase = new DriveBase(0, 1, 0, 1, 2, 3);
        CameraServer.getInstance().startAutomaticCapture(0);
        CameraServer.getInstance().startAutomaticCapture(1);

        timer = new Timer();

        m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
        m_chooser.addOption("My Auto", kCustomAuto);
        SmartDashboard.putData("Auto choices", m_chooser);
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
        velcroHatch.rotate(armPower);

        // if B is pressed, deploy hatch pneumatics

        driveLeftCommand = driverLeft.getY();
        driveRightCommand = driverRight.getY();

        aimingLights.set(driverLeft.getRawButton(2));

        if (usingVelcroHatch) {
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
    }

    /**
     * This autonomous (along with the chooser code above) shows how to select
     * between different autonomous modes using the dashboard. The sendable chooser
     * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
     * remove all of the chooser code and uncomment the getString line to get the
     * auto name from the text box below the Gyro
     *
     * <p>
     * You can add additional auto modes by adding additional comparisons to the
     * switch structure below with additional strings. If using the SendableChooser
     * make sure to add them to the chooser code above as well.
     */
    @Override
    public void autonomousInit() {
        m_autoSelected = m_chooser.getSelected();
        // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
        System.out.println("Auto selected: " + m_autoSelected);
    }

    /**
     * This function is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic() {
        switch (m_autoSelected) {
        case kCustomAuto:
            // Put custom auto code here
            break;
        case kDefaultAuto:
        default:
            // Put default auto code here
            break;
        }
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
