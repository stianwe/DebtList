package versioning;

public class Version {

	protected int a, b, c;
	private boolean release;
	
	public Version(int a, int b, int c, boolean release) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.release = release;
	}
	
	@Override
	public String toString() {
		return "v" + a + "." + b + "." + c + (release ? "" : " TEST");
	}
	
	public boolean isRelease() {
		return release;
	}
	
	public boolean isCompatible(Version v) {
		if(v instanceof PrereleaseVersion) {
			return v.isCompatible(this);
		}
		return this.a == v.a;
	}
	
	public boolean isGreaterThan(Version v) {
		if(this.a > v.a) {
			return true;
		}
		else if (this.a < v.a) {
			return false;
		}
		if(this.b > v.b) {
			return true;
		}
		else if (this.b < v.b) {
			return false;
		}
		if(this.c > v.c) {
			return true;
		}
		return false;
	}
}
