import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class GameWindow extends JPanel implements Runnable {

    /*
     * Alterable Values!
     */
    static int gameWidth = 1000; // gameColumnAmount*ActualTileSize;
    static int gameHeight = 1000; // gameRowAmount*ActualTileSize;

    
    int FPS = 144;

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

    public void updateDisplay(Graphics2D graphics){
        
        BufferedImage buffer = new BufferedImage(gameWidth, gameHeight, BufferedImage.TYPE_INT_ARGB);

        for(int i = 0; i < l.getHeight(); i++){ // Displaying all the nice little cells!
            for(int j = 0; j < l.getWidth(); j++){
                graphics.setColor(l.getColor(i,j));
                graphics.fill(l.getCell(i, j));

                // //Grid Lines
                // graphics.setColor(Color.BLACK);
                // graphics.draw(l.getCell(i,j));
                
                // //Velocity Feild
                // graphics.setColor(Color.WHITE);
                // graphics.draw(l.getHorizontalLine(i, j));
                // graphics.draw(l.getVerticalLine(i, j));

            }
        }
        graphics.drawImage(buffer, 0, 0, null);
        //l.drawStreamlines(graphics);

    }

    public void update(double dt){      
        //Update the Cell
        l.updateLiquid(deltaTime / FPS);
    }

    // Function that paints the updated version of the frame {FPS} times a second.
    public void paintComponent(Graphics g) {

        // Quick definition of varibles to use with the G2D library
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;

        //Sets 0,0  to the bottom left
        graphics.scale(1, -1);
        graphics.translate(0, -getHeight());


        //Do Things!
        updateDisplay(graphics);

        graphics.dispose();
    }

}