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

package simElectricity.Client;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import simElectricity.Client.Gui.*;
import simElectricity.Client.Render.RenderTower;
import simElectricity.Client.Render.RenderWindMillTop;
import simElectricity.Client.Render.RenderWire;
import simElectricity.Common.Blocks.TileEntity.*;
import simElectricity.Common.Blocks.WindMill.TileWindMillTop;
import simElectricity.Common.CommonProxy;

public class ClientProxy extends CommonProxy {
    @Override
    public World getClientWorld() {
        return FMLClientHandler.instance().getClient().theWorld;
    }

    @Override
    public void registerTileEntitySpecialRenderer() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileWire.class, new RenderWire());
        ClientRegistry.bindTileEntitySpecialRenderer(TileWindMillTop.class, new RenderWindMillTop());
        ClientRegistry.bindTileEntitySpecialRenderer(TileTower.class, new RenderTower());
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te instanceof TileQuantumGenerator)
            return new GuiQuantumGenerator(player.inventory, (TileQuantumGenerator) te);
        if (te instanceof TileVoltageMeter)
            return new GuiVoltageMeter(player.inventory, (TileVoltageMeter) te);
        if (te instanceof TileElectricFurnace)
            return new GuiElectricFurnace(player.inventory, (TileElectricFurnace) te);
        if (te instanceof TileSimpleGenerator)
            return new GuiSimpleGenerator(player.inventory, (TileSimpleGenerator) te);
        if (te instanceof TileAdjustableResistor)
            return new GuiAdjustableResistor(player.inventory, te);
        if (te instanceof TileAdjustableTransformer)
            return new GuiAdjustableTransformer(player.inventory, te);
        if (te instanceof TileSwitch)
            return new GuiSwitch(player.inventory, te);
        if (te instanceof TileSolarInverter)
            return new GuiSolarInverter(player.inventory, te);

        return null;
    }
}