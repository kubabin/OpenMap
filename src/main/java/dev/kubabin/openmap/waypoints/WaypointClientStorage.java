package dev.kubabin.openmap.waypoints;

import dev.kubabin.openmap.CachedTile;
import dev.kubabin.openmap.Openmap;
import dev.kubabin.openmap.waypoints.networking.CreateWaypointPayload;
import dev.kubabin.openmap.waypoints.networking.DeleteWaypointPayload;
import dev.kubabin.openmap.waypoints.networking.WaypointSyncPayload;
import net.minecraft.nbt.*;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// For storing waypoints on the client, when OpenMap isn't installed on the server.
public class WaypointClientStorage {
    private static List<Waypoint> waypoints;
    public static boolean isClientSide = true;
    public static void loadWaypoints(){
        if (!isClientSide) return;
        File waypointsFile = new File(CachedTile.getWorldDataDir().resolve("waypoints.dat").toUri());
        try (InputStream stream = new FileInputStream(waypointsFile)){
            CompoundTag waypointsTag = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            waypoints = new ArrayList<>();
            ListTag waypointsList = waypointsTag.getList("waypoints", ListTag.TAG_COMPOUND);
            for (Tag tag : waypointsList){
                waypoints.add(Waypoint.fromTag((CompoundTag) tag));
            }
        } catch (Exception e) {
            Openmap.LOGGER.error("Error loading waypoints: {}", e.getMessage());
        }
    }
    public static void saveWaypoints(){
        if (!isClientSide) return;
        if (waypoints == null) return;
        File waypointsFile = new File(CachedTile.getWorldDataDir().resolve("waypoints.dat").toUri());
        try (OutputStream stream = new FileOutputStream(waypointsFile)){
            CompoundTag waypointsTag = new CompoundTag();
            ListTag waypointsList = new ListTag();
            for (Waypoint waypoint : waypoints){
                waypointsList.add(waypoint.toTag());
            }
            waypointsTag.put("waypoints", waypointsList);
            NbtIo.writeCompressed(waypointsTag, stream);
        } catch (Exception e) {
            Openmap.LOGGER.error("Error saving waypoints: {}", e.getMessage());
        }
    }

    public static List<Waypoint> getWaypoints() {
        return waypoints;
    }
    public static void addWaypoint(Waypoint waypoint){
        if (isClientSide){
            waypoints.add(waypoint);
        } else {
            PacketDistributor.sendToServer(CreateWaypointPayload.from(waypoint));
        }
    }
    public static void deleteWaypoint(UUID uuid){
        if (isClientSide){
            for (int i = 0; i < waypoints.size(); i++){
                Waypoint waypoint = waypoints.get(i);
                if (waypoint.uuid().equals(uuid)){
                    waypoints.remove(i);
                    break;
                }
            }
        } else {
            PacketDistributor.sendToServer(new DeleteWaypointPayload(uuid));
        }
    }
    public static void handleWaypointSync(final WaypointSyncPayload payload,
                                          final IPayloadContext context){
        context.enqueueWork(() -> {
            waypoints = payload.waypoints();
        });
    }
}
