#include <DualVNH5019MotorShield.h>
#include <EnableInterrupt.h>

/* Define the Sensors */
#define PS1 A0 //Short Range (Face front, right side)
#define PS2 A1 //Short Range (Face front, middle)
#define PS3 A2 //Short Range (Face right, at the front)
#define PS4 A3 //Short Range (Face front, left side)
#define PS5 A4 //Short Range (Face right, at the back)
#define PS6 A5 //Long Range (Face left, at the front)

/* Timing Variables */
#define DELAY 30                       // Delay for camera taking photo
#define OFFSET 20                      // Additional delay for camera taking photo before rotating
#define TIMEOUT 30000                 // Timeout for waiting camera

volatile long timer = 0;

/* Motor Variables */
const short rightEnc = 11;  // Motor M2 (Right)
const short leftEnc = 3;   // Motor M1 (Left)

unsigned long leftStartTimer;
unsigned long rightStartTimer;
volatile float lCount, rCount; 
volatile float lCountSum, rCountSum;

float offset, travel_ticks;

/* Command Variables */
String *pString;
String inputString  = "";             // A String to hold incoming data
String prev_cmd  = "";                // Previous command
String cmd[7] = {};

/* Types of ModeFlags */
short mode  = 1;                  // 1 = Exploration, 2 = FP            
bool image = false;                   // True = IMAGE REC
bool wait = false;                    
short wall;                           //wall = -1, indicates there is block wall

/* Map */
short x = 1, y = 18, direction = 3;
bool turn = false;

DualVNH5019MotorShield md;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);

  cli(); //Disable global interrupts

  TCCR1A = 0; //Register set to 0
  TCCR1B = 0; //Register set to 0
  TCNT1 = 0;

  OCR1A = 15999; //Counter for 10KHz interrupt 16*10^6/1000-1 no prescaler
  TCCR1B |= (1 << WGM12); //CTC mode
  TCCR1B |= (1 << CS10); //No prescaler
  TIMSK1 |= (1 << OCIE1A); //Compare interrupt mode

  sei(); //Enable global interrupts
  
  md.init();
  
  enableInterrupt(leftEnc, forwardLeft, RISING);
  enableInterrupt(rightEnc, forwardRight, RISING);
  
  Serial.flush(); 
}

void loop() {
  
  if (Serial.available()) {

    char inChar = (char)Serial.read();
    if (inChar == '\n') {
       readCommand(&inputString);
       
      inputString = "";
      Serial.flush();
    }
    else {
      inputString += inChar;
    }
  }
}

ISR(TIMER1_COMPA_vect) {
  timer++;  //Increment timer each ISR
}

void forwardLeft()
{
  lCount++;
  lCountSum++;
  leftStartTimer = millis();
}

void forwardRight()
{
  rCount++;
  rCountSum++;
  rightStartTimer = millis();
}
