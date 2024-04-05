package smartin.miapi.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.crafting.CraftingScreen;
import smartin.miapi.entity.ShieldingArmorFacet;
import smartin.miapi.mixin.InGameHudAccessor;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    private static final Identifier ICONS = new Identifier("textures/gui/icons.png");

    @Inject(
            method = "renderStatusBars(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At("TAIL")
    )
    private void miapi$customDrawContext(DrawContext context, CallbackInfo ci) {
        InGameHud inGameHud = (InGameHud) (Object) (this);
        PlayerEntity playerEntity = ((InGameHudAccessor) inGameHud).callGetCameraPlayer();
        int heartBars = ((InGameHudAccessor) inGameHud).callGetHeartRows(((InGameHudAccessor) inGameHud).callGetHeartCount(playerEntity));
        ShieldingArmorFacet facet = ShieldingArmorFacet.KEY.get(playerEntity);
        // Calculate health and absorption values
        int playerHealth = MathHelper.ceil(playerEntity.getHealth());
        int renderHealthValue = ((InGameHudAccessor) inGameHud).getRenderHealthValue();
        int scaledWidth = ((InGameHudAccessor) inGameHud).getScaledWidth();
        int shieldingArmorMaxAmount = (int) facet.getMaxAmount() / 2;
        int shieldingArmorCurrentAmount = (int) (facet.getCurrentAmount());
        Miapi.LOGGER.info("current " + shieldingArmorCurrentAmount + " max " + shieldingArmorMaxAmount);
        int scaledHeight = ((InGameHudAccessor) inGameHud).getScaledHeight();
        float maxHealth = Math.max((float) playerEntity.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH), (float) Math.max(renderHealthValue, playerHealth));
        int absorptionAmount = MathHelper.ceil(playerEntity.getAbsorptionAmount());
        int healthAbsorptionTotal = MathHelper.ceil((maxHealth + (float) absorptionAmount) / 2.0F / 10.0F);
        int numHearts = Math.max(10 - (healthAbsorptionTotal - 2), 3);
        int startY = scaledHeight - 39 - (healthAbsorptionTotal - 1) * numHearts - 10;
        for (int index = 0; index < shieldingArmorMaxAmount; index++) {
            int heartX = scaledWidth / 2 - 91 + (index % 10) * 8;
            int yOffset = (index / 10) * 10;
            int heartTextureIndex = index * 2 + 1;
            if (heartTextureIndex < shieldingArmorCurrentAmount) {
                context.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, heartX, startY - yOffset, 430, 96, 9, 9,512, 512);
            } else if (heartTextureIndex == shieldingArmorCurrentAmount) {
                context.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, heartX, startY - yOffset, 439, 96, 9, 9,512, 512);
            } else {
                context.drawTexture(CraftingScreen.BACKGROUND_TEXTURE, heartX, startY - yOffset, 448, 96, 9, 9,512, 512);
            }
        }

    }
}