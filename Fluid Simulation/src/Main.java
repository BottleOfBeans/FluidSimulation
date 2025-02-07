
import javax.swing.*;

public class Main { 
    public static void main(String args[]) throws Exception {
        /*
         * Setting up the JFrame Window
         * Resizeable --> False
         * Close Operation --> Exit On Close
         * Window Name --> "Romir's Silly Goofy Little Game Thing :)"
         * Window Visibility --> True
         */
        JFrame window = new JFrame();
        
        // Closing the window on the X button click
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // Window can be adjusted
        window.setResizable(false);

        // Window Title
        window.setTitle("Fluid Simulation");
        // Addding the window to the window?


        GameWindow gameWindow = new GameWindow();
        window.add(gameWindow);
        // No Header (Borderless)
        //window.setUndecorated(true);

        // Pack, creating and starting the FPS loop
        window.pack();
        window.setVisible(true);
        gameWindow.startWindowThread();
    }

}
