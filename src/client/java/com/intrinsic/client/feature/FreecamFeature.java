package com.intrinsic.client.feature;

import com.intrinsic.client.IntrinsicClient;
import com.intrinsic.client.config.IntrinsicConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class FreecamFeature {
    private static final float BASE_SPEED = 7.0f;
    private static final float SPRINT_MULT = 3.0f;
    private static final double SMOOTHING_STIFFNESS = 18.0;
    private static final double DT_CLAMP_MIN = 1.0 / 240.0;
    private static final double DT_CLAMP_MAX = 0.1;
    private static final double EMERGENCY_TP_DISTANCE_SQR = 400.0 * 400.0;

    private static boolean active = false;
    private static Vec3 cameraPos = Vec3.ZERO;
    private static Vec3 velocity = Vec3.ZERO;
    private static float cameraYaw = 0f;
    private static float cameraPitch = 0f;

    private static CameraType savedCameraType = CameraType.FIRST_PERSON;
    private static long lastFrameNs = 0L;

    private FreecamFeature() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(FreecamFeature::tick);
    }

    public static boolean isActive() { return active; }
    public static Vec3 cameraPos() { return cameraPos; }
    public static float cameraYaw() { return cameraYaw; }
    public static float cameraPitch() { return cameraPitch; }

    public static void applyMouseLook(double deltaX, double deltaY) {
        if (!active) return;
        double sensSetting = Minecraft.getInstance().options.sensitivity().get();
        double sens = sensSetting * 0.6 + 0.2;
        double factor = sens * sens * sens * 8.0 * 0.15;
        cameraYaw = Mth.wrapDegrees(cameraYaw + (float) (deltaX * factor));
        cameraPitch = Mth.clamp(cameraPitch + (float) (deltaY * factor), -90f, 90f);
    }

    public static void bumpSpeed(double notches) {
        if (notches == 0) return;
        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        double mult = cfg.freecamSpeedMultiplier * Math.pow(1.1, notches);
        cfg.freecamSpeedMultiplier = Mth.clamp(mult, cfg.freecamMinMult, cfg.freecamMaxMult);
        cfg.save();
    }

    private static void enter(Minecraft client, LocalPlayer player) {
        cameraPos = player.getEyePosition(1.0f);
        velocity = Vec3.ZERO;
        cameraYaw = player.getYRot();
        cameraPitch = player.getXRot();

        // Deliberately do NOT touch the player body: no delta zeroing, no
        // stopFallFlying, no position anchor packet. The body must keep
        // behaving normally (gravity, inertia decay, elytra gliding) so
        // server-side anti-cheats don't see the position freeze that used
        // to happen under the travel-cancel implementation. Inputs are
        // muted via KeyboardInputFreecamMixin, so the body won't react to
        // keys the user presses for the camera — it just decays to rest
        // under natural physics.

        savedCameraType = client.options.getCameraType();
        client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        lastFrameNs = 0L;
        active = true;
    }

    private static void exit(Minecraft client) {
        active = false;
        velocity = Vec3.ZERO;
        client.options.setCameraType(savedCameraType);
    }

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        boolean want = IntrinsicClient.getConfig().freecamEnabled
                && player != null && client.level != null;

        if (want && !active) enter(client, player);
        else if (!want && active) exit(client);
    }

    public static void onRenderFrame() {
        if (!active) return;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level == null) return;

        long now = System.nanoTime();
        double dt = lastFrameNs == 0L ? 1.0 / 60.0 : (now - lastFrameNs) / 1.0e9;
        lastFrameNs = now;
        dt = Mth.clamp(dt, DT_CLAMP_MIN, DT_CLAMP_MAX);

        Vec3 eye = player.getEyePosition(1.0f);
        if (cameraPos.distanceToSqr(eye) > EMERGENCY_TP_DISTANCE_SQR) {
            cameraPos = eye;
            velocity = Vec3.ZERO;
            return;
        }

        if (client.screen != null) {
            velocity = velocity.scale(1.0 - Math.min(1.0, SMOOTHING_STIFFNESS * dt));
            cameraPos = cameraPos.add(velocity.scale(dt));
            return;
        }

        double fwd = 0, strafe = 0, vertical = 0;
        var opts = client.options;
        if (opts.keyUp.isDown())    fwd += 1;
        if (opts.keyDown.isDown())  fwd -= 1;
        if (opts.keyLeft.isDown())  strafe += 1;
        if (opts.keyRight.isDown()) strafe -= 1;
        if (opts.keyJump.isDown())  vertical += 1;
        if (opts.keyShift.isDown()) vertical -= 1;

        IntrinsicConfig cfg = IntrinsicClient.getConfig();
        double speed = BASE_SPEED * cfg.freecamSpeedMultiplier;
        if (opts.keySprint.isDown()) speed *= SPRINT_MULT;

        double yawRad = Math.toRadians(cameraYaw);
        double sinY = Math.sin(yawRad);
        double cosY = Math.cos(yawRad);
        double tx = (-sinY * fwd + cosY * strafe) * speed;
        double tz = ( cosY * fwd + sinY * strafe) * speed;
        double ty = vertical * speed;
        Vec3 target = new Vec3(tx, ty, tz);

        double blend = 1.0 - Math.exp(-SMOOTHING_STIFFNESS * dt);
        velocity = velocity.add(target.subtract(velocity).scale(blend));
        if (velocity.lengthSqr() < 1.0e-8) velocity = Vec3.ZERO;

        cameraPos = cameraPos.add(velocity.scale(dt));
    }
}
