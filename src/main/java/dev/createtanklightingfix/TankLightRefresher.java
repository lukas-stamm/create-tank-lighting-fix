package dev.createtanklightingfix;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import dev.createtanklightingfix.mixin.FluidTankBlockEntityAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

final class TankLightRefresher {
	private TankLightRefresher() {}

	static void refreshChunk(ServerLevel level, LevelChunk chunk) {
		boolean refreshed = false;
		for (var blockEntity : chunk.getBlockEntities().values()) {
			if (!(blockEntity instanceof FluidTankBlockEntity tank) || !tank.isController()) {
				continue;
			}
			if (refreshController(level, tank)) {
				refreshed = true;
			}
		}
		if (refreshed) {
			level.getChunkSource().getLightEngine().propagateLightSources(chunk.getPos());
			CreateTankLightingFix.LOGGER.debug("Refreshed tank lights in chunk {}", chunk.getPos());
		}
	}

	static boolean refreshController(ServerLevel level, FluidTankBlockEntity controller) {
		if (controller.isRemoved() || controller.getLevel() != level || !controller.isController()) {
			return false;
		}

		FluidTankBlockEntityAccessor access = (FluidTankBlockEntityAccessor) controller;
		boolean window = access.createTankLightingFix$hasWindow();

		access.createTankLightingFix$onFluidStackChanged(controller.getFluid(0));

		if (window) {
			controller.setWindows(true);
		} else if (access.createTankLightingFix$getLuminosity() > 0) {
			BlockPos pos = controller.getBlockPos();
			level.getChunkSource().getLightEngine().checkBlock(pos);
		} else {
			return false;
		}

		CreateTankLightingFix.LOGGER.debug(
			"Refreshing tank light at {} (window={}, luminosity={}, fluid={})",
			controller.getBlockPos(),
			window,
			access.createTankLightingFix$getLuminosity(),
			controller.getFluid(0).getHoverName().getString()
		);
		return true;
	}
}
