package engine.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class BatchDependencyQueue<I> implements Runnable {

	private AtomicInteger processedBatchCount;
	private AtomicInteger pendingBatchCount;
	
	private LinkedBlockingQueue<Future<I>> batchQueue;
	private Future<I> currentBatch;
	
	private ConcurrentLinkedQueue<Dependency<Integer>> dependencyList;
	private Dependency<Integer> currentDependency;
	
	private boolean interruptSignal;
	private boolean shutdownSignal;
	
	public BatchDependencyQueue() {
		interruptSignal = false;
		shutdownSignal = false;
		
		batchQueue = new LinkedBlockingQueue<Future<I>>();
		dependencyList = new ConcurrentLinkedQueue<Dependency<Integer>>();
		
		processedBatchCount = new AtomicInteger(0);
		pendingBatchCount = new AtomicInteger(0);
	}
	
	@Override
	public void run() {
		while(!isInterrupted()) {
			try {
				currentBatch = batchQueue.take();
				currentBatch.get();
				
				processedBatchCount.incrementAndGet();
				pendingBatchCount.decrementAndGet();
				
				// Confirmation of integrity
				if (!pendingBatchCount.compareAndSet(batchQueue.size(), batchQueue.size()))
					interrupt();
				
				// Check for shutdown
				if (isShuttingDown() && (getPendingBatchCount() == 0)) {
					grantDependencies();
					return;
				}
				
				// Reassess pending dependency
				currentDependency = dependencyList.peek();
				
				while(currentDependency != null && currentDependency.isFulfilled(getProcessedBatchCount())) {
					currentDependency.resume();
					dependencyList.remove();
					
					// Consider next dependency
					currentDependency = dependencyList.peek();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				interrupt();
			} catch (ExecutionException e) {
				e.printStackTrace();
				interrupt();
			}
		}
	}
	
	//abstract void processBatchResult(I result);
	
	public void addBatch(Future<I> batch) {
		if (isShuttingDown())
			return;
		
		batchQueue.offer(batch);
		pendingBatchCount.incrementAndGet();
	}
	
	public void addDependency(Dependency<Integer> dependency) {
		if (isShuttingDown())
			return;
		
		if (getPendingBatchCount() == 0)
			dependency.resume();
		else
			dependencyList.add(dependency);
	}
	
	private void grantDependencies() {
		for(Dependency<Integer> dependency : dependencyList)
			dependency.resume();
	}
	
	private void offerDummy() {
		FutureTask<I> task =  new FutureTask<I>(new Callable<I>() {

			@Override
			public I call() throws Exception {
				return null;
			}
			
		});
		
		task.run();
		batchQueue.add(task);
	}
	
	public void shutdown() {
		setShutdown();
		
		if (getPendingBatchCount() == 0)
			offerDummy();
	}
	
	public void interrupt() {
		setInterrupted();
		
		batchQueue.clear();
		offerDummy();
		currentBatch.cancel(true);
	}
	
	private void setShutdown() {
		this.shutdownSignal = true;
	}
	
	private boolean isShuttingDown() {
		return this.shutdownSignal;
	}
	
	private void setInterrupted() {
		this.interruptSignal = true;
	}
	
	private boolean isInterrupted() {
		return this.interruptSignal;
	}
	
	public int getProcessedBatchCount() {
		return processedBatchCount.get();
	}
	
	public int getPendingBatchCount() {
		return pendingBatchCount.get();
	}
	
	public int getPendingDependenciesCount() {
		return dependencyList.size();
	}

}