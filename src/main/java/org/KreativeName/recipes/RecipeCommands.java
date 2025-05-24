package org.KreativeName.recipes;


import org.KreativeName.recipes.handlers.*;
import org.KreativeName.recipes.utils.TabCompletionHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RecipeCommands implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final AddCommandHandler addHandler;
    private final RemoveCommandHandler removeHandler;
    private final ListCommandHandler listHandler;
    private final ReloadCommandHandler reloadHandler;
    private final HelpCommandHandler helpHandler;
    private final TabCompletionHelper tabHelper;

    public RecipeCommands(JavaPlugin plugin) {
        this.plugin = plugin;
        this.addHandler = new AddCommandHandler(plugin);
        this.removeHandler = new RemoveCommandHandler(plugin);
        this.listHandler = new ListCommandHandler(plugin);
        this.reloadHandler = new ReloadCommandHandler(plugin);
        this.helpHandler = new HelpCommandHandler();
        this.tabHelper = new TabCompletionHelper();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            helpHandler.handle(sender, args);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                addHandler.handle(sender, args);
                break;
            case "remove":
                removeHandler.handle(sender, args);
                break;
            case "list":
                listHandler.handle(sender, args);
                break;
            case "reload":
                reloadHandler.handle(sender, args);
                break;
            case "help":
                helpHandler.handle(sender, args);
                break;
            default:
                sender.sendMessage("§cUnknown subcommand. Use /cr help for a list of commands.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return tabHelper.getCompletions(sender, args);
    }
}