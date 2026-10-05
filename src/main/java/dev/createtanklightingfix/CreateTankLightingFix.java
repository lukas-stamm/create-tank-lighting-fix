package dev.createtanklightingfix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CreateTankLightingFix.MOD_ID)
public class CreateTankLightingFix {
	public static final String MOD_ID = "createtanklightingfix";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public CreateTankLightingFix(IEventBus modBus) {
		NeoForge.EVENT_BUS.register(TankLightReloadHandler.class);
		LOGGER.info("Create Tank Lighting Fix loaded (event handler registered)");
	}
}
