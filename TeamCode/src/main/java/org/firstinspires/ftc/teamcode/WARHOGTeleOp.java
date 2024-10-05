package org.firstinspires.ftc.teamcode;

import static java.lang.Math.PI;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

@TeleOp(name="WARHOGTeleOp", group="")
public class WARHOGTeleOp extends LinearOpMode {
    public WARHOGTeleOp() throws InterruptedException {}

    @Override
    public void runOpMode() throws InterruptedException {

        //set up classes
        Drivetrain drivetrain = new Drivetrain(hardwareMap, telemetry);
        NewIntakeOuttake newIntakeOuttake = new NewIntakeOuttake(hardwareMap, telemetry);
        //Intake intake = new Intake(hardwareMap); //Just so there is no errors take out.

        //set up variables
        double joyx, joyy, joyz, gas, basespeed, offset, /*slideMovement,
                maxIncrease,*/ modAngle;
        boolean slideMinimumPos = false, slideLowPos = false, slideMediumPos = false, slideHighPos = false, slideMaxPos = false,
                centricityToggle, resetDriveAngle, clawToggle,
                uprightArmPos = false, sizingArmPos = false, downArmPos = false;

        offset = 0;
        Drivetrain.Centricity centricity = Drivetrain.Centricity.FIELD;

        basespeed = .4;
        int armPos = newIntakeOuttake.getArmPos();
        double armSpeed = .1;
        int armPosChange;

        int slidePos = newIntakeOuttake.getSlidePos();
        double slideSpeed = .1;
        int slidePosChange;

        Gamepad currentGamepad1 = new Gamepad();
        Gamepad currentGamepad2 = new Gamepad();
        Gamepad previousGamepad1 = new Gamepad();
        Gamepad previousGamepad2 = new Gamepad();

        while (!isStarted() && !isStopRequested()) {
            //outtake.openClaw();
            armPos = newIntakeOuttake.getArmPos();
            slidePos = newIntakeOuttake.getSlidePos();
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
            telemetry.addData("angle", drivetrain.getIMUAngleData(Drivetrain.AngleType.HEADING)/PI*180);


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

            armPosChange = (int)(currentGamepad2.left_stick_y*armSpeed);
            slidePosChange = (int)(currentGamepad2.right_stick_y*slideSpeed);
            clawToggle = currentGamepad2.left_bumper && !previousGamepad2.left_bumper;

            sizingArmPos = currentGamepad2.dpad_right;
            uprightArmPos = currentGamepad2.dpad_left;
            downArmPos = currentGamepad2.b;

            slideMinimumPos = currentGamepad2.dpad_down;
            slideHighPos = currentGamepad2.dpad_up;
            slideMediumPos = currentGamepad2.a;


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

            //reset the angle
            if(resetDriveAngle){
                drivetrain.resetAngleData(Drivetrain.AngleType.HEADING);
            }

            modAngle = (drivetrain.getIMUAngleData(Drivetrain.AngleType.HEADING)/PI*180)%360;    //********Reposition or take out these 2 lines if not needed, figure out what nod angle is for*********
            telemetry.addData("mod angle", modAngle);

            //move arm
            armPos += armPosChange;
            if(armPos<0){armPos=0;}
            if(armPos>10){armPos=10;}

            newIntakeOuttake.setArmByController(armPos);
            telemetry.addData("Arm Position", armPos);
            telemetry.addData("True Arm Position", newIntakeOuttake.getArmPos());

            //defined arm positions
            if(uprightArmPos){
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                armPos = newIntakeOuttake.getArmPos();
            }
            if(downArmPos){
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.DOWN);
                armPos = newIntakeOuttake.getArmPos();
            }
            if(sizingArmPos){
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.SIZING);
                armPos = newIntakeOuttake.getArmPos();
            }

            //move slide
            slidePos += slidePosChange;
            if(slidePos<0){slidePos=0;}
            if(slidePos>1000){slidePos=1000;}

            newIntakeOuttake.setSlideHeightByController(slidePos);
            telemetry.addData("Slide Position", slidePos);
            telemetry.addData("True Slide Position", newIntakeOuttake.getSlidePos());

            //defined slide position
            if(slideMinimumPos){
                newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.MINIMUM);
                slidePos = newIntakeOuttake.getSlidePos();
            }
            if(slideLowPos){
                newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.LOW);
                slidePos = newIntakeOuttake.getSlidePos();
            }
            if(slideMediumPos){
                newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.MEDIUM);
                slidePos = newIntakeOuttake.getSlidePos();
            }
            if(slideHighPos){
                newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.HIGH);
                slidePos = newIntakeOuttake.getSlidePos();
            }
            if(slideMaxPos){
                newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.MAX);
                slidePos = newIntakeOuttake.getSlidePos();
            }

            //open/close the claw
            if(clawToggle) {
                newIntakeOuttake.toggleClaw();
            }
            telemetry.addData("Claw: ", newIntakeOuttake.isClawOpen());

            //end step
            telemetry.update();
        }

    }

}