package dev.createtanklightingfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.neoforged.neoforge.fluids.FluidStack;

@Mixin(FluidTankBlockEntity.class)
public interface FluidTankBlockEntityAccessor {
	@Accessor("window")
	boolean createTankLightingFix$hasWindow();

	@Accessor("luminosity")
	int createTankLightingFix$getLuminosity();

	@Invoker("onFluidStackChanged")
	void createTankLightingFix$onFluidStackChanged(FluidStack newFluidStack);
}
