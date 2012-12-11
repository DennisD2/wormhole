package de.spurtikus.softtek;

/**
 * Interface defining the application's command IDs.
 * Key bindings can be defined for specific commands.
 * To associate an action with a command, use IAction.setActionDefinitionId(commandId).
 *
 * @see org.eclipse.jface.action.IAction#setActionDefinitionId(String)
 */
public interface ICommandIds {

    public static final String CMD_OPEN = "softtek.open";
    public static final String CMD_OPEN_MESSAGE = "softtek.openMessage";

    public static final String CMD_SAVE = "softtek.save";
    public static final String CMD_SAVE_MESSAGE = "softtek.saveMessage";
    
}
