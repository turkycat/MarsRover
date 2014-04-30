package Serial;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jesse Frush
 */

/**
 * this class interacts with serial ports. It is capable of both sending Strings
 * of data and receiving data events. It is capable of invoking callback methods
 * to any ISerialListener objects added as listeners to this class. Whenever the
 * new-line character '\n' is received, it will print the string received as
 * well as invoking the callback methods.
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

	//The output stream to the port
	private OutputStream output;

	//a list of ISerialListeners which can receive callbacks from this class
	private List<ISerialListener> listeners;

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
		this.listeners = new LinkedList<ISerialListener>();
	}
	
	
	public synchronized void clearBuffer()
	{
		parsedString = "";
		data = false;
		combiner = 0;
		lowerRecieved = false;
		System.out.println( "Serial buffer cleared:" + parsedString );
	}

	/**
	 * attempts to initialize the serial connection by connecting to the
	 * specified port name and given parameters
	 */
	public synchronized void init()
	{
		CommPortIdentifier selectedPort = null;
		Enumeration allPorts = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while( allPorts.hasMoreElements() )
		{
			CommPortIdentifier current = (CommPortIdentifier) allPorts.nextElement();
			if( current.getName().equals( portName ) )
			{
				selectedPort = current;
				break;
			}
			System.out.println( "Port " + current.getName() + " found. Does not match selected port: " + portName + ". Continuing..." );
		}

		//if unable to find the com port
		if( selectedPort == null )
		{
			System.out.println( "Could not connect to COM port. LET ME GUESS, YOU'RE IN THE CPRE LAB?" );
			return;
		}

		try
		{
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) selectedPort.open( this.getClass().getName(), TIME_OUT );

			// set port parameters
			serialPort.setSerialPortParams( dataRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );

			// open the streams
			input = new BufferedReader( new InputStreamReader( serialPort.getInputStream() ) );
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener( this );
			serialPort.notifyOnDataAvailable( true );
			System.out.println( "HALLELELOOOIA Successfully connected to " + portName );
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
	 * adds a listener to this class which may recieve callbacks
	 */
	public synchronized void addListener( ISerialListener listener )
	{
		listeners.add( listener );
	}

	private boolean lowerRecieved = false;
	private char combiner;
	private boolean data = false;

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent( SerialPortEvent oEvent )
	{
		if( oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE )
		{
			try
			{
				while( input.ready() )
				{
					int val = input.read();
					char inputChar = (char) ( 0xff & val );

					if( lowerRecieved )
					{
						combiner |= ( inputChar & 0x0f ) << 4;

						if( parsedString.equals( "" ) )
						{
							if( combiner == 's' )
							{
								data = false;
								parsedString = parsedString + combiner;
							}
							else if( combiner == 'd' )
							{
								data = true;
								parsedString = parsedString + combiner;
							}
							else
							{
								System.out.println( "Character ignored: " + combiner );
							}
						}
						else
						{
							parsedString = parsedString + combiner;
						}

						lowerRecieved = false;
					}
					else
					{
						combiner = 0;
						combiner |= inputChar & 0x0f;
						lowerRecieved = true;
					}
					
					if( ( !data && parsedString.length() == 12 ) || ( data && parsedString.length() == 182 ) )
					{
						// System.out.println( parsedString );
						for( ISerialListener listener : listeners )
						{
							listener.onStringReceived( parsedString );
						}
						parsedString = "";
					}
				}
			}
			catch( Exception e )
			{
				System.err.println( e.toString() );
			}
		}
		// Ignore all the other event types
	}

	/**
	 * called when data needs to be sent across the serial connection
	 */
	public synchronized void sendData( String data )
	{
		if( output == null ) return;

		try
		{
			output.write( data.getBytes() );
			output.flush();
		}
		catch( IOException e )
		{
			System.out.println( "Unable to send data: " + data );
		}
		
	}

	/**
	 * called when data needs to be sent across the serial connection
	 */
	public synchronized void sendData( char ch )
	{
		System.out.println( "serial received request: " + ch );
		try
		{
			output.write( ch );
			output.flush();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	//	public static void main( String[] args ) throws Exception
	//	{
	////		SerialConnection serial = new SerialConnection( "COM6", 57600 );
	//		SerialConnection serial = new SerialConnection();
	//		serial.initialize();
	//		Thread t = new Thread()
	//		{
	//			public void run()
	//			{
	//				//the following line will keep this app alive for 1000 seconds,
	//				//waiting for events to occur and responding to them (printing incoming messages to console).
	//				try
	//				{
	//					Thread.sleep( 1000000 );
	//				}
	//				catch( InterruptedException ie )
	//				{
	//				}
	//			}
	//		};
	//		t.start();
	//		System.out.println( "Started" );
	//		
	//		serial.sendData( "hello thar!\n" );
	//		Thread.sleep( 15000 );
	//		serial.sendData( "more data!\n" );
	//	}

}
