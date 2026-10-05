package dev.createtanklightingfix;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;

/**
 * Spawn/login chunks often get fixed before the client receives light data.
 * Chunks loaded while exploring are usually sent with stale light=0 first.
 *
 * Refresh tank light when a player starts watching a chunk (before/around send)
 * and again shortly after the chunk packet is sent, so later chunks win the race.
 */
public final class TankLightReloadHandler {
	private TankLightReloadHandler() {}

	@SubscribeEvent
	public static void onChunkWatch(ChunkWatchEvent.Watch event) {
		scheduleRefresh(event.getLevel(), event.getChunk(), 1);
	}

	@SubscribeEvent
	public static void onChunkSent(ChunkWatchEvent.Sent event) {
		// After the client already got the chunk (possibly with light=0),
		// force another server refresh so light update packets follow.
		scheduleRefresh(event.getLevel(), event.getChunk(), 1);
		scheduleRefresh(event.getLevel(), event.getChunk(), 5);
		scheduleRefresh(event.getLevel(), event.getChunk(), 15);
	}

	private static void scheduleRefresh(ServerLevel level, LevelChunk chunk, int delayTicks) {
		if (!chunkHasTank(chunk)) {
			return;
		}

		level.getServer().tell(new TickTask(level.getServer().getTickCount() + delayTicks, () -> {
			LevelChunk current = level.getChunkSource().getChunkNow(chunk.getPos().x, chunk.getPos().z);
			if (current == null) {
				return;
			}
			TankLightRefresher.refreshChunk(level, current);
		}));
	}

	private static boolean chunkHasTank(LevelChunk chunk) {
		for (var blockEntity : chunk.getBlockEntities().values()) {
			if (blockEntity instanceof FluidTankBlockEntity) {
				return true;
			}
		}
		return false;
	}
}
