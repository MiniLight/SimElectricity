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

package simElectricity.Client.Gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import simElectricity.API.Network;
import simElectricity.Common.Blocks.Container.ContainerIC2Generator;
import simElectricity.Common.Blocks.TileEntity.TileIC2Generator;

@SideOnly(Side.CLIENT)
public class GuiIC2Generator extends GuiContainer {
    protected TileIC2Generator tileentity;

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(0, guiLeft + 96, guiTop + 18, 20, 20, "--"));
        buttonList.add(new GuiButton(1, guiLeft + 116, guiTop + 18, 16, 20, "-"));
        buttonList.add(new GuiButton(2, guiLeft + 132, guiTop + 18, 16, 20, "+"));
        buttonList.add(new GuiButton(3, guiLeft + 148, guiTop + 18, 20, 20, "++"));
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id < 4) {
            switch (button.id) {
                case 0:
                    if (GuiScreen.isCtrlKeyDown())
                        tileentity.outputVoltage -= 512;
                    else
                        tileentity.outputVoltage -= 32;
                    break;
                case 1:
                    if (GuiScreen.isCtrlKeyDown())
                        tileentity.outputVoltage -= 1;
                    else
                        tileentity.outputVoltage -= 8;
                    break;
                case 2:
                    if (GuiScreen.isCtrlKeyDown())
                        tileentity.outputVoltage += 1;
                    else
                        tileentity.outputVoltage += 8;
                    break;
                case 3:
                    if (GuiScreen.isCtrlKeyDown())
                        tileentity.outputVoltage += 512;
                    else
                        tileentity.outputVoltage += 32;
                    break;
                default:
            }

            if (tileentity.outputVoltage < 1)
                tileentity.outputVoltage = 1;
            if (tileentity.outputVoltage > 8192)
                tileentity.outputVoltage = 8192;
            Network.updateTileEntityFieldsToServer(tileentity, "outputVoltage");
        }
    }

    public GuiIC2Generator(InventoryPlayer inventoryPlayer, TileEntity tileEntity) {
        super(new ContainerIC2Generator(inventoryPlayer, tileEntity));
        tileentity = (TileIC2Generator) tileEntity;
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        //draw text and stuff here
        //the parameters for drawString are: string, x, y, color

        fontRendererObj.drawString(StatCollector.translateToLocal("tile.sime:IC2Generator.name"), 8, 6, 4210752);

        fontRendererObj.drawString("Vo = " + String.format("%.1f", tileentity.outputVoltage) + "Eu/P", 8, 24, 4210752);
        fontRendererObj.drawString("Output Rate = " +  String.format("%.1f", tileentity.powerRate) + "Eu/Tick", 8, 43, 4210752);
        fontRendererObj.drawString("Buffered = " +  String.format("%.1f", tileentity.bufferedEnergy) + "Eu", 8, 56, 4210752);
        
        //draws "Inventory" or your regional equivalent
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96, 4210752);
        fontRendererObj.drawString(tileentity.getFunctionalSide().toString(), xSize - 38, ySize - 96, 4210752);
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