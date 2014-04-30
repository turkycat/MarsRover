/* Final.c
 * Created: 4/8/2014 5:38:26 PM
 * Author: pmmartin
 */ 

#include "lcd.h"
#include "master.h"
#define BAUD BAM  //implemented in master.h

//int p;
//char string[30];
//extern unsigned int dangerflag;

int main(void)
{
	oi_t *sensor_data = oi_alloc();
	init_all(sensor_data);
	
	oi_update_sensor(sensor_data);
	
	lprintf("Start");
	wait_ms(1000);
	//servtst();
	
	
	
	while(1)
	{
		//if( !dangerflag )
		//{
			sensor_check( sensor_data, 1 );
		//}
		//USART_Transmit_NoSignedIssues_Buffer( status_string );
		//dangerflag = 0;
		handle_cmd(sensor_data);
	}
	
	
}