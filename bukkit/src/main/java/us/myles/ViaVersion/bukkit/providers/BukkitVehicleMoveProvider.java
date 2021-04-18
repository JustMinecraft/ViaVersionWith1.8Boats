package us.myles.ViaVersion.bukkit.providers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.bukkit.util.NMSUtil;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.VehicleMoveProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

public class BukkitVehicleMoveProvider extends VehicleMoveProvider {

    // Use for nms
    private Method craftEntityHandle;
    private Method entitySetLocation;

    public BukkitVehicleMoveProvider() {
        setupReflection();
    }

    public void doVehicleMove(double x, double y, double z, float yaw, float pitch, UserConnection userConnection) {
        ProtocolInfo info = userConnection.getProtocolInfo();
        UUID uuid = info.getUuid();

        Via.getPlatform().runSync(() -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return;
            }

            Entity vehicle = player.getVehicle();
            if (vehicle == null) {
                return;
            }

            try {
                // Do ((CraftEntity) vehicle).getHandle().setLocation(x, y, z, yaw, pitch);
                Object entityPlayer = craftEntityHandle.invoke(vehicle);
                entitySetLocation.invoke(entityPlayer, x, y, z, yaw, pitch);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Via.getPlatform().getLogger().log(Level.SEVERE, "Failed to move player vehicle. Please report this issue to the ViaVersion Github: " + e.getMessage(), e);
            }
        });
    }

    private void setupReflection() {
        try {
            this.craftEntityHandle = NMSUtil.obc("entity.CraftEntity").getDeclaredMethod("getHandle");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find CraftEntity.getHandle", e);
        }
        try {
            this.entitySetLocation = NMSUtil.nms("Entity").getDeclaredMethod("setLocation", double.class, double.class, double.class, float.class, float.class);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException("Couldn't find Entity.setLocation", e);
        }
    }
}