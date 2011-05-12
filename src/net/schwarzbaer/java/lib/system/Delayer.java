package net.schwarzbaer.java.lib.system;

public class Delayer implements Runnable {
	
	private Runnable task;
	private long taskTime;
	private boolean running;

	public Delayer(Runnable task) {
		this.task = task;
		taskTime = 0;
		running = false;
	}
	
	public void delayTask( long ms ) {
		//System.out.println("Delayer (re)set to "+ms+" miliseconds");
		taskTime = System.currentTimeMillis()+ms;
		if (!running) new Thread(this).start();
	}
	
	@Override
	public void run() {
		//System.out.println("Delayer started");
		running = true;
		synchronized (this) {
			while (true) {
				long currentTime = System.currentTimeMillis();
				if (currentTime>=taskTime) { 
					task.run();
					break;
				} else {
					try { wait(taskTime - currentTime); } catch (InterruptedException e) {}
				}
			}
		}
		running = false;
		//System.out.println("Delayer ended");
	}

}
