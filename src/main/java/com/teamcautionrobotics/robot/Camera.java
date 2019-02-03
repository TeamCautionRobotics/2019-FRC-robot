/*
UsbCamera camera1;
UsbCamera camera2;
VideoSink server;
Joystick joy1 = new Joystick(0);
boolean prevTrigger = false;
void robotInit() {
  camera1 = CameraServer.getInstance().startAutomaticCapture(0);
  camera2 = CameraServer.getInstance().startAutomaticCapture(1);
  server = CameraServer.getInstance().addServer("Switched camera");
  camera1.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen);
  camera2.setConnectionStrategy(VideoSource.ConnectionStrategy.kKeepOpen);
}
void teleopPeriodic() {
  if (joy1.getTrigger() && !prevTrigger) {
    System.out.println("Setting camera 2");
    server.setSource(camera2);
  } else if (!joy1.getTrigger() && prevTrigger) {
    System.out.println("Setting camera 1");
    server.setSource(camera1);
  }
  prevTrigger = joy1.getTrigger();
}