package dev.kubabin.openmap.waypoints;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class WaypointSavedData extends SavedData {
    private static final String DATA_NAME = "openmap_waypoints";

    private final Map<UUID, List<Waypoint>> waypoints = new HashMap<>();

    public WaypointSavedData() {
    }

    public static WaypointSavedData load(CompoundTag tag) {
        WaypointSavedData data = new WaypointSavedData();

        ListTag players = tag.getList("players", Tag.TAG_COMPOUND);

        for (Tag playerTag : players) {
            CompoundTag player = (CompoundTag) playerTag;

            UUID uuid = player.getUUID("uuid");
            List<Waypoint> playerWaypoints = new ArrayList<>();

            ListTag waypointList =
                    player.getList("waypoints", Tag.TAG_COMPOUND);

            for (Tag waypointTag : waypointList) {
                CompoundTag waypoint = (CompoundTag) waypointTag;

                playerWaypoints.add(new Waypoint(
                        waypoint.getDouble("x"),
                        waypoint.getDouble("y"),
                        waypoint.getDouble("z"),
                        waypoint.getString("name"),
                        waypoint.getString("icon"),
                        waypoint.getUUID("uuid")
                ));
            }

            data.waypoints.put(uuid, playerWaypoints);
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag players = new ListTag();

        for (Map.Entry<UUID, List<Waypoint>> entry : waypoints.entrySet()) {
            CompoundTag player = new CompoundTag();

            player.putUUID("uuid", entry.getKey());

            ListTag waypointList = new ListTag();

            for (Waypoint waypoint : entry.getValue()) {
                CompoundTag waypointTag = new CompoundTag();

                waypointTag.putDouble("x", waypoint.x());
                waypointTag.putDouble("y", waypoint.y());
                waypointTag.putDouble("z", waypoint.z());
                waypointTag.putString("name", waypoint.name());
                waypointTag.putString("icon", waypoint.icon().toString());
                waypointTag.putUUID("uuid", waypoint.uuid());

                waypointList.add(waypointTag);
            }

            player.put("waypoints", waypointList);
            players.add(player);
        }

        tag.put("players", players);

        return tag;
    }

    public static WaypointSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        WaypointSavedData::new,
                        (tag, provider) -> WaypointSavedData.load(tag),
                        null
                ),
                DATA_NAME
        );
    }

    public List<Waypoint> getWaypoints(UUID playerId) {
        return waypoints.getOrDefault(playerId, List.of());
    }

    public void addWaypoint(UUID playerId, Waypoint waypoint) {
        waypoints
                .computeIfAbsent(playerId, ignored -> new ArrayList<>())
                .add(waypoint);

        setDirty();
    }

    public boolean removeWaypoint(UUID playerId, Waypoint waypoint) {
        List<Waypoint> playerWaypoints = waypoints.get(playerId);

        if (playerWaypoints == null) {
            return false;
        }

        boolean removed = playerWaypoints.remove(waypoint);

        if (removed) {
            setDirty();

            if (playerWaypoints.isEmpty()) {
                waypoints.remove(playerId);
            }
        }

        return removed;
    }

    public void clearWaypoints(UUID playerId) {
        if (waypoints.remove(playerId) != null) {
            setDirty();
        }
    }
}