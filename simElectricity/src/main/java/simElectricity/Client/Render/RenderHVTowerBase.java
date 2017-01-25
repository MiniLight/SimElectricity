package simElectricity.Client.Render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import simElectricity.API.Client.ITextureProvider;

@SideOnly(Side.CLIENT)
public abstract class RenderHVTowerBase extends TileEntitySpecialRenderer implements ITextureProvider {
	public final CableRender render = new CableRender(this);	

	public abstract void renderTower(TileEntity tower, double x, double y, double z);
	
	public void renderCable(TileEntity tileEntity, double x, double y, double z){
		IHVTower tower = (IHVTower) tileEntity;
		for (int i = 0; i < tower.getNeighborInfo().length; i += 3) {
        	renderCableTo(tileEntity,
        			tileEntity.getWorldObj().getTileEntity(tower.getNeighborInfo()[i], tower.getNeighborInfo()[i + 1], tower.getNeighborInfo()[i + 2]),
        			x,y,z);
	    }
	}
	
	/**
	 * Render a hv insulator
	 * @param num Number of rings
	 * @param length
	 */
    public void renderInsulator(int num, double length) {
        render.render_cube(0.1, length, 0.1);
        
        GL11.glTranslated(0, 0.1, 0);
        render.render_cube(0.3, 0.04, 0.3, 2);
        
        for (int i = 0; i<num ;i++){
        	GL11.glTranslated(0, 0.08, 0);
        	render.render_cube(0.3, 0.04, 0.3, 2);
        }
    }
	
    public void renderCableTo(TileEntity tileEntity,TileEntity neighbor, double x, double y, double z){
    	if (neighbor!=null)
    		renderCableTo(tileEntity, neighbor, x, y, z, calculateTension(tileEntity, neighbor));
    }
    
    public void renderCableTo(TileEntity tileEntity,TileEntity neighbor, double x, double y, double z, double tension){
    	IHVTower tower = (IHVTower) tileEntity;
        if (neighbor instanceof IHVTower) {
	        IHVTower towerNeighbor = (IHVTower) neighbor;
	        
	        boolean swap = false;
	        if (getDirection(tower) == getDirection(towerNeighbor)){           //Same
	        	swap = false;
	        }else if (getDirection(tower)%2 == getDirection(towerNeighbor)%2){ //Opposite
	        	swap = true;
	        }else if (getDirection(tower)%2 ==0){
	        	    if (neighbor.xCoord > tileEntity.xCoord) {
                        if (neighbor.zCoord > tileEntity.zCoord){//1
                        	swap = getDirection(towerNeighbor) == 1;
                        	if (getDirection(tower) == 2)
                        		swap =! swap;
                        }else{									 //4
                        	swap = getDirection(towerNeighbor) == 3;
                        	if (getDirection(tower) == 2)
                        		swap =! swap;
                        }
                    } else {
                        if (neighbor.zCoord > tileEntity.zCoord){//1
                        	swap = getDirection(towerNeighbor) == 3;
                        	if (getDirection(tower) == 2)
                        		swap =! swap;
                        }else{									 //4
                        	swap = getDirection(towerNeighbor) == 1;
                        	if (getDirection(tower) == 2)
                        		swap =! swap;
                        }
                    }
	        }else{
        	    if (neighbor.xCoord > tileEntity.xCoord) {
                    if (neighbor.zCoord > tileEntity.zCoord){//1
                    	swap = getDirection(towerNeighbor) == 0;
                    	if (getDirection(tower) == 3)
                    		swap =! swap;
                    }else{									 //4
                    	swap = getDirection(towerNeighbor) == 2;
                    	if (getDirection(tower) == 3)
                    		swap =! swap;
                    }
                } else {
                    if (neighbor.zCoord > tileEntity.zCoord){//1
                    	swap = getDirection(towerNeighbor) == 2;
                    	if (getDirection(tower) == 3)
                    		swap =! swap;
                    }else{									 //4
                    	swap = getDirection(towerNeighbor) == 0;
                    	if (getDirection(tower) == 3)
                    		swap =! swap;
                    }
                }		        	
	        }
	        
	        //Mid
	        float[] offset1 = getRotatedOffset(tower,1);
	        float[] offset2 = getRotatedOffset(towerNeighbor,1);        
	        GL11.glPushMatrix();
	        GL11.glTranslated(x, y, z);
	        GL11.glTranslated(0.5 + offset1[0], offset1[1], 0.5 + offset1[2]);
	        
	        render.renderHalfParabolicCable(
	        		tileEntity.xCoord + offset1[0], tileEntity.yCoord + offset1[1], tileEntity.zCoord + offset1[2],
	        		neighbor.xCoord + offset2[0], neighbor.yCoord + offset2[1], neighbor.zCoord + offset2[2],
	                0.075, tension, 1);
	        
	        GL11.glPopMatrix();       
	        
	        //R
	        offset1 = getRotatedOffset(tower, swap ? 0 :2);
	        offset2 = getRotatedOffset(towerNeighbor,2);
	        GL11.glPushMatrix();
	        GL11.glTranslated(x, y, z);
	        GL11.glTranslated(0.5 + offset1[0], offset1[1], 0.5 + offset1[2]);
	        
	        render.renderHalfParabolicCable(
	        		tileEntity.xCoord + offset1[0], tileEntity.yCoord + offset1[1], tileEntity.zCoord + offset1[2],
	        		neighbor.xCoord + offset2[0], neighbor.yCoord + offset2[1], neighbor.zCoord + offset2[2],
	        		0.075, tension, 1);
	        
	        GL11.glPopMatrix();
	        
	        //L
	        offset1 = getRotatedOffset(tower,swap ? 2 :0);
	        offset2 = getRotatedOffset(towerNeighbor,0);
	        GL11.glPushMatrix();
	        GL11.glTranslated(x, y, z);
	        GL11.glTranslated(0.5 + offset1[0], offset1[1], 0.5 + offset1[2]);
	        
	        render.renderHalfParabolicCable(
	        		tileEntity.xCoord + offset1[0], tileEntity.yCoord + offset1[1], tileEntity.zCoord + offset1[2],
	        		neighbor.xCoord + offset2[0], neighbor.yCoord + offset2[1], neighbor.zCoord + offset2[2],
	        		0.075, tension, 1);
	        
	        GL11.glPopMatrix();
        }
    }
    
	@Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float f) {
    	IHVTower tower = (IHVTower) tileEntity;

    	//Tower rendering
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glTranslated(0.5, 0, 0.5);
        GL11.glRotatef(getDirection(tower)*270F + 90F, 0F, 1F, 0F);

        //Debugging purpose, indicates the direction
        //GL11.glPushMatrix();
        //render.renderCable(0, 0, 0, 1, 0, 0, 0.1, 2);
        //GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        renderTower(tileEntity,x,y,z);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        //End of tower rendering
        
        //Cable rendering
        renderCable(tileEntity,x,y,z);
    }
    
    float[] getRotatedOffset(IHVTower tower, int index){
    	float x = tower.offsetArray()[3*index];
    	float y = tower.offsetArray()[3*index+1];
    	float z = tower.offsetArray()[3*index+2];
    	
    	if (tower.getFacing() == 2 || tower.getFacing() == 3){
    		z = -z;
    	}
    	
    	switch (tower.getFacing()) {
	        case 2: //N
	            return new float[]{x,y,z};
	        case 5: //E
	        	return new float[]{z,y,x};
	        case 3: //S
	        	return new float[]{-x,y,-z};
	        default://W
	        	return new float[]{-z,y,-x};
    	}
    }
    
    int getDirection(IHVTower tower) {
        return RenderUtil.getDirection(tower.getFacing());
    }
        
    double getTension(TileEntity te){
    	if (te instanceof IHVTower)
    		return ((IHVTower) te).getWireTension();
    	return 0;
    }
    
    double calculateTension(TileEntity te1, TileEntity te2){
    	return (RenderUtil.distanceOf(te1.xCoord, te1.zCoord, te2.xCoord, te2.zCoord) 
    			* (getTension(te1)+getTension(te2))/2.0f);
    }
}
