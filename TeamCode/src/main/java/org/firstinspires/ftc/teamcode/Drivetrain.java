package org.firstinspires.ftc.teamcode;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Thread.sleep;

import com.qualcomm.hardware.bosch.BNO055IMUNew;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

public class Drivetrain{

    private DcMotor leftFrontDrive;
    private DcMotor leftBackDrive;
    private DcMotor rightFrontDrive;
    private DcMotor rightBackDrive;
    //private BNO055IMU imu;
    private IMU internalIMU;
    private Telemetry telemetry;
    //static final int TickPerRev = 10; // need to measure
    double angleOffset = 0;

    Drivetrain(HardwareMap hardwareMap, Telemetry telemetry) throws InterruptedException {

        leftFrontDrive = hardwareMap.get(DcMotor.class, "leftFrontDrive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "leftBackDrive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rightFrontDrive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "rightBackDrive");

        //Default, but just covering our butts
        leftFrontDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFrontDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBackDrive.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Most robots need the motors on one side to be reversed to drive forward.
        // When you first test your robot, push the left joystick forward
        // and flip the direction ( FORWARD <-> REVERSE ) of any wheel that runs backwards
        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);

        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);



        /* // Set up the parameters with which we will use our IMU. Note that integration
        // algorithm here just reports accelerations to the logcat log; it doesn't actually
        // provide positional information.
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.RADIANS;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled      = true;
        parameters.loggingTag          = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);*/

        //New IMU thing
        internalIMU = hardwareMap.get(BNO055IMUNew.class, "internalIMU");
        internalIMU.initialize(
                new IMU.Parameters(
                        new RevHubOrientationOnRobot(
                                RevHubOrientationOnRobot.LogoFacingDirection.UP, //TODO will need to change to actually work, REMEMBER this
                                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
                                )
                )
        );

        this.telemetry = telemetry;

    }

    public enum DirectionMode {FORWARD, SIDE, ROTATE}

    //Which Motor quality to get
    public enum MotorQuality {POW, POS, VOL}

    public enum MotorPlacement {LEFTFRONT, LEFTBACK, RIGHTFRONT, RIGHTBACK}

    public enum Centricity {BOT, FIELD}

    public enum AngleType {YAW, PITCH, ROLL, X, Y, Z, HEADING}

    //This method is used to create an array of directions for the motors based on a set movement direction
    private int[] DetermineDirection(DirectionMode mode){
        int[] directions = new int[]{1, 1, 1, 1};

        switch (mode) {
            case FORWARD:
                break;

            case SIDE:
                directions[1] = -1;
                directions[2] = -1;
                break;

            case ROTATE:
                directions[2] = -1;
                directions[3] = -1;
                break;
        }

        return directions;
    }

    public void setMotorPowers(double leftFrontPower, double leftBackPower, double rightFrontPower, double rightBackPower){
        leftFrontDrive.setPower(leftFrontPower);
        leftBackDrive.setPower(leftBackPower);
        rightFrontDrive.setPower(rightFrontPower);
        rightBackDrive.setPower(rightBackPower);
    }

    public void setMotorPowers(double[] motorPowers){
        leftFrontDrive.setPower(motorPowers[0]);
        leftBackDrive.setPower(motorPowers[1]);
        rightFrontDrive.setPower(motorPowers[2]);
        rightBackDrive.setPower(motorPowers[3]);
    }

    //Just runs motors, USE ONLY FOR AUTO, I don't know why we need this
    private void RunMotorsForSeconds(double secs, double power) throws InterruptedException{
        setMotorPowers(power, power, power,power);
        sleep((long)(secs)*1000);
        setMotorPowers(0,0,0,0);
    }

    public void setIndividualPower(MotorPlacement pos, double pow) {
        switch (pos) {
            case LEFTFRONT:
                leftFrontDrive.setPower(pow);
                break;
            case LEFTBACK:
                leftBackDrive.setPower(pow);
                break;
            case RIGHTFRONT:
                rightFrontDrive.setPower(pow);
                break;
            case RIGHTBACK:
                rightBackDrive.setPower(pow);
                break;
        }
    }

    //Method takes a movement mode (direction) and moves the motors
    public void move(DirectionMode mode, double pow) {

        int[] directions = DetermineDirection(mode);

        setMotorPowers(directions[0] * pow, directions[1] * pow, directions[2] * pow, directions[3] * pow);
    }

    public double getMotorInfo(MotorPlacement pos, MotorQuality qual){
        DcMotor workingMotor;
        switch (pos){
            case LEFTFRONT:
                workingMotor = leftFrontDrive;
                break;
            case LEFTBACK:
                workingMotor = leftBackDrive;
                break;
            case RIGHTFRONT:
                workingMotor = rightFrontDrive;
                break;
            case RIGHTBACK:
                workingMotor = rightBackDrive;
                break;
            default:
                workingMotor = leftFrontDrive;
        }

        switch (qual){
            case POS:
                return workingMotor.getCurrentPosition();
            case VOL:
            case POW:
                return workingMotor.getPower();
            default:
                return workingMotor.getPower();
        }

    }

    /*
    //rather confusingly this just gives the heading
    public double getIMUData(){
        return -imu.getAngularOrientation().firstAngle+angleOffset;
    }

    public void resetAngle(){
        angleOffset = imu.getAngularOrientation().firstAngle;
    }

    public void setAngleOffset(double angle){
        angleOffset = angle*PI/180;
    }*/


    //Refactor of the IMU functions above

    //Gets a specific angle from the IMU, returns in Radians
    public double getIMUAngleData(AngleType angle){
        YawPitchRollAngles orientation = internalIMU.getRobotYawPitchRollAngles();

        // TODO = Test but this should work as an 'or' operator
        switch (angle){
            default:
            case YAW:
            case Z:
            case HEADING:
                return -orientation.getYaw(AngleUnit.RADIANS); //Neg. because Vander said math works better that way Also to optimize switch to degrees to take out the conversions elsewhere

            case PITCH:
            case X:
                return orientation.getPitch(AngleUnit.RADIANS);

            case ROLL:
            case Y:
                return orientation.getRoll((AngleUnit.RADIANS));

        }
    }

    //Resets the heading angle in the IMU, TODO might just want to simplify this to resetYaw()
    public void resetAngleData(AngleType angle){

        switch (angle){
            default:
            case YAW:
            case Z:
            case HEADING:
                internalIMU.resetYaw();
                break;

            case PITCH:
            case X:
                //Can't reset the Pitch
                break;

            case ROLL:
            case Y:
                //Can't reset the Roll
                break;

        }
    }

    //TODO = TEST may not need angle offset with the new imu interface

    //put in the three vectors (z being rotation) and it will move the motors the correct powers
    public double[] driveVectors(Centricity centric, double joyx, double joyy, double joyz, double spd){ //spd is a speed coefficient

        //set up movement vectors and relate them to input ("joy") vec tors
        double x, y, max;
        if(centric==Centricity.FIELD){
            //double angle = getIMUData(); TODO = TEST
            double angle = getIMUAngleData(AngleType.HEADING);

            x = joyx*cos(angle) - joyy*sin(angle);
            y = joyx*sin(angle) + joyy*cos(angle);
        }
        else{
            x = joyx;
            y = joyy;
        }

        //set up the list of motor powers, related to the movement vectors, z being rotation
        double[] motorPowers = new double[]{
                (x + y + joyz)*spd,
                (-x + y + joyz)*spd,
                (-x + y - joyz)*spd,
                (x + y - joyz)*spd
        };

        //make sure no individual motor powers are higher than 1
        max = 0;
        for (double candidate:motorPowers){
            if(abs(candidate)>max){max = abs(candidate);}
        }

        if(max>1){
            for(int i=0; i<4; i++){
                motorPowers[i] /= max;
            }
        }

        //set the motor powers
        setMotorPowers(motorPowers);

        return motorPowers;
    }

    // this function is designed for the auto part
    public void MoveForDis(double distance, double pow) throws InterruptedException {

        // calculate the distance, inches to ticks?
        int dis = (int)(distance * 1000 / 23.5 * 50/48);

        // reset encoders
        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // set target distance
        leftFrontDrive.setTargetPosition(dis);
        leftBackDrive.setTargetPosition(dis);
        rightFrontDrive.setTargetPosition(dis);
        rightBackDrive.setTargetPosition(dis);

        // change encoder mode
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // set power, slowly turn up to max power, to avoid robot wheelies
        for(int i =1; i<21; i++) {
            leftFrontDrive.setPower(pow*i/20);
            leftBackDrive.setPower(pow*i/20);
            rightFrontDrive.setPower(pow*i/20);
            rightBackDrive.setPower(pow*i/20);
            sleep(10);
        }

        //Check to see if the motor is still going to its position
        while ( leftFrontDrive.isBusy() ||
                leftBackDrive.isBusy() ||
                rightFrontDrive.isBusy() ||
                rightBackDrive.isBusy() ) {}


        // change encoder mode back to normal
        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        // set power to 0
        leftFrontDrive.setPower(0);
        leftBackDrive.setPower(0);
        rightFrontDrive.setPower(0);
        rightBackDrive.setPower(0);

    }

    // this function is designed for the auto part TODO = Test
    public void SideMoveForDis(double distance, double pow) {

        // calculate the distance
        int dis = (int)(distance * 1000 / 19.75);

        // set direction
        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);

        // reset encoders
        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        // set target distance
        leftFrontDrive.setTargetPosition(dis);
        leftBackDrive.setTargetPosition(dis);
        rightFrontDrive.setTargetPosition(dis);
        rightBackDrive.setTargetPosition(dis);

        // change encoder mode
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // set power, TODO will probably need to change some into neg. powers to move the bot sideways
        leftFrontDrive.setPower(pow);
        leftBackDrive.setPower(pow);
        rightFrontDrive.setPower(pow);
        rightBackDrive.setPower(pow);

        //If needed put rev up code here

        //Check to see if the motor is still going to its position
        while ( leftFrontDrive.isBusy() ||
                leftBackDrive.isBusy() ||
                rightFrontDrive.isBusy() ||
                rightBackDrive.isBusy() ) {}

        // set power to 0
        leftFrontDrive.setPower(0);
        leftBackDrive.setPower(0);
        rightFrontDrive.setPower(0);
        rightBackDrive.setPower(0);

        // change encoder mode back to normal
        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);

        // set power to 0
        leftFrontDrive.setPower(0);
        leftBackDrive.setPower(0);
        rightFrontDrive.setPower(0);
        rightBackDrive.setPower(0);
    }

    //For auto, does not need imu data
    public void RotateForDegree(int degree, double pow) {

        // calculate the degree
        int deg = (int)(degree * 1000 / 105);

        // reset encoders
        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //TEST, but lines 397-398, 413-416, and 441-442 can probably be simplified
        // set direction
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        // set target distance
        leftFrontDrive.setTargetPosition(deg);
        leftBackDrive.setTargetPosition(deg);
        rightFrontDrive.setTargetPosition(deg);
        rightBackDrive.setTargetPosition(deg);

        // change encoder mode
        leftFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        // set power
        leftFrontDrive.setPower(pow);
        leftBackDrive.setPower(pow);
        rightFrontDrive.setPower(-pow);
        rightBackDrive.setPower(-pow);

        // run for a while
        while ( leftFrontDrive.isBusy() ||
                leftBackDrive.isBusy() ||
                rightFrontDrive.isBusy() ||
                rightBackDrive.isBusy() ) {}

        // set power to 0
        leftFrontDrive.setPower(0);
        leftBackDrive.setPower(0);
        rightFrontDrive.setPower(0);
        rightBackDrive.setPower(0);

        // change encoder mode back to normal
        leftFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBackDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);
    }

    //For auto, but would require an imu
    public void rotateToPosition(double targetAngle, double pow){
        //double angle = getIMUData()*180/PI; TODO = TEST
        double angle = getIMUAngleData(AngleType.HEADING)*180/PI;
        double angleMod = 0;
        double speedCoef = 1;

        //Basically will make the robot turn the short way to the target angle and not the long way
        if(abs(targetAngle-angle)<abs(targetAngle-(angle-360))){
            if(abs(targetAngle-angle)<abs(targetAngle-(angle+360))){
                angleMod=0;
            }else{
                angleMod=360;
            }
        }else{
            angleMod=-360;
        }

        //Turn slower as distance till target angle is approaching zero
        while (abs(targetAngle-(angle+angleMod))>0.2){
            //angle = getIMUData()*180/PI; TODO = TEST
            angle = getIMUAngleData(AngleType.HEADING)*180/PI;

            if (abs(targetAngle-(angle+angleMod))<=30){
                speedCoef = abs(targetAngle-(angle+angleMod))/30;
            }
            else{
                speedCoef=1;
            }

            move(DirectionMode.ROTATE, abs(pow) * ((targetAngle-(angle+angleMod)) / abs(targetAngle-(angle+angleMod))) * speedCoef);


            telemetry.addData("Target Angle", targetAngle);
            telemetry.addData("Actual Angle", (angle+angleMod));

            telemetry.update(); //Can use this because this method won't be in anything realtime, as it stops everything.
        }
    }
}