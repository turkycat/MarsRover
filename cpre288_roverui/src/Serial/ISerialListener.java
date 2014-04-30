package Serial;

/**
 * @author Jesse Frush
 * 
 * this interface serves as a callback from the SerialConnection class
 */
public interface ISerialListener
{
	public void onStringReceived( String data );
}
