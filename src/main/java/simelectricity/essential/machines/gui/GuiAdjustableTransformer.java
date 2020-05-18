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
public final class GuiAdjustableTransformer<T extends ContainerAdjustableTransformer> extends SEGuiContainer<T> {
    public GuiAdjustableTransformer(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        //draw text and stuff here
        //the parameters for drawString are: string, x, y, color

        this.font.drawString(this.title.getFormattedText(), 8, 6, 4210752);

        this.font.drawString(I18n.format("gui.simelectricity.ratio_step_up"), 18, 85, 4210752);
        this.font.drawString(I18n.format("gui.simelectricity.resistance_secondary"), 18, 124, 4210752);

        this.font.drawString("1:" + String.format("%.1f", this.container.ratio), 74, 22, 4210752);
        this.font.drawString(String.format("%.3f", this.container.outputResistance) + " \u03a9", 100, 56, 4210752);
        this.font.drawString(SEUnitHelper.getVoltageStringWithUnit(this.container.vPri), 16, 30, 4210752);
        this.font.drawString(SEUnitHelper.getVoltageStringWithUnit(this.container.vSec), 110, 30, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float opacity, int par2, int par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        bindTexture("textures/gui/adjustable_transformer.png");
        blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        blit(this.guiLeft + 74, this.guiTop + 32, 176, 0, 28, 48);

        this.directionSelector.set(this.container.inputSide, this.container.outputSide);
    }

    @Override
    public void init() {
        super.init();


        int xbase = 18;
        int ybase = 97;

        addServerButton(0, this.guiLeft + xbase, this.guiTop + ybase, 20, 20, "-1");
        addServerButton(1, this.guiLeft + xbase + 20, this.guiTop + ybase, 20, 20, "-.1");
        addServerButton(2, this.guiLeft + xbase + 40, this.guiTop + ybase, 30, 20, "-.01");
        addServerButton(3, this.guiLeft + xbase + 70, this.guiTop + ybase, 30, 20, "+.01");
        addServerButton(4, this.guiLeft + xbase + 100, this.guiTop + ybase, 20, 20, "+.1");
        addServerButton(5, this.guiLeft + xbase + 120, this.guiTop + ybase, 20, 20, "+1");

        addServerButton(6, this.guiLeft + xbase, this.guiTop + ybase + 38, 20, 20, "-1");
        addServerButton(7, this.guiLeft + xbase + 20, this.guiTop + ybase + 38, 20, 20, "-.1");
        addServerButton(8, this.guiLeft + xbase + 40, this.guiTop + ybase + 38, 30, 20, "-.01");
        addServerButton(9, this.guiLeft + xbase + 70, this.guiTop + ybase + 38, 30, 20, "+.01");
        addServerButton(10, this.guiLeft + xbase + 100, this.guiTop + ybase + 38, 20, 20, "+.1");
        addServerButton(11, this.guiLeft + xbase + 120, this.guiTop + ybase + 38, 20, 20, "+1");

        this.directionSelector = addDirectionSelector(this.guiLeft + 24, this.guiTop + 52);
    }
}
