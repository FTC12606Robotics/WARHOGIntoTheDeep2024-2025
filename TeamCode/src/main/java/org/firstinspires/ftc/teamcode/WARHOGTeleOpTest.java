package org.firstinspires.ftc.teamcode;

import static java.lang.Math.PI;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

@TeleOp(name="WARHOGTeleOpTest", group="")
public class WARHOGTeleOpTest extends LinearOpMode {
    public WARHOGTeleOpTest() throws InterruptedException {}

    @Override
    public void runOpMode() throws InterruptedException {

        //set up classes
        Drivetrain drivetrain = new Drivetrain(hardwareMap, telemetry);

        //set up variables
        double joyx, joyy, joyz, gas, basespeed, offset, modAngle;
        boolean centricityToggle, resetDriveAngle;

        offset = 0;
        Drivetrain.Centricity centricity = Drivetrain.Centricity.FIELD;

        basespeed = .4;

        Gamepad currentGamepad1 = new Gamepad();
        Gamepad currentGamepad2 = new Gamepad();
        Gamepad previousGamepad1 = new Gamepad();
        Gamepad previousGamepad2 = new Gamepad();

        while (!isStarted() && !isStopRequested()) {

            try {
                previousGamepad1.copy(currentGamepad1);
                previousGamepad2.copy(currentGamepad2);

                currentGamepad1.copy(gamepad1);
                currentGamepad2.copy(gamepad2);
            }
            catch (Exception e) {
                // Swallow the possible exception, it should not happen as
                // currentGamepad1/2 are being copied from valid Gamepads.
            }

            if(currentGamepad1.dpad_left && !previousGamepad1.dpad_left){
                offset-=90;
            }
            if(currentGamepad1.dpad_right && !previousGamepad1.dpad_right){
                offset+=90;
            }
            if (offset==360){offset=0;}
            if (offset==-90){offset=270;}

            telemetry.addData("Angle Offset", offset);
            telemetry.update();
        }


        //When start is pressed?

        //drivetrain.setAngleOffset(offset); //Leave it but we probably don't need it. Keep in mind that the backend is not really there anymore


        while(opModeIsActive()){
            //set up inputs
            try {
                previousGamepad1.copy(currentGamepad1);
                previousGamepad2.copy(currentGamepad2);

                currentGamepad1.copy(gamepad1);
                currentGamepad2.copy(gamepad2);
            }
            catch (Exception e) {
                // Swallow the possible exception, it should not happen as
                // currentGamepad1/2 are being copied from valid Gamepads.
            }
            //telemetry.addData("angle", drivetrain.getIMUData()/PI*180); TODO = TEST
            telemetry.addData("Yaw Angle/Heading (deg.)", drivetrain.getIMUAngleData(Drivetrain.AngleType.YAW)*180/PI);
            telemetry.addData("Pitch Angle (deg.)", drivetrain.getIMUAngleData(Drivetrain.AngleType.PITCH)*180/PI);
            telemetry.addData("Roll Angle (deg.)", drivetrain.getIMUAngleData(Drivetrain.AngleType.ROLL)*180/PI);


            //set up inputs

            //inputs that toggle the modes
            centricityToggle = currentGamepad1.dpad_down && !previousGamepad1.dpad_down; //change whether the drive is bot or field centric

            resetDriveAngle = currentGamepad1.dpad_up; //use when the robot is facing away from you

            //code to switch between field centric and bot centric drive
            if(centricityToggle){
                if(centricity==Drivetrain.Centricity.BOT){
                    centricity = Drivetrain.Centricity.FIELD;
                }
                else{
                    centricity = Drivetrain.Centricity.BOT;
                }
            }

            //set up vectors
            joyx = currentGamepad1.left_stick_x;
            joyy = -currentGamepad1.left_stick_y;
            joyz = -currentGamepad1.right_stick_x;
            gas = currentGamepad1.right_trigger*(1-basespeed);

            //print vectors
            telemetry.addData("y", joyy);
            telemetry.addData("x", joyx);
            telemetry.addData("z", joyz);

            //set and print motor powers
            double[] motorPowers = drivetrain.driveVectors(centricity, joyx, joyy, joyz, basespeed+gas);
            for (double line:motorPowers){
                telemetry.addLine( Double.toString(line) );
            }

            telemetry.addData("RightFrontDrive: ", drivetrain.getMotorInfo(Drivetrain.MotorPlacement.RIGHTFRONT, Drivetrain.MotorQuality.POW));
            telemetry.addData("RightBackDrive: ", drivetrain.getMotorInfo(Drivetrain.MotorPlacement.RIGHTBACK, Drivetrain.MotorQuality.POW));
            telemetry.addData("LeftFrontDrive: ", drivetrain.getMotorInfo(Drivetrain.MotorPlacement.LEFTFRONT, Drivetrain.MotorQuality.POW));
            telemetry.addData("LeftBackDrive: ", drivetrain.getMotorInfo(Drivetrain.MotorPlacement.LEFTBACK, Drivetrain.MotorQuality.POW));


            //reset the angle
            if(resetDriveAngle){
                //drivetrain.resetAngle(); TODO = TEST
                drivetrain.resetAngleData(Drivetrain.AngleType.HEADING);
            }

            //modAngle = (drivetrain.getIMUData()/PI*180)%360;    //********Reposition or take out these 2 lines if not needed, figure out what mod angle is for*********
            modAngle = (drivetrain.getIMUAngleData(Drivetrain.AngleType.HEADING)*180/PI)%360; //TODO = TEST
            telemetry.addData("mod angle", modAngle);

            //end step
            telemetry.update();
        }

    }
}