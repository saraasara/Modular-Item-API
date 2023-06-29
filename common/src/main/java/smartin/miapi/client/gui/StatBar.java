package smartin.miapi.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

@Environment(EnvType.CLIENT)
public class StatBar extends InteractAbleWidget {
    double primaryPercent = 0;
    double secondaryPercent = 0;
    int primaryColor = 0;
    int secondaryColor = 0;
    int offColor = 0;

    public StatBar(int x, int y, int width, int height, int offColor) {
        super(x, y, width, height, Text.empty());
        this.offColor = offColor;
    }

    public void setPrimary(double primaryPercent, int color) {
        primaryPercent = Math.min(1, Math.max(0, primaryPercent));
        this.primaryColor = color;
        this.primaryPercent = primaryPercent;
    }

    public void setSecondary(double secondaryPercent, int color) {
        secondaryPercent = Math.min(1, Math.max(0, secondaryPercent));
        this.secondaryColor = color;
        this.secondaryPercent = secondaryPercent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX() + width, getY() + height, ColorHelper.Argb.getArgb(255, 255, 0, 255));
        context.fill(getX(), getY(), (int) (getX() + width * primaryPercent), height + getY(), primaryColor);
        context.fill((int) (getX() + width * primaryPercent), getY(), (int) (getX() + width * secondaryPercent), height + getY(), secondaryColor);
        context.fill((int) (getX() + width * secondaryPercent), getY(), getX() + width, height + getY(), offColor);

    }
}
