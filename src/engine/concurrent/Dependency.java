package engine.concurrent;

public abstract class Dependency<I> {

	private I dependencyValue;
	
	public Dependency(I dependencyValue) {
		this.dependencyValue= dependencyValue;
	}
	
	public I getDependencyValue() {
		return this.dependencyValue;
	}
	
	public abstract boolean isFulfilled(I comparisonValue);
	public abstract void resume();
}
