import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javax.swing.*;

public class GameWindow extends JPanel implements Runnable {

    /*
     * Alterable Values!
     */
    static int gameWidth = 1000; // gameColumnAmount*ActualTileSize;
    static int gameHeight = 1000; // gameRowAmount*ActualTileSize;
    int FPS = 60;

    /*
     *  FUN STUFF!
     */


    //Establishes a container in the middle of the screen that all things should interact with.   (Ellipse Shaped)
    static Ellipse2D Circular_Container = new Ellipse2D.Double(gameWidth/2 - gameWidth/8, gameHeight/2 - gameHeight/8, gameWidth/4, gameHeight/4);  
    
    ArrayList<Particle> particles = new ArrayList<>();

     /*
      * 
      */


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

        /*
         * Anything done at startup
         */
        particles.add(new Particle());

        Vector2 newVec = new Vector2(0,0);
        Vector2 superVec = (newVec.add(new Vector2(1, 1)));
        System.out.println(superVec.x);
        System.out.println(superVec.y);

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

    /*
     * Little Function to Visualize the Container
     */
    public void visualizeContainer(Graphics2D graphics) {
        graphics.setColor(Color.RED);
        graphics.draw(Circular_Container);
    }

    public void update(double dt){
        //Update all the things that need to be updated

        for(Particle p : particles){
            p.updateParticle((float) dt);
            p.updatePosition();

            /*
             * Debug Particle Console Print
             */
            System.out.println(p.debug());
        
        
        }
    }

    // Function that paints the updated version of the frame {FPS} times a second.
    public void paintComponent(Graphics g) {

        //Randomization Stuff

        // Quick definition of varibles to use with the G2D library
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;

        //Show Particle Bounds
        graphics.setColor(Color.WHITE);
        for(Particle p : particles){
            graphics.draw(p.getParticle());
        }


        //Show Container Bounds
        visualizeContainer(graphics);
        
        graphics.dispose();
    }

}
//hi