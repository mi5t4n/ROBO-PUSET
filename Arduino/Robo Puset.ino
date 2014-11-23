#include <Motor.h>
#include <SoftwareSerial.h>

#define LEFT_UP 101
#define LEFT_DOWN 102
#define RIGHT_UP 103
#define RIGHT_DOWN 104
#define FORWARD_UP 105
#define FORWARD_DOWN 106
#define BACKWARD_UP 107
#define BACKWARD_DOWN 108

#define RX 10
#define TX 11

SoftwareSerial mySerial(RX,TX);
Motor motorLeft(8,9),motorRight(10,11); //Creating motor objects
enum Motor::State state; //Stores the motion state of the robot

void setup() {
  Serial.begin(9600);
  mySerial.begin(9600); 
}

void loop() {
   if(mySerial.available()){
    int a = mySerial.read();
    Serial.println(a,DEC);
    
    switch (a) {
    case FORWARD_UP:
       motorLeft.freeze();
       motorRight.freeze();
       state=Motor::FREEZE;
       break;
       
    case FORWARD_DOWN:
       motorLeft.goForward();
       motorRight.goForward();
       state=Motor::FORWARD;
       break;

    case BACKWARD_DOWN:
       motorLeft.goBackward();
       motorRight.goBackward();
       state=Motor::BACKWARD;
       break;

    case BACKWARD_UP:
       motorLeft.freeze();
       motorRight.freeze();
       state=Motor::FREEZE;
       break;

    case LEFT_DOWN:
      //Its more effective to turn if we move one of the robot to backward and the other to the right
       if (motorLeft.getState()!=Motor::BACKWARD) motorLeft.goBackward(); 
       if (motorRight.getState()!=Motor::FORWARD) motorRight.goForward();
       break;

    case LEFT_UP:
       if(state==Motor::FORWARD) {
          if(motorLeft.getState()!=Motor::FORWARD) motorLeft.goForward();
          if(motorRight.getState()!=Motor::FORWARD) motorRight.goForward();
        } else if (state==Motor::BACKWARD){
          if(motorLeft.getState()!=Motor::BACKWARD) motorLeft.goBackward();
          if(motorRight.getState()!=Motor::BACKWARD) motorRight.goBackward();
          motorRight.goBackward();
        } else if (state==Motor::FREEZE) {
          motorLeft.freeze();
          motorRight.freeze();
          state=Motor::FREEZE;
       }
       break;

    case RIGHT_DOWN:
       if (motorRight.getState()!=Motor::BACKWARD) motorRight.goBackward();
       if (motorLeft.getState()!=Motor::FORWARD) motorLeft.goForward();
       break;

      case RIGHT_UP:
        if(state==Motor::FORWARD) {
          if(motorLeft.getState()!=Motor::FORWARD) motorLeft.goForward();
          if(motorRight.getState()!=Motor::FORWARD) motorRight.goForward();
        } else if (state==Motor::BACKWARD){
          if(motorLeft.getState()!=Motor::BACKWARD) motorLeft.goBackward();
          if(motorRight.getState()!=Motor::BACKWARD) motorRight.goBackward();
          motorRight.goBackward();
        } else if (state==Motor::FREEZE) {
          motorLeft.freeze();
          motorRight.freeze();
          state=Motor::FREEZE;
        }
        break;
   }
 }
}
