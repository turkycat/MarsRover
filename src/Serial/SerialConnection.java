package Serial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;


/**
 * @author Jesse Frush
 */


public class SerialConnection implements SerialPortEventListener
{
	private static final int TIME_OUT = 2000;
	
	private final String portName;
	private final int dataRate;
	
	//the SerialPort object, which will allow us to manage a connection
	SerialPort serialPort;
	
	//the string object which is used for parsing whole lines one character at a time.
	private String parsedString;
	
	//A BufferedReader which will be fed by a InputStreamReader converting the bytes into characters
	private BufferedReader input;
	
	// The output stream to the port
	private OutputStream output;
	
	
	/**
	 * a default constructor which uses Com4 with a 57600 data rate
	 */
	public SerialConnection()
	{
		this( "COM4", 57600 );
	}
	
	/**
	 * a constructor which accepts a port name and connection speed
	 */
	public SerialConnection( String portName, int dataRate )
	{
		parsedString = new String();
		this.portName = portName;
		this.dataRate = dataRate;
	}
	
	


	public void initialize()
	{
		// the next line is for Raspberry Pi and 
		// gets us into the while loop and was suggested here was suggested http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		//System.setProperty( "gnu.io.rxtx.SerialPorts", "/dev/ttyACM0" );

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements())
		{
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			if (currPortId.getName().equals(portName))
			{
				portId = currPortId;
				break;
			}
		}
		
		//if unable to find the com port
		if( portId == null )
		{
			System.out.println( "Could not find COM port." );
			return;
		}

		
		try
		{
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open( this.getClass().getName(), TIME_OUT );

			// set port parameters
			serialPort.setSerialPortParams( dataRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );

			// open the streams
			input = new BufferedReader( new InputStreamReader( serialPort.getInputStream() ) );
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener( this );
			serialPort.notifyOnDataAvailable( true );
		}
		catch( Exception e )
		{
			System.err.println( e.toString() );
		}
	}

	/**
	 * This should be called when you stop using the port. This will prevent
	 * port locking on platforms like Linux.
	 */
	public synchronized void close()
	{
		if( serialPort != null )
		{
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent( SerialPortEvent oEvent )
	{
		if( oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE )
		{
			try
			{
				char inputChar = (char) input.read();
				if( inputChar == '\n' )
				{
					System.out.println( parsedString );
					parsedString = "";
				}
				else
				{
					parsedString = parsedString + inputChar;
				}
			}
			catch( Exception e )
			{
				System.err.println( e.toString() );
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}
	
	/**
	 * called when data needs to be sent across the serial connection
	 */
	public void sendData(String data)
	{
		if( output == null ) return;
		char[] characters = data.toCharArray();

		try
		{
//			for (char c : characters)
//			{
//				output.write(c);
//			}
//			output.flush();
			output.write( data.getBytes() );
			output.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) throws Exception
	{
//		SerialConnection serial = new SerialConnection( "COM6", 57600 );
		SerialConnection serial = new SerialConnection();
		serial.initialize();
		Thread t = new Thread()
		{
			public void run()
			{
				//the following line will keep this app alive for 1000 seconds,
				//waiting for events to occur and responding to them (printing incoming messages to console).
				try
				{
					Thread.sleep( 1000000 );
				}
				catch( InterruptedException ie )
				{
				}
			}
		};
		t.start();
		System.out.println( "Started" );
		
		serial.sendData( "hello thar!\n" );
		Thread.sleep( 15000 );
		serial.sendData( "more data!\n" );
	}

}
