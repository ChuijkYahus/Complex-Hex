package dev.kineticcat.complexhex.client.render.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import dev.kineticcat.complexhex.entity.HoldoutEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static dev.kineticcat.complexhex.Complexhex.id;

public class HoldoutRenderer extends EntityRenderer<HoldoutEntity> {

    public static final ResourceLocation tex = id("textures/entity/holdout.png");

    public HoldoutRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull HoldoutEntity entity) {return tex;}

    @Override
    public void render(HoldoutEntity holdout, float yaw, float partialTick, PoseStack ps, MultiBufferSource multiBufferSource, int packedLight) {
        int colour = 0xffffffff;
        int black = 0;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);



        float inner = 1f;
//        float hitboxSize = 4f / 16f;

        Vector3f innerSize = new Vector3f(inner, inner, inner);

        ps.pushPose();

        ps.mulPose(Axis.YP.rotationDegrees(180f - yaw));
//        ps.mulPose(Axis.XP.rotationDegrees(180 - nix.getXRot()));
        ps.mulPose(Axis.ZP.rotationDegrees(180f));

        int light = LevelRenderer.getLightColor(holdout.level(), BlockPos.containing(holdout.position()));

        drawCube(holdout, innerSize, new Vector3f(-innerSize.x / 2f, -innerSize.y / 2f, innerSize.z / 2f), ps, multiBufferSource, light, black);

        ps.popPose();
        super.render(holdout, yaw, partialTick, ps, multiBufferSource, packedLight);
    }

    private static void vertex(
            Matrix4f mat,
            Matrix3f normal,
            int light,
            VertexConsumer verts,
            int colour,
            float x, float y, float z,
            float u, float v,
            float nx, float ny, float nz) {
        verts.vertex(mat, x, y, z)
                .color(colour)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
//                .normal(normal, nx, ny, nz)
                .endVertex();
    }

    private void drawCube(
            HoldoutEntity holdout,
            Vector3f vec,
            Vector3f translate,
            PoseStack ps,
            MultiBufferSource buffer,
            int light,
            int colour
    ) {
        ps.pushPose();
        // X is right, Y is down, Z is *in*
        // Our origin will be the lower-left corner of the scroll touching the wall
        // (so it has "negative" thickness)
        ps.translate(translate.x, translate.y, translate.z);

        float dx = vec.x, dy = vec.y, dz = -vec.z;
        // inverseHull ? 0 : vec.x
        // inverseHull ? 0 : vec.y
        // inverseHull ? 0 : -vec.z
        var last = ps.last();
        var mat = last.pose();
        var norm = last.normal();

        var verts = buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(holdout)));
//        var verts = buffer.getBuffer(holdout());

        // Remember: CCW
        // Top face
        vertex(mat, norm, light, verts, colour, 0, 0, 0, 0, 0, 0, -1, 0);
        vertex(mat, norm, light, verts, colour, 0, 0, dz, 0, 1, 0, -1, 0);
        vertex(mat, norm, light, verts, colour, dx, 0, dz, 1, 1, 0, -1, 0);
        vertex(mat, norm, light, verts, colour, dx, 0, 0, 1, 0, 0, -1, 0);
        ps.popPose();
    }

    public RenderType holdout() {
        return HOLDOUT;
    }

    private static final RenderStateShard.TransparencyStateShard HOLDOUT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("additive_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ZERO);
    }, () -> {});

    private static final RenderType HOLDOUT = RenderType.create(
            id("holdout").toString(),
            DefaultVertexFormat.BLIT_SCREEN,
            VertexFormat.Mode.QUADS,
            256,
            RenderType.CompositeState.builder()
                    .setTransparencyState(HOLDOUT_TRANSPARENCY)
                    .createCompositeState(false)
    );
}
