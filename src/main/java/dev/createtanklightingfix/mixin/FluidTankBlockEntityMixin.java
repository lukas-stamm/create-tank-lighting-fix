package dev.createtanklightingfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Create restores luminosity from NBT but never refreshes the server light
 * engine after chunk reload. Retries after light data is applied so chunks
 * loaded while exploring are not left with stale light=0.
 */
@Mixin(FluidTankBlockEntity.class)
public abstract class FluidTankBlockEntityMixin {
	@Unique
	private int createTankLightingFix$attempts;

	@Unique
	private boolean createTankLightingFix$done;

	@Inject(
		method = "setLuminosity",
		at = @At(
			value = "INVOKE",
			target = "Lcom/simibubi/create/content/fluids/tank/FluidTankBlockEntity;sendData()V"
		)
	)
	private void createTankLightingFix$refreshLightOnLuminosityChange(int luminosity, CallbackInfo ci) {
		FluidTankBlockEntity self = (FluidTankBlockEntity) (Object) this;
		Level level = self.getLevel();
		if (level == null || level.isClientSide()) {
			return;
		}
		level.getChunkSource().getLightEngine().checkBlock(self.getBlockPos());
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void createTankLightingFix$retryUntilLightReady(CallbackInfo ci) {
		if (createTankLightingFix$done) {
			return;
		}

		FluidTankBlockEntity self = (FluidTankBlockEntity) (Object) this;
		Level level = self.getLevel();
		if (level == null) {
			return;
		}

		createTankLightingFix$attempts++;

		if (level.isClientSide()) {
			// Client light packets can arrive after the BE; recheck a few times.
			level.getChunkSource().getLightEngine().checkBlock(self.getBlockPos());
			if (createTankLightingFix$attempts >= 20) {
				createTankLightingFix$done = true;
			}
			return;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		LevelChunk chunk = serverLevel.getChunkSource()
			.getChunkNow(self.getBlockPos().getX() >> 4, self.getBlockPos().getZ() >> 4);
		boolean lightReady = chunk != null && chunk.isLightCorrect();

		if (!lightReady && createTankLightingFix$attempts < 40) {
			return;
		}

		// Apply on a sparse schedule so we win late light-section overwrites
		// without calling setWindows every tick.
		int n = createTankLightingFix$attempts;
		boolean apply = n == 1 || n == 5 || n == 10 || n == 20 || n == 40;
		if (apply) {
			if (self.isController()) {
				FluidTankBlockEntityAccessor access = (FluidTankBlockEntityAccessor) self;
				access.createTankLightingFix$onFluidStackChanged(self.getFluid(0));
				if (access.createTankLightingFix$hasWindow()) {
					self.setWindows(true);
				} else {
					serverLevel.getChunkSource().getLightEngine().checkBlock(self.getBlockPos());
				}
			} else {
				serverLevel.getChunkSource().getLightEngine().checkBlock(self.getBlockPos());
			}
		}

		if (n >= 40) {
			createTankLightingFix$done = true;
		}
	}
}
