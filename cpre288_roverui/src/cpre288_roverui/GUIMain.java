package cpre288_roverui;

import java.util.Deque;
import java.util.LinkedList;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import Serial.ISerialListener;
import Serial.SerialConnection;

public class GUIMain
{
	private static RoverFrame frame;
	private static EventListenerThread eventThread;
	private static SerialConnection serial;
	
	public static void main(String[] args)
	{
		/* 
		 * let swing construct a UI thread and begin handling UI events, starting with
		 * creating the UI frame
		 */
		SwingUtilities.invokeLater( new Runnable(){

			@Override
			public void run()
			{
				createAndShow();
			}
		});
		
		
		
		//sleeping for a second is a safety net for Swing stuff to get set up before we start the event thread
		try
		{
			Thread.sleep( 1000 );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		
		
		//set up the event thread
		eventThread = new EventListenerThread();
		eventThread.start();
		
		
		//set up the serial connection and add the event thread as a listener
		serial = new SerialConnection( "COM4", 57600 );
		serial.init();
		serial.addListener( eventThread );
		
//		char one = 0b11000000;
//		char two = 0;
//		String testString = "s" + one + two;
//		eventThread.onStringReceived( testString );
		

		//
//		try
//		{
//			Thread.sleep( 1000 );
//		}
//		catch( InterruptedException e )
//		{
//			e.printStackTrace();
//		}
//		
//		
//		one = 0b00000011;
//		testString = "s" + one + two;
//		eventThread.onStringReceived( testString );

		
//		//test plot data
//		final int[][] data = new int[180][2];
//		for( int i = 0; i < 180; ++i )
//		{
//			data[i][0] = i * 2;
//			data[i][1] = 500 - ( i * 2 );
//		}
		
		
		/**
		 * plot some test data by creating event thread events
		 */
//		for( int i = 0; i < 180; ++i )
//		{
//			String s = "" + (char)0 + ((char) i) + ((char) data[i][0]) + ((char) ( data[i][0] >> 8 ) );
//			eventThread.onStringReceived( s );
//			
//			s = "" + (char)1 + ((char) i) + ((char) data[i][1]) + ((char) ( data[i][1] >> 8 ) );
//			eventThread.onStringReceived( s );
//		}

		
		
		/**
		 * plot some test data by setting the data directly
		 */
//		SwingUtilities.invokeLater( new Runnable(){
//
//			@Override
//			public void run()
//			{
//				frame.plotData( data );
//			}
//			
//		});
	}
	
	
	/**
	 * initializes the GUI
	 * 	- must only be invoked on the GUI thread using SwingUtilities.invokeLater()
	 */
	public static void createAndShow()
	{
		frame = new RoverFrame();
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
	}
	
	
	/**
	 * invokes a send request to the serial connection
	 */
	public static void sendRequest( char request )
	{
		serial.sendData( request );
		System.out.println( "data sent: " + request );
	}
	
	
	
	/**
	 * instructs the serial connection to wipe it's data
	 */
	public static void clearSerialBuffer()
	{
		serial.clearBuffer();
	}
	
	
	
	private static class EventListenerThread extends Thread implements ISerialListener
	{
		private Deque<String> queue;
		private boolean running;
		
		public EventListenerThread()
		{
			this.queue = new LinkedList<String>();
			this.running = true;
		}

		@Override
		public synchronized void onStringReceived( String data )
		{
			queue.add( data );
		}
		
		@Override
		public void run()
		{
			while( running )
			{
				synchronized( this )
				{
					while( queue.size() > 0 )
					{
						String current = queue.pop();
						if( current.charAt( 0 ) == 's' )
						{
							System.out.println( "sending status string" );
							frame.onStatusString( current );
							continue;
						}
						else if( current.charAt( 0 ) == 'd' )
						{
							System.out.println( "sending data string" );
							frame.onDataString( current );
						}
						else
						{
							System.out.println( "invalid string" );
						}
					}
				}
			}
		}
		
	}
}
