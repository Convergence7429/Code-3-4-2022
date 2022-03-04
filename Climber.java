package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.ctre.phoenix.motorcontrol.ControlMode;

public class Climber {
    WPI_TalonFX centerClimberHeightMotor = new WPI_TalonFX(4);

    public void climberInit() {
        centerClimberHeightMotor.setSelectedSensorPosition(0.0);
        centerClimberHeightMotor.enableVoltageCompensation(true);
        centerClimberHeightMotor.configVoltageCompSaturation(12.5);
    }

    public void climberTeleop() {
        double climberSpeed = Constants.xbox.getRawAxis(1); // left stick up and down
        if ((climberSpeed > -0.2) && (climberSpeed < 0.2)) {
            climberSpeed = 0.0;
        }
        centerClimberHeightMotor.set(ControlMode.PercentOutput,
                climberSpeed);
    }
}