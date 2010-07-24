package usc.resl.harsh;

/**
 * This class holds quaternion type object. This is useful while performing
 * conversion in between euler angles and quaternions and axes angle notation.
 * 
 * @author Kangaroo
 * 
 */
public class Quaternion
{
	public float w;
	public float x;
	public float y;
	public float z;

	/**
	 * Creates a new Quaternion object. The values are initialized to 
	 * [1, 0, 0, 0]
	 */
	public Quaternion()
	{
		Quaternion.this.w = 1;
		Quaternion.this.x = 0;
		Quaternion.this.y = 0;
		Quaternion.this.z = 0;
	}

	/**
	 * Creates a new Quaternion object. The values are initialized to 
	 * [w, x, y, z]
	 * 
	 * @param w
	 *            Angle by which rotation takes place (Real part of the
	 *            quaternion)
	 * @param x
	 *            X-axes (Imaginary part of the quaternion)
	 * @param y
	 *            Y-axes (Imaginary part of the quaternion)
	 * @param z
	 *            Z-axes (Imaginary part of the quaternion)
	 */
	public Quaternion(float w, float x, float y, float z)
	{
		Quaternion.this.w = w;
		Quaternion.this.x = x;
		Quaternion.this.y = y;
		Quaternion.this.z = z;
	}

	/**
	 * Create a new Quaternion object from Euler angles
	 * 
	 * @param pitch
	 *            Roll in the euler angle
	 * @param roll
	 *            Pitch in the euler angle
	 * @param yaw
	 *            Yaw in the euler angle
	 */
	public static Quaternion fromEuler(float pitch, float roll, float yaw)
	{
		Quaternion Qx = new Quaternion((float) Math.cos(pitch / 2), (float) Math
				.sin(pitch / 2), 0f, 0f);
		Quaternion Qy = new Quaternion((float) Math.cos(roll / 2), 0f,
				(float) Math.sin(roll / 2), 0f);
		Quaternion Qz = new Quaternion((float) Math.cos(yaw / 2), 0f, 0f,
				(float) Math.sin(yaw / 2));

		Quaternion temp = Quaternion.multiply(Qx, Qy);

		Quaternion current = Quaternion.multiply(temp, Qz);

		return current;
	}

	/**
	 * Create a new Quaternion object from axes angle notation
	 * 
	 * @param ax
	 *            x-axis in axes angle notation
	 * @param ay
	 *            y-axis in axes angle notation
	 * @param az
	 *            z-axs in axes angle notation
	 * @param theta
	 *            Angle in axes angle notation
	 */
	public static Quaternion fromAxes(int ax, int ay, int az, int theta)
	{
		Quaternion temp = new Quaternion();
		
		temp.w = (float) Math.cos(theta / 2);
		temp.x = (float) (ax * Math.sin(theta / 2));
		temp.y = (float) (ay * Math.sin(theta / 2));
		temp.z = (float) (az * Math.sin(theta / 2));
		
		return temp;
	}

	/**
	 * This multiplies the first quaternion with the second. Note that the order
	 * of multiplication is q1 X q2
	 * 
	 * @param q1
	 *            First quaternion in the multiplication
	 * @param q2
	 *            Second quaternion in the multiplication
	 * @return Multiplied Quaternion
	 */
	public static Quaternion multiply(Quaternion q1, Quaternion q2)
	{
		Quaternion temp = new Quaternion();

		temp.w = (q1.w * q2.w) - (q1.x * q2.x) - (q1.y * q2.y) - (q1.z * q2.z);
		temp.x = (q1.w * q2.x) + (q1.x * q2.w) + (q1.y * q2.z) - (q1.z * q2.y);
		temp.y = (q1.w * q2.y) + (q1.y * q2.w) + (q1.z * q2.x) - (q1.x * q2.z);
		temp.z = (q1.w * q2.z) + (q1.z * q2.w) + (q1.x * q2.y) - (q1.y * q2.x);
		
		return temp;
	}

	/**
	 * This function converts the quaternion into the axes angle notation. The
	 * values are returned by the arguments passed
	 * 
	 * @param ax
	 *            The x-axis from axes angle rotation.
	 * @param ay
	 *            The y-axis from axes angle rotation.
	 * @param az
	 *            The z-axis from axes angle rotation.
	 * @param angle
	 *            The angle from axes angle rotation.
	 */
	public void quaternionToAxesAngle(Quaternion q)
	{
		float ax;
		float ay;
		float az;
		float angle;
		
		angle = (float) (2.0f * Math.acos(Quaternion.this.w));

		float scale = (float) Math.sqrt(Quaternion.this.x * Quaternion.this.x
				+ Quaternion.this.y * Quaternion.this.y + Quaternion.this.z
				* Quaternion.this.z);

		ax = Quaternion.this.x / scale;
		ay = Quaternion.this.y / scale;
		az = Quaternion.this.z / scale;
		
		q.w = angle;
		q.x = ax;
		q.y = ay;
		q.z = az;
		
	}
}
