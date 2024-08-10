package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class DroneLaunch {

    private Servo launcher;

    boolean launched = false;

    double armedPos = 1;
    double launchedPos = 0.2;

    //Initializing the class
    DroneLaunch(HardwareMap hardwareMap){
        launcher = hardwareMap.get(Servo.class, "launcher");
    }

    //Call to arm the launcher
    public void ArmDrone() {
        launcher.setPosition(armedPos);
        launched = false;
    }

    //Call to launch the drone
    public void LaunchDrone(){
        launcher.setPosition(launchedPos);
        launched = true;
    }

    //For telemetry readouts
    public boolean Status(){
        return launched;
    }
}