package test.command_design_pattern;

public class Switch {

    private Command command;

    public Switch(Command command) {
        this.command = command;
    }

    public void pressButton() {
        command.execute();
    }

    public void pressUndo() {
        command.undo();
    }

    public void setCommand(Command command) {
        this.command = command;
    }
}
