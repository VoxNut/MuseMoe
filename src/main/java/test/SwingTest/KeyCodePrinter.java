package test.SwingTest;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyCodePrinter extends JFrame implements KeyListener {

    public KeyCodePrinter() {
        setTitle("KeyCode Printer");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addKeyListener(this);
        setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // This method can be left empty
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        System.out.println("Key Pressed: " + keyChar + " | KeyCode: " + keyCode);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // This method can be left empty
    }

    public static void main(String[] args) {
        new KeyCodePrinter();
    }
}

