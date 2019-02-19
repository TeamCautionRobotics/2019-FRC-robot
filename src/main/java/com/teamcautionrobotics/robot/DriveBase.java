package com.teamcautionrobotics.robot;

import com.teamcautionrobotics.misc2019.AbstractPIDSource;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DriveBase {

    private final VictorSP driveLeft;
    private final VictorSP driveRight;

    private final Encoder leftEncoder;
    private final Encoder rightEncoder;

    private final ADXRS450_Gyro gyro;

    private boolean useLeftEncoder = false;

    private double heading;
    public double courseHeading;

    public PIDController pidController;
    public final DriveBasePIDOutput pidOutput;

    private double lastLeftPower;
    private double leftInputDerivative;

    private double lastRightPower;
    private double rightInputDerivative;

    // Change in time
    private final double dt = TimedRobot.kDefaultPeriod;

    // This value is the derivative of the input power, which is only proportional
    // to the actual jerk of the robot in m/s^3
    private double jerkLimit = 1;

    public DriveBase(int left, int right, int leftA, int leftB, int rightA, int rightB) {
        driveLeft = new VictorSP(left);
        driveRight = new VictorSP(right);

        leftEncoder = new Encoder(leftA, leftB, false, EncodingType.k4X);
        rightEncoder = new Encoder(rightA, rightB, true, EncodingType.k4X);

        leftEncoder.setDistancePerPulse((4 * Math.PI) / 1024);
        rightEncoder.setDistancePerPulse((4 * Math.PI) / 1024);

        pidOutput = new DriveBasePIDOutput();

        pidController = new PIDController(0.04, 0, 0.1, 0, new DriveBasePIDSource(PIDSourceType.kDisplacement),
                pidOutput);
        pidController.setOutputRange(-1, 1);
        pidController.setAbsoluteTolerance(3);

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

    public double getJerkLimit() {
        return jerkLimit;
    }

    public void setJerkLimit(double jerkLimit) {
        this.jerkLimit = jerkLimit;
    }

    public void driveSmoothly(double leftInput, double rightInput) {
        updateDerivatives(leftInput, rightInput);
        double leftPower = leftInput;
        double rightPower = rightInput;

        // limit jerk for each side if predicted jerk is too high
        if (leftInputDerivative > jerkLimit) {
            leftPower = limitJerk(leftInput, lastLeftPower);
        }

        if (rightInputDerivative > jerkLimit) {
            rightPower = limitJerk(rightInput, lastRightPower);
        }
        drive(leftPower, rightPower);

        lastLeftPower = leftPower;
        lastRightPower = rightPower;
    }

    private void updateDerivatives(double leftInput, double rightInput) {
        leftInputDerivative = (leftInput - lastLeftPower) / dt;
        rightInputDerivative = (rightInput - lastRightPower) / dt;
    }

    // make the actual jerk = the threshold
    private double limitJerk(double input, double lastPower) {
        // change in input
        double di = dt * jerkLimit;
        double desiredInput = lastPower + di;
        return desiredInput;
    }

    public void resetGyro() {
        gyro.reset();
    }

    public double getGyroAngle() {
        return gyro.getAngle();
    }

    public void resetEncoders() {
        leftEncoder.reset();
        rightEncoder.reset();
    }

    public boolean usingLeftEncoder() {
        return useLeftEncoder;
    }

    public void setUseLeftEncoder(boolean useLeftEncoder) {
        this.useLeftEncoder = useLeftEncoder;
    }

    public double getDistance() {
        if (useLeftEncoder) {
            return getLeftDistance();
        } else {
            // default to right encoder
            return getRightDistance();
        }
    }

    public double getSpeed() {
        if (useLeftEncoder) {
            return getLeftSpeed();
        } else {
            // default to right encoder
            return getRightSpeed();
        }
    }

    public double getRightDistance() {
        return rightEncoder.getDistance();
    }

    public double getRightSpeed() {
        return rightEncoder.getRate();
    }

    public double getLeftDistance() {
        return leftEncoder.getDistance();
    }

    public double getLeftSpeed() {
        return leftEncoder.getRate();
    }

    public void pidInit() {
        heading = getGyroAngle();
        courseHeading = heading;
    }

    private class DriveBasePIDOutput implements PIDOutput {

        private DriveBasePIDOutput() {}

        @Override
        public void pidWrite(double speed) {
            SmartDashboard.putNumber("pid drive speed", speed);
            double angle = heading - getGyroAngle();
            drive(speed, speed - angle * 0.03);
        }
    }

    class DriveBasePIDSource extends AbstractPIDSource {

        public DriveBasePIDSource(PIDSourceType sourceType) {
            super(sourceType);
        }

        @Override
        public double pidGet() {
            switch (type) {
            case kDisplacement:
                return getDistance();
            case kRate:
                return getSpeed();
            default:
                return 0.0;
            }
        }
    }
}