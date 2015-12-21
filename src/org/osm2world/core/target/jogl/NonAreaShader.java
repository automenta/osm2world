package org.osm2world.core.target.jogl;

import com.jogamp.opengl.GL3;

import com.jogamp.opengl.util.PMVMatrix;

/**
 * Shader for {@link NonAreaPrimitive}.
 */
public class NonAreaShader extends AbstractShader {

	private int modelViewProjectionMatrixID;
	private int vertexPositionID;
	private int vertexColorID;
	
	public NonAreaShader(GL3 gl) {
		super(gl, "/shaders/nonarea");
		
		// get indices of named attributes
		vertexPositionID = gl.glGetAttribLocation(shaderProgram, "VertexPosition");
		vertexColorID = gl.glGetAttribLocation(shaderProgram, "VertexColor");
		
		// get indices of uniform variables
		modelViewProjectionMatrixID = gl.glGetUniformLocation(shaderProgram, "ModelViewProjectionMatrix");
		
		this.validateShader();
	}
	
	/**
	 * Send uniform matrices "ProjectionMatrix, ModelViewMatrix and ModelViewProjectionMatrix" to vertex shader
	 * @param pmvMatrix the PMVMatrix containing all matrices
	 */
	public void setPMVMatrix(PMVMatrix pmvMatrix) {
		float[] pmvMat = new float[16];
		JOGLUtil.multMatrix(pmvMatrix.glGetPMatrixf(), pmvMatrix.glGetMvMatrixf(), pmvMat);
		gl.glUniformMatrix4fv(this.getModelViewProjectionMatrixID(), 1, false, pmvMat, 0);
	}
	
	/**
	 * Returns the id to use to bind the vertex position attribute.
	 */
	public int getVertexPositionID() {
		return vertexPositionID;
	}
	
	/**
	 * Returns the id to use to bind the vertex color attribute.
	 */
	public int getVertexColorID() {
		return vertexColorID;
	}
	
	public int getModelViewProjectionMatrixID() {
		return modelViewProjectionMatrixID;
	}
	
}