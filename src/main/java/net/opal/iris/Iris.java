package net.opal.iris;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.opal.iris.client.IrisClientEventRegistration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

import net.opal.iris.option.ConfigOptions;
import net.opal.iris.option.ConfigReloader;
import net.opal.iris.network.IrisNetworkHandler;
import net.opal.iris.server.backitem.BackShieldProtection;

@Mod(Iris.MODID)
public class Iris {
    public static final String MODID = "iris";
    public static final Logger LOGGER = LogManager.getLogger();

    public Iris(IEventBus modEventBus) {
        ConfigOptions.load();

        NeoForge.EVENT_BUS.addListener(ConfigReloader::register);
        NeoForge.EVENT_BUS.register(BackShieldProtection.class);
        modEventBus.register(IrisNetworkHandler.class);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            IrisClientEventRegistration.register(modEventBus);
        }
    }
}
