import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

/*
 * Main Ideas
 *  
 *      - Fluid is Incompressible, the Velocity in must be equal to the velocity out
 *      - Fluid is incompressible, the divergence of the velocity field must be zero
 *      - Fluid is viscous, the velocity field must be smooth (Perhaps Later)
 *      - Fluid conserves momentum!
 * 
 *      - Velocity horizontally is stored at the lines between two grids sideways, this allows for interactions to be calculated. 
 *      - Velocity vertically is stored at the lines between two grids up and down, this allows for interactions to be calculated.
 * 
 *      Simulation Steps:
 *      - Add forces to the velocity field  (Drag / Gravity)
 *      - Make the velocity field incompressible (Projection)
 *      - Move the velocity field (Advection)
 * 
 *      Reminders
 *      - Positive is downward
 *      - Positive is rightward
 *      - Negative is up
 *      - Negative is leftward
 */




public class Liquid extends GameWindow{

    static final int XCELLS = 10;
    static final int YCELLS = 10;


    static int Width = XCELLS + 2;
    static int Height = YCELLS + 2;

    float cellWidth = gameWidth/Width;
    float cellHeight = gameHeight/Height;

    static Rectangle2D[][] cells = new Rectangle2D.Float[Height][Width]; // Cells that can be visualized!
    static Color[][] colors = new Color[Height][Width];

    float[][] u = new float[Height][Width]; // Horizontal Velocity Components
    float[][] v = new float[Height][Width]; // Vertical Velocity Components
    
    float[][] newU = new float[Height][Width]; // New Vertical Velocity Components
    float[][] newV = new float[Height][Width]; // New Vertical Velocity Components


    float[][] p = new float[Height][Width]; // Preassure Value for Each Cell
    int[][] s = new int[Height][Width]; // Scalar Value --> 0 represents a wall, 1 represents fluid
            
    public Liquid(){

        //Initialize the Liquid!
        for(int row = 0; row < Height; row++){
            for(int col = 0; col < Width; col++){
                
                //Check if a Boundary
                if(row == 0 || col == 0 || row == Height-1 || col == Width-1){
                    s[row][col] = 0; // Set the scalar value to show that it is a wall. 
                }

                //Zero out all the components!
                u[row][col] = 0f;
                v[row][col] = 0f;
                newU[row][col] = 0f;
                newV[row][col] = 0f;
                p[row][col] = 0f;


                //Set up the Cells!
                cells[row][col] = new Rectangle2D.Float(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
                colors[row][col] = Color.pink;
            }
        }
        
        
    }    

    
    
    
    
    
    
    
    
    
    public void update(){
        
        //1. Add Velocity Ect. 
        //2. Make Incompressible  Ect.
        //3. Advect Velocity
        //4. Repeat!
    
    }


    
    
    //Visualization Code!
    public Rectangle2D getCell(int i,int j){
        return cells[i][j];
    }
    public Color getColor(int i, int j){
        return colors[i][j];
    }
    public Line2D getVectorLine(int i, int j){
        
        float StartingPointX = (float) cells[i][j].getCenterX(); 
        float StartingPointY = (float) cells[i][j].getCenterY();

        return new Line2D.Double(StartingPointX, StartingPointY, StartingPointX + u[i][j], StartingPointY + v[i][j]);
    }
}