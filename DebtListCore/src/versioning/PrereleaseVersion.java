package versioning;

public class PrereleaseVersion extends Version {

	private int tiny;
	
	public PrereleaseVersion(String version) {
		super(version);
		if(!version.contains(" pre-release")) {
			System.out.println("This was not a pre-release version, but we are using it anyways");
			return;
		}
		// We need to pare the tiny part
		String[] vs = version.split("\\.");
		String v = vs[3].replaceAll(" pre-release", "");
		this.tiny = Integer.parseInt(v);
	}
	
	public PrereleaseVersion(int mega, int big, int small, int tiny) {
		super(mega, big, small, false);
		this.tiny = tiny;
	}
	
	@Override
	public String toString() {
		return "v" + super.a + "." + super.b + "." + super.c + "." + this.tiny + " pre-release";
	}
	
	@Override
	public boolean isCompatible(Version v) {
		return super.a == v.a && super.b == v.b;
	}
	
	@Override
	public boolean isGreaterThan(Version v) {
		if(v instanceof PrereleaseVersion) {		
			return isGreaterThanHelper(v, this.tiny > ((PrereleaseVersion) v).tiny);
		} else {
			return super.isGreaterThan(v);
		}
	}
}
