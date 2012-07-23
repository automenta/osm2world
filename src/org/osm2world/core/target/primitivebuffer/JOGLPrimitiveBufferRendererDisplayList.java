package org.osm2world.core.target.primitivebuffer;

import static java.lang.Math.*;
import static javax.media.opengl.GL2.GL_COMPILE;
import static org.osm2world.core.target.common.rendering.OrthoTilesUtil.CardinalDirection.closestCardinal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.media.opengl.GL2;

import org.osm2world.core.math.VectorXYZ;
import org.osm2world.core.target.common.Primitive;
import org.osm2world.core.target.common.material.Material;
import org.osm2world.core.target.common.material.Material.Transparency;
import org.osm2world.core.target.common.rendering.Camera;
import org.osm2world.core.target.common.rendering.OrthoTilesUtil.CardinalDirection;
import org.osm2world.core.target.common.rendering.Projection;
import org.osm2world.core.target.jogl.JOGLTarget;

/**
 * renders the contents of a {@link PrimitiveBuffer} using JOGL.
 * Uses display lists to speed up the process.
 * 
 * If you don't need the renderer anymore, it's recommended to manually call
 * {@link #freeResources()} to delete the display lists. Otherwise, this will
 * not be done before a destructor call.
 */
public class JOGLPrimitiveBufferRendererDisplayList extends
	JOGLPrimitiveBufferRenderer {
	
	private PrimitiveBuffer primitiveBuffer; //keeping this referenced is only necessary because of indexed vertices
		
	/** pointer to the display list with static, non-transparent geometry */
	private Integer displayListPointer;
	
	/** transparent primitives, need to be sorted by distance from camera */
	private List<PrimitiveWithMaterial> transparentPrimitives =
			new ArrayList<PrimitiveWithMaterial>();
	
	/**
	 * the camera direction that was the basis for the previous sorting
	 * of {@link #transparentPrimitives}.
	 */
	private CardinalDirection currentPrimitiveSortDirection = null;
	
	private static final class PrimitiveWithMaterial {
		
		public final Primitive primitive;
		public final Material material;
		
		private PrimitiveWithMaterial(Primitive primitive, Material material) {
			this.primitive = primitive;
			this.material = material;
		}
		
	}
	
	public JOGLPrimitiveBufferRendererDisplayList(GL2 gl, PrimitiveBuffer primitiveBuffer) {
		
		super(gl);
		
		this.primitiveBuffer = primitiveBuffer;
		
		primitiveBuffer.optimize();
		
		displayListPointer = gl.glGenLists(1);
		
		gl.glNewList(displayListPointer, GL_COMPILE);

		for (Material material : primitiveBuffer.getMaterials()) {
			
			if (material.getTransparency() == Transparency.TRUE) {
				
				for (Primitive primitive : primitiveBuffer.getPrimitives(material)) {
					transparentPrimitives.add(
							new PrimitiveWithMaterial(primitive, material));
				}
				
			} else {
				
				JOGLTarget.setMaterial(gl, material, textureManager);
	
				for (Primitive primitive : primitiveBuffer.getPrimitives(material)) {
					renderPrimitive(gl, primitiveBuffer, primitive);
				}
				
			}
			
		}

		gl.glEndList();
		
	}

	private void renderPrimitive(GL2 gl, PrimitiveBuffer primitiveBuffer,
			Primitive primitive) {

		List<VectorXYZ> vertices =
			new ArrayList<VectorXYZ>(primitive.indices.length);
		
		for (int index : primitive.indices) {
			vertices.add(primitiveBuffer.getVertex(index));
		}
		
		JOGLTarget.drawPrimitive(gl, JOGLTarget.getGLConstant(primitive.type),
				vertices, primitive.normals, primitive.texCoordLists);
		
	}
	
	@Override
	public void render(final Camera camera, final Projection projection) {
		
		/* render static geometry */
		
		if (displayListPointer == null)
			throw new IllegalStateException("display list has been deleted");
		
		gl.glCallList(displayListPointer);
		
		/* render transparent primitives back-to-front */

		sortPrimitivesBackToFront(camera, projection);
		
		Material previousMaterial = null;
		
		for (PrimitiveWithMaterial p : transparentPrimitives) {
			
			if (!p.material.equals(previousMaterial)) {
				JOGLTarget.setMaterial(gl, p.material, textureManager);
				previousMaterial = p.material;
			}
			
			renderPrimitive(gl, primitiveBuffer, p.primitive);
			
		}
		
	}

	private void sortPrimitivesBackToFront(final Camera camera,
			final Projection projection) {
		
		if (projection.isOrthographic() &&
				abs(camera.getViewDirection().xz().angle() % (PI/2)) < 0.01 ) {
			
			/* faster sorting for cardinal directions */
			
			CardinalDirection closestCardinal = closestCardinal(camera.getViewDirection().xz().angle());
			
			if (closestCardinal.isOppositeOf(currentPrimitiveSortDirection)) {
			
				Collections.reverse(transparentPrimitives);
				
			} else if (closestCardinal != currentPrimitiveSortDirection) {
					
				Comparator<PrimitiveWithMaterial> comparator = null;
				
				switch(closestCardinal) {
				
				case N:
					comparator = new Comparator<PrimitiveWithMaterial>() {
						@Override
						public int compare(PrimitiveWithMaterial p1, PrimitiveWithMaterial p2) {
							return Double.compare(primitivePos(p2).z, primitivePos(p1).z);
						}
					};
					break;
				
				case E:
					comparator = new Comparator<PrimitiveWithMaterial>() {
						@Override
						public int compare(PrimitiveWithMaterial p1, PrimitiveWithMaterial p2) {
							return Double.compare(primitivePos(p2).x, primitivePos(p1).x);
						}
					};
					break;
					
				case S:
					comparator = new Comparator<PrimitiveWithMaterial>() {
						@Override
						public int compare(PrimitiveWithMaterial p1, PrimitiveWithMaterial p2) {
							return Double.compare(primitivePos(p1).z, primitivePos(p2).z);
						}
					};
					break;
					
				case W:
					comparator = new Comparator<PrimitiveWithMaterial>() {
						@Override
						public int compare(PrimitiveWithMaterial p1, PrimitiveWithMaterial p2) {
							return Double.compare(primitivePos(p1).x, primitivePos(p2).x);
						}
					};
					break;
					
				}
				
				Collections.sort(transparentPrimitives, comparator);
				
			}
				
			currentPrimitiveSortDirection = closestCardinal;
			
		} else {
			
			/* sort based on distance to camera */
			
			Collections.sort(transparentPrimitives, new Comparator<PrimitiveWithMaterial>() {
				@Override
				public int compare(PrimitiveWithMaterial p1, PrimitiveWithMaterial p2) {
					return Double.compare(
							distanceToCameraSq(camera, p2),
							distanceToCameraSq(camera, p1));
				}
			});
			
			currentPrimitiveSortDirection = null;
			
		}
		
	}

	private double distanceToCameraSq(Camera camera, PrimitiveWithMaterial p) {
		return primitivePos(p).distanceToSquared(camera.getPos());
	}

	private VectorXYZ primitivePos(PrimitiveWithMaterial p) {
		
		double sumX = 0, sumY = 0, sumZ = 0;
		
		for (int index : p.primitive.indices) {
			VectorXYZ v = primitiveBuffer.getVertex(index);
			sumX += v.x;
			sumY += v.y;
			sumZ += v.z;
		}
		
		return new VectorXYZ(sumX / p.primitive.indices.length,
				sumY / p.primitive.indices.length,
				sumZ / p.primitive.indices.length);
		
	}
	
	@Override
	public void freeResources() {
		
		if (displayListPointer != null) {
			gl.glDeleteLists(displayListPointer, 1);
			displayListPointer = null;
		}

		super.freeResources();
		
	}
	
}