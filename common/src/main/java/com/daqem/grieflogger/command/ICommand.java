package com.daqem.grieflogger.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public interface ICommand {

    LiteralArgumentBuilder<CommandSourceStack> getCommand();
}
