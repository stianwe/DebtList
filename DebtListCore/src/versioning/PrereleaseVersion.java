package versioning;

public class PrereleaseVersion extends Version {

	private int tiny;
	
	public PrereleaseVersion(int mega, int big, int small, int tiny) {
		super(mega, big, small, false);
	}
	
	@Override
	public String toString() {
		return "v" + super.a + "." + super.b + "." + super.c + "." + this.tiny + " pre-release";
	}
	
	@Override
	public boolean isCompatible(Version v) {
		return super.a == v.a && super.b == v.b;
	}
}
