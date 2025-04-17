package test.SwingTest;

import javax.swing.*;
import java.awt.*;

public class GridBagLayoutDemo extends JFrame {

    private final JButton button1;
    private final JButton button2;
    private final JButton button3;
    private final JButton button4;
    private final JButton button5;


    public GridBagLayoutDemo() {
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());


        GridBagConstraints gbc = new GridBagConstraints();

        button1 = new JButton("Button 1");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(button1, gbc);

        button2 = new JButton("Button 2");
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.gridheight = 5;
        gbc.gridx = 1;
        gbc.gridy = 1;
        button2.add(Box.createVerticalStrut(100));
        add(button2, gbc);

        button3 = new JButton("Button 3");
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weighty = 1;
        add(button3, gbc);

        button4 = new JButton("Button 4");
        gbc.gridx = 4;
        gbc.gridy = 5;
        add(button4, gbc);

        button5 = new JButton("Button 5");
        gbc.gridx = 5;
        gbc.gridy = 5;
        add(button5, gbc);

        setVisible(true);
    }

}
