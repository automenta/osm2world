package org.osm2world.core.target.jogl;

import java.nio.FloatBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.math.FloatUtil;

public class JOGLUtil {

	/**
	 * Gets the current OpenGL viewport
	 */
	public static int[] getGlViewport(GL gl) {
		int[] viewport = new int[4];
		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		return viewport;
	}

	/**
	 * Multiply matrix: [d] = [a] x [b]
	 * @param a 4x4 matrix in column-major order
	 * @param b 4x4 matrix in column-major order
	 * @param d result a*b in column-major order
	 * @return given result matrix <i>d</i> for chaining
	 */
	public static float[] multMatrix(final FloatBuffer a, final FloatBuffer b, final float[] d) {
		return FloatUtil.multMatrix(a.array(), a.arrayOffset() + a.position(), b.array(), b.arrayOffset() + b.position(), d, 0);
	}

	/**
	 * Invert the given matrix.
	 * <p>
	 * Returns <code>null</code> if inversion is not possible,
	 * e.g. matrix is singular due to a bad matrix.
	 * </p>
	 *
	 * @param msrc 4x4 matrix in column-major order, the source
	 * @param mres 4x4 matrix in column-major order, the result - may be <code>msrc</code> (in-place)
	 * @return given result matrix <i>mres</i> for chaining if successful, otherwise <code>null</code>. See above.
	 */
	public static float[] invertMatrix(final FloatBuffer msrc, final float[] mres) {
		return FloatUtil.invertMatrix(msrc.array(), msrc.arrayOffset() + msrc.position(), mres, 0);
	}

	/**
	 * @param m_in 4x4 matrix in column-major order
	 * @param m_in_off
	 * @param v_in 4-component column-vector
	 * @param v_out m_in * v_in
	 * @return given result vector <i>v_out</i> for chaining
	 */
	public static float[] multMatrixVec(final FloatBuffer m_in, final float[] v_in, final float[] v_out) {
		return FloatUtil.multMatrixVec(m_in.array(), m_in.arrayOffset() + m_in.position(), v_in, 0, v_out, 0);
	}

	/**
	 * Multiply matrix: [d] = [a] x [b]
	 * @param a 4x4 matrix in column-major order
	 * @param b 4x4 matrix in column-major order
	 * @param d result a*b in column-major order
	 * @return given result matrix <i>d</i> for chaining
	 */
	public static float[] multMatrix(final FloatBuffer a, final float[] b, final float[] d) {
		return FloatUtil.multMatrix(a.array(), a.arrayOffset() + a.position(), b, 0, d, 0);
	}

}
