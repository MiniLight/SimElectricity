package simelectricity.essential.utils;

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.util.IIcon;

public class SERenderHeap {
	private final LinkedList<double[][]> cubes = new LinkedList();
	private final LinkedList<IIcon[]> textures = new LinkedList();
	
	public SERenderHeap clone(){
		SERenderHeap ret = new SERenderHeap();
		for (double[][] cube: cubes)
			ret.cubes.add(SERenderHelper.createSafeCopy(cube));
		
		for (IIcon[] textureArray: textures){
			ret.textures.add(new IIcon[]{
					textureArray[0],
					textureArray[1],
					textureArray[2],
					textureArray[3],
					textureArray[4],
					textureArray[5]
			});
		}
		return ret;
	}
	
	public SERenderHeap appendHeap(SERenderHeap heap){
		cubes.addAll(heap.cubes);
		textures.addAll(heap.textures);
		return this;
	}
	
	public SERenderHeap addCube(double[][] cube, IIcon texture){
		cubes.add(cube);
		textures.add(SERenderHelper.createTextureArray(texture));
		return this;
	}
	
	public SERenderHeap transform(double x, double y, double z){
		Iterator<double[][]> cubeIterator = cubes.iterator();
		while (cubeIterator.hasNext())
			SERenderHelper.translateCoord(cubeIterator.next(), x, y, z);
		return this;
	}
	
	public SERenderHeap rotateToVec(double xStart, double yStart, double zStart, double xEnd, double yEnd, double zEnd){
		Iterator<double[][]> cubeIterator = cubes.iterator();
		while (cubeIterator.hasNext())
			SERenderHelper.rotateToVec(cubeIterator.next(), xStart, yStart, zStart, xEnd, yEnd, zEnd);
		return this;
	}
	
	/**
	 * Rotate respect to vector (x,y,z)
	 * @param angle in degree
	 * @param x
	 * @param y
	 * @param z
	 */
	public SERenderHeap rotateAroundVector(float angle, double x, double y, double z){
		Iterator<double[][]> cubeIterator = cubes.iterator();
		while (cubeIterator.hasNext())
			SERenderHelper.rotateAroundVector(cubeIterator.next(), angle, x, y, z);
		return this;
	}
	
	public SERenderHeap rotateAroundZ(float angle){
		Iterator<double[][]> cubeIterator = cubes.iterator();
		while (cubeIterator.hasNext())
			SERenderHelper.rotateAroundZ(cubeIterator.next(), angle);
		return this;
	}
	
	public void applyToTessellator(int lightValue){
		Iterator<double[][]> cubeIterator = cubes.iterator();
		Iterator<IIcon[]> textureIterator = textures.iterator();
		while (cubeIterator.hasNext()){
			double[][] cube = cubeIterator.next();
			IIcon[] textureArray = textureIterator.next();
			SERenderHelper.addCubeToTessellator(cube, textureArray, lightValue);
		}
	}
}