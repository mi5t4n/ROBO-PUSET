/*
	Motor.cpp - Library for controlling the motor. 
	Created by Sagar Bd Tamang,
	Released into the public domain.
*/

#include "Arduino.h"
#include "Motor.h"

Motor::Motor(unsigned int pinA, unsigned int pinB) {
  _pinA = pinA; _pinB = pinB;
  pinMode(pinA,OUTPUT);
  pinMode(pinB,OUTPUT);
  _state=FREEZE;
}

bool Motor::goForward() {
  _state = FORWARD;
  digitalWrite(_pinA, HIGH);
  digitalWrite(_pinB, LOW);
  return true;
}

bool Motor::goBackward() {
  _state = BACKWARD;
  digitalWrite(_pinA, LOW);
  digitalWrite(_pinB, HIGH);
  return true;
}

bool Motor::freeze() {
  _state = FREEZE;
  digitalWrite(_pinA, LOW);
  digitalWrite(_pinB, LOW);
  return true;
}

int Motor::getState() {
  return _state;
}
