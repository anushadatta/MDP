/*
  Right sensors maximum 30cm (3 blocks away)
  Front sensors maximum 30cm (3 blocks away)
  Left sensor maximum 75cm (7 blocks away)
 */

short sensorRead(short n, short sensor){
  short x[n];
  short i;
  for(i = 0; i < n; i++){
    x[i] = analogRead(sensor);
  }
  quickSort(x, 0, n-1);
  return x[n/2];
}

float sensorDist(short n) {

  switch(n) {
    case 1:
      return PS1_dist();
    case 2:
      return PS2_dist();
    case 3:
      return PS3_dist();
    case 4:
      return PS4_dist();
    case 5:
      return PS5_dist();
    case 6:
      return PS6_dist();
    default:
      return 0;
  }
}

float PS1_dist() {  // front sensor

  float dist; 
  short v = sensorRead(55, PS1);
  
  dist =  104.69653080450969 + 
          (-0.68866603909896*v) + 
          (0.0010397687806182594*(pow(v,2))) +
          (0.0000059165339270677024*(pow(v,3))) +
          (-2.7951943869216925*(pow(10,-8))*(pow(v,4))) +
          (4.3953909758772216*(pow(10,-11))*(pow(v,5))) +
          (-2.4319532318450626*(pow(10,-14))*(pow(v,6)));
                
  if (dist < 10) // Obstacle
    return dist; 
  else 
    return dist; // Distance reading
}

float PS2_dist() { // front sensor

  float dist;
  short v = sensorRead(55, PS2);

  dist =  122.58380034410557 + 
          (-1.084595687635811*v) + 
          (0.0043700070148310665*(pow(v,2))) +
          (-0.000008179372581671972*(pow(v,3))) +
          (3.802250306991852*(pow(10,-9))*(pow(v,4))) +
          (7.708341473521636*(pow(10,-12))*(pow(v,5))) +
          (-7.850791376490975*(pow(10,-15))*(pow(v,6)));

  if (dist < 10) // Obstacle
    return dist; 
  else 
    return dist; // Distance reading
      
}

float PS4_dist() { //front sensor

  float dist;
  short v = sensorRead(55, PS4);

  dist =  132.5282062314556 + 
          (-1.2942121646983222*v) + 
          (0.005950800055422258*(pow(v,2))) +
          (-0.000013753172491250238*(pow(v,3))) +
          (1.3784834016699527*(pow(10,-8))*(pow(v,4))) +
          (-1.057254246749177*(pow(10,-12))*(pow(v,5))) +
          (-4.870569428974944*(pow(10,-15))*(pow(v,6)));

  if (dist < 10) // Obstacle
    return dist; 
  else 
    return dist; // Distance reading

}

float PS3_dist() { // right sensor
  
  float dist;
  short v = sensorRead(55, PS3);

  dist =  118.20649642147744 + 
          (-1.0846023518831043*v) + 
          (0.004876087207494868*(pow(v,2))) +
          (-0.00001218270204734551*(pow(v,3))) +
          (1.6735167915532297*(pow(10,-8))*(pow(v,4))) +
          (-1.140433748458387*(pow(10,-11))*(pow(v,5))) +
          (2.7774699522208183*(pow(10,-15))*(pow(v,6))); 
           
    if (dist < 10) // Obstacle
        return dist; 
     else 
        return dist; // Distance reading

}

float PS5_dist() { //right sensor
  
  float dist;
  short v = sensorRead(55, PS5);

  dist =   200.12365935412566 + 
          (-2.5662248397789758*v) + 
          (0.01576541642553869*(pow(v,2))) +
          (-0.000053114993783938306*(pow(v,3))) +
          (9.958739325749927*(pow(10,-8))*(pow(v,4))) +
          (-9.716375971636663*(pow(10,-11))*(pow(v,5))) +
          (3.8345577953944555*(pow(10,-14))*(pow(v,6))); 
           
  if (dist < 10) // Obstacle
    return dist; 
  else 
    return dist; // Distance reading

}

float PS6_dist() { // left long range
  
  float dist;
  short v = sensorRead(55, PS6);

  dist =   124.38354984083443 + 
          (-0.08910567749905615*v) + 
          (-0.0045859050461581315*(pow(v,2))) +
          (0.00002643502743980106*(pow(v,3))) +
          (-6.528538622795478*(pow(10,-8))*(pow(v,4))) +
          (7.758181345492602*(pow(10,-11))*(pow(v,5))) +
          (-3.649197495338657*(pow(10,-14))*(pow(v,6)));  
            
  if (dist < 10)
    return dist;
  else 
    return dist;
}

void swap(short *a, short *b){
  short temp = *a;
  *a = *b;
  *b = temp;
}

void quickSort(short arr[], short leftEnd, short rightEnd){
  if (leftEnd >= rightEnd){
    return;
  }
  short pivot = arr[rightEnd];
  short cnt = leftEnd;
  for(short j = leftEnd; j <= rightEnd; j++){
    if(arr[j] <= pivot){
      swap(&arr[cnt],&arr[j]);
      cnt++;
    }
  }
  quickSort(arr, leftEnd, cnt-2);
  quickSort(arr, cnt, rightEnd);  
}

