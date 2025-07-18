package src.main.java;

import javax.swing.*;

public class Main extends JPanel {

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
