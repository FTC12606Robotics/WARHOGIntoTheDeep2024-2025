package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class NewIntakeOuttake {
    public DcMotor slideMotor;
    private DcMotor armMotor;
    private Servo clawServo;

    private Telemetry telemetry;

    int slideMax = 9050;
    int slideMin = 0;

    int armMin = 0;
    int armMax = 600;

    double clawOpen = .40;
    double clawClose = 0.53;
    final static double slideSpeed = 1;
    final static double armSpeed = .4;

    //enum slideHeight {MINIMUM, LOW, MEDIUM, HIGH, MAX}

    enum slideHeight {MINIMUM(0), LOW(2500), MEDIUM(6000), HIGH(7500), MAX(9000);
        private int value;

        private slideHeight(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    //enum armPos {UPRIGHT, DOWN, SIZING}
    enum armPos {UPRIGHT(500), DOWN(0), SIZING(100);
       private int value;

       private armPos(int value) {
           this.value = value;
       }

       public int getValue() {
           return value;
       }
      }

    NewIntakeOuttake(HardwareMap hardwareMap, Telemetry telemetry){
        slideMotor = hardwareMap.get(DcMotor.class, "slideMotor");
        slideMotor.setDirection(DcMotor.Direction.FORWARD);
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


    //=======================Slide Stuff========================

    //Set a slide height
    public void setSlideHeight(slideHeight height) {
        /*switch (height) {
            case MINIMUM:
                slideMotor.setTargetPosition(0);
                break;
            case LOW:
                slideMotor.setTargetPosition(3000);
                break;
            case MEDIUM:
                slideMotor.setTargetPosition(3500);
                break;
            case HIGH:
                slideMotor.setTargetPosition(4000);
                break;
            case MAX:
                slideMotor.setTargetPosition(slideMax);
            default:
                break;
        }*/
        slideMotor.setTargetPosition(height.getValue()); //todo TEST

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
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setPower(0);
    }

    //Lower slide to minimum, might take this out because above function works fine.
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
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setPower(0);
    }

    public void setSlideHeightNoWait(slideHeight height){
        /*switch (height) {
            case MINIMUM:
                slideMotor.setTargetPosition(0);
                break;
            case LOW:
                slideMotor.setTargetPosition(3000);
                break;
            case MEDIUM:
                slideMotor.setTargetPosition(5000);
                break;
            case HIGH:
                slideMotor.setTargetPosition(6500);
                break;
            case MAX:
                slideMotor.setTargetPosition(slideMax);
            default:
                break;
        }*/

        slideMotor.setTargetPosition(height.getValue()); //todo TEST

        slideMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slideMotor.setPower(slideSpeed);
        /*while(slideMotor.isBusy()) {
            telemetry.addLine("Set Slide Height");
            telemetry.addData("Slide Position", slideMotor.getCurrentPosition());
            telemetry.addData("Slide Target", slideMotor.getTargetPosition());
            telemetry.addData("Slide Power", slideMotor.getPower());
            telemetry.addData("Mode", slideMotor.getMode());
            telemetry.update();
        }*/

        //Solution to previous problem was to take these out, but it caused jittering. Needs more testing
        //slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //slideMotor.setPower(0);
    }

    //TODO test
    public void setSlideHeightByController(int pos){

    }

    public int getSlidePos(){
        return slideMotor.getCurrentPosition();
    }

    public boolean isSlideGoingToPos(){return (slideMotor.getMode()==DcMotor.RunMode.RUN_TO_POSITION);}


    //======================Arm Stuff=========================

    public void setArmByDefault(armPos position){
        /*switch (position) {
            case DOWN:
                armMotor.setTargetPosition(100);
                break;
            case UPRIGHT:
                armMotor.setTargetPosition(500);
                break;
            case SIZING:
                armMotor.setTargetPosition(100);
                break;
            default:
                break;
        }*/

        armMotor.setTargetPosition(position.getValue()); //todo TEST

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
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        armMotor.setPower(0);
    }

    public void setArmByDefaultNoWait(armPos position){
        /*switch (position) {
            case DOWN:
                armMotor.setTargetPosition(100);
                break;
            case UPRIGHT:
                armMotor.setTargetPosition(500);
                break;
            case SIZING:
                armMotor.setTargetPosition(100);
                break;
            default:
                break;
        }*/

        armMotor.setTargetPosition(position.getValue()); //todo TEST

        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armMotor.setPower(armSpeed);
        /*while(armMotor.isBusy()) {
            telemetry.addLine("Set Arm Height");
            telemetry.addData("Arm Position", armMotor.getCurrentPosition());
            telemetry.addData("Arm Target", armMotor.getTargetPosition());
            telemetry.addData("Arm Power", armMotor.getPower());
            telemetry.addData("Mode", armMotor.getMode());
            telemetry.update();
        }*/

        //Solution to previous problem was to take these out, but it caused jittering. Needs more testing
        //armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //armMotor.setPower(0);

        //if (getArmPos()<(targetPos+5) && getArmPos()>(targetPos-5)){
        //    //armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //    //armMotor.setPower(0);
        //}
    }

    public int defaultArmValue(armPos pos){
        /*switch (pos) {
            case DOWN:
                return 100;
            case UPRIGHT:
                return  500;
            case SIZING:
                return 100;
            default:
                return 0;
        }*/
        return pos.getValue();
    }

    //TODO test
    public void setArmByController(int pos){
        armMotor.setTargetPosition(pos*10);
        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armMotor.setPower(armSpeed);
    }

    public int getArmPos(){
        return armMotor.getCurrentPosition();
    }


    }
    //===================Claw Stuff===================

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

    //This is used to toggle the claw, based on the isClawOpen func.
    public void toggleClaw(){
        if (isClawOpen()){
            closeClaw();
        }
        else{
            openClaw();
        }
    }
    public boolean isClawOpen(){return clawServo.getPosition()<=.45;}
    public double clawPos(){return clawServo.getPosition();}
}
