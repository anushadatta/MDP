/* Distance Variables */
short FRONT_MIN = 5.5;   // The minimum distance of robot from obstacle
short FRONT_MAX = 10;    // The maximum distance of robot from obstacle for calibration
short FRONT_MIN_FINAL = 5.5;
short FRONT_MAX_FINAL = 30;

/* Delay Variables */
const short caDelay = 10; 
const short roDelay = 100; 

const short trial = 30;           // no. of times to try before breaks 

bool emergStop()
{  
  if (PS1_dist() < (FRONT_MIN + 3) | 
      PS2_dist() < (FRONT_MIN + 3) | 
      PS4_dist() < (FRONT_MIN + 3))
  {
    md.setBrakes(400, 400);
    delay(roDelay);
    caliFrontAngle();
    return true;
  }

  return false;
}

float frontDist()
{
  float sum = 0;
  short count = 0;
  
  if (PS1_dist() < FRONT_MAX) {
    sum += PS1_dist();
    count++;
  }
  if (PS2_dist() < FRONT_MAX) {
    sum += PS2_dist();
    count++;
  }
  if (PS4_dist() < FRONT_MAX) {
    sum += PS4_dist();
    count++;
  }
  if (count == 0) {
    return 990; //if theres no obstacle, robot will move 
  
  else {
    return sum / (double)count;
  }
}

void caliFrontDist()
{
  if (frontDist() > FRONT_MAX)			      //if robot is more than 10cm away from obstacle
    return;

  short count = 0;                            
  float diff = frontDist() - FRONT_MIN;
  
  while (abs(diff) > 0.1)                     
  {
    if (diff < 0){                            
      count++;                                
      reverseShort(abs(diff));
      if (count > trial)                      
      {
        break;
      }
    }
    else{                                    
      count--;                               
      forwardShort(abs(diff));
      if (count < -trial)                     
      {
        break;
      }
    }
    diff = frontDist() - FRONT_MIN;
  }

  delay(caDelay * 10); 

}

float frontDistFinal()
{
  float sum = 0;
  short count = 0;
  
  if (PS1_dist() < FRONT_MAX_FINAL) {
    sum += PS1_dist();
    count++;
  }
  if (PS2_dist() < FRONT_MAX_FINAL) {
    sum += PS2_dist();
    count++;
  }
  if (PS4_dist() < FRONT_MAX_FINAL) {
    sum += PS4_dist();
    count++;
  }
  if (count == 0) {
    return 990;
  }
  else {
    return sum / (double) count;
  }
}

void caliFrontFinal()
{
  if (frontDistFinal() > FRONT_MAX_FINAL)            //if robot is more than 10cm away from obstacle
    return;

  short count = 0;                            
  float diff = frontDistFinal() - FRONT_MIN_FINAL;
  
  while (abs(diff) > 0.1)                     
  {
    if (diff < 0){                            
      count++;                               
      reverseShort(abs(diff));
      if (count > trial)                      
      {
        break;
      }
    }
    else{                                     
      count--;                                
      forwardShort(abs(diff));
      if (count < -trial)                     
      {
        break;
      }
    }
    diff = frontDistFinal() - FRONT_MIN_FINAL;
  }
  delay(caDelay * 10); 

}

void frontCali() {
  caliFrontDist();
  delay(caDelay);
  caliFrontAngle();
  delay(caDelay);

  if (image) {
    if (!isWall()) {
      Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
      if (wait) waitCamera();
      else delay(DELAY + OFFSET);
    }
  }
}

void caliFrontAngle()
{
  short left, right;
  if (PS1_dist() < FRONT_MAX & PS4_dist() < FRONT_MAX) {
    left = 4; right = 1;
  }
  else if (PS1_dist() < FRONT_MAX & PS2_dist() < FRONT_MAX) {
    left = 2; right = 1;
  }
  else if (PS2_dist() < FRONT_MAX & PS4_dist() < FRONT_MAX) {
    left = 4; right = 2;
  }
  else
    return;
   
  float diff = sensorDist(right) - sensorDist(left);       //(right) front sensor distance - (left) front sensor distance
  float prevDiff;
  short count = 0;                          
  float offset = 1;                           

  while (abs(diff) > 0.08)               
  { 
    prevDiff = diff;
    if (diff > 0){                            //robot is facing more to left
      count++;                                //increase count for right turn
      rightShort(abs(diff) * offset);
      if (count > trial)                     
      {
        rightShort(1);       
        count = 0;
      }
    }
    else{                                     //robot is facing more to right
      count--;                                //reduce count for left turn
      leftShort(abs(diff) * offset);
      if (count < -trial)                     
      {
        leftShort(1);        
        count = 0;
      }
    }
    diff = sensorDist(right) - sensorDist(left);
    if ((abs(diff) > abs(prevDiff)) & (abs(diff - prevDiff) > 0.3))
        break;
  }
  
  delay(caDelay * 10);
}

void caliRightAngle()
{
  if (PS3_dist() > FRONT_MAX | PS5_dist() > FRONT_MAX)      //if obstacle too far away, dont calibrate
    return;                                               
  
  short count = 0;                           
  float diff = PS3_dist() - PS5_dist();      //(front) right sensor distance - (back) right sensor distance
  float prevDiff;
  float offset = 1;                         

  while (abs(diff) > 0.1)                    
  {
    prevDiff = diff;
    if (diff < 0){                            
      count++;                          
      rightShort(abs(diff) * offset);
      if (count > trial)                        
      {
        rightShort(1);
        count = 0;
      }
    }
    else{                                     
      count--;                                
      leftShort(abs(diff) * offset);
      if (count < -trial)                       
      {
        leftShort(1);                     
        count = 0;
      }
    }
    diff = PS3_dist() - PS5_dist();
    if ((abs(diff) > abs(prevDiff)) & (abs(diff - prevDiff) > 0.1))
      break;
  }

  delay(caDelay * 10);

}

void fullCali()
{
 float ps3 = PS3_dist();
 float ps5 = PS5_dist();
  if (ps3 < FRONT_MIN - 2 || ps5 < FRONT_MIN - 2 || 
      (ps3 < FRONT_MAX && ps3 > FRONT_MIN + 1) || 
      (ps5 < FRONT_MAX && ps5 > FRONT_MIN + 1)) {
    rightMovement(90);
    delay(caDelay);
    caliFrontDist();
    delay(caDelay);
    caliFrontAngle();
    delay(caDelay);
  
    if (image) {
      if (!isWall()) {
        Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
        if (wait) waitCamera();
        else delay(DELAY + OFFSET);
      }
    }
    
    leftMovement(90);
    delay(caDelay);
  }
  caliFrontDist();
  delay(caDelay);
  caliFrontAngle();
  delay(caDelay);
  caliRightAngle();
  delay(caDelay);
  //hasFullCalibrated = true;
}

void sideCali() {
  float ps3 = PS3_dist();
  float ps5 = PS5_dist();
  if (ps3 < FRONT_MIN - 2.5 || ps5 < FRONT_MIN - 2.5 || 
     (ps3 < FRONT_MAX && ps3 > FRONT_MIN + 2.5) || 
     (ps5 < FRONT_MAX && ps5 > FRONT_MIN + 2.5)) {
    rightMovement(90);
    delay(caDelay);
    caliFrontDist();
    delay(caDelay);
    caliFrontAngle();
    delay(caDelay);
    
    if (image) {
      if (!isWall()) {
        Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
        if (wait) waitCamera();
        else delay(DELAY + OFFSET);
      }
    }

    leftMovement(90);
    delay(caDelay);
    caliFrontDist();
    delay(caDelay * 10);

  }
  
  caliFrontDist();
  delay(caDelay* 9);
  caliRightAngle();
  delay(caDelay);
  turn = true;
  
}

void initialCali()
{
  rightMovement(90);

  if (image) {
    Serial.println("R:cam:1:18:0$"); //performing image rec
    if (wait) waitCamera();
    else delay(DELAY + OFFSET);
  }

  delay(500);
  leftMovement(90);
  delay(caDelay);
  
  caliFrontDist();
  delay(caDelay);
  caliFrontAngle();
  delay(roDelay);
  leftMovement(90);
  delay(caDelay);
  caliFrontDist();
  delay(caDelay);
  caliFrontAngle();
  delay(roDelay);
  rightMovement(90);
  delay(caDelay);
  caliRightAngle();
  delay(caDelay);
  
}

void takeOnLeft() {

  leftMovement(90);
  delay(caDelay);
  caliFrontDist();
  delay(caDelay);
  caliFrontAngle();
  delay(caDelay);

  if (image) {
    if (!isWall()) {
      Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
      if (wait) waitCamera();
      else delay(DELAY + OFFSET);
    }
  }

  rightMovement(90);
  delay(caDelay);
  caliRightAngle();
  delay(caDelay);
  
}

void takeOnRight() {

  rightMovement(90);
  delay(caDelay);
  caliFrontDist();
  delay(caDelay);
  caliFrontAngle();
  delay(caDelay);

  if (image) {
    if (!isWall()) {
      Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
      if (wait) waitCamera();
      else delay(DELAY + OFFSET);
    }
  }

  leftMovement(90);
  delay(caDelay);
  caliRightAngle();
  delay(caDelay);
    
}