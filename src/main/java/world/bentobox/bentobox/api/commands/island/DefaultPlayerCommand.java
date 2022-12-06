package world.bentobox.bentobox.api.commands.island;

import java.util.Collections;
import java.util.List;

import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * This is default player command class. It contains all necessary parts for main /[gamemode] command.
 * @since 1.13.0
 * @author BONNe
 */
public abstract class DefaultPlayerCommand extends CompositeCommand {

    /**
     * This is the top-level command constructor for commands that have no parent.
     *
     * @param addon   - GameMode addon
     */
    protected DefaultPlayerCommand(GameModeAddon addon) {
        // Register command with alias from config.
        super(addon,
                addon.getWorldSettings().getPlayerCommandAliases().split(" ")[0],
                addon.getWorldSettings().getPlayerCommandAliases().split(" "));
    }

    /**
     * Setups anything that is necessary for default main user command.
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#setup()
     */
    @Override
    public void setup() {
        // Description
        this.setDescription("commands.island.help.description");
        // Limit to player
        this.setOnlyPlayer(true);
        // Permission
        this.setPermission("island");

        // Set up default subcommands

    }


    /**
     * Defines what will be executed when this command is run.
     * @see world.bentobox.bentobox.api.commands.BentoBoxCommand#execute(User, String, List)
     */
    @Override
    public boolean execute(User user, String label, List<String> args) {
        if (user == null) {
            return false;
        }

        if (!args.isEmpty()) {
            user.sendMessage("general.errors.unknown-command", TextVariables.LABEL, this.getTopLabel());
            return false;
        }

        // Check if user has an island.
        if (this.getIslands().getIsland(this.getWorld(), user.getUniqueId()) != null) {
            // Default command if user has an island.
            return runCommand(user, label, this.<GameModeAddon>getAddon().getWorldSettings().getDefaultPlayerAction(), "go");
        } else {
            // Default command if user does not have an island.
            return runCommand(user, label, this.<GameModeAddon>getAddon().getWorldSettings().getDefaultNewPlayerAction(), "create");
        }
    }

    private boolean runCommand(User user, String label, String command, String defaultSubCommand) {
        if (command == null || command.isEmpty()) {
            command = defaultSubCommand;
        }
        // Call sub command or perform command if it does not exist
        if (this.getSubCommand(command).isPresent()) {
            return this.getSubCommand(command).
                    map(c -> c.call(user, c.getLabel(), Collections.emptyList())).
                    orElse(false);
        } else {
            // Command is not a known sub command - try to perform it directly - some plugins trap these commands, like Deluxe menus
            if (command.startsWith("/")) {
                // If commands starts with Slash, don't append the prefix
                return user.performCommand(command.substring(1));
            } else {
                return user.performCommand(label + " " + command);
            }
        }
    }
}