package cpre288_roverui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class RoverFrame extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -586582539665675594L;
	private RoverStatusPanel status;
	private RoverSweepPanel sweep;

	public RoverFrame()
	{
		setSize( 1200, 800 );
		status = new RoverStatusPanel();
		sweep = new RoverSweepPanel();

		add( status );
		add( sweep );
		setVisible( true );
	}
	
	
	public void plotData( int[][] data )
	{
		sweep.plotData( data );
		repaint();
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

			setBorder( new EmptyBorder( 0, 10, 10, 10 ) );

			//initialize status indicators
			String names[] = { "Left Bumper", "Right Bumper", "Left Cliff",
					"Fr/L Cliff", "Fr/R Cliff", "Right Cliff",
					"Left Cliff Sig", "Fr/L Cliff Sig", "Fr/R Cliff Sig",
					"Right Cliff Sig" };
			statusItems = new StatusItem[names.length];

			for( int i = 0; i < names.length; ++i )
			{
				statusItems[i] = new StatusItem( names[i] );
				add( names[i], statusItems[i] );

//				///for funzies, delete later TODO
//				if( i % 3 == 0 )
//				{
//					statusItems[i].setStatus( RED );
//				}
//				else if( i % 2 == 0 )
//				{
//					statusItems[i].setStatus( YELLOW );
//				}
			}

			//setLayout( innerLayout );
			setVisible( true );
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
		private final int xOrigin = 600;
		private final int yOrigin = 700;
		private int[][] coords;

		public RoverSweepPanel()
		{
			thickStroke = new BasicStroke( 5.0f );
			medStroke = new BasicStroke( 3.0f );
			thinStroke = new BasicStroke( 1.0f );
			font = new Font( "Serif", Font.BOLD, 16 );
			setSize( 1200, 675 );
			setBackground( Color.WHITE );

		}
		
		
		public void plotData( int[][] coords )
		{
			if( coords == null || coords.length != 180 || coords[0] == null || coords[0].length != 2 ) throw new IllegalArgumentException();
			this.coords = coords;
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
			g.drawArc( 100, 200, 1000, 1000, 0, 180 );

			//draw the inner arc
			g.drawArc( 350, 450, 500, 500, 0, 180 );

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

			g.drawChars( new char[] { '5', '0', ' ', 'c', 'm' }, 0, 5, 80, 720 );
			g.drawChars( new char[] { '5', '0', ' ', 'c', 'm' }, 0, 5, 1080, 720 );
			g.drawChars( new char[] { '2', '5', ' ', 'c', 'm' }, 0, 5, 330, 720 );
			g.drawChars( new char[] { '2', '5', ' ', 'c', 'm' }, 0, 5, 830, 720 );
			
			
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
}
