/*
	Motor.h - Header file for controlling the motor. 
	Created by Sagar Bd Tamang,
	Released into the public domain.
*/

#ifndef Motor_h
#define Motor_h

#include "Arduino.h"

class Motor {
  public:
    enum State {FORWARD, BACKWARD, FREEZE};
    
  private:
    unsigned int _pinA, _pinB;
    enum State _state;
    
  public:
    Motor(unsigned int, unsigned int);
    bool goForward(); //Moves the motor forward.
    bool goBackward(); //Moves the motor BACKWARDward.
    bool freeze();   //Stops the motor.
    int getState();	//Gets state of the motor.
};

#endif

