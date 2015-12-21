package org.osm2world.viewer.view;

import java.awt.Color;
import java.io.IOException;

import org.osm2world.core.target.jogl.JOGLUtil;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.graph.curve.Region;
import com.jogamp.graph.curve.opengl.RegionRenderer;
import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRegionUtil;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.font.FontSet;
import com.jogamp.graph.geom.SVertex;
import com.jogamp.opengl.util.PMVMatrix;

public class TextRendererShader implements org.osm2world.viewer.view.TextRenderer {
	private TextRegionUtil textRegionUtil;
	private RegionRenderer renderer;
	private Font textRendererFont = null;
	private int width = 0, height = 0;
	private float scale = 1;
	private GL2ES2 gl;
	
	public TextRendererShader(GL2ES2 gl) {
		this.gl = gl;
		try {
			//InputStream fontFile = getClass().getResourceAsStream("media/futura.ttf");
			//textRendererFont = FontFactory.get(fontFile, true);
			textRendererFont = FontFactory.getDefault().get(FontSet.FAMILY_REGULAR, FontSet.STYLE_SERIF);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		RenderState renderState = RenderState.createRenderState(SVertex.factory());
		
		/* GL_DEPTH_TEST is globally enabled. Setting this here causes defaultBlendEnable/Disable to Disable/Enable depth testing.
	 	   This causes depth testing to get DISABLED while rendering text!*/
		renderState.setHintMask(RenderState.BITHINT_GLOBAL_DEPTH_TEST_ENABLED);

		renderer = RegionRenderer.create(renderState, RegionRenderer.defaultBlendEnable, RegionRenderer.defaultBlendDisable);
		renderer.init(gl, Region.VBAA_RENDERING_BIT);
		if (!renderer.isInitialized()) {
			throw new IllegalStateException("Text renderer not initlialized.");
		}
		
		textRegionUtil = new TextRegionUtil(Region.VBAA_RENDERING_BIT);
	}

//	@Override
//	public void drawText(String string, Vector3D pos, Color color) {
//		textRenderer.resetModelview(gl);
//		textRenderer.setColorStatic(gl, color.getRed(), color.getGreen(), color.getBlue());
//		float[] posF = {(float)pos.getX(), (float)pos.getY(), -(float)pos.getZ()};
//		int[] texSize = {0};
//		textRenderer.drawString3D(gl.getGL2ES2(), textRendererFont, string, posF, 12, texSize);
//	}

	protected void drawText(String string, float x, float y, Color color) {
		float pixelSize = textRendererFont.getPixelSize(14, 72);
		
		renderer.enable(gl, true);
		renderer.getRenderState().setColorStatic(color.getRed(), color.getGreen(), color.getBlue(), 1.0f);
		final PMVMatrix pmv = renderer.getMatrix();
		pmv.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		pmv.glLoadIdentity();
		pmv.glTranslatef(x, y, 0);
		int [] viewport = JOGLUtil.getGlViewport(gl); // save viewport as it gets overwritten in drawString3D
		textRegionUtil.drawString3D(gl, renderer, textRendererFont, pixelSize*scale, string, null , new int[]{8});
		gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]); // restore saved viewport
		renderer.enable(gl, false);
	}
	
	@Override
	public void destroy() {
		renderer.destroy(gl);
		renderer = null;
		textRegionUtil = null;
		textRendererFont = null;
		gl = null;
	}

	@Override
	public void drawTextTop(String string, float x, float y, Color color) {
		this.drawText(string, x*scale, height - y*scale, color);
	}

	@Override
	public void drawTextBottom(String string, float x, float y, Color color) {
		this.drawText(string, x*scale, y*scale, color);
	}

	@Override
	public void reshape(int width, int height) {
		this.width = width;
		this.height = height;
		renderer.reshapeOrtho(width, height, -100000, 100000);
	}

	@Override
	public void setScale(float scale) {
		this.scale = scale;
	}

}
