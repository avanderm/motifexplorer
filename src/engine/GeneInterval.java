package engine;

public class GeneInterval {

	private int a, b;
	
	public GeneInterval() {
		this(0,0);
	}
	
	public GeneInterval(int min, int max) {
		a = min;
		b = max;
	}
	
	public int min() {
		return a;
	}
	
	public int max() {
		return b;
	}
	
	public void setMinMax(int min, int max) {
		a = min;
		b = max;
	}
	
	public boolean contains(int arg) {
		return (arg >= min() && arg <= max());
	}
	
	public boolean leftOf(int arg) {
		return (arg > max());
	}
	
	public boolean rightOf(int arg) {
		return (arg < min());
	}

}
