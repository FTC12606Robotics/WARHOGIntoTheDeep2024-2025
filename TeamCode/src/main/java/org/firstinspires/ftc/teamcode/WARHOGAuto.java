package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

//import java.util.ArrayList;

@Autonomous(name="WARHOGAuto", group="")
public class WARHOGAuto extends LinearOpMode {

    public WARHOGAuto() throws InterruptedException {}

    //private StartPosColor startPosColor = StartPosColor.RED; //Shouldn't matter this game
    //private enum StartPosColor {RED, BLUE}
    private StartPosPosition startPosPosition = StartPosPosition.RIGHT;
    private enum StartPosPosition {LEFT, RIGHT}
    private ParkPos parkPos = ParkPos.NO;
    private enum ParkPos {NO, ASCENT, OBSERVATION} //For where to park if at all
    private ActionCombination actionCombination = ActionCombination.PARK_ONLY;
    private enum ActionCombination {PARK_ONLY, NET_ONLY, NET2, SPECIMEN_ONLY, NONE, NET_PARK, SPECIMEN_PARK}

    OpenCvCamera camera;
    //AprilTagDetectionPipeline aprilTagDetectionPipeline;

    Gamepad currentGamepad1 = new Gamepad();
    Gamepad currentGamepad2 = new Gamepad();
    Gamepad previousGamepad1 = new Gamepad();
    Gamepad previousGamepad2 = new Gamepad();

    static final double FEET_PER_METER = 3.28084;

    boolean left=false, right=false, red=false, blue=false; //Bools to set position

    //Set to default used above
    boolean willPark = true; //for interior code use for if the robot will park
    boolean willNet = false; //for interior code use for if the robot will place a pixel on a spike
    boolean willSpecimen = false; //for interior code use for if the robot will place a pixel on the backdrop
    boolean useCamera = true; //for testing to say if it will use the camera

    double speed = .50;
    double startSleep = 0; //How many seconds to wait before starting autonomous

    //this stuff does not need to be changed
    // Lens intrinsics
    // UNITS ARE PIXELS
    // NOTE: this calibration is for the C920 webcam at 800x448.
    // You will need to do your own calibration for other configurations!
/*
    double fx = 578.272;
    double fy = 578.272;
    double cx = 402.145;
    double cy = 221.506;

    double tagsize = 0.166;

    //tag ID 1,2,3 from the 36h11 family
    int LEFT = 1;
    int MIDDLE = 2;
    int RIGHT = 3;

    int ID_TAG_OF_INTEREST = 18;

    AprilTagDetection tagOfInterest = null;
 */

    @Override
    public void runOpMode() throws InterruptedException {

        Drivetrain drivetrain = new Drivetrain(hardwareMap, telemetry);
        NewIntakeOuttake newIntakeOuttake = new NewIntakeOuttake(hardwareMap, telemetry);

        //Setup Camera and OpenCV
/*        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        //aprilTagDetectionPipeline = new AprilTagDetectionPipeline(tagsize, fx, fy, cx, cy);

        //camera.setPipeline(aprilTagDetectionPipeline);

        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                camera.startStreaming(1280,720, OpenCvCameraRotation.UPSIDE_DOWN);
            }

            @Override
            public void onError(int errorCode)
            {
                telemetry.addLine("Camera Failed to setup");
                telemetry.update();
            }
        });
*/

        telemetry.setMsTransmissionInterval(50);

        //init loop
        while (!isStarted() && !isStopRequested()) {

            //set up inputs - have previous so that you can check rising edge
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

            //set up initialization procedures
            /*if (currentGamepad1.b) {
                startPosColor = StartPosColor.RED;
            }
            if (currentGamepad1.x) {
                startPosColor = StartPosColor.BLUE;
            }*/
            if (currentGamepad1.dpad_left) {
                startPosPosition = StartPosPosition.LEFT;
            }
            if (currentGamepad1.dpad_right) {
                startPosPosition = StartPosPosition.RIGHT;
            }

            //Override speed with driver hub
            if(currentGamepad1.y && !previousGamepad1.y){
                speed+=.05;
            }
            if(currentGamepad1.a && !previousGamepad1.a){
                speed-=.05;
            }
            if(speed>1){
                speed=1;
            }
            if(speed<.3){
                speed=.3;
            }

            //Override startSleep with driver hub
            if(currentGamepad1.dpad_up && !previousGamepad1.dpad_up){
                startSleep+=.5;
            }
            if(currentGamepad1.dpad_down && !previousGamepad1.dpad_down){
                startSleep-=.5;
            }
            if(startSleep>20){
                startSleep=20;
            }
            if(startSleep<0){
                startSleep=0;
            }

            //To set where to park
            if (currentGamepad1.right_bumper && !previousGamepad1.right_bumper) {
                if (parkPos == ParkPos.NO){ //Only runs on init
                    parkPos = ParkPos.ASCENT;
                }
                else if(parkPos == ParkPos.ASCENT){
                    parkPos = ParkPos.OBSERVATION;
                }
                else if (parkPos == ParkPos.OBSERVATION){
                    parkPos = ParkPos.ASCENT;
                }
            }

            //Go through different combinations of things to do and set bools
            if (currentGamepad1.left_bumper && !previousGamepad1.left_bumper){
                if(actionCombination == ActionCombination.PARK_ONLY){
                    actionCombination = ActionCombination.SPECIMEN_ONLY;
                    willPark = false;
                    willSpecimen = true;
                    willNet = false;
                }
                else if(actionCombination == ActionCombination.SPECIMEN_ONLY){
                    actionCombination = ActionCombination.NET_ONLY;
                    willPark = false;
                    willSpecimen = false;
                    willNet = true;
                }
                else if(actionCombination == ActionCombination.NET_ONLY){
                    actionCombination = ActionCombination.NET2;
                    willPark = false;
                    willSpecimen = false;
                    willNet = true;
                }
                else if(actionCombination == ActionCombination.NET2){
                    actionCombination = ActionCombination.SPECIMEN_PARK;
                    willPark = true;
                    willSpecimen = true;
                    willNet = false;
                }
                else if(actionCombination == ActionCombination.SPECIMEN_PARK){
                    actionCombination = ActionCombination.NET_PARK;
                    willPark = true;
                    willSpecimen = false;
                    willNet = true;
                }
                else if(actionCombination == ActionCombination.NET_PARK){
                    actionCombination = ActionCombination.NONE;
                    willPark = false;
                    willSpecimen = false;
                    willNet = false;
                }
                else if(actionCombination == ActionCombination.NONE){
                    actionCombination = ActionCombination.PARK_ONLY;
                    willPark = true;
                    willSpecimen = false;
                    willNet = false;
                }
            }

            //For camera usage in decision making
            if (currentGamepad1.left_stick_button && !previousGamepad1.left_stick_button){
                useCamera = !useCamera;
            }

            //telemetry.addData("Color (b/x)", startPosColor);
            telemetry.addData("Start Position (left/right)", startPosPosition);
            telemetry.addData("Speed (a/y)", speed);
            telemetry.addData("startSleep (up/down)", startSleep);
            telemetry.addData("Park Pos. (rbumper)", parkPos);
            telemetry.addData("Combination (lbumper)", actionCombination);
            telemetry.addLine();
            telemetry.addData("Will Park", willPark);
            telemetry.addData("Will Specimen", willSpecimen);
            telemetry.addData("Will Net", willNet);
            telemetry.addLine();
            telemetry.addData("Use Camera? (lsbtn)", useCamera);

            /*ArrayList<AprilTagDetection> currentDetections = aprilTagDetectionPipeline.getLatestDetections();

            //detect apriltags
            if(currentDetections.size() != 0)
            {
                boolean tagFound = false;

                for(AprilTagDetection tag : currentDetections)
                {
                    if(tag.id == LEFT || tag.id == MIDDLE || tag.id == RIGHT)
                    {
                        tagOfInterest = tag;
                        tagFound = true;
                        break;
                    }
                }

                if(tagFound)
                {
                    telemetry.addLine("Tag of interest is in sight!\n\nLocation data:");
                    tagToTelemetry(tagOfInterest);
                }
                else
                {
                    telemetry.addLine("Don't see tag of interest :(");

                    if(tagOfInterest == null)
                    {
                        telemetry.addLine("(The tag has never been seen)");
                    }
                    else
                    {
                        telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:");
                        tagToTelemetry(tagOfInterest);
                    }
                }

            }
            else
            {
                telemetry.addLine("Don't see tag of interest :(");

                if(tagOfInterest == null)
                {
                    telemetry.addLine("(The tag has never been seen)");
                }
                else
                {
                    telemetry.addLine("\nBut we HAVE seen the tag before; last seen at:");
                    tagToTelemetry(tagOfInterest);
                }
            }*/

            telemetry.update();
            sleep(10);
        }


        //=============Start command just came in================

        //Stop the camera
        //camera.stopStreaming();

        //Set modifier values
        /*switch (startPosColor){
            case RED:
                red = true;
                break;
            case BLUE:
                blue = true;
                break;
        }*/
        switch (startPosPosition){
            case LEFT:
                left = true;
                break;
            case RIGHT:
                right = true;
                break;
        }

        //2024-2025 Autonomous Main Code:

        //Wait
        sleep((long)((startSleep)*1000));

        //Blocks to run for different start positions
        if(left){
            if(actionCombination==ActionCombination.NET_ONLY){
                //Yea
                drivetrain.MoveForDis(4, speed);
                //sleep(100);
                drivetrain.SideMoveForDis(-46, speed);
                //sleep(300);
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                //sleep(100);
                drivetrain.RotateForDegree(170, speed/2);
                //sleep(500);
                newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.MAX);
                sleep(750);
                newIntakeOuttake.setArm(1050);
                sleep(400);
                newIntakeOuttake.openClaw();
                sleep(200);
                newIntakeOuttake.closeClaw();
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                sleep(800);
                drivetrain.RotateForDegree(-60, speed/2);
                //sleep(200);
                newIntakeOuttake.retractSlide();
                //sleep(200);
                drivetrain.MoveForDis(-6, speed); //So we don't hit the pole?
                //sleep(300);
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.DOWN);
            }
            if(actionCombination==ActionCombination.NET2){
                //Yea
                drivetrain.MoveForDis(4, speed);
                //sleep(100);
                drivetrain.SideMoveForDis(-20, speed);
                //sleep(300);
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                //sleep(100);
                drivetrain.RotateForDegree(170, speed/2);
                //sleep(500);
                newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.MAX);
                sleep(300);
                newIntakeOuttake.setArm(1050);
                sleep(200);
                newIntakeOuttake.openClaw();
                //sleep(200);
                //newIntakeOuttake.closeClaw();
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                sleep(500);
                drivetrain.RotateForDegree(-168, speed/2);
                //sleep(200);
                newIntakeOuttake.setSlide(1000);
                //newIntakeOuttake.retractSlide();
                //sleep(200);
                //drivetrain.SideMoveForDis(9, speed); //Line up with sample
                //sleep(300);
                newIntakeOuttake.setArm(50);
                drivetrain.MoveForDis(2, speed);
                newIntakeOuttake.setSlide(2800);
                sleep(200);
                newIntakeOuttake.closeClaw();
                sleep(100);
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                //sleep(100);
                //drivetrain.SideMoveForDis(-8, speed*.75);
                drivetrain.RotateForDegree(168, speed/2);
                newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.MAX);
                sleep(100);
                drivetrain.MoveForDis(1, speed);
                sleep(50);
                newIntakeOuttake.setArm(1050);
                sleep(100);
                newIntakeOuttake.openClaw();
                sleep(200);
                newIntakeOuttake.closeClaw();
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                sleep(800);
                drivetrain.RotateForDegree(-60, speed/2);
                //sleep(200);
                newIntakeOuttake.retractSlide();
                //sleep(200);
                drivetrain.MoveForDis(-6, speed); //So we don't hit the pole?
                //sleep(300);
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.DOWN);
            }
            if(actionCombination==ActionCombination.SPECIMEN_ONLY){
                //OK?
            }
            if(actionCombination==ActionCombination.NET_PARK){
                if (parkPos==ParkPos.ASCENT){
                    //Yea
                    drivetrain.MoveForDis(4, speed);
                    sleep(50);
                    drivetrain.SideMoveForDis(-46, speed);
                    //sleep(100);
                    newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                    //sleep(100);
                    drivetrain.RotateForDegree(170, speed/2);
                    sleep(150);
                    newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.MAX);
                    sleep(750);
                    newIntakeOuttake.setArm(1050);
                    sleep(400);
                    newIntakeOuttake.openClaw();
                    sleep(200);
                    newIntakeOuttake.closeClaw();
                    //Don't think we need a pause here
                    newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                    sleep(500);
                    drivetrain.RotateForDegree(-54, speed/2);
                    //sleep(100);
                    newIntakeOuttake.retractSlide();
                    //sleep(100);
                    drivetrain.SideMoveForDis(44.5, speed);
                    sleep(400);
                    drivetrain.MoveForDis(-30, speed);
                    drivetrain.MoveForDis(-8.5, speed/1.4);

                }
                if (parkPos==ParkPos.OBSERVATION){
                    //Why?/prob not
                }
            }
            if(actionCombination==ActionCombination.SPECIMEN_PARK){
                if (parkPos==ParkPos.ASCENT){
                    //Possible
                }
                if (parkPos==ParkPos.OBSERVATION){
                    //NO
                }
                telemetry.addLine("Ummm, what are you doing?");
            }
            if(actionCombination==ActionCombination.PARK_ONLY){
                if (parkPos==ParkPos.ASCENT){
                    drivetrain.MoveForDis(4, speed);
                    sleep(100);
                    drivetrain.SideMoveForDis(-26, speed);
                    sleep(600);
                    drivetrain.MoveForDis(47, speed);
                    sleep(700);
                    drivetrain.RotateForDegree(110, speed/2);
                    sleep(600);
                    newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                    sleep(800);
                    drivetrain.MoveForDis(-15, speed/2);
                }
                else{
                    telemetry.addLine("ERROR: STARTED LEFT; PARK POS.: OBSERVATION/NO  #No can do.");
                }
            }

            telemetry.addLine("Auto Complete");
            telemetry.update();
        }
        else if(right){
            if(actionCombination==ActionCombination.SPECIMEN_ONLY){
                //YES TODO never been done
                drivetrain.MoveForDis(24, speed);
                sleep(1000);
                drivetrain.SideMoveForDis(-12, speed);
                sleep(1000);
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                sleep(1000);
                newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.LOW);
                sleep(1000);
                newIntakeOuttake.setArm(300);
                sleep(1000);
                //retract a little to clip the specimen on the pole
                newIntakeOuttake.retractSlide();
                sleep(1000);
                newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.DOWN);
                sleep(1000);
                newIntakeOuttake.openClaw();
                sleep(1000);
                drivetrain.SideMoveForDis(12, speed);

            }
            if(actionCombination==ActionCombination.NET_PARK){
                //Probably don't want to do net
                if (parkPos==ParkPos.ASCENT){
                    //Yeah, no
                }
                if (parkPos==ParkPos.OBSERVATION){
                    //Ok to park if don't do net
                    drivetrain.MoveForDis(4, speed);
                    sleep(200);
                    drivetrain.SideMoveForDis(40, speed);
                }
            }
            if(actionCombination==ActionCombination.SPECIMEN_PARK){
                if (parkPos==ParkPos.OBSERVATION){
                    //Yea
                    drivetrain.MoveForDis(24, speed);
                    sleep(1000);
                    drivetrain.SideMoveForDis(-12, speed);
                    sleep(1000);
                    newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.UPRIGHT);
                    sleep(1000);
                    newIntakeOuttake.setSlideHeight(NewIntakeOuttake.slideHeight.LOW);
                    sleep(1000);
                    newIntakeOuttake.setArm(300); //TODO
                    sleep(1000);
                    //retract a little to clip the specimen on the pole
                    newIntakeOuttake.retractSlide();
                    sleep(1000);
                    newIntakeOuttake.setArmByDefault(NewIntakeOuttake.armPos.DOWN);
                    sleep(1000);
                    newIntakeOuttake.openClaw();
                    sleep(1000);
                    drivetrain.MoveForDis(-20, speed);
                    sleep(1000);
                    drivetrain.SideMoveForDis(30, speed);
                }
            }
            if(actionCombination==ActionCombination.PARK_ONLY){
                if (parkPos==ParkPos.OBSERVATION){
                    drivetrain.MoveForDis(4, speed);
                    sleep(200);
                    drivetrain.SideMoveForDis(40, speed);
                }
                else{
                    telemetry.addLine("ERROR: STARTED RIGHT; PARK POS.: ASCENT/NO  #No can do.");
                }
            }

            telemetry.addLine("Auto Complete");
            telemetry.update();
        }

    /*void tagToTelemetry(AprilTagDetection detection)
    {
        telemetry.addLine(String.format("\nDetected tag ID=%d", detection.id));
        telemetry.addLine(String.format("Translation X: %.2f feet", detection.pose.x*FEET_PER_METER));
        telemetry.addLine(String.format("Translation Y: %.2f feet", detection.pose.y*FEET_PER_METER));
        telemetry.addLine(String.format("Translation Z: %.2f feet", detection.pose.z*FEET_PER_METER));
        telemetry.addLine(String.format("Rotation Yaw: %.2f degrees", Math.toDegrees(detection.pose.yaw)));
        telemetry.addLine(String.format("Rotation Pitch: %.2f degrees", Math.toDegrees(detection.pose.pitch)));
        telemetry.addLine(String.format("Rotation Roll: %.2f degrees", Math.toDegrees(detection.pose.roll)));

    }*/

    }
}