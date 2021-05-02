void forwardShort(float dist)
{
  travel_ticks = dist;
  lCountSum = 0;
  rCountSum = 0;
  md.setSpeeds(200, 200);

  short front_min = FRONT_MIN;
  FRONT_MIN = front_min - 3;
  while(lCountSum < travel_ticks | rCountSum < travel_ticks) //just to make sure that the robots moves until the travel ticks 
    if (emergStop())
      break;
  FRONT_MIN = front_min;
  md.setBrakes(300, 300);
  md.setSpeeds(0, 0);
  delay(caDelay);
}

void reverseShort(float dist)
{
  travel_ticks = dist;
  lCountSum = 0;
  rCountSum = 0;
  md.setSpeeds(-200, -200);

  while(lCountSum < travel_ticks | rCountSum < travel_ticks);
  
  md.setBrakes(300, 300);
  md.setSpeeds(0, 0);
  delay(caDelay);
}

void leftShort(float angle)
{
  travel_ticks = angle * 1.0;
  lCountSum = 0;
  rCountSum = 0;
  md.setSpeeds(200, -200);

  while(lCountSum < travel_ticks | rCountSum < travel_ticks);
  
  md.setBrakes(300, 300); 
  md.setSpeeds(0, 0); 
  delay(caDelay);
}

void rightShort(float angle)
{
  travel_ticks = angle * 1.0;
  lCountSum = 0;
  rCountSum = 0;
  md.setSpeeds(-200, 200);

  while(lCountSum < travel_ticks | rCountSum < travel_ticks);

  md.setBrakes(300, 300);
  md.setSpeeds(0, 0);
  delay(caDelay);
}
