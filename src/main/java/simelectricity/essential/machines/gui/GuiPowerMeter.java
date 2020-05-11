package simelectricity.essential.machines.gui;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.lwjgl.opengl.GL11;
import simelectricity.essential.utils.SEUnitHelper;
import simelectricity.essential.utils.client.gui.SEGuiContainer;

@OnlyIn(Dist.CLIENT)
public class GuiPowerMeter<T extends ContainerPowerMeter> extends SEGuiContainer<T> {
    ////////////////////////
    /// Switch
    ////////////////////////
    private static final int switchSize = 32;
    private static final int switchX = 115;
    private static final int switchY = 48;

    public GuiPowerMeter(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        //draw text and stuff here
        //the parameters for drawString are: string, x, y, color

        this.font.drawString(this.title.getFormattedText(), 8, 6, 4210752);

        int ybase = 22;
        this.font.drawString(I18n.format("gui.simelectricity.voltage"), 10, ybase, 4210752);
        this.font.drawString(SEUnitHelper.getVoltageStringWithUnit(this.container.voltage), 10, ybase + 8, 4210752);
        this.font.drawString(I18n.format("gui.simelectricity.current"), 10, ybase + 16, 4210752);
        this.font.drawString(SEUnitHelper.getCurrentStringWithUnit(this.container.current), 10, ybase + 24, 4210752);
        this.font.drawString(I18n.format("gui.simelectricity.power_input"), 10, ybase+32, 4210752);
        this.font.drawString(SEUnitHelper.getPowerStringWithUnit(this.container.voltage*this.container.current), 10, ybase + 40, 4210752);
        this.font.drawString(I18n.format("gui.simelectricity.used_energy"), 10, ybase + 48, 4210752);
        this.font.drawString(SEUnitHelper.getEnergyStringInKWh(this.container.bufferedEnergy), 10, ybase + 56, 4210752);
        this.font.drawString(SEUnitHelper.getEnergyStringInJ(this.container.bufferedEnergy), 10, ybase + 64, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int par2, int par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture("textures/gui/power_meter.png");
        blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        blit(this.guiLeft + switchX, this.guiTop + switchY, this.container.isOn ? 208 : 176, 0, 32, 32);

        this.directionSelector.set(this.container.inputSide, this.container.outputSide);
    }

    @Override
    public void init() {
        super.init();
        int xbase = 18;
        int ybase = 97;

        this.directionSelector = addDirectionSelector(this.guiLeft + 116, this.guiTop + 20);
        addServerButton(1, this.guiLeft + 10, this.guiTop + ybase, 40, 20, "Clear");
    }
}
