import java.awt.*;
import javax.swing.*;

public class GameWindow extends JPanel implements Runnable {

    /*
     * Alterable Values!
     */
    static int gameWidth = 1000; // gameColumnAmount*ActualTileSize;
    static int gameHeight = 1000; // gameRowAmount*ActualTileSize;

    
    int FPS = 1;

    /*
     *  FUN STUFF!
     */

    //Establishes Liquid!
    static Liquid l = new Liquid();


    // DO NOT TOUCH!
    double deltaTime = 0;
    Thread gameThread;

    
    // Creating the game windows and setting up the settings
    public GameWindow() {
        this.setPreferredSize(new Dimension(gameWidth, gameHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
    }

    // Starting thread, managing frame updates
    public void startWindowThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {

        // Managing updates and FPS
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null) {

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;

            lastTime = currentTime;
            if (delta >= 1) {
                update(delta); // Sending the deltaTime through the update function
                deltaTime = delta;
                repaint();
                delta--;
            }

        }
    }

    public void update(double dt){

        l.updateCells(dt);

    }

    // Function that paints the updated version of the frame {FPS} times a second.
    public void paintComponent(Graphics g) {

        // Quick definition of varibles to use with the G2D library
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;

        //Do Things!
        l.updateCellDisplay(graphics);
        l.drawVelocityLines(graphics);

        graphics.dispose();
    }

}
//hi