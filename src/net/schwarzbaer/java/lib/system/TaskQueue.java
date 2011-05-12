package net.schwarzbaer.java.lib.system;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskQueue implements Runnable {
	
	private final boolean withDebugOutput;
	private final int maxConcurrentTasks;
	private final ConcurrentLinkedQueue<SingleTask> queue;
	private Thread thread;
	private int taskCounter;
	
	public TaskQueue(int maxConcurrentTasks) {
		this(maxConcurrentTasks,false);
	}
	public TaskQueue(int maxConcurrentTasks, boolean withDebugOutput) {
		this.maxConcurrentTasks = maxConcurrentTasks;
		this.withDebugOutput = withDebugOutput;
		this.queue = new ConcurrentLinkedQueue<SingleTask>();
		thread = null;
		taskCounter = 0;
	}

	private void initialize() {
		if (thread==null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void add(Runnable atomicTask) {
		initialize();
		queue.add(new SingleTask(atomicTask));
		synchronized (this) { notify(); }
	}
	
	@Override
	public void run() {
		while(true) {
			while (queue.peek()==null) {
				synchronized (this) {
					try { wait(1000); } catch (InterruptedException e) {}
				}
			}
			while (getTaskCounter()>=maxConcurrentTasks) {
				synchronized (this) {
					try { wait(); } catch (InterruptedException e) {}
				}
			}
			queue.poll().start();
			
		}
	}

	private synchronized int getTaskCounter() {
		return taskCounter;
	}

	private synchronized void incTaskCounter() {
		if (withDebugOutput) System.out.printf("TaskQueue.SingleTask started (%d->%d)\r\n",taskCounter,taskCounter+1);
		taskCounter++;
	}

	private synchronized void decTaskCounter() {
		if (withDebugOutput) System.out.printf("TaskQueue.SingleTask stopped (%d->%d)\r\n",taskCounter,taskCounter-1);
		taskCounter--;
		notify(); 
	}

	private class SingleTask implements Runnable {
	
		private final Runnable atomicTask;

		public SingleTask(Runnable atomicTask) {
			this.atomicTask = atomicTask;
		}

		public void start() {
			new Thread(this).start();
		}

		@Override
		public void run() {
			incTaskCounter();
			atomicTask.run();
			decTaskCounter();
		}
		
	}
}