package com.teamcautionrobotics.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.VictorSP;

public class DriveBase {

    private final VictorSP driveLeft;
    private final VictorSP driveRight;

    private final ADXRS450_Gyro gyro;

    private double heading;
    public double courseHeading;

    public DriveBase(int left, int right) {
        driveLeft = new VictorSP(left);
        driveRight = new VictorSP(right);

        gyro = new ADXRS450_Gyro();
        gyro.calibrate();
        heading = gyro.getAngle();
        courseHeading = heading;
    }

    public void drive(double left, double right) {
        driveLeft.set(left);
        driveRight.set(-right);
    }

    public void drive(double speed) {
        drive(speed, speed);
    }

    public void resetGyro() {
        gyro.reset();
    }

    public double getGyroAngle() {
        return gyro.getAngle();
    }
}