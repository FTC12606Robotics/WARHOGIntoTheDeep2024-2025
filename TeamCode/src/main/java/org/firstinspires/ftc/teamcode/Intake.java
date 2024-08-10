package org.firstinspires.ftc.teamcode;

import static java.lang.Thread.sleep;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
//import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Intake {
    private Servo armLeft;
    private Servo armRight;
    private Servo wrist;
    private Servo rightClaw;
    private Servo leftClaw;

    double armMax = .99;
    double armMin = 0;
    double retracted = 1;
    double extended = armMin;
    double armHover = .2; //Just off the ground for transporting
    double armBoardParallel = .5;  //Parallel to the board
    double armUpright = .6; //Might not be needed
    double driveSizing = .8;
    double startSizing = .85;

    double endMod = .1;
    double newPos;

    boolean isClawOpen = true;

    WristMode wristMode;
    //set up servos and variables
    Intake(HardwareMap hardwareMap/*, Telemetry telemetry*/){
        armLeft = hardwareMap.get(Servo.class, "armLeft");
        armRight = hardwareMap.get(Servo.class, "armRight");
        wrist = hardwareMap.get(Servo.class, "wrist");
        rightClaw = hardwareMap.get(Servo.class, "right_claw");
        leftClaw = hardwareMap.get(Servo.class, "left_claw");
        wristMode = WristMode.MATCHED;
        //For begin of auto?
        closeClaw();
    }

    public enum WristMode{INDEPENDENT, MATCHED, SIDEWAYS}
    public enum Height{EXTENDED, UPRIGHT, RETRACTED, HOVER, BOARDPARALLEL, DRIVESIZING, STARTSIZING}

    //moves arm to a position
    public void runArm(double pos) throws InterruptedException{
        if (pos>armMax){
            newPos = armMax;
        }
        else if (pos<armMin){
            newPos = armMin;
        }
        else {
            newPos = pos;
        }

        armLeft.setPosition(newPos);
        armRight.setPosition(1-newPos);

        //if matched, make the wrist keep the cone upright
        if (wristMode== WristMode.MATCHED){
            if(newPos<.3){
                    runWrist(newPos + (.3-newPos)/.3*endMod);
            }
            else {
                runWrist(newPos-.02);
            }
        }

        if(isClawOpen()){
            openClaw();
        }
    }

    //moves arm to a position, changing the wristmode during only this function
    public void runArm(double pos, WristMode newWristMode){
        if (pos>armMax){
            newPos = armMax;
        }
        else if (pos>armMin){
            newPos = armMin;
        }
        else {
            newPos = pos;
        }

        armLeft.setPosition(newPos);
        armRight.setPosition(1-newPos);

        //if matched, make the wrist keep the cone upright
        if (newWristMode== WristMode.MATCHED){
            if(newPos>.7){
                runWrist(newPos+endMod);
            }
            else {
                runWrist(newPos);
            }
        }

        if(isClawOpen()){
            openClaw();
        }
    }

    //moves arm to a position
    public void runArm(double pos, double wristMod){
        if (pos>armMax){
            newPos = armMax;
        }
        else if (pos>armMin){
            newPos = armMin;
        }
        else if (pos>armMin){
            newPos = armMin;
        }
        else {
            newPos = pos;
        }

        armLeft.setPosition(newPos);
        armRight.setPosition(1-newPos);

        //make the wrist keep the cone upright, with the modification

        runWrist(newPos+wristMod);


        if(isClawOpen()){
            openClaw();
        }
    }

    public double runArm(Height height) throws InterruptedException{
        switch(height){
            case EXTENDED:
                runArm(extended);
                return extended;
            case UPRIGHT:
                runArm(armUpright);
                return armUpright;
            case RETRACTED:
                runArm(retracted);
                return retracted;
            case HOVER:
                runArm(armHover);
                return armHover;
            case BOARDPARALLEL:
                runArm(armBoardParallel);
                return armBoardParallel;
            case DRIVESIZING:
                runArm(driveSizing);
                return driveSizing;
            case STARTSIZING:
                runArm(startSizing);
                return startSizing;
            default:
                break;
        }

        if(isClawOpen()){
            openClaw();
        }

        return armUpright;
    }

    //this method moves the wrist to a position
    public void runWrist(double pos){
        wrist.setPosition(pos);
    }

    //this method changes whether the wrist matches rotation of the arm
    public void changeWristMode(WristMode newWristMode){
        wristMode = newWristMode;
    }

    //this method moves the claw to a position
    public void moveLeftClaw(double pos){
        leftClaw.setPosition(pos);
    }
    public void moveRightClaw(double pos){
        rightClaw.setPosition(pos);
    }

    public void openClaw(){
        isClawOpen = true;
        if(getArmPos()>.5) {
            moveLeftClaw(.6);
            moveRightClaw(.2);
        }
        else{
            moveLeftClaw(.25);
            moveRightClaw(.75);
        }
    }
    public void closeClaw(){
        isClawOpen = false;
        moveLeftClaw(.75);
        moveRightClaw(.2);
    }

    public void toggleClaw(){
        if (!isClawOpen()){
            openClaw();
        }
        else{
            closeClaw();
        }
    }

    //Don't think it is needed anymore
    public void intakeCone() throws InterruptedException{
        closeClaw();
        sleep(25);
        runArm(Height.RETRACTED);
        sleep(200);
        openClaw();
        sleep(50);
        runArm(Height.UPRIGHT);
    }

    //this method returns the arm's position
    //0 is all the way out, 1 is all the way in
    public double getArmPos(){
        return armLeft.getPosition();
    }

    public boolean isClawOpen(){
        return isClawOpen;
    }
}