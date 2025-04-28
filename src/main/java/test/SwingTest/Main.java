package test.SwingTest;

import javax.swing.*;

public class Main extends JFrame {


    public Main(String title) {
        super(title);
        setSize(600, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JPanel panel = new CavaVisualizerPanel();
        add(panel);

        setVisible(true);

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main("Cava test");
        });
    }

}
