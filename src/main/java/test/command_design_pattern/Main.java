package test.command_design_pattern;

public class Main {
    public static void main(String[] args) {
        Light light = new Light();

        LightOnCommand lightOnCommand = new LightOnCommand(light);
        LightOffCommand lightOffCommand = new LightOffCommand(light);
        Switch lightSwitch = new Switch(lightOnCommand);

        lightSwitch.pressButton();
        lightSwitch.pressUndo();
        lightSwitch.setCommand(lightOffCommand);
        lightSwitch.pressButton();
        lightSwitch.pressUndo();


    }
}
