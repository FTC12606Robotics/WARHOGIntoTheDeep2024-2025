package org.firstinspires.ftc.teamcode;

import static java.lang.Math.PI;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

@TeleOp(name="WARHOGMotorTest", group="")
public class WARHOGMotorTest extends LinearOpMode {
    public WARHOGMotorTest() throws InterruptedException {}

    @Override
    public void runOpMode() throws InterruptedException {

        //set up classes
        //Drivetrain drivetrain = new Drivetrain(hardwareMap, telemetry);
        NewNewIntakeOuttake newNewIntakeOuttake = new NewNewIntakeOuttake(hardwareMap, telemetry);

        //set up variables
        double joyx, joyy, joyz, gas, baseSpeed, offset, modAngle;
        boolean slideMinimumPos, slideLowPos, slideMediumPos, slideHighPos, slideMaxPos,
                centricityToggle, resetDriveAngle, clawToggle, encoderReset, PIDTEST, nolIMIT, armEncoderReset, slideEncoderReset,
                uprightArmPos, sizingArmPos, downArmPos;

        offset = 0;
        Drivetrain.Centricity centricity = Drivetrain.Centricity.FIELD;

        baseSpeed = .4;
        int armPos = newNewIntakeOuttake.getArmPos();
        double armSpeed = .1;
        int armPosChange;

        int slidePos = newNewIntakeOuttake.getSlidePos();
        double slideSpeed = .1;
        int slidePosChange;

        Gamepad currentGamepad1 = new Gamepad();
        Gamepad currentGamepad2 = new Gamepad();
        Gamepad previousGamepad1 = new Gamepad();
        Gamepad previousGamepad2 = new Gamepad();

        while (!isStarted() && !isStopRequested()) {
            //newIntakeOuttake.closeClaw();
            armPos = newNewIntakeOuttake.getArmPos();
            slidePos = newNewIntakeOuttake.getSlidePos();
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

        //drivetrain.setAngleOffset(offset); //We'll see if this works

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
            //telemetry.addData("angle", drivetrain.getIMUAngleData(Drivetrain.AngleType.HEADING)/PI*180);


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

            armPosChange = -(int)(currentGamepad2.left_stick_y); //Change to neg. to make code cleaner later TODO
            slidePosChange = -(int)(currentGamepad2.right_stick_y); //Change to neg. to make code cleaner later
            clawToggle = currentGamepad2.left_bumper && !previousGamepad2.left_bumper;
            encoderReset = currentGamepad2.right_bumper && !previousGamepad2.right_bumper;
            armEncoderReset = currentGamepad2.left_stick_button;
            slideEncoderReset = currentGamepad2.right_stick_button;

            sizingArmPos = currentGamepad2.b;
            uprightArmPos = currentGamepad2.dpad_left;
            downArmPos = currentGamepad2.dpad_right;

            slideMinimumPos = currentGamepad2.dpad_down;
            slideLowPos = currentGamepad2.x;
            slideMediumPos = currentGamepad2.a;
            slideHighPos = currentGamepad2.dpad_up;
            slideMaxPos = currentGamepad2.y;

            PIDTEST = currentGamepad2.left_trigger != 0;
            nolIMIT = currentGamepad2.right_trigger !=0;

            //set up vectors
            joyx = currentGamepad1.left_stick_x;
            joyy = -currentGamepad1.left_stick_y;
            joyz = -currentGamepad1.right_stick_x;
            gas = currentGamepad1.right_trigger*(1-baseSpeed);

            //print vectors
            telemetry.addData("y", joyy);
            telemetry.addData("x", joyx);
            telemetry.addData("z", joyz);

/*
            //set and print motor powers
            double[] motorPowers = drivetrain.driveVectors(centricity, joyx, joyy, joyz, baseSpeed+gas);
            for (double line:motorPowers){
                telemetry.addLine( Double.toString(line) );
            }

            //reset the angle
            if(resetDriveAngle){
                drivetrain.resetAngleData(Drivetrain.AngleType.HEADING);
            }

            modAngle = (drivetrain.getIMUAngleData(Drivetrain.AngleType.HEADING)/PI*180)%360;    //********Reposition or take out these 2 lines if not needed, figure out what nod angle is for*********
            telemetry.addData("mod angle", modAngle);
*/

            //Reset Motor Encoders to Zero
            if(encoderReset){
                newNewIntakeOuttake.resetEncoders();
            }
            if(armEncoderReset){
                newNewIntakeOuttake.resetArmMotorEncoder();
            }
            if(slideEncoderReset){
                newNewIntakeOuttake.resetSlideMotorEncoder();
            }

            //move arm
            armPos += armPosChange;
            int powArm = 0;
            if (armPosChange < 0){
                //powArm = -1;
                powArm = armPosChange;
            }
            else if (armPosChange > 0){
                //powArm = 1;
                powArm = armPosChange;
            }
            else{
                powArm = 0;
            }

            if (gamepad2.left_stick_y !=0 && !nolIMIT){
                newNewIntakeOuttake.setArmControllerPower2(powArm);
                telemetry.addLine("Moving arm with stick");
            }
            else if (gamepad2.left_stick_y !=0 && nolIMIT){
                newNewIntakeOuttake.setArmControllerPowerNoLimit(powArm);
                telemetry.addLine("Moving arm with stick WITHOUT LIMIT");
            }
            else  if (gamepad2.left_stick_y == 0 && !newNewIntakeOuttake.isArmGoingToPos()){
                newNewIntakeOuttake.setArmControllerPower2(0);
            }

            telemetry.addData("Arm Position", armPos);
            telemetry.addData("True Arm Position", newNewIntakeOuttake.getArmPos());

            //defined arm positions
            if(uprightArmPos){
                newNewIntakeOuttake.setArmByDefaultNoWait(NewNewIntakeOuttake.armPos.UPRIGHT);
            }
            if(downArmPos){
                newNewIntakeOuttake.setArmByDefaultNoWait(NewNewIntakeOuttake.armPos.DOWN);
            }
            if(sizingArmPos){
                newNewIntakeOuttake.setArmByDefaultNoWait(NewNewIntakeOuttake.armPos.SUBSIZING);
            }
            armPos = newNewIntakeOuttake.getArmPos();


            //move slide
            slidePos += slidePosChange;
            int powSlide = 0;
            if (slidePosChange < 0){
                //powSlide = -1;
                powSlide = slidePosChange;
            }
            else if (slidePosChange > 0){
                //powSlide = 1;
                powSlide = slidePosChange;
            }
            else{
                powSlide = 0;
            }

            if (gamepad2.right_stick_y != 0 && !nolIMIT){
                newNewIntakeOuttake.setSlideControllerPower(powSlide);
                telemetry.addLine("Moving slide with stick");
            }
            else if (gamepad2.right_stick_y != 0 && nolIMIT){
                newNewIntakeOuttake.setSlideControllerPowerNoLimit(powSlide);
                telemetry.addLine("Moving slide with stick WITHOUT LIMIT");
            }
            else if (gamepad2.right_stick_y == 0 && !newNewIntakeOuttake.isSlideGoingToPos()){
                newNewIntakeOuttake.setSlideControllerPower(0);
            }

            telemetry.addData("Slide Position", slidePos);
            telemetry.addData("True Slide Position", newNewIntakeOuttake.getSlidePos());

            //TODO FOR TEST
            if (PIDTEST){
                newNewIntakeOuttake.setSlideHeightPID(NewNewIntakeOuttake.slideHeight.MEDIUM);
            }

            //defined slide positions
            if(slideMinimumPos){
                newNewIntakeOuttake.setSlideHeightNoWait(NewNewIntakeOuttake.slideHeight.MINIMUM);
            }
            if(slideLowPos){
                newNewIntakeOuttake.setSlideHeightNoWait(NewNewIntakeOuttake.slideHeight.LOW);
            }
            if(slideMediumPos){
                newNewIntakeOuttake.setSlideHeightNoWait(NewNewIntakeOuttake.slideHeight.MEDIUM);
            }
            if(slideHighPos){
                newNewIntakeOuttake.setSlideHeightNoWait(NewNewIntakeOuttake.slideHeight.HIGH);
            }
            if(slideMaxPos){
                newNewIntakeOuttake.setSlideHeightNoWait(NewNewIntakeOuttake.slideHeight.MAX);
            }
            slidePos = newNewIntakeOuttake.getSlidePos(); //Update other counter

            //open/close the claw
            if(clawToggle) {newNewIntakeOuttake.toggleClaw();}
            telemetry.addData("Claw Open?: ", newNewIntakeOuttake.isClawOpen());

            telemetry.addData("Left y joy: ", currentGamepad2.left_stick_y);
            telemetry.addData("right y joy: ", currentGamepad2.right_stick_y);
            telemetry.addData("powSlide: ", powSlide);
            telemetry.addData("powArm: ", powArm);

            //end step
            telemetry.update();
        }

    }

}
