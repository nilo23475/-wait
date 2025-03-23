package com.example.waitcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WaitCommandMod implements ModInitializer {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerWaitCommand(dispatcher));
    }

    private void registerWaitCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("wait")
                .then(CommandManager.argument("milliseconds", IntegerArgumentType.integer(0))
                        .then(CommandManager.argument("command", StringArgumentType.greedyString())
                                .executes(context -> {
                                    int delay = IntegerArgumentType.getInteger(context, "milliseconds");
                                    String command = StringArgumentType.getString(context, "command");
                                    ServerCommandSource source = context.getSource();
                                    MinecraftServer server = source.getServer();

                                    scheduler.schedule(() -> {
                                        server.getCommandManager().executeWithPrefix(source, command);
                                    }, delay, TimeUnit.MILLISECONDS);

                                    source.sendFeedback(Text.literal("Command scheduled to run in " + delay + " ms"), false);
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
