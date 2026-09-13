package net.vas.rdpcore.client;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.vas.rdpcore.config.RDPConfig;
import net.vas.rdpcore.cosmology.CelestialObjectDefinition;
import net.vas.rdpcore.cosmology.CosmologyManager;
import net.vas.rdpcore.cosmology.Phase;
import org.lwjgl.opengl.GL11;

/**
 * World-space extension layer. It deliberately runs after vanilla sky rendering
 * rather than replacing WorldProvider.skyRenderer, preserving Celeritas and
 * other provider-owned sky implementations.
 */
public final class CosmologyRenderer {
    private static final int LAT = 8, LON = 12;
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private long lastWorldTime = Long.MIN_VALUE;
    private double transition;

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if (!RDPConfig.COSMOLOGY_CLIENT_RENDERING || minecraft.world == null || minecraft.player == null) return;
        World world = minecraft.world;
        boolean day = world.isDaytime();
        List<CelestialObjectDefinition> objects = CosmologyManager.active(world, day);
        long time = world.getTotalWorldTime();
        if (time != lastWorldTime) {
            transition = Math.min(1.0D, transition + 1.0D / Math.max(1.0D, RDPConfig.COSMOLOGY_TRANSITION_SECONDS * 20.0D));
            lastWorldTime = time;
        }
        if (objects.isEmpty() || transition <= 0.0D) return;

        double px = minecraft.player.lastTickPosX + (minecraft.player.posX - minecraft.player.lastTickPosX) * event.getPartialTicks();
        double py = minecraft.player.lastTickPosY + (minecraft.player.posY - minecraft.player.lastTickPosY) * event.getPartialTicks();
        double pz = minecraft.player.lastTickPosZ + (minecraft.player.posZ - minecraft.player.lastTickPosZ) * event.getPartialTicks();
        GlStateManager.pushMatrix();
        GlStateManager.translate(-px, -py, -pz);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.depthMask(false);
        int count = 0;
        try {
            for (CelestialObjectDefinition object : objects) {
                if (count++ >= Math.max(0, RDPConfig.COSMOLOGY_MAX_OBJECTS)) break;
                drawObject(object, px, py, pz, world.getTotalWorldTime() + event.getPartialTicks());
            }
        } finally {
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }

    private void drawObject(CelestialObjectDefinition d, double px, double py, double pz, double ticks) {
        double az = Math.toRadians(d.azimuth), el = Math.toRadians(d.elevation);
        double distance = Math.max(64.0D, d.maxDistance);
        double x = px + Math.cos(el) * Math.sin(az) * distance;
        double y = py + Math.sin(el) * distance;
        double z = pz + Math.cos(el) * Math.cos(az) * distance;
        float radius = (float) (Math.tan(Math.toRadians(d.angularSize) * 0.5D) * distance * d.scale);
        if (radius <= 0.0F) return;
        minecraft.getTextureManager().bindTexture(d.texture);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate((float) (d.rotationSpeed * ticks), 0.0F, 1.0F, 0.0F);
        drawSphere(radius, d.brightness, d.alpha * transition, d.phase);
        GlStateManager.popMatrix();
    }

    private void drawSphere(float radius, double brightness, double alpha, double phase) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        double illumination = Math.max(0.0D, Math.min(1.0D, phase));
        for (int lat = 0; lat < LAT; lat++) {
            double v0 = (double) lat / LAT, v1 = (double) (lat + 1) / LAT;
            double p0 = Math.PI * (v0 - 0.5D), p1 = Math.PI * (v1 - 0.5D);
            for (int lon = 0; lon < LON; lon++) {
                double u0 = (double) lon / LON, u1 = (double) (lon + 1) / LON;
                vertex(buffer, radius, p0, u0, brightness, alpha, illumination);
                vertex(buffer, radius, p1, u0, brightness, alpha, illumination);
                vertex(buffer, radius, p1, u1, brightness, alpha, illumination);
                vertex(buffer, radius, p0, u1, brightness, alpha, illumination);
            }
        }
        tessellator.draw();
    }

    private void vertex(BufferBuilder b, float radius, double latitude, double longitude,
                        double brightness, double alpha, double phase) {
        double c = Math.cos(latitude);
        double x = c * Math.cos(longitude * Math.PI * 2.0D);
        double y = Math.sin(latitude);
        double z = c * Math.sin(longitude * Math.PI * 2.0D);
        // A controlled terminator gives phases without requiring phase-specific PNGs.
        double light = Math.max(0.08D, (x * (phase * 2.0D - 1.0D) + 1.0D) * 0.5D);
        int color = (int) Math.max(0, Math.min(255, brightness * light * 255.0D));
        int a = (int) Math.max(0, Math.min(255, alpha * 255.0D));
        b.pos(x * radius, y * radius, z * radius).tex((longitude % 1.0D), (latitude + Math.PI * 0.5D) / Math.PI)
            .color(color, color, color, a).endVertex();
    }
}
