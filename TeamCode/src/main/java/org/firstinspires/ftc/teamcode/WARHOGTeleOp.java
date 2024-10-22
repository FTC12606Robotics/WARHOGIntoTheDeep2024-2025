package org.firstinspires.ftc.teamcode;

import static java.lang.Math.PI;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import com.qualcomm.robotcore.hardware.DcMotor; //Take out when have good code

@TeleOp(name="WARHOGTeleOp", group="")
public class WARHOGTeleOp extends LinearOpMode {
    public WARHOGTeleOp() throws InterruptedException {}

    @Override
    public void runOpMode() throws InterruptedException {

        //set up classes
        Drivetrain drivetrain = new Drivetrain(hardwareMap, telemetry);
        NewIntakeOuttake newIntakeOuttake = new NewIntakeOuttake(hardwareMap, telemetry);

        //set up variables
        double joyx, joyy, joyz, gas, baseSpeed, offset, modAngle;
        boolean slideMinimumPos = false, slideLowPos = false, slideMediumPos = false, slideHighPos = false, slideMaxPos = false,
                centricityToggle, resetDriveAngle, clawToggle,
                uprightArmPos = false, sizingArmPos = false, downArmPos = false;

        offset = 0;
        Drivetrain.Centricity centricity = Drivetrain.Centricity.FIELD;

        baseSpeed = .4;
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
            //newIntakeOuttake.closeClaw();
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

            armPosChange = (int)(currentGamepad2.left_stick_y);
            slidePosChange = (int)(currentGamepad2.right_stick_y);
            clawToggle = currentGamepad2.left_bumper && !previousGamepad2.left_bumper;

            sizingArmPos = currentGamepad2.dpad_right;
            uprightArmPos = currentGamepad2.dpad_left;
            downArmPos = currentGamepad2.b;

            slideMinimumPos = currentGamepad2.dpad_down;
            slideLowPos = currentGamepad2.x;
            slideMediumPos = currentGamepad2.a;
            slideHighPos = currentGamepad2.dpad_up;
            slideMaxPos = currentGamepad2.y;


            //set up vectors
            joyx = currentGamepad1.left_stick_x;
            joyy = -currentGamepad1.left_stick_y;
            joyz = -currentGamepad1.right_stick_x;
            gas = currentGamepad1.right_trigger*(1-baseSpeed);

            //print vectors
            telemetry.addData("y", joyy);
            telemetry.addData("x", joyx);
            telemetry.addData("z", joyz);


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


            //move arm
            armPos += armPosChange;
            int powArm = 0;
            if (armPosChange < 0){
                powArm = 1;
            }
            else if (armPosChange > 0){
                powArm = -1;
            }
            else{
                powArm = 0;
            }
            /*if(!newIntakeOuttake.isArmGoingToPos()) {
                telemetry.addLine("moving using stick");
                newIntakeOuttake.setArmControllerPower(powArm);
                //telemetry.addLine("moving using stick");
            }*/
            if (gamepad2.left_stick_y !=0){
                newIntakeOuttake.setArmControllerPower(powArm);
            }

            //newIntakeOuttake.setArmByController(armPos);
            telemetry.addData("Arm Position", armPos);
            telemetry.addData("True Arm Position", newIntakeOuttake.getArmPos());

            //defined arm positions
            if(uprightArmPos){
                newIntakeOuttake.setArmByDefaultNoWait(NewIntakeOuttake.armPos.UPRIGHT);
                int armTarget = newIntakeOuttake.defaultArmValue(NewIntakeOuttake.armPos.UPRIGHT);
                if (newIntakeOuttake.getArmPos()<(armTarget+5) && newIntakeOuttake.getArmPos()>(armTarget-5)){
                    newIntakeOuttake.armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    newIntakeOuttake.armMotor.setPower(0);
                }
            }
            if(downArmPos){
                newIntakeOuttake.setArmByDefaultNoWait(NewIntakeOuttake.armPos.DOWN);
                int armTarget = newIntakeOuttake.defaultArmValue(NewIntakeOuttake.armPos.DOWN);
                if (newIntakeOuttake.getArmPos()<(armTarget+5) && newIntakeOuttake.getArmPos()>(armTarget-5)){
                    newIntakeOuttake.armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    newIntakeOuttake.armMotor.setPower(0);
                }
            }
            if(sizingArmPos){
                newIntakeOuttake.setArmByDefaultNoWait(NewIntakeOuttake.armPos.SIZING);
                int armTarget = newIntakeOuttake.defaultArmValue(NewIntakeOuttake.armPos.SIZING);
                if (newIntakeOuttake.getArmPos()<(armTarget+5) && newIntakeOuttake.getArmPos()>(armTarget-5)){
                    newIntakeOuttake.armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    newIntakeOuttake.armMotor.setPower(0);
                }
            }
            armPos = newIntakeOuttake.getArmPos();


            //move slide
            slidePos += slidePosChange;
            int powSlide = 0;
            if (slidePosChange < 0){
                powSlide = 1;
            }
            else if (slidePosChange > 0){
                powSlide = -1;
            }
            else{
                powSlide = 0;
            }
            /*if(!newIntakeOuttake.isSlideGoingToPos()) {
                telemetry.addLine("moving using stick");
                newIntakeOuttake.setSlideControllerPower(powSlide);
                //telemetry.addLine("moving using stick");
            }*/
            if (gamepad2.right_stick_y != 0){
                newIntakeOuttake.setSlideControllerPower(powSlide);
            }

            //newIntakeOuttake.setSlideHeightByController(slidePos);
            telemetry.addData("Slide Position", slidePos);
            telemetry.addData("True Slide Position", newIntakeOuttake.getSlidePos());

            //defined slide positions
            if(slideMinimumPos){
                newIntakeOuttake.setSlideHeightNoWait(NewIntakeOuttake.slideHeight.MINIMUM);
                int slideTarget = newIntakeOuttake.defaultSlideValue(NewIntakeOuttake.slideHeight.MINIMUM);
                if (newIntakeOuttake.getSlidePos()<(slideTarget+5) && newIntakeOuttake.getSlidePos()>(slideTarget-5)){
                    newIntakeOuttake.slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    newIntakeOuttake.slideMotor.setPower(0);
                }
            }
            if(slideLowPos){
                newIntakeOuttake.setSlideHeightNoWait(NewIntakeOuttake.slideHeight.LOW);
                int slideTarget = newIntakeOuttake.defaultSlideValue(NewIntakeOuttake.slideHeight.LOW);
                if (newIntakeOuttake.getSlidePos()<(slideTarget+5) && newIntakeOuttake.getSlidePos()>(slideTarget-5)){
                    newIntakeOuttake.slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    newIntakeOuttake.slideMotor.setPower(0);
                }
            }
            if(slideMediumPos){
                newIntakeOuttake.setSlideHeightNoWait(NewIntakeOuttake.slideHeight.MEDIUM);
                int slideTarget = newIntakeOuttake.defaultSlideValue(NewIntakeOuttake.slideHeight.MEDIUM);
                if (newIntakeOuttake.getSlidePos()<(slideTarget+5) && newIntakeOuttake.getSlidePos()>(slideTarget-5)){
                    newIntakeOuttake.slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    newIntakeOuttake.slideMotor.setPower(0);
                }
            }
            if(slideHighPos){
                newIntakeOuttake.setSlideHeightNoWait(NewIntakeOuttake.slideHeight.HIGH);
                int slideTarget = newIntakeOuttake.defaultSlideValue(NewIntakeOuttake.slideHeight.HIGH);
                if (newIntakeOuttake.getSlidePos()<(slideTarget+5) && newIntakeOuttake.getSlidePos()>(slideTarget-5)){
                    newIntakeOuttake.slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    newIntakeOuttake.slideMotor.setPower(0);
                }
            }
            if(slideMaxPos){
                newIntakeOuttake.setSlideHeightNoWait(NewIntakeOuttake.slideHeight.MAX);
                int slideTarget = newIntakeOuttake.defaultSlideValue(NewIntakeOuttake.slideHeight.MAX);
                if (newIntakeOuttake.getSlidePos()<(slideTarget+5) && newIntakeOuttake.getSlidePos()>(slideTarget-5)){
                    newIntakeOuttake.slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    newIntakeOuttake.slideMotor.setPower(0);
                }
            }
            slidePos = newIntakeOuttake.getSlidePos(); //Update other counter

            //open/close the claw
            if(clawToggle) {newIntakeOuttake.toggleClaw();}
            telemetry.addData("Claw Open?: ", newIntakeOuttake.isClawOpen());
            telemetry.addData("Claw Pos: ", newIntakeOuttake.clawPos());
            telemetry.addData("claw trigger: ", clawToggle);

            telemetry.addData("Left y joy: ", currentGamepad2.left_stick_y);
            telemetry.addData("right y joy: ", currentGamepad2.right_stick_y);
            telemetry.addData("powSlide: ", powSlide);
            telemetry.addData("powArm: ", powArm);

            //end step
            telemetry.update();
        }

    }

}