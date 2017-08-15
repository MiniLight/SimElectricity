package simelectricity.essential.client.grid;

import org.lwjgl.opengl.GL11;

import net.minecraft.tileentity.TileEntity;

public class TileRenderTransmissionTower2 extends TileRenderTranmissionTowerBase{
	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks, int destroyStage) {
    	ISETransmissionTower tw = ((ISETransmissionTower) tileEntity);
    	super.renderTileEntityAt(tileEntity, x, y, z, partialTicks, destroyStage);
    	
    	int meta = tileEntity.getBlockMetadata();
    	
    	if ((tileEntity.getBlockMetadata()>>3) == 0 && render1 && render2){
        	double[] 	from = new double[9], to = new double[9];
    		
    		for (int i=0; i<9; i++){
    			from[i] = fixedfrom1[i];
    			to[i] = fixedfrom2[i];
    		}
    		
    		TransmissionTowerRenderHelper.swapIfIntersect(from,to);
            GL11.glPushMatrix();
            GL11.glTranslated(x-tileEntity.getPos().getX(), y-tileEntity.getPos().getY(), z-tileEntity.getPos().getZ());
            
            GL11.glPushMatrix();
            GL11.glTranslated(from[0], from[1],from[2]);
            CableGLRender.renderParabolicCable(from[0], from[1],from[2], to[0], to[1],to[2], 0.075, 2, this, 1);
            GL11.glPopMatrix();
            
    		TransmissionTowerRenderHelper.swapIfIntersect(from,to);
            GL11.glPushMatrix();
            GL11.glTranslated(from[3], from[4],from[5]);
            CableGLRender.renderParabolicCable(from[3], from[4],from[5], to[3], to[4],to[5], 0.075, 2, this, 1);
            GL11.glPopMatrix();
            
            GL11.glPushMatrix();
            GL11.glTranslated(from[6], from[7],from[8]);
            CableGLRender.renderParabolicCable(from[6], from[7],from[8], to[6], to[7],to[8], 0.075, 2, this, 1);
            GL11.glPopMatrix();
            
            GL11.glPopMatrix();
    	}
    }
}
