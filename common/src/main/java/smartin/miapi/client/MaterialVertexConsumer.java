package smartin.miapi.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.material.Material;
import smartin.miapi.registries.RegistryInventory;

@Environment(EnvType.CLIENT)
public class MaterialVertexConsumer implements VertexConsumer {
    protected final VertexConsumer delegate;
    protected final int x;
    protected final int y;

    public MaterialVertexConsumer(VertexConsumer delegate, Material material) {
        this.delegate = delegate;
        Sprite sprite = MiapiClient.materialAtlasManager.getMaterialSprite(material.getSpriteId());
        if (sprite != null) {
            //Miapi.LOGGER.error(material.getKey() + " " + sprite.getY());
            this.x = sprite.getX();
            this.y = sprite.getY();
        } else {
            this.x = 0;
            this.y = 0;
        }
        //RegistryInventory.Client.entityTranslucentMaterialShader.getUniformOrDefault("materialUV").set(x,y);
        //RegistryInventory.Client.entityTranslucentMaterialShader.markUniformsDirty();
    }

    @Override
    public void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
        this.quad(matrixEntry, quad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, x, y, blue, new int[]{light, light, light, light}, x + y * (2 ^ 16) * 0, false);
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return delegate.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return delegate.color(x, y, blue, alpha);
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return delegate.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return delegate.overlay(x, y);
    }

    @Override
    public VertexConsumer overlay(int v) {
        return delegate.overlay(x, y);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return delegate.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return delegate.normal(x, y, z);
    }

    @Override
    public void next() {
        delegate.next();
    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {
        delegate.fixedColor(red, green, blue, alpha);
    }

    @Override
    public void unfixColor() {
        delegate.unfixColor();
    }
}
