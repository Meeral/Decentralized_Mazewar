import java.io.Serializable;


public class MazewarNetworkPacket implements Serializable 
{
	public int type;
	public int error_code;

	public long processId;
	
	public String NAME;
	
	public Point Spawn_Point;
	
	public Client client;
	
	public Direction direction;
	
	public LamportClock lc;

	public static final int REQUEST_TO_JOIN		= 0;
	public static final int REQUEST_TO_LEAVE	= 1;
	public static final int START_GAME			= 2;
	public static final int LEAVE_GAME			= 3;
	
}