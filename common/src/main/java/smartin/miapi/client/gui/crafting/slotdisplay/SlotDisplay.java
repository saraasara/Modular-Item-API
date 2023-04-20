package smartin.miapi.client.gui.crafting.slotdisplay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import org.lwjgl.opengl.GL11;
import smartin.miapi.Miapi;
import smartin.miapi.client.gui.InteractAbleWidget;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.item.modular.properties.SlotProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SlotDisplay extends InteractAbleWidget {
    private final Map<SlotProperty.ModuleSlot, ModuleButton> buttonMap = new HashMap<>();
    private ItemStack stack;
    private MatrixStack slotProjection = new MatrixStack();
    private double lastMouseX;
    private double lastMouseY;
    private boolean mouseDown0 = false;
    private boolean mouseDown1 = false;
    private SlotProperty.ModuleSlot selected = null;
    private final Consumer<SlotProperty.ModuleSlot> setSelected;
    private SlotProperty.ModuleSlot baseSlot;

    public SlotDisplay(ItemStack stack, int x, int y, int height, int width, Consumer<SlotProperty.ModuleSlot> selected) {
        super(x, y, width, height, Text.literal("Item Display"));
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        slotProjection.scale(1.0F, -1.0F, 1.0F);
        this.setSelected = selected;
        this.setBaseSlot(new SlotProperty.ModuleSlot(new ArrayList<>()));
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (mouseDown0) {
            handleLeftClickDrag(lastMouseX - mouseX, lastMouseY - mouseY);
        } else if (mouseDown1) {
            handleRightClickDrag(lastMouseX - mouseX, lastMouseY - mouseY);
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        boolean mouseOver = x >= this.x && y >= this.y && x < this.x + this.width && y < this.y + this.height;
        if (!mouseOver) {
            mouseDown0 = false;
            mouseDown1 = false;
        } else {
            return true;
        }
        return super.isMouseOver(x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            mouseDown0 = true;
        } else if (button == 1 && isMouseOver(mouseX, mouseY)) {
            mouseDown1 = true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            mouseDown0 = false;
        } else if (button == 1) {
            mouseDown1 = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void handleLeftClickDrag(double deltaX, double deltaY) {
        MatrixStack newStack = new MatrixStack();
        newStack.translate((float) -deltaX / 100, -(float) deltaY / 100, 0);
        newStack.multiplyPositionMatrix(slotProjection.peek().getPositionMatrix());
        slotProjection = newStack;
    }

    private void handleRightClickDrag(double deltaX, double deltaY) {
        float angleX = (float) -(deltaY * 0.02f);
        float angleY = (float) -(deltaX * 0.02f);
        slotProjection.multiply(Quaternion.fromEulerXyz(new Vec3f(angleX, angleY, 0)));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (isMouseOver(mouseX, mouseY)) {
            double scale = Math.pow(2, amount / 10);
            slotProjection.scale((float) scale, (float) scale, 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    public void setBaseSlot(SlotProperty.ModuleSlot slot) {
        baseSlot = slot;
    }

    public void setItem(ItemStack itemStack) {
        stack = itemStack;
        buttonMap.forEach((slot, moduleButton) -> {
            children().remove(moduleButton);
        });
        buttonMap.clear();
        ModularItem.getModules(stack).allSubModules().forEach(moduleInstances -> {
            SlotProperty.getSlots(moduleInstances).forEach((number, slot) -> {
                buttonMap.computeIfAbsent(slot, newSlot -> {
                    ModuleButton newButton = new ModuleButton(0, 0, 10, 10, newSlot);
                    addChild(newButton);
                    return newButton;
                });
            });
        });
        if (baseSlot != null) {
            baseSlot.inSlot = ModularItem.getModules(stack);
            buttonMap.computeIfAbsent(baseSlot, newSlot -> {
                ModuleButton newButton = new ModuleButton(0, 0, 10, 10, newSlot);
                addChild(newButton);
                return newButton;
            });
        }
    }

    public int getSize() {
        int size = Math.min(width, height);
        size = Math.max(5, size - 10);
        return size;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        enableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
        renderSlot(stack, matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        disableScissor();
    }

    public void select(SlotProperty.ModuleSlot selected) {
        this.selected = selected;
    }

    private Vec3f position() {
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
        return new Vec3f(x + (float) (width - 16) / 2, y + (float) (height - 16) / 2, (100.0F + renderer.zOffset + 50));
    }

    public void renderSlot(ItemStack stack, MatrixStack matrices, int mouseX, int mouseY, float delta) {
        //GL11.GL_SCISSOR_TEST
        ItemRenderer renderer = MinecraftClient.getInstance().getItemRenderer();
        MinecraftClient.getInstance().getTextureManager().getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).setFilter(false, false);
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        //RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        Vec3f pos = position();
        matrixStack.translate(pos.getX(), pos.getY(), pos.getZ());
        matrixStack.scale(getSize(), getSize(), -16.0F);
        RenderSystem.applyModelViewMatrix();
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        boolean bl = false;
        if (bl) {
            DiffuseLighting.disableGuiDepthLighting();
        }
        renderer.renderItem(stack, ModelTransformation.Mode.GUI, 15728880, OverlayTexture.DEFAULT_UV, slotProjection, immediate, 0);
        renderButtons(stack, matrixStack, slotProjection, 1);
        immediate.draw();
        RenderSystem.enableDepthTest(); // added
        if (bl) {
            DiffuseLighting.enableGuiDepthLighting();
        }
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }

    public void renderButtons(ItemStack stack, MatrixStack matrixStack, MatrixStack otherProjection, float delta) {
        buttonMap.forEach((currentslot, button) -> {
            Vec3f position = SlotProperty.getTransform(currentslot).translation;
            position.add(-4.5f, -4.5f, 1);
            Vector4f pos = new Vector4f(position.getX() / 16, position.getY() / 16, position.getZ() / 16, 1.0f);
            pos.transform(otherProjection.peek().getPositionMatrix());
            pos.transform(matrixStack.peek().getPositionMatrix());
            button.x = (int) pos.getX() - button.getWidth() / 2;
            button.y = (int) pos.getY() - button.getHeight() / 2;
        });
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    public class ModuleButton extends InteractAbleWidget {
        private static final Identifier ButtonTexture = new Identifier(Miapi.MOD_ID, "textures/button.png");
        public SlotProperty.ModuleSlot instance;

        public ModuleButton(int x, int y, int width, int height, SlotProperty.ModuleSlot instance) {
            super(x, y, width, height, Text.literal(" "));
            this.instance = instance;
        }

        private void setSelected(SlotProperty.ModuleSlot instance) {
            selected = instance;
            setSelected.accept(instance);
        }

        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            RenderSystem.depthMask(false);
            this.renderButton(matrices, mouseX, mouseY, delta);
            RenderSystem.depthFunc(GL11.GL_ALWAYS);
            RenderSystem.depthMask(true);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.isMouseOver(mouseX, mouseY)) {
                setSelected(this.instance);
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, ButtonTexture);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            int textureSize = 30;
            int textureOffset = 0;

            if (this.instance.equals(selected)) {
                textureOffset = 20;
            } else if (this.isMouseOver(mouseX, mouseY)) {
                textureOffset = 10;
            }

            drawTexture(matrices, x, y, 0, textureOffset, 0, this.width, this.height, textureSize, 10);
        }
    }
}