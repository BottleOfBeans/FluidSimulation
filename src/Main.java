import javax.swing.*;

public class Main {
    public static void main(String args[]) {
        
        
        /*
         * Basic Window Setup
         */
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(true);
                
        /*
         * Window Title
         */
        window.setTitle("Fluid Simulation");
        
        /*
         * Window Initizilation
         */
        GameWindow gameWindow = new GameWindow();
        window.add(gameWindow);
        window.setUndecorated(true);
        

        /*
         * Visual Loop Start
         */
        window.pack();
        window.setVisible(true);
        gameWindow.startWindowThread();
    }

}
