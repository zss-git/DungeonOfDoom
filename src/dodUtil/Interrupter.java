/**
 * A wrapper for a runnable that runs it after a delay, but can be told to stop.
 * 
 * @author Zachary Shannon
 * @version 21 Apr 2015
 */
package dodUtil;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Interrupter {
	
	//Runnables to keep track of.
	private Runnable interruptTask;
	private Runnable curTask;
	
	//Executor
	private ScheduledThreadPoolExecutor executor; 
	
	/**
	 * Create a new interrupter
	 * @param setTask A task to use as the interrupter.
	 */
	public Interrupter(Runnable setTask){
		interruptTask = setTask;
		
		//Create new interrupter objects.
		executor = new ScheduledThreadPoolExecutor(1);
		curTask = interruptTask;
	}
	
	/**
	 * Specify a time after which this task should be executed.
	 * @param milliseconds
	 */
	public void interruptIn(int milliseconds){
		//Schedule for specified time.
		executor.schedule(curTask, milliseconds, TimeUnit.MILLISECONDS);
	}
	/**
	 * Cancel execution of this task.
	 */
	public void cancel(){
		executor.shutdownNow();
		
		//Recreate interrupter objects.
		executor = new ScheduledThreadPoolExecutor(1);
		curTask = interruptTask;
	}
	
	
}
