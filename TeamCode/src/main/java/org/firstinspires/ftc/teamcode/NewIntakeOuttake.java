package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class NewIntakeOuttake {
    private DcMotor slideMotor;
    private DcMotor armMotor;
    private Servo clawServo;

    private Telemetry telemetry;

    final int slideMax = 6670;
    final int slideMin = 0;

    final int armMin = 0;
    final int armMax = 1280;

    final static int motorLimitBuffer = 100;

    final static double clawOpen = .40;
    final static double clawClose = 0.54;
    final static double slideSpeed = 1;
    final static double armSpeed = .40;

    //enum slideHeight {MINIMUM, LOW, MEDIUM, HIGH, MAX}
    enum slideHeight {MINIMUM(0), LOW(1600), MEDIUM(3300), HIGH(5500), MAX(6650);
        private int value;

        private slideHeight(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    //enum armPos {UPRIGHT, DOWN, SIZING}
    enum armPos {UPRIGHT(1200), DOWN(0), SUBSIZING(350);
       private int value;

       private armPos(int value) {
           this.value = value;
       }

       public int getValue() {
           return value;
       }
      }

      //For PID
    double integralSum = 0;
    final double Kp = 0.013;
    final double Ki = 0.00;
    final double Kd = 0.00;

    ElapsedTime timer = new ElapsedTime();
    private double lastError = 0;

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

    //For resetting encoders
    public void resetEncoders(){
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        telemetry.addLine("Resought Motor Encoders");
    }

    //For future use, no worky
    /*public double motorAmps(){
        double slideAmps = slideMotor.getCurrent();
        double armAmps = armMotor.getCurrent();

        double amps = slideAmps + armAmps;
        return amps;
    }*/


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

    public double PIDControl(int target, double currentPos){
        double error = target - currentPos;
        integralSum += error * timer.seconds();
        double derivative = (error - lastError)/timer.seconds();
        lastError = error;

        timer.reset();

        double output = (error*Kp) + (derivative*Kd) + (integralSum*Ki);
        return output;
    }

    public void setSlideHeightPID(slideHeight height) {

        //slideMotor.setTargetPosition(height.getValue());

        slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        double power = PIDControl(height.getValue(), slideMotor.getCurrentPosition());
        slideMotor.setPower(power);
        telemetry.addData("Setting Slide Height with PID", power);
        /*while(slideMotor.isBusy()) {
            telemetry.addLine("Set Slide Height");
            telemetry.addData("Slide Position", slideMotor.getCurrentPosition());
            telemetry.addData("Slide Target", slideMotor.getTargetPosition());
            telemetry.addData("Slide Power", slideMotor.getPower());
            telemetry.addData("Mode", slideMotor.getMode());
            telemetry.update();
        }
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setPower(0);*/
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
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        slideMotor.setPower(0);
    }

    //Setting to predetermined locations without waiting for conformation of pos.
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

    public void setSlideControllerPower(double power){
        int SlideMax = slideMax; //For the soft limit
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (power == -1){
            power = -slideSpeed;
        }
        if (power == 1){
            power = slideSpeed;
        }

        int pos = slideMotor.getCurrentPosition();

        //for the soft limit
        if (getArmPos() <= 400){
            SlideMax = 6300;
        }

        // Approach limits with reduced speed
        if (pos >= (SlideMax - motorLimitBuffer) && power > 0) {
            power = power/2; // Slow down as it approaches max
        }
        else if (pos <= (slideMin + motorLimitBuffer) && power < 0) {
            power = power/2; // Slow down as it approaches min
        }

        if (pos< SlideMax && pos> slideMin){
            telemetry.addData("slide power: T1", power);
            slideMotor.setPower(power);
        }
        else if (pos>=SlideMax && power<0){
            telemetry.addData("slide power: T2", power);
            slideMotor.setPower(power);
        }
        else if (pos<=slideMin && power>0){
            telemetry.addData("slide power: T3", power);
            slideMotor.setPower(power);
        }
        else{
            slideMotor.setPower(0);
        }
    }

    //For emergencies
    public void setSlideControllerPowerNoLimit(double power){
        slideMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (power == -1){
            power = -slideSpeed;
        }
        if (power == 1){
            power = slideSpeed;
        }

        slideMotor.setPower(power);
    }

    //For the instances where I need to get the values of the defaults
    public int defaultSlideValue(slideHeight pos){return pos.getValue();}

    public int getSlidePos(){return slideMotor.getCurrentPosition();}

    //For checking if the slide is currently in motion with a default position
    public boolean isSlideGoingToPos(){return (slideMotor.getMode()==DcMotor.RunMode.RUN_TO_POSITION);}

    //Reset the slideMotor separately
    public void resetSlideMotorEncoder(){
        slideMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        telemetry.addLine("Resetted slide Encoder");
    }


    //======================Arm Stuff=========================

    //Set an arm angle
    public void setArmByDefault(armPos position){

        armMotor.setTargetPosition(position.getValue());

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

    //Setting to predetermined locations without waiting for conformation of pos.
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
    }

    //Set an arm height not as a default
    public void setArm(int position) {

        if (position <= armMax && position >= armMin) {
            armMotor.setTargetPosition(position);
        }
        else{
            telemetry.addLine("Error invalid position");
        }

        armMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        armMotor.setPower(armSpeed);
        while (armMotor.isBusy()) {
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

    public void setArmControllerPower(double power){
        //armMotor.setPower(0);
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (power == -1){
            power = -armSpeed;
        }
        if (power == 1){
            power = armSpeed;
        }

        //int pos = getArmPos();
        int pos = armMotor.getCurrentPosition();

        // Approach limits with reduced speed
        if (pos >= (slideMax - motorLimitBuffer) && power > 0) {
            power = power/2; // Slow dow as it approaches max
        }
        else if (pos <= (slideMin + motorLimitBuffer) && power < 0) {
            power = power/2; // Slow down as it approaches min
        }

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
    }

    //For emergencies
    public void setArmControllerPowerNoLimit(double power){
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        if (power == -1){
            power = -armSpeed;
        }
        if (power == 1){
            power = armSpeed;
        }

        armMotor.setPower(power);
    }

    //For the instances where I need to get the values of the defaults
    public int defaultArmValue(armPos pos){return pos.getValue();}

    public int getArmPos(){return armMotor.getCurrentPosition();}

    //For checking if the arm is currently in motion with a default position
    public boolean isArmGoingToPos(){return (armMotor.getMode()==DcMotor.RunMode.RUN_TO_POSITION);}

    //Checking if slide is in ok pos. first
    public boolean armOKMove(){
        return !(slideMotor.getCurrentPosition()>=1000); //TODO see if team wants this
    }

    //Reset the armMotor separately
    public void resetArmMotorEncoder(){
        armMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        telemetry.addLine("Resetted arm Encoder");
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