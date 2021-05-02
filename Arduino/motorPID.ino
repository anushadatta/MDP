bool updatePID(short M1, short M2, float prop, float deriv, float integ)
{
  if (travel_ticks == 0)
  {
    return false;
  }

  if (M1 == 1 & M2 == 1) 
  { //If sensors detect that there is obstacles infront, it will stop
    if (emergStop()) {                        
      return false;                               
    }
  }

  float error, prevErr, totalErr;
  float M1_speed, M2_speed;

  error = 0;
  prevErr = 0;
  totalErr = 0;

  lCountSum = 0;
  rCountSum = 0;
  lCount = 0;
  rCount = 0;
  timer = 0;

  const short m2speed = 350;
  const short m1speed = 350;
  if (mode == 1 && M1 == 1 && M2 == 1){
    M2_speed = 350;
    M1_speed = 350;
  }
  else{
    M2_speed = 350;
    M1_speed = 350;
  }

  md.setSpeeds(M1_speed * M1, M2_speed * M2);

  while(lCountSum < travel_ticks && rCountSum < travel_ticks)
  {
    if (M1 == 1 & M2 == 1) {
      if (emergStop()) {
        return false;
      }
    }
    if (timer >= 80) //every 80 miliseconds, update the PID. The bigger the timer, the slower it will update.
    {
      prevErr = error;
      totalErr += error;
      error = lCountSum - rCountSum; // calculate difference between wheels

      M1_speed -= (prop * error) + (deriv * (error - prevErr)); // reduce tune speed of faster wheel to match slower wheel
      M1_speed -= (integ * totalErr); // speed scaling

      if (mode == 2)
      {
        M1_speed = max(min(m1speed, M1_speed), 0); //checking purposes and prevent to reach 400
      }

      md.setM1Speed(M1_speed * M1);

      lCount = 0;
      rCount = 0;
      timer = 0;
    }

  }

  md.setSpeeds(0, 0);
  delay(15);
  md.setBrakes(380, 380);

  return true;
}

float forwardTicks(float dist)
{
  float ticks;
  if (mode == 1) {

    ticks = 240;
    offset = 0;
  }
  
  else if (mode == 2) {
    short i = 290; // no. of travel_ticks
    if ( dist <= 10 )
    {
      offset = 0;
      ticks = i * 1;
    }
    else if ( dist <= 20 )
    {
      offset = 0;
      ticks = i * 2;
    }
    else if ( dist <= 30 )
    {
      offset = 0;
      ticks = i * 3;
    }
    else if ( dist <= 40 )
    {
      offset = 0;
      ticks = i * 4;
    }
    else if ( dist <= 50 )
    {
      offset = 0;
      ticks = i * 5;
    }
    else if ( dist <= 60 )
    {
      offset = 0;
      ticks = i * 6;
    }
    else if ( dist <= 70 )
    {
      offset = 0;
      ticks = i * 7;
    }
    else if ( dist <= 80 )
    {
      offset = 0;
      ticks = i * 8 + 100;
    }
    else if ( dist <= 90 )
    {
      offset = 0;
      ticks = i * 9 + 45;
    }
    else if ( dist <= 100 )
    {
      offset = 0;
      ticks = i * 10 + 45;
    }
    else
      ticks = 0;
  }

  return ticks;
}

void forwardMovement(float dist)
{
  lCountSum = 0;
  rCountSum = 0;

  switch (direction) {
    case 0:
      y--;
      break;
    case 1:
      x++;
      break;
    case 2:
      y++;
      break;
    case 3:
      x--;
      break;
    default:
      break;
  }
  travel_ticks = forwardTicks(dist);
  if (mode == 1)
    updatePID(1, 1, 4.9, 3, 1.5); 
  if (mode == 2) {
    updatePID(1, 1, 0.4, 0, 0); 
    if (travel_ticks > 1000)
      delay(100);
  }
  caliFrontDistance();
  delay(caDelay);
  caliFrontAngle();

  travel_ticks = 0;
  timer = 0;
}

void backwardMovement(float dist)
{
  switch (direction) {
    case 0:
      y++;
      break;
    case 1:
      x--;
      break;
    case 2:
      y--;
      break;
    case 3:
      x++;
      break;
    default:
      break;
  }

  travel_ticks = 240;
  updatePID(-1, -1, 0.4, 0.1, 0);

  travel_ticks = 0;
  timer = 0;
}

short leftTicks = 365;
void leftMovement(float angle)
{
  lCountSum = 0;
  rCountSum = 0;
  direction = (direction + 3) % 4;
  if (mode == 1) {
    travel_ticks = leftTicks;
    offset = 0;
  }
  else if (mode == 2) {
    travel_ticks = leftTicks;
    offset = 0;
  }
  else {
    travel_ticks = 0;
  }

  updatePID(-1, 1, 0.3, 0.1, 0);
  
  travel_ticks = 0;
  timer = 0;
  leftStartTimer = 0;
}

short rightTicks = 355;
void rightMovement(float angle)
{
  lCountSum = 0;
  rCountSum = 0;
  direction = (direction + 1) % 4;
  if (mode == 1) {
    travel_ticks = rightTicks;
    offset = 0;
  }
  else if (mode == 2) {
    travel_ticks = rightTicks;
    offset = 0;
  }
  else {
    travel_ticks = 0;
  }
 
  updatePID(1, -1, 0.3, 0.1, 0);

  travel_ticks = 0;
  timer = 0;

  rightStartTimer = 0;
}
