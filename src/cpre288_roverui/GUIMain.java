package cpre288_roverui;

import javax.swing.JFrame;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class GUIMain
{
	private static RoverFrame frame;
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater( new Runnable(){

			@Override
			public void run()
			{
				createAndShow();
			}
		});
		
		try
		{
			Thread.sleep( 1000 );
		}
		catch( InterruptedException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final int[][] data = new int[180][2];
		for( int i = 0; i < 180; ++i )
		{
			data[i][0] = i * 2;
			data[i][1] = 500 - ( i * 2 );
		}
		
		SwingUtilities.invokeLater( new Runnable(){

			@Override
			public void run()
			{
				frame.plotData( data );
			}
			
		});
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
}
