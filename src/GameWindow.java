import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

public class GameWindow extends JPanel implements Runnable {

    /*
     * Window settings --> Ratio of 16:9
     */
    static int gameWidth = 1920; // gameColumnAmount*ActualTileSize;
    static int gameHeight = 1080; // gameRowAmount*ActualTileSize;
    
    Thread gameThread;
    int FPS = 144;
    
    KeyHandler keyH = new KeyHandler();
    


    /*
     * Container
     */
    static Rectangle2D container = new Rectangle2D.Double(100,100, gameWidth - 200, gameHeight - 200);


    /*
     * Particles
     */
    final int NUMPARTICLES = 10;
    Particle particles[] = new Particle[NUMPARTICLES]; 

    /*
     * More Window Settings
     */
    public GameWindow() {
        this.setPreferredSize(new Dimension(gameWidth, gameHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.addKeyListener(keyH);
    }

    /*
     * Starting the thread and managing frame updates
     */
    public void startWindowThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    /*
     * Frame Updates :D
     */
    @Override
    public void run() {
        /*
         * Add code here that runs before he game starts
         */
        //=============================================

        for(int i = 0; i<NUMPARTICLES; i++){
            
            Random rand = new Random();
            
            int lowerLimitX = 105;
            int upperLimitX = gameWidth - 105;
            int lowerLimitY = 105;
            int upperLimitY = gameHeight - 105; 

            int randomX = rand.nextInt(lowerLimitX, upperLimitX);
            int randomY = rand.nextInt(lowerLimitY, upperLimitY);

            particles[i] = new Particle(new Vector2(randomX, randomY));
        }

        //=============================================
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
                repaint();
                delta--;
            }

        }
    }

    /*
     * Game updates run here
     */
    public void update(double deltaTime) {
        ;
    }

    /*
     * Anything to be displayed runs here
     */
    public void paintComponent(Graphics g) {

        // Quick definition of varibles to use with the G2D library
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;

        /*
         * Drawing Particles
         */
        graphics.setColor(Color.WHITE);
        for(Particle p : particles){
            graphics.fill(p.getParticle());
        }

        //Drawing the boundary
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.draw(container);
        
        graphics.dispose();
    }

}
