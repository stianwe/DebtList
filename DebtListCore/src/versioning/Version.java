package versioning;

public class Version {

	protected int a, b, c;
	private boolean release;
	
	public static Version parseVersion(String version) {
		System.out.println("Parsing version: " + version);
		if(version.contains("pre-release")) {
			return new PrereleaseVersion(version);
		}
		return new Version(version);
	}
	
	public Version(String version) {
		String newVersion = version.replaceAll(" TEST", "");
		if(newVersion.length() != version.length()) {
			this.release = false;
		}
		newVersion = newVersion.substring(1);
		System.out.println("Splitting version: " + newVersion);
		String[] vs = newVersion.split("\\.");
		this.a = Integer.parseInt(vs[0]);
		this.b = Integer.parseInt(vs[1]);
		this.c = Integer.parseInt(vs[2]);
	}
	
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
		return isGreaterThanHelper(v, false);
	}
	
	protected boolean isGreaterThanHelper(Version v, boolean defaultB) {
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
		else if(this.c < v.c) {
			return false;
		}
		return defaultB;
	}
}
