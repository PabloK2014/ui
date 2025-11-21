package com.myaddon.iuaddon.client.gui;

import com.denfop.container.ContainerBaseMolecular;
import com.denfop.gui.GuiMolecularTransformer;
import com.myaddon.iuaddon.IUAddon;
import com.myaddon.iuaddon.tiles.TileEntityMysticalGrower;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class GuiMysticalGrower extends GuiMolecularTransformer {

    private TileEntityMysticalGrower tile;

    public GuiMysticalGrower(ContainerBaseMolecular container1) {
        super(container1);
        this.xSize = 212;  // Ширина текстуры
        this.ySize = 202;  // Высота текстуры
        
        // Get tile entity from container
        if (container1.base instanceof TileEntityMysticalGrower) {
            this.tile = (TileEntityMysticalGrower) container1.base;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        this.bindTexture();
        int xStart = (this.width - this.xSize) / 2;
        int yStart = (this.height - this.ySize) / 2;
        
        // Draw main GUI background
        this.drawTexturedModalRect(xStart, yStart, 0, 0, this.xSize, this.ySize);
        
        if (tile != null) {
            // Draw progress bar
            // Слот прогресса: x88 y48 - x102 y56 (размер 14x8)
            // Текстура анимации: x212 y1 - x226 y9 (размер 14x8)
            double progress = tile.getGuiProgress();
            // System.out.println("GUI Progress: " + progress);
            if (progress > 0) {
                int progressWidth = (int) (progress * 14);
                // System.out.println("Drawing progress bar with width: " + progressWidth);
                
                // Рисуем полосу прогресса
                this.drawTexturedModalRect(xStart + 88, yStart + 48, 212, 1, progressWidth, 8);
            }
            
            // Draw energy bar
            // Слот энергии: x46 y95 - x174 y99 (размер 128x4)
            // Текстура анимации: x0 y205 - x128 y210 (размер 128x5)
            if (tile.energy != null && tile.energy.getCapacity() > 0) {
                double energyRatio = tile.energy.getFillRatio();
                if (energyRatio > 0 && energyRatio <= 1.0) { // Проверяем, что ratio в нормальных пределах
                    int energyWidth = (int) (energyRatio * 128);
                    // System.out.println("Energy ratio: " + energyRatio + " Width: " + energyWidth);
                    
                    // Рисуем полосу энергии
                    this.drawTexturedModalRect(xStart + 46, yStart + 95, 0, 205, energyWidth, 4);
                }
            }
        }
    }



    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation(IUAddon.MOD_ID, "textures/gui/mystical_grower.png");
    }
    
    @Override
    public String getName() {
        return "Mystical Grower";
    }
}