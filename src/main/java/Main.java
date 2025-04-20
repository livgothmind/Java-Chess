package src.main.java;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class Main extends JPanel {

    // Avvio la GUI --> main
    public static void main(String[] args) {
        JFrame frame = new JFrame("Java-Chess");
        GameGUI gui = new GameGUI();
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(gui);
        frame.setVisible(true);
    }
}
