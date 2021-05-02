void readCommand(String *pString) {

  prev_cmd = *pString;
  sepCommand(*pString, ':');

  if ((*pString).startsWith("A:ok:start_explore")) {
    wait = cmd[3].toInt();
//    image = true;
    x = 1;
    y = 18;
    direction = 1;
    if (image) {
      Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
      if (wait) waitCamera();
      else delay(DELAY + OFFSET);
    }
    printSensor();
    turn = false;
    mode = 1;
  }
  
  else if ((*pString).startsWith("A:ok:start_path")) 
  {  
    mode = 2;
    image = false;
  }
  else if ((*pString).startsWith("A:req:send_sensor"))
  {
    printSensor();
  }
  
  else if ((*pString).startsWith("A:cmd:forward")) {
    
    forwardMovement(cmd[3].toFloat() * 10);

    if (mode == 1 & image) {
      if (!isWall()) {
        Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
        if (wait) waitCamera();
        else delay(DELAY + OFFSET);
      }
    }

    if (mode == 1) {
      delay(100);
      printSensor();
      
    }
    else if (mode == 2) {
      Serial.println("B:stat:" + cmd[2] + ":" + cmd[3] + "$");
      
   }
  }

  else if ((*pString).startsWith("A:cmd:reverse")) {
    
    backwardMovement(cmd[3].toFloat() * 10);
    delay(100);

    if (mode == 1 & image) {
      if (!isWall()) {
        Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
        if (wait) waitCamera();
        else delay(DELAY + OFFSET);
      }
    }

    if (mode == 1) {
      printSensor();
    }
    else if (mode == 2) {
      Serial.println("B:stat:" + cmd[2] + ":" + cmd[3] + "$");
    }
    
  }

  else if ((*pString).startsWith("A:cmd:left")) {
    
    leftMovement(90);
    
    if (mode == 1 & image) {
      if (!isWall()) {
        Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
        if (wait) waitCamera();
        else delay(DELAY + OFFSET);
      }
    }

    if (cmd[3].toInt() == 1) {
      delay(100);
      printSensor();
    }
    
    if (mode == 2) {
      Serial.println("B:stat:" + cmd[2] + ":1$");
      delay(200); 
    }
  }
  
  else if ((*pString).startsWith("A:cmd:right")) {
    
    rightMovement(90);

    if (mode == 1 & image) {
      if (!isWall()) {
        Serial.println("R:cam:" + String(x) + ":" + String(y) + ":" + String(direction) + "$");
        if (wait) waitCamera();
        else delay(DELAY + OFFSET);
      }
    }

    if (cmd[3].toInt() == 1) {
      delay(100);
      printSensor();
    }
    
    if (mode == 2) {
      Serial.println("B:stat:" + cmd[2] + ":1$");
      delay(100); 
    }
  }
  
 else if ((*pString).startsWith("A:cmd:sc") || (*pString).startsWith("A:cmd:rsc")) {
    delay(100);
    sideCali();
   
    if (mode == 1)
      printSensor();    
  }
  
  else if ((*pString).startsWith("A:cmd:fc") || (*pString).startsWith("A:cmd:rfc")) {
    fullCali();
    delay(100);
    printSensor();
  }

  else if ((*pString).startsWith("A:cmd:frontc")) {
    if (mode == 2) {  //Precautionary measure if fastest path didn't end in goal zone, then just continue moving
      if (direction == 0) {
        while (frontDist() > FRONT_MIN) {
          forwardMovement(100);
          delay(200);
          caliFrontDist();
          delay(100);
          caliFrontAngle();
          delay(100);
          sideCali();
          delay(200);
        }
        if (PS3_dist() > FRONT_MIN && PS5_dist() > FRONT_MIN) {
          rightMovement(90);
          delay(200);
          while (frontDist() > FRONT_MIN) {
            forwardMovement(100);
            delay(200);
            sideCali();
            delay(200);
          }
        }
      }
      else if (direction == 1) {
        while (frontDist() > FRONT_MIN) {
          forwardMovement(100);
          delay(100);
          sideCali();
          delay(100);
        }
        if (PS6_dist() > FRONT_MIN) {
          leftMovement(90);
          delay(100);
          while (frontDist() > FRONT_MIN) {
            forwardMovement(100);
            delay(100);
            sideCali();
            delay(100);
          }
        }
      }
    }
    frontCali(); 
    printSensor();
  }
  
  else if ((*pString).startsWith("A:cmd:ic")) {
    initialCali();
    delay(50);
  }
  
  else if ((*pString).startsWith("A:cmd:ps")) {
    printSensor();
  }

  else if ((*pString).startsWith("A:cmd:rir")) {
    takeOnRight();
    if (mode == 1)
      printSensor();
  }

  else if ((*pString).startsWith("A:cmd:lir")) {
    takeOnLeft();
    if (mode == 1)
      printSensor();
  }

  else if ((*pString).startsWith("A:cmd:cali_final")) {
    caliFrontFinal();
    sideCali();
    caliFrontFinal();
  }
  
}

void printSensor() {

  float sensorDatas[6];
  short threshold[6] = {30, 30, 30, 30, 30, 80};

  sensorDatas[0] = PS1_dist();
  sensorDatas[1] = PS2_dist();
  sensorDatas[2] = PS3_dist();
  sensorDatas[3] = PS4_dist();
  sensorDatas[4] = PS5_dist();
  sensorDatas[5] = PS6_dist();    //long range
  
  for (short i = 0 ; i < 6; i++) {
    if (sensorDatas[i] > threshold[i]) {
      sensorDatas[i] = 0;
      continue;
    }
    else if (sensorDatas[i] < 0) {
      sensorDatas[i] = 1;
      continue;
    }
    else {
      sensorDatas[i] = sensorDatas[i] / 10 +1;
    }
  }
  
  Serial.println("P:map:sensor:[" + String(int(sensorDatas[0])) + "," + String(int(sensorDatas[1])) + "," + String(int(sensorDatas[2])) + "," + String(int(sensorDatas[3])) + "," + String(int(sensorDatas[4])) + "," + String(int(sensorDatas[5])) + "]$");

}

void sepCommand(String data, char sep) {
  
  String tmp = "";
  short index = 0;
  short maxIndex = data.length() - 1;
  
  for (short i = 0; i <= maxIndex; i++) {
    if (data.charAt(i) == sep) {
      cmd[index] = tmp;
      index++;
      tmp = "";
    }
    else {
      tmp += data.charAt(i);
    }
  }

  cmd[index] = tmp;
  
  if (++index < 7) {
    for (index = index; index < 7; index++) {
      cmd[index] = "";  
    }
  }
  
}

bool isWall() {
  if ((y == 18 && direction == 2) || 
      (y == 1 && direction == 0) ||
      (x == 1 && direction == 3) ||
      (x == 13 && direction == 1))
    return true;
  else
    return false;
}

void waitCamera() {

  String command = "A:cam_ok";
  timer = 0;
  
  while(timer <= TIMEOUT) {
    while (Serial.available()) {
      char inChar = (char)Serial.read();
      inputString += inChar;
      if (inChar == '\n') {
      pString = &inputString;
      inputString = "";
      Serial.flush();
      }
    }
  }
}