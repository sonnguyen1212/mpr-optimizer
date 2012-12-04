package mpr;

public class Point3D {
	public static final double Epsilon = 0.001;

	private String x;
	private String y;
	private String z;

	public Point3D(String x, String y, String z) {
		x = x.replace("--", "");
		y = y.replace("--", "");
		z = z.replace("--", "");

		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String getX() {
		return x;
	}

	void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public String getZ() {
		return z;
	}

	public void setZ(String z) {
		this.z = z;
	}
	public boolean isEqual(Point3D other){
		if ((Math.abs(Double.parseDouble(x) - Double.parseDouble(other.getX())) <= Epsilon) &&
		   (Math.abs(Double.parseDouble(y) - Double.parseDouble(other.getY())) <= Epsilon) &&
		   (Math.abs(Double.parseDouble(z) - Double.parseDouble(other.getZ())) <= Epsilon))
			return true;
		
		return false;
	}
}
