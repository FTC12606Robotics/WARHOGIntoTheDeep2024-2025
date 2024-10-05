package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class NewIntakeOuttake {
    public DcMotor slideMotor;
    private DcMotor armMotor;
    private Servo clawServo;

    private Telemetry telemetry;

    private int slideMax = 1000;
    private int slideMin = 100;

    private double armMin = 0;
    private double armMax = 1;

    private double clawOpen = 0;
    private double clawClose = .50;

    final static double slideSpeed = .5;
    final static double armSpeed = .5;

    enum slideHeight {MINIMUM, LOW, MEDIUM, HIGH, MAX}
    enum armPos {UP, DOWN}

    NewIntakeOuttake(HardwareMap hardwareMap, Telemetry telemetry){
        slideMotor = hardwareMap.get(DcMotor.class, "slideMotor");
        slideMotor.setDirection(DcMotor.Direction.REVERSE);
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        armMotor = hardwareMap.get(DcMotor.class, "armMotor");
        armMotor.setDirection(DcMotor.Direction.REVERSE);
        armMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        clawServo = hardwareMap.get(Servo.class, "clawServo");
        closeClaw();

        this.telemetry = telemetry;
    }


    //Slide Stuff

    //Set a slide height
    public void setSlideHeight(slideHeight height) {
        switch (height) {
            case MINIMUM:
                slideMotor.setTargetPosition(0);
                break;
            case LOW:
                slideMotor.setTargetPosition(500);
                break;
            case MEDIUM:
                slideMotor.setTargetPosition(750);
                break;
            case HIGH:
                slideMotor.setTargetPosition(1000);
                break;
            case MAX:
                slideMotor.setTargetPosition(slideMax);
            default:
                break;
        }

        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(slideSpeed);
        while(slideMotor.isBusy()) {
            telemetry.addLine("Set Slide Height");
            telemetry.addData("Slide Position", slideMotor.getCurrentPosition());
            telemetry.addData("Slide Target", slideMotor.getTargetPosition());
            telemetry.addData("Slide Power", slideMotor.getPower());
            telemetry.addData("Mode", slideMotor.getMode());
            telemetry.update();
        }
        //slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //slideMotor.setPower(0);
    }

    //Lower slide to minimum
    public void retractSlide(){
        slideMotor.setTargetPosition(0);

        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(slideSpeed);
        while(slideMotor.isBusy()) {
            telemetry.addLine("Retract Slide");
            telemetry.addData("Slide Position", slideMotor.getCurrentPosition());
            telemetry.addData("Slide Target", slideMotor.getTargetPosition());
            telemetry.addData("Slide Power", slideMotor.getPower());
            telemetry.addData("Mode", slideMotor.getMode());
            telemetry.update();
        }
        //slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //slideMotor.setPower(0);
    }


    //Arm Stuff

    public void setArmByDefault(armPos position){
        switch (position) {
            case DOWN:
                armMotor.setTargetPosition(100);
                break;
            case UP:
                armMotor.setTargetPosition(500);
                break;
            default:
                break;
        }

        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armMotor.setPower(armSpeed);
        while(armMotor.isBusy()) {
            telemetry.addLine("Set Arm Height");
            telemetry.addData("Arm Position", armMotor.getCurrentPosition());
            telemetry.addData("Arm Target", armMotor.getTargetPosition());
            telemetry.addData("Arm Power", armMotor.getPower());
            telemetry.addData("Mode", armMotor.getMode());
            telemetry.update();
        }
        //
        //
    }


    //Claw Stuff

    //Moves the claw
    public void runClaw(double pos){
        clawServo.setPosition(pos);
    }
    public void openClaw(){
        runClaw(clawOpen);
    }
    public void closeClaw(){
        runClaw(clawClose);
    }

    public void toggleClaw(){
        if (isClawOpen()){
            openClaw();
        }
        else{closeClaw();}
    }
    public boolean isClawOpen(){
        return clawServo.getPosition()<=.2;
    }
}
