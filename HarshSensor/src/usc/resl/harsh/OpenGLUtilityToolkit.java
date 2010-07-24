package usc.resl.harsh;

import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class OpenGLUtilityToolkit
{
	// to perform cross product between 2 vectors in myGluLookAt
	private static void CrossProd(float x1, float y1, float z1, float x2, float y2, float z2,
			float res[])
	{
		res[0] = y1 * z2 - y2 * z1;
		res[1] = x2 * z1 - x1 * z2;
		res[2] = x1 * y2 - x2 * y1;
	}

	// my own implementation
	public static void gluLookAt(float eyeX, float eyeY, float eyeZ, float lookAtX,
			float lookAtY, float lookAtZ, float upX, float upY, float upZ,
			GL10 gl)
	{
		// i am not using here proper implementation for vectors.
		// if you want, you can replace the arrays with your own
		// vector types
		float f[] = new float[3];

		// calculating the viewing vector
		f[0] = lookAtX - eyeX;
		f[1] = lookAtY - eyeY;
		f[2] = lookAtZ - eyeZ;

		float fMag;
		double upMag;
		fMag = (float) Math.sqrt(f[0] * f[0] + f[1] * f[1] + f[2] * f[2]);
		upMag = Math.sqrt(upX * upX + upY * upY + upZ * upZ);

		// normalizing the viewing vector
		if (fMag != 0)
		{
			f[0] = f[0] / fMag;
			f[1] = f[1] / fMag;
			f[2] = f[2] / fMag;
		}

		// normalising the up vector. no need for this here if you have your
		// up vector already normalised, which is mostly the case.
		if (upMag != 0)
		{
			upX = (float) (upX / upMag);
			upY = (float) (upY / upMag);
			upZ = (float) (upZ / upMag);
		}

		float s[] = new float[3];
		float u[] = new float[3];

		CrossProd(f[0], f[1], f[2], upX, upY, upZ, s);
		CrossProd(s[0], s[1], s[2], f[0], f[1], f[2], u);

		float M[] = { s[0], u[0], -f[0], 0, s[1], u[1], -f[1], 0, s[2], u[2],
				-f[2], 0, 0, 0, 0, 1 };

		FloatBuffer mBuffer = FloatBuffer.allocate(M.length);
		
		mBuffer.put(M);
		
		gl.glMultMatrixf(mBuffer);
		
		gl.glTranslatef(-eyeX, -eyeY, -eyeZ);
	}
}
