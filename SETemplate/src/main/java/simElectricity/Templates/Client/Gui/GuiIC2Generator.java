/*
 * Copyright (C) 2014 SimElectricity
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package simElectricity.Templates.Client.Gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import simElectricity.Templates.Container.ContainerIC2Generator;
import simElectricity.Templates.Utils.IGuiSyncHandler;

@SideOnly(Side.CLIENT)
public class GuiIC2Generator extends GuiContainer implements IGuiSyncHandler{
	double inputPower, bufferedEnergy, powerRate, outputVoltage;
	
	@Override
	public void onGuiEvent(byte eventID, Object[] data) {
		switch (eventID){
		case 0:
			inputPower = (Double) data[0];
			bufferedEnergy = (Double) data[1];
			powerRate = (Double)data[2];
			outputVoltage = (Double)data[3];
			break;
		}
	}
	
    protected TileEntity te;

    public GuiIC2Generator(InventoryPlayer inventoryPlayer, TileEntity tileEntity) {
        super(new ContainerIC2Generator(inventoryPlayer, tileEntity));
        te = tileEntity;
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        //draw text and stuff here
        //the parameters for drawString are: string, x, y, color

        fontRendererObj.drawString(StatCollector.translateToLocal("tile.sime:IC2Generator.name"), 8, 6, 4210752);

        fontRendererObj.drawString("Pi = " + String.format("%.1f", inputPower) + "Eu/P", 8, 23, 4210752);      
        fontRendererObj.drawString("Vo = " + String.format("%.1f", outputVoltage) + "Eu/P", 8, 34, 4210752);
        fontRendererObj.drawString("Output Rate = " +  String.format("%.1f", powerRate) + "Eu/Tick", 8, 45, 4210752);
        fontRendererObj.drawString("Buffered = " +  String.format("%.1f", bufferedEnergy) + "Eu", 8, 56, 4210752);
        
        //draws "Inventory" or your regional equivalent
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int par2, int par3) {
        //draw your Gui here, only thing you need to change is the path
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(new ResourceLocation("simElectricity:textures/gui/GUI_IC2Converter.png"));
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}