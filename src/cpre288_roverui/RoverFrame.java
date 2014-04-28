package cpre288_roverui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class RoverFrame extends JFrame
{
	private static final long serialVersionUID = -586582539665675594L;
	private static final boolean DEBUG = false;
	
	private RoverStatusPanel status;
	private RoverSweepPanel plotter;
	private RoverControlPanel control;

	public RoverFrame()
	{
		setSize( 1200, 900 );
		
		BorderLayout layout = new BorderLayout();
		
		status = new RoverStatusPanel();
		plotter = new RoverSweepPanel();
		control = new RoverControlPanel();
		setLayout( layout );
		
		add( status, BorderLayout.PAGE_START );
		add( plotter, BorderLayout.CENTER );
		add( control, BorderLayout.PAGE_END );

//		layout.addLayoutComponent( control, BorderLayout.LINE_START );
//		layout.addLayoutComponent( status, BorderLayout.CENTER );
//		layout.addLayoutComponent( plotter, BorderLayout.PAGE_END );
//		add( control );
//		add( status );
//		add( plotter );
		setVisible( true );
	}
	
	
	/**
	 * invokes the plotter to use new data and instructs the frame to repaint
	 */
	public void plotData( int[][] data )
	{
		plotter.plotData( data );
		repaint();
	}
	
	
	/**
	 * called by the custom event thread in GUIMain when a HUD status string has been received
	 */
	public void onStatusString( final String data )
	{
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run()
			{
				status.processStatusString( data );
				repaint();
			}
			
		});
	}
	

	
	/**
	 * called by the custom event thread in GUIMain when a HUD status string has been received
	 */
	public void onDataString( final String data )
	{
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run()
			{
				plotter.processDataString( data );
				repaint();
			}
			
		});
	}
	

	/**
	 * this panel provides status indicators to the GUI representing the current
	 * status of the rover - contains the top portion of this frame.
	 */
	private class RoverStatusPanel extends JPanel
	{
		private static final long serialVersionUID = 2620111384139839386L;
		
		private final ImageIcon indicators[];
		private final int GREEN = 0;
		private final int YELLOW = 1;
		private final int RED = 2;

		private StatusItem statusItems[];

		public RoverStatusPanel()
		{
			setSize( 1200, 125 );
			setBackground( Color.WHITE );
			
			//initialize image icons
			indicators = new ImageIcon[3];
			try
			{
				indicators[GREEN] = new ImageIcon( ImageIO.read( new File( "green.png" ) ) );
				indicators[YELLOW] = new ImageIcon( ImageIO.read( new File( "yellow.png" ) ) );
				indicators[RED] = new ImageIcon( ImageIO.read( new File( "red.png" ) ) );
			}
			catch( FileNotFoundException e )
			{
				//do nothing
			}
			catch( IOException e )
			{
				//do nothing
			}

			//sets the border
			setBorder( new EmptyBorder( 0, 10, 10, 10 ) );
			

			//initialize status indicators
			String names[] = { "Left Bumper", "Right Bumper", "Left Cliff",
					"Fr/L Cliff", "Fr/R Cliff", "Right Cliff",
					"Wheel Drop L", "Wheel Drop M", "Wheel Drop R" };
			statusItems = new StatusItem[names.length];

			for( int i = 0; i < names.length; ++i )
			{
				statusItems[i] = new StatusItem( names[i] );
				add( names[i], statusItems[i] );
			}

			//setLayout( innerLayout );
			setVisible( true );
		}
		
		
		/**
		 * sets the status indicators to green or red depending on each bit
		 * key:
		 * first character:
		 * bit		item
		 * ---------------
		 * 0	=	left bumper
		 * 1	=	right bumper
		 * 2	=	left cliff
		 * 3	=	front lift cliff
		 * 4	=	front right cliff
		 * 5	=	right cliff
		 * 6	=	left cliff sig
		 * 7	=	front left cliff sig
		 * second character:
		 * bit		item
		 * ---------------
		 * 0	=	front right cliff sig
		 * 1	=	right cliff sig
		 */
		public void processStatusString( String status )
		{
			if( status == null ) return;
			if( status.length() < 3 ) return;
			
			char[] bytes = status.toCharArray();
			if( bytes[0] != 's' ) return;
			
			//the first character represented by the first 8 bits
			for( int i = 0; i < 9; ++i )
			{
				int j = i / 8;
				if( ( bytes[j + 1] & ( 1 << i ) ) > 0 )
				{
					statusItems[i].setStatus( 2 );
				}
				else
				{
					statusItems[i].setStatus( 0 );
				}
			}
		}
		
		

		private class StatusItem extends JPanel
		{
			private static final long serialVersionUID = 5284360491634959021L;
			
			private JLabel label;

			public StatusItem( String text )
			{
				//configure the panel element to display as required
				setBackground( Color.WHITE );
				setSize( 120, 100 );

				//set necessary label properties
				label = new JLabel( text, indicators[GREEN], JLabel.CENTER );
				label.setVerticalTextPosition( JLabel.BOTTOM );
				label.setHorizontalTextPosition( JLabel.CENTER );
				label.setFont( new Font( "Serif", Font.BOLD, 16 ) );

				//add the label to the panel's default FlowLayout
				add( label );

				setVisible( true );
			}

			public void setStatus( int status )
			{
				label.setIcon( indicators[status] );
			}
		}
	}

	
	


	/**
	 * this panel provides the scanner sweep representation to the GUI
	 * 	- contains the bottom portion of this frame.
	 */
	private class RoverSweepPanel extends JPanel
	{
		private static final long serialVersionUID = -728852959642756744L;

		private final BasicStroke thickStroke;
		private final BasicStroke medStroke;
		private final BasicStroke thinStroke;
		private final Font font;
//		private final int xOrigin = 600;
//		private final int yOrigin = 700;
		private final int xOrigin = 600;
		private final int yOrigin = 550;
		private int[][] coords;
		
		//for debugging
		private PrintWriter outputFile;

		public RoverSweepPanel()
		{
			thickStroke = new BasicStroke( 5.0f );
			medStroke = new BasicStroke( 3.0f );
			thinStroke = new BasicStroke( 1.0f );
			font = new Font( "Serif", Font.BOLD, 16 );
			setSize( 1200, 675 );
			setBackground( Color.WHITE );
			clearData();
			outputFile = null;
			try
			{
				File file = new File("output.txt");
				outputFile = new PrintWriter( file );
			}
			catch( Exception e )
			{
				//do nothing
			}
		}
		
		
		public void plotData( int[][] coords )
		{
			if( coords == null || coords.length != 180 || coords[0] == null || coords[0].length != 2 ) throw new IllegalArgumentException();
			this.coords = coords;
		}
		
		
		
		public void clearData()
		{
			this.coords = new int[180][2];
		}
		
		
		/**
		 * accepts data in the form of a string, for sensors
		 * @param data
		 */
		public void processDataString( String data )
		{
			if( DEBUG )
			{
				outputFile.println( "processing data string" );
				System.out.println( "processing data string" );
			}
			if( data == null ) return;
			if( data.equals( "d reset     " ) ) clearData();
			
			//byte[] bytes = data.getBytes();
			char[] bytes = data.toCharArray();
			
			int sensorID = bytes[1];
			
			if( DEBUG )
			{
				outputFile.println( "sensorID: " + sensorID );
				System.out.println( "sensorID: " + sensorID );
			}
			
			if( !( sensorID == 1 || sensorID == 2 ) )
			{
				
				if( DEBUG )
				{
					outputFile.println( "returning cuz sensor ID" );
					System.out.println( "returning cuz sensor ID" );
				}
				return;
			}
			
			char degree = (char) bytes[2];
			
			if( DEBUG )
			{
				outputFile.println( "degree: " + (int)degree );
				System.out.println( "degree: " + (int)degree );
			}
			
			if( !( degree >= 0 && degree <= 180 ) )
			{
				if( DEBUG )
				{
					outputFile.println( "returning cuz degree " + (int)degree );
					System.out.println( "returning cuz degree " + (int)degree );
				}
				return;
			}
			
			int value = bytes[3] | ( bytes[4] << 8 );
			value &= 0x0000ffff;

			if( DEBUG )
			{
				outputFile.println( "value " + (int)value );
				outputFile.flush();
				System.out.println( "value " + (int)value );
			}
			
			//if( coords == null ) clearData();
			coords[ degree ][ sensorID - 1 ] = value;
		}
		

		@Override
		/**
		 * paints the component W.R.T. the frame in which it is contained
		 */
		public void paintComponent( Graphics gb )
		{
			super.paintComponent( gb );
			Graphics2D g = (Graphics2D) gb;

			
			//draw angular increments
			for( int i = 15; i < 180; i += 15 )
			{
				if( i == 45 || i == 90 || i == 135 )
				{
					g.setStroke( medStroke );
					g.setColor( Color.BLACK );
				}
				else
				{
					g.setStroke( thinStroke );
					g.setColor( Color.GRAY );
				}
				SimplePoint p = getCartesian( i, 500.0 );
				g.drawLine( xOrigin, yOrigin, xOrigin - p.x, yOrigin - p.y );
			}
			
			//return stroke size and font to default for this panel
			g.setStroke( thickStroke );
			g.setFont( font );
			g.setColor( Color.BLACK );

			//			x,   y, width, height, startAngle, arcAngle
			//draw the outer arc
			g.drawArc( 100, 50, 1000, 1000, 0, 180 );

			//draw the inner arc
			g.drawArc( 350, 300, 500, 500, 0, 180 );

			//used for testing purposes
			/*
			 * g.setColor( Color.RED ); g.drawLine( 100, 700, 350, 700 );
			 * 
			 * g.setColor( Color.BLUE ); g.drawLine( 850, 700, 1100, 700 );
			 * 
			 * g.setColor( Color.GREEN ); g.drawLine( 350, 700, 850, 700 );
			 */

			//drawing bottom line
			g.setColor( Color.BLACK );
			g.drawLine( 0, yOrigin, 1200, yOrigin );

			//draw origin
			g.drawOval( xOrigin - 5, yOrigin - 5, 10, 10 );

			//draw text
			char[] characters = "50 cm".toCharArray();
			g.drawChars( characters, 0, 5, 80, 570 );
			g.drawChars( characters, 0, 5, 1080, 570 );
			characters = "25 cm".toCharArray();
			g.drawChars( characters, 0, 5, 330, 570 );
			g.drawChars( characters, 0, 5, 830, 570 );
			
			
			//draw current coordinates
			if( coords != null )
			{
				g.setStroke( medStroke );
				

				g.setColor( Color.RED );
				SimplePoint last = getCartesian( 0, coords[0][0] );
				for( int i = 1; i < 180; ++i )
				{
					SimplePoint current = getCartesian( i, coords[i][0] );
					g.drawLine( xOrigin - last.x, yOrigin - last.y, xOrigin - current.x, yOrigin - current.y );
					last = current;
				}
				

				g.setColor( Color.BLUE );
				last = getCartesian( 0, coords[0][1] );
				for( int i = 1; i < 180; ++i )
				{
					SimplePoint current = getCartesian( i, coords[i][1] );
					g.drawLine( xOrigin - last.x, yOrigin - last.y, xOrigin - current.x, yOrigin - current.y );
					last = current;
				}
			}
		}

		private SimplePoint getCartesian( double degrees, double length )
		{
			int x = (int) ( length * Math.cos( Math.toRadians( degrees ) ) );
			int y = (int) ( length * Math.sin( Math.toRadians( degrees ) ) );
			return new SimplePoint( x, y );
		}

		/**
		 * an immutable point class containing public values
		 */
		private class SimplePoint
		{
			public final int x;
			public final int y;

			public SimplePoint( int x, int y )
			{
				this.x = x;
				this.y = y;
			}
		}
	}


	/**
	 * this panel provides the control abilities and text boxes for the Rover
	 */
	private class RoverControlPanel extends JPanel
	{
		public static final int ZERO = 0;
		public static final int ONE = 1;
		public static final int TWO = 2;
		public static final int THREE = 3;
		
		
		public RoverControlPanel()
		{
			setSize( 1200, 100 );
			//setLocation( 0, 800 );
			

			ImageIcon[] directionalButtons = new ImageIcon[4];

			try
			{
				directionalButtons[0] = new ImageIcon( ImageIO.read( new File( "left.png" ) ) );
				directionalButtons[1] = new ImageIcon( ImageIO.read( new File( "forward.png" ) ) );
				directionalButtons[2] = new ImageIcon( ImageIO.read( new File( "back.png" ) ) );
				directionalButtons[3] = new ImageIcon( ImageIO.read( new File( "right.png" ) ) );
			}
			catch( IOException e )
			{
				//do nothing
			}
			

			
			//set up buttons
			JPanel buttons = new JPanel();
			//buttons.setBackground( Color.BLACK );
			//BorderLayout layout = new BorderLayout();
			//buttons.setLayout( layout );
			
			char[] instructionCodes = { 'L', 'F', 'B', 'R' };
			for( int i = 0; i < 4; ++i )
			{
				JLabel label = new JLabel( directionalButtons[i] );
				label.addMouseListener( new MouseClickListener( instructionCodes[i] ) );
				buttons.add( label );
			}	
			
			//add the buttons layout to the current frame
			add( buttons );
			
			//add the filler spacer
			JPanel spacer = new JPanel();
			spacer.setSize( 500, 100 );
			spacer.setPreferredSize( new Dimension( 200, 100 ) );
			add( spacer );
			
			JPanel textFields = new JPanel();
			GridLayout grid = new GridLayout( 2, 4 );
			textFields.setLayout( grid );
			String[] textFieldNames = { "Field One  ", "Field Two  ", "Field Three  ", "Field Four  " };
			
			for( int i = 0; i < 4; ++i )
			{
				JTextField title = new JTextField();
				title.setFocusable( false );
				title.setFont( new Font( "Serif", Font.BOLD, 26 ) );
				title.setBorder( BorderFactory.createEmptyBorder() );
				title.setText( textFieldNames[i] );
				textFields.add( title );
				
				JTextField dynamic = new JTextField();
				dynamic.setFocusable( false );
				dynamic.setFont( new Font( "Serif", Font.BOLD, 26 ) );
				dynamic.setBorder( BorderFactory.createEmptyBorder() );
				dynamic.setText( "" + i );
				textFields.add( dynamic );
			}
			add( textFields );

			setVisible( true );
		}
		

		
		
		private class MouseClickListener implements MouseListener
		{
			private char id;
			
			public MouseClickListener( char id )
			{
				this.id = id;
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				GUIMain.sendRequest( id );
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
			
		}
	}
}