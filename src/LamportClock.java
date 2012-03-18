import java.util.concurrent.atomic.AtomicInteger;

// 
// Lamport Clock Implementation
//

public class LamportClock 
{
	private AtomicInteger value;

	public LamportClock(int defaultValue)
	{
		this.value.set(defaultValue);
	}
	
	public AtomicInteger getCurrentValue()
	{
		return this.value;
	}
	
	public void incrementClock()
	{
		this.value.getAndIncrement();
	}
}
