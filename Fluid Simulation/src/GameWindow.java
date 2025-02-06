import java.awt.*;
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

     //Establishes the Cell Divisions and Size

    // 50x50 cells;
    public static int amountOfCells = 100;
    
    static Cell[][] cells = new Cell[amountOfCells][amountOfCells];



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

        //Setup Cell size positions.
        initCells();

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

    private void initCells(){
        float cellWidth = (gameWidth)/cells[0].length; 
        float cellHeight = (gameHeight)/cells.length;

        //Intitizalize the grid of cells.
        for(int i = 0; i < cells.length; i++){
            for(int j = 0; j < cells[i].length; j++){
                cells[i][j] = new Cell(
                    j*cellWidth, //Upper Left X Coord
                    i*cellHeight, //Upper Left Y Coord
                    cellWidth, //Cell Width
                    cellHeight, //Cell Height
                    i,
                    j
                );
            }
        }
    }

    public void visualizeCells(Graphics2D graphics) {
        graphics.setColor(Color.RED);
        
        /* Debug Cell Setup */
        /* 
        for(Cell[] row : cells){
            for(Cell c : row){
                if(c.borderCell){
                    graphics.setColor(Color.GRAY);
                    if(c.cornerCell){
                        graphics.setColor(Color.DARK_GRAY);
                    }
                    graphics.fill(c.getCell());
                }
                else{
                    graphics.setColor(Color.LIGHT_GRAY);
                    graphics.fill(c.getCell());
                }
                graphics.setColor(Color.BLACK);
                graphics.draw(c.getCell());
            }
        }
        */

        /*
         * Actually Visualize the Cells Better
         */
        for(Cell Row[] :cells){
            for(Cell c : Row){
                if(c != null){

                    /*
                     * Update Cell
                     */
                    c.update((float) deltaTime);

                    //Fill the cell with the color nessecary
                    graphics.setColor((Color) c.getColor());
                    graphics.fill(c.getCell());

                    //Fill the outline of the cell with black!
                    graphics.setColor(Color.BLACK);
                    graphics.draw(c.getCell());

                    // Draw Velocity Lines
                    graphics.setColor(Color.white);
                    graphics.draw(c.getVelocityLine());

                    //Draw Debug Strings
                    c.debugString(graphics);
                }
            }
        }
    }

    public void update(double dt){
        ;
    }

    // Function that paints the updated version of the frame {FPS} times a second.
    public void paintComponent(Graphics g) {

        // Quick definition of varibles to use with the G2D library
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;

        //Show Container Bounds
        visualizeCells(graphics);
        
        graphics.dispose();
    }

}
//hi