package frc.robot;

import com.revrobotics.CANSparkMax.ControlType;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PneumaticHub;
import edu.wpi.first.wpilibj.PneumaticsControlModule;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.util.Color;

public class Robot extends TimedRobot {

  static Shooter shooter = new Shooter();
  static DriveTrain drive = new DriveTrain();
  static Intake intake = new Intake();
  static Climber climber = new Climber();
  static PowerDistribution PDP = new PowerDistribution(6, ModuleType.kRev);

  public void clearStickyFaults() {
    PDP.clearStickyFaults();

    shooter.masterShooterMotor.clearStickyFaults();
    shooter.slaveShooterMotor.clearStickyFaults();
    shooter.hoodMotor.clearFaults();

    drive.flMotor.clearFaults();
    drive.frMotor.clearFaults();
    drive.blMotor.clearFaults();
    drive.brMotor.clearFaults();

    intake.intakeMotor.clearFaults();
    intake.indexerMotor.clearFaults();
    intake.intakeAngleMotor.clearFaults();

    climber.centerClimberHeightMotor.clearStickyFaults();
    // add more climber motors
  }

  static boolean isRed;

  int autoStage = 0;
  Timer autonomousTimer = new Timer();

  ///////////////////////////////////////////////////////
  // Robot

  @Override
  public void robotInit() {
    clearStickyFaults();
    drive.driveTrainInit();
    shooter.shooterRobotInit();
    climber.climberInit();
    intake.intakeInit();

    CameraServer.startAutomaticCapture();

    if (DriverStation.getAlliance().equals(Alliance.Red)) {
      isRed = true;
    } else {
      isRed = false;
    }
  }

  @Override
  public void robotPeriodic() {
    // System.out.println("calculated " +
    // (shooter.shooterWheelLinearVelocityToMotorVelocity(shooter.calculatedVelocity)));
    // System.out.println("actual speed " +
    // (shooter.masterShooterMotor.getSelectedSensorVelocity()));
    // System.out.println("calculated angle: " + (90.0 - (shooter.calculatedAngle *
    // 180.0 / Math.PI)));
    // System.out.println("actual angle: " +
    // shooter.hoodMotor.getEncoder().getPosition());
  }

  ////////////////////////////////////////////////////////
  // Autonomous

  @Override
  public void autonomousInit() {
    clearStickyFaults();
    autonomousTimer.reset();
    autonomousTimer.start();
    autoStage = 0;
    shooter.shooterInit();
    shooter.resetHoodEncoders();
  }

  @Override
  public void autonomousPeriodic(){
    shooter.shoot();
    shooter.shooterIdle(0.0);
    intake.indexByColor();

    switch(autoStage){

      case 0 : {
        drive.stopMotors();
        drive.resetDriveTrainEncoders();
        autoStage = 1;
        break;
      }

      case 1 : {
        drive.driveTrainByInches(8.25, 5);
        intake.intakeAngleMotor.set(-0.5);
        if(autonomousTimer.get() > 1.15){
          intake.intakeAngleMotor.set(0.0);
          intake.intakeAngleMotor.getPIDController().setReference(intake.intakeAngleMotor.getEncoder().getPosition(),ControlType.kPosition);
          intake.intakeMotor.set(-0.8);
          drive.stopMotors();
          drive.resetDriveTrainEncoders();
          shooter.shootInit();
          autoStage = 2;
        }
        break;
      }

      case 2 : {
        drive.driveTrainByInches(90.0, 0);
        if(autonomousTimer.get() > 5.5){
          drive.stopMotors();
          drive.resetDriveTrainEncoders();
          if(shooter.centerRobotOnTarget()){
            shooter.shooting = true;
          } else {
            shooter.shooting = false;
          }
        }
        if(autonomousTimer.get() > 12.0){
          shooter.shooting = false;
          drive.stopMotors();
          drive.resetDriveTrainEncoders();
          autoStage = 3;
        }
        break;
      }

      case 3 : {
        drive.driveTrainByInches(13.5, 5);
        if(autonomousTimer.get() > 15.0){
          drive.stopMotors();
          drive.resetDriveTrainEncoders();
          autoStage = 4;
        }
        break;
      }

      case 4 : {
        drive.driveTrainByInches(90.0, 0);
        if(autonomousTimer.get() > 20.0){
          drive.stopMotors();
          drive.resetDriveTrainEncoders();
          shooter.shootInit();
          autoStage = 5;
        }
        break;
      }

      case 5 : {
        drive.driveTrainByInches(50.0, 1);
        if(autonomousTimer.get() > 25.0){
          drive.stopMotors();
          drive.resetDriveTrainEncoders();
          if(shooter.centerRobotOnTarget()){
            shooter.shooting = true;
          } else {
            shooter.shooting = false;
          }
        }
        break;
      }
    }

    // if(autonomousTimer.get() > 15.0){
    //   autoStage = 6;
    // }

  }

  /*
   * 
   * @Override
   * public void autonomousPeriodic() {
   * 
   * shooter.shoot();
   * 
   * switch (autoStage) {
   * case 0: {
   * drive.resetDriveTrainEncoders();
   * intake.intakeMotor.set(-0.8);
   * intake.intakeAngleMotor.set(-0.4);
   * if (timer.get() > 0.43) {
   * intake.intakeAngleMotor.set(0.0);
   * autoStage = 1;
   * }
   * break;
   * }
   * 
   * case 1: {
   * drive.driveTrainByInches(7.25, 5);
   * if (timer.get() > 1.5) {
   * drive.stopMotors();
   * drive.resetDriveTrainEncoders();
   * autoStage = 2;
   * }
   * break;
   * }
   * case 2: {
   * drive.driveTrainByInches(90.0, 0);
   * if (timer.get() > 6.0) {
   * drive.stopMotors();
   * drive.resetDriveTrainEncoders();
   * // intake.intakeMotor.set(0.0);
   * autoStage = 3;
   * }
   * break;
   * }
   * 
   * case 3: {
   * drive.driveTrainByInches(7.25, 5);
   * if (timer.get() > 7.5) {
   * drive.stopMotors();
   * drive.resetDriveTrainEncoders();
   * autoStage = 4;
   * }
   * break;
   * }
   * 
   * case 4: {
   * drive.driveTrainByInches(90.0, 1);
   * if (timer.get() > 12.0) {
   * drive.stopMotors();
   * drive.resetDriveTrainEncoders();
   * autoStage = 5;
   * }
   * break;
   * }
   * 
   * case 5: {
   * drive.driveTrainByInches(20.0, 4);
   * if (timer.get() > 15.0) {
   * drive.stopMotors();
   * drive.resetDriveTrainEncoders();
   * shooter.shootInit();
   * shooter.shooting = true;
   * autoStage = 5;
   * }
   * break;
   * }
   * 
   * case 6: {
   * if (timer.get() > 20.0) {
   * shooter.shooting = false;
   * }
   * break;
   * }
   * }
   * }
   */

  ///////////////////////////////////////////////////////////
  // Tele-operated

  @Override
  public void teleopInit() {
    clearStickyFaults();
    shooter.shooterInit();
    intake.isIntakeDown = true;
  }

  @Override
  public void teleopPeriodic() {

    if (Constants.stick.getRawButton(2)) {
      shooter.centerRobotOnTarget();
    } else {
      drive.driveTrainByControls(Constants.stick.getRawAxis(1), Constants.stick.getRawAxis(0),
          Constants.stick.getRawAxis(2), false);
    }
    intake.intakeTeleop();

    shooter.shoot();
    shooter.dumpShot();
    shooter.shooterIdle(0.0);
    shooter.hoodControl();

    
    // climber.climberTeleop();
  }

  /////////////////////////////////////////////////////////////
  // Test
  
  //PneumaticsControlModule pcm = new PneumaticsControlModule(15, PneumaticsModuleType.REVPH);
  PneumaticHub ph = new PneumaticHub(15);
  //DoubleSolenoid doubleSolenoid = new DoubleSolenoid(PneumaticsModuleType.REVPH, 8, 9);
  DoubleSolenoid doubleSolenoid;

  @Override
  public void testInit() {
    ph.clearStickyFaults();
    DoubleSolenoid doubleSolenoid = ph.makeDoubleSolenoid(0, 1);
    doubleSolenoid.set(Value.kForward);
    autonomousTimer.reset();
    autonomousTimer.start();
  }

  @Override
  public void testPeriodic() {

    if(autonomousTimer.get() > 10.0){
      doubleSolenoid.set(Value.kReverse);
      autonomousTimer.reset();
      autonomousTimer.start();
    }

    //System.out.println("intake angle motor encoders: " + intake.intakeAngleMotor.getEncoder().getPosition());

    // still have the intake up and down stuff. Move at 0.5 speed until get within a certain number of encoder counts of the goal
    // then PID
    // have buttons that go up and down at 0.35 or something and if you press the buttons, you still switch between intake up and down
    // that finish the way

    // could double check distance calculation

    //////////////////////////////////////////////////////////////////////////
    // Color detectedColor = intake.indexerColorSensor.getColor();
    // System.out.println("blue: " + new Color(0.0, 0.290, 0.0).blue);
    // //double difference = Math.abs(detectedColor.blue - 0.2975); // or 0.295. < 0.015
    // double difference = Math.abs(detectedColor.red - 0.295); // 0.2932 // 0.2976
    // System.out.println("difference: " + difference);

    // if (difference < 0.025){
    //   intake.indexerMotor.set(0.0);
    // } else {
    //   intake.indexerMotor.set(-0.2);
    // }
    // System.out.println(
    //     "red: " + detectedColor.red + ",     blue: " + detectedColor.blue + ",     green: " + detectedColor.green);
    // ColorMatchResult match = intake.colorMatch.matchClosestColor(detectedColor);
    // System.out.println(match.color.red + " " + match.color.blue + " " + match.color.green);

    // if(Constants.stick.getRawButtonPressed(1)){
    //   shooter.shooting = !shooter.shooting;
    // }

    // if ((Math.abs(detectedColor.red - new Color(0.195, 0.374, 0.432).red) < 0.05) && detectingBall) {
    //   ballPresent = true;
    //   intakeTimer.reset();
    //   intakeTimer.start();
    // } else {
    //   if(!ballPresent){
    //     intake.indexerMotor.set(-0.2);
    //   }
    // }

    // if(ballPresent){
    //   detectingBall = false;
    //   intake.indexerMotor.set(-0.2);
    //   if(intakeTimer.get() > 0.6){
    //     intake.indexerMotor.set(0.0);
    //     if(shooter.shooting || shooter.dumpShot || (Constants.xbox.getPOV() == 180)){
    //       detectingBall = true;
    //       ballPresent = false;
    //     }
    //   }
    // }


    // // if(ballPresent){
      
    // // } else {

    // // }
    
    
    // // else {
    // //   ballPresent = false;
    // //   intake.indexerMotor.set(-0.2);
    // // }

    // if(ballPresent){

    // }
    /////////////////////////////////////////////////////////////////////

    // shooter.masterShooterMotor.set(ControlMode.PercentOutput, 0.87);
    // System.out.println("calculated " +
    // (shooter.shooterWheelLinearVelocityToMotorVelocity(shooter.calculatedVelocity)));
    // System.out.println("actual speed " +
    // (shooter.masterShooterMotor.getSelectedSensorVelocity()));
    // System.out.println("calculated angle: " + (90.0 - (shooter.calculatedAngle *
    // 180.0 / Math.PI)));
    // System.out.println("actual angle: " +
    // shooter.hoodMotor.getEncoder().getPosition());

    // if(Constants.stick.getRawButtonPressed(5)){
    // shooter.angleError -= 0.05;
    // } else if(Constants.stick.getRawButton(6)){
    // shooter.angleError += 0.05;
    // } else if(Constants.stick.getRawButton(3)){
    // shooter.angleError -= 0.01;
    // } else if(Constants.stick.getRawButtonPressed(4)){
    // shooter.angleError += 0.01;
    // }

    // System.out.println(shooter.getXDistanceFromCenterOfHub(NetworkTableInstance.getDefault().getTable("limelight").getEntry("ty").getDouble(0.0)));
    // System.out.println(shooter.angleError);

    // public static float getXDistanceFromCenterOfHub(double verticalAngle) { // x
    // // distance from front of robot to fender. better for testing
    // return (float) (((y - cameraHeight) / Math.tan(Math.toRadians(verticalAngle +
    // cameraAngle - angleError))) - horizontalDistanceFromLimelightToFrontOfRobot -
    // distanceFromFenderToTape);
    // }

    // if(Constants.stick.getRawButtonPressed(7)){
    // shooterActivated = !shooterActivated;
    // }
    // if(Constants.stick.getRawButtonPressed(11)){ // adjust shooter speed
    // shooterActivated = true;
    // shooterSpeed -= 0.05;
    // }
    // if(Constants.stick.getRawButtonPressed(12)){
    // shooterActivated = true;
    // shooterSpeed += 0.05;
    // }
    // if(shooterActivated){
    // shooter.masterShooterMotor.set(ControlMode.PercentOutput, shooterSpeed);
    // } else {
    // shooter.masterShooterMotor.set(0.0);
    // }
  }

  //////////////////////////////////////////////////////////////
  // Disabled

  @Override
  public void disabledInit() {
    NetworkTableInstance.getDefault().getTable("limelight").getEntry("ledMode").setDouble(1.0);
  }

  @Override
  public void disabledPeriodic() {
  }

}