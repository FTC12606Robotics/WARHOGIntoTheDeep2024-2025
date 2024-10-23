package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class NewIntakeOuttake {
    public DcMotor slideMotor;
    public DcMotor armMotor; //Change later when get good stuff
    private Servo clawServo;

    private Telemetry telemetry;

    int slideMax = 9050;
    int slideMin = 0;

    int armMin = 0;
    int armMax = 510;

    double clawOpen = .40;
    double clawClose = 0.53;
    final static double slideSpeed = .75;
    final static double armSpeed = .25;

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


    //=======================Slide Stuff========================

    //Set a slide height
    public void setSlideHeight(slideHeight height) {

        slideMotor.setTargetPosition(height.getValue());

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

        slideMotor.setTargetPosition(height.getValue());

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

    //TODO
    public void setSlideControllerPower(double power){
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (power == -1){
            power = -slideSpeed;
        }
        if (power == 1){
            power = slideSpeed;
        }

        int pos = getSlidePos();
        if (pos< slideMax && pos> slideMin){
            telemetry.addData("power slide",power);
            slideMotor.setPower(power);
        }
        else if (pos>=slideMax && power<0){
            slideMotor.setPower(power);
        }
        else if (pos<=slideMin && power>0){
            slideMotor.setPower(power);
        }
        else{
            slideMotor.setPower(0);
        }
        /*else{
            telemetry.addLine("You suck charlie");
            if (pos>= slideMax){
                telemetry.addLine("You suck charlie!");
                if (power<0){
                    telemetry.addLine("You suck charlie!!");
                    slideMotor.setPower(power);
                }
                else {
                    telemetry.addLine("You suck charlie!!!");
                    //slideMotor.setTargetPosition(max);
                    slideMotor.setPower(0);
                }
            }
            else if (pos<slideMin){
                telemetry.addLine("You suck charlie!!!!");
                if (power>0){
                    telemetry.addLine("You suck charlie!!!!!");
                    slideMotor.setPower(power);
                }
                else {
                    telemetry.addLine("You suck charlie!!!!!!");
                    //slideMotor.setTargetPosition(min);
                    slideMotor.setPower(0);
                }
            }
            //Catch weirdness
            else{
                slideMotor.setPower(0);
            }
        }*/
    }

    public int defaultSlideValue(slideHeight pos){return pos.getValue();}

    public int getSlidePos(){return slideMotor.getCurrentPosition();}

    public boolean isSlideGoingToPos(){return (slideMotor.getMode()==DcMotor.RunMode.RUN_TO_POSITION);}


    //======================Arm Stuff=========================

    public void setArmByDefault(armPos position){

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

        armMotor.setTargetPosition(position.getValue());

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

    //TODO
    public void setArmControllerPower(double power){
        //armMotor.setPower(0);
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (power == -1){
            power = -armSpeed;
        }
        if (power == 1){
            power = armSpeed;
        }

        int pos = getArmPos();

        if (pos< armMax && pos> armMin){
            telemetry.addData("arm power: ", power);
            armMotor.setPower(power);
        }
        else if(pos>= armMax && power<0){
            armMotor.setPower(power);
        }
        else if (pos<= armMin && power>0){
            armMotor.setPower(power);
        }
        else{
            armMotor.setPower(0);
        }
        /*else{
            telemetry.addLine("You suck charlie");
            if (pos>= armMax){
                if (power<0){
                    armMotor.setPower(power);
                }
                else {
                    //armMotor.setTargetPosition(max);
                    armMotor.setPower(0);
                }
            }
            else if (pos< armMin){
                if (power>0){
                    armMotor.setPower(power);
                }
                else {
                    //armMotor.setTargetPosition(min);
                    armMotor.setPower(0);
                }
            }
            //Catch weirdness
            else{
                armMotor.setPower(0);
            }
        }*/
    }

    public int defaultArmValue(armPos pos){return pos.getValue();}

    public int getArmPos(){return armMotor.getCurrentPosition();}

    public boolean isArmGoingToPos(){return (armMotor.getMode()==DcMotor.RunMode.RUN_TO_POSITION);}


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
