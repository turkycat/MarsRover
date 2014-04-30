#include "lcd.h"
#include "open_interface.h"
#include <stdlib.h>
#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/sleep.h>
#include <string.h>
#include <stdio.h>
#include <math.h>

#ifndef FOSC
	#define FOSC 16000000 //set the main oscillator frequency
#endif
#define RS232 38400 //target baud rate
#define BAM 57600//target baud rate
#define IROBOT 57600 // iRobot baud rate
#ifndef BAUD
	#define BAUD BAM  //target baud rate
#endif

#ifndef MYUBRR
	#define MYUBRR ((FOSC/8)/BAUD)-1 //baud code for register
#endif

#define VREF 2.56

#define CR 13 //carriage return
#define NL 10 //new line
// A program that displays a message in a banner style to the Vortex LCD screen.

void Servo_Init();

//Compatibility for Servo()
unsigned int servo_set(double angle);

int Servo(int degrees);

//initialize the ping sensor
void Sonar_init();

//Send the first pulse that initiates the ping read
void Sonar_start();

double ping_pulse();

//read the ping sensor, returns the delta time value
double ping_read();

//Clear a string, placing all nulls
void clrstr(char* str);

//Initialize ADC
void ADC_init();

//Read ADC
unsigned int ADC_read();

//Read the IR sensor 5 times, average the readings, then return the value
unsigned int IR_read();

//Read the IR sensor, returning a float
//float IR_read();

//Initialize USART0
void USART_Init( unsigned int ubrr );

//USART0 transmission
void USART_Transmit( unsigned char data );

//USART0 receive
unsigned char USART_Receive( void );

// Text scrolling routine
void scrolltxt (char* msg);

// A program that moves the iRobot, can turn, and has collision awareness.

// Turning routine
void turn(oi_t *sensor_data, int degrees, int speed);

//Move forward
void move_forward(oi_t*sensor, int travel);

//Move backward
void move_backward(oi_t*sensor, int travel);

// Moving routine
void move(oi_t *sensor_data, int mm, int speed);

//under construction, but currently working
void turn_clockwise(oi_t*sensor_data, int degrees);
void turn_counterclockwise(oi_t*sensor_data, int degrees);
void env_scan(void);
void analyze_env();
int object_process();
void handle_cmd(oi_t*sensor);
void sensor_check(oi_t*sensor, int send);
//end construction

//collision routine
int collide(oi_t *sensor_data, int side);

// Blocks for a specified number of milliseconds
void wait_ms(unsigned int milliseconds);

// Blocks for a specified number of microseconds
void wait_us(unsigned int time_val);

// Shaft encoder initialization
void shaft_encoder_init(void);

// Shaft encoder read function
signed int read_shaft_encoder(void);

// Initialize Stepper Motor
void stepper_init(void);

// Stepper motor move function
void  move_stepper_step(int steps, int dir);

// Initialize PORTC, which is used by the push buttons
void init_push_buttons(void);

// Return the position of button being pushed

// struct pb //didn't get working
// {	char b8 : 1;
// 	char b7 : 1;
// 	char b6 : 1;
// 	char b5 : 1;
// 	char b4 : 1;
// 	char b3 : 1;
// 	char b2 : 1;
// 	char b1 : 1;
// } ;
unsigned char read_push_buttons(void);

//ask for just the sensors, not the full poll.
void oi_update_sensor(oi_t *self);

//scan for objects. takes two passes, making sure there is an object. passes back the number of objects found
int scan();

//USART print
void uprint(char string[25]);

//for testing servos to get the timing right
void servtst();

//initialize EVERYTHING!!!
void init_all(oi_t *self);

void finish_song_light();

//transmits a single character as two characters, avoids some kind of stupid crazy hardware issue that occurs between send and recv
void USART_TransmitNoSignedIssues( char data );

//uses the above function to transmit a buffer of size 12 using the above function
void USART_Transmit_NoSignedIssues_Buffer( unsigned char *data, int length );