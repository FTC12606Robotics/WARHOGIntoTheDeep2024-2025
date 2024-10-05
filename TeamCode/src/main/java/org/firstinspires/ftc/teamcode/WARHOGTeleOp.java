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
        Intake intake = new Intake(hardwareMap); //Just so there is no errors take out.

        //set up variables
        double joyx, joyy, joyz, gas, basespeed, armpos, wristmod, offset, /*slideMovement,
                maxIncrease,*/ armposChange, intakeArmSpeed=.03, modAngle;
        boolean autoEjectMode = false;
        boolean autoIntakeMode = false;
        //boolean pauseToResetMaxIncrease = false;
        boolean /*outtakeGround, outtakeLow, outtakeMedium, outtakeHigh, toggleOuttakeClaw = false,*/
                centricityToggle, resetDriveAngle, autoEjectToggle, autoIntakeToggle, toggleIntakeClaw, /*oneDriver = false, oneDriverToggle,*/
                extendIntakeArm = false, retractIntakeArm = false, uprightIntakeArm = false, sizingIntakeArm = false, hoverIntakeArm = false, boardParallelIntakeArm = false,
                /*intakeCone = false,*/ wristFixed = false, wristFixedToggle = false/*, isOuttakeAtTarget,
                outtakeClawMoveIntake = false*/;

        offset = 0;
        Drivetrain.Centricity centricity = Drivetrain.Centricity.FIELD;

        basespeed = .4;
        armpos = intake.runArm(Intake.Height.UPRIGHT);

        Gamepad currentGamepad1 = new Gamepad();
        Gamepad currentGamepad2 = new Gamepad();
        Gamepad previousGamepad1 = new Gamepad();
        Gamepad previousGamepad2 = new Gamepad();

        while (!isStarted() && !isStopRequested()) {
            //outtake.openClaw();
            armpos = intake.runArm(Intake.Height.UPRIGHT);
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
        autoEjectMode = false;
        autoIntakeMode = false;

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

            //isOuttakeAtTarget = outtake.update();

    //set up inputs

            //inputs that toggle the modes
            centricityToggle = currentGamepad1.dpad_down && !previousGamepad1.dpad_down; //change whether the drive is bot or field centric
            autoEjectToggle = currentGamepad2.start && !previousGamepad2.start;
            autoIntakeToggle = currentGamepad2.back && !previousGamepad2.back;
            wristFixedToggle = currentGamepad2.left_trigger>.2 && !(previousGamepad2.left_trigger>.2);

            //change the modes based on the inputs
            if(wristFixedToggle) {
                wristFixed = !wristFixed;
            }
            if(autoEjectToggle){
                autoEjectMode = !autoEjectMode;
            }
            if(autoIntakeToggle){
                autoIntakeMode = !autoIntakeMode;
            }


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

            armposChange = currentGamepad2.left_stick_y*intakeArmSpeed;
            toggleIntakeClaw = currentGamepad2.left_bumper && !previousGamepad2.left_bumper;
            //*****************KEEP THE AUTO INTAKE MODE FOR NOW, MIGHT USE IT LATER*****************
            if(autoIntakeMode){
                //intakeCone = currentGamepad2.dpad_down;
                //if(toggleIntakeClaw){
                //waitForLoops.addWaitEvent("Raise Intake Arm", 20);
                //}
            }
            else {
                retractIntakeArm = currentGamepad2.dpad_down;
            }
            //sizingIntakeArm = currentGamepad2.dpad_right || (outtakeClawMoveIntake&&intake.getArmPos()>.7);
            sizingIntakeArm = currentGamepad2.dpad_right;
            uprightIntakeArm = currentGamepad2.dpad_left;
            extendIntakeArm = currentGamepad2.dpad_up && !previousGamepad2.dpad_up;
            hoverIntakeArm = currentGamepad2.x;
            boardParallelIntakeArm = currentGamepad2.b;


            //set up vectors
            joyx = currentGamepad1.left_stick_x;
            joyy = -currentGamepad1.left_stick_y;
            joyz = -currentGamepad1.right_stick_x;
            gas = currentGamepad1.right_trigger*(1-basespeed);

            //print vectors
            telemetry.addData("y", joyy);
            telemetry.addData("x", joyx);
            telemetry.addData("z", joyz);


            //turn off wrist fixed if arm is over a certain threshold
            if(toggleIntakeClaw && intake.isClawOpen() && intake.getArmPos()>.01){
                wristFixed=true;
            }
            if(toggleIntakeClaw && !intake.isClawOpen()){
                //wristFixed=false;
            }
            if(intake.getArmPos()>1.1){
                //wristFixed=false;
            }

            if(wristFixed){
                intake.changeWristMode(Intake.WristMode.INDEPENDENT);
            }
            else{
                intake.changeWristMode(Intake.WristMode.MATCHED);
            }

            //set and print motor powers
            double[] motorPowers = drivetrain.driveVectors(centricity, joyx, joyy, joyz, basespeed+gas);
            for (double line:motorPowers){
                telemetry.addLine( Double.toString(line) );
            }

            //reset the angle
            if(resetDriveAngle){
                drivetrain.resetAngleData(Drivetrain.AngleType.HEADING);
            }

            //move arm
            armpos += armposChange;
            if(armpos<0){armpos=0;}
            if(armpos>1){armpos=1;}
            //defined positions
            if(retractIntakeArm){
                armpos = intake.runArm(Intake.Height.RETRACTED);
            }
            modAngle = (drivetrain.getIMUAngleData(Drivetrain.AngleType.HEADING)/PI*180)%360;    //********Reposition or take out these 2 lines if not needed, figure out what nod angle is for*********
            telemetry.addData("mod angle", modAngle);
            //telemetry.addData("left cone stack", leftConeStack);
            //telemetry.addData("right cone stack", rightConeStack);
            if(extendIntakeArm){
                /*if(modAngle>45 && modAngle<135){
                    armpos = .15-.0375*(5-leftConeStack);
                    leftConeStack -= 1;
                    if(leftConeStack<1){
                        leftConeStack = 5;
                    }
                }
                else if(modAngle<-45 && modAngle>-135){
                    armpos = .15-.0375*(5-rightConeStack);
                    rightConeStack -= 1;
                    if(rightConeStack<1){
                        rightConeStack = 5;
                    }
                }
                else {*/
                    armpos = intake.runArm(Intake.Height.EXTENDED);
                //}
            }
            if(uprightIntakeArm){
                armpos = intake.runArm(Intake.Height.UPRIGHT);
            }
            if(hoverIntakeArm){
                armpos = intake.runArm(Intake.Height.HOVER);
            }
            if(boardParallelIntakeArm){
                armpos = intake.runArm(Intake.Height.BOARDPARALLEL);
            }
            if(sizingIntakeArm){
                armpos = intake.runArm(Intake.Height.DRIVESIZING);
            }
            /*if(intakeCone){
                intake.intakeCone();
            }*/

            //move the arm, modifying the wrist's position if right trigger is pressed
            wristmod = 0; //(currentGamepad2.left_trigger-.2)*.625;
            if(wristmod>0){
                intake.runArm(armpos, wristmod);
                telemetry.addData("Wrist Mod", wristmod);
            }
            else {
                intake.runArm(armpos);
            }
            telemetry.addData("Arm Position", armpos);

            //open/close the claw
            if(toggleIntakeClaw){
                intake.toggleClaw();
            }

            //end step
            telemetry.update();
        }

    }

}