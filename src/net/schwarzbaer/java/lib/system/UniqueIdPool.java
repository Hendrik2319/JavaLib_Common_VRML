package net.schwarzbaer.java.lib.system;

import java.util.HashSet;
import java.util.Random;

public class UniqueIdPool {
	
	public final static long NO_ID = -1;
	
	static UniqueIdPool instance = null;
	static UniqueIdPool getInstance() {
		if (instance==null)
			instance = new UniqueIdPool();
		return instance;
	}

	private HashSet<Long> pool;
	private Random rnd;
	
	public UniqueIdPool() {
		pool = new HashSet<>();
		rnd = new Random();
	}
	
	public void add(long id) { pool.add(id); }
	public boolean exists(long id) { return pool.contains(id); }
	public boolean notExists(long id) { return !exists(id); }
	public void clearPool() { pool.clear(); }

	public long createNewID() {
		long id;
		while ( exists(id=rnd.nextLong()) || id<0 );
		add(id);
		return id;
	}

}
