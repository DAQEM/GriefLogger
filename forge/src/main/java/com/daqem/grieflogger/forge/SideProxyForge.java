package com.daqem.grieflogger.forge;

import com.daqem.grieflogger.GriefLogger;
import com.daqem.grieflogger.command.argument.FilterArgument;
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class SideProxyForge {

    SideProxyForge() {
        EventBuses.registerModEventBus(GriefLogger.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        registerCommandArgumentTypes();
    }

    private void registerCommandArgumentTypes() {
        DeferredRegister<ArgumentTypeInfo<?, ?>> argTypeRegistry = DeferredRegister.create(GriefLogger.MOD_ID, Registries.COMMAND_ARGUMENT_TYPE);
        argTypeRegistry.register("filter", () -> ArgumentTypeInfos.registerByClass(FilterArgument.class, SingletonArgumentInfo.contextFree(FilterArgument::filter)));
        argTypeRegistry.register();
    }

    public static class Server extends SideProxyForge {
        Server() {
            GriefLogger.init();
        }
    }

    public static class Client extends SideProxyForge {

        Client() {
        }
    }
}
