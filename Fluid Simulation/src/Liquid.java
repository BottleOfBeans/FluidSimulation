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

    static final int XCELLS = 8;
    static final int YCELLS = 8;

    static final float OVERRELAXTION_CONST = 1;
    static final int PHYSICS_STEPS = 1;     
    static final float GRAVITY = 9.8f;

    static final float VECTOR_LINE_SCALE = 0.01f;

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
                
            
                //Zero out all the components!
                u[row][col] = 0f;
                v[row][col] = 0f;
                newU[row][col] = 0f;
                newV[row][col] = 0f;
                p[row][col] = 0f;
                s[row][col] = 1;

                //Set up the Cells!
                cells[row][col] = new Rectangle2D.Float(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
                colors[row][col] = Color.gray;

                /*
                 * Special Cases!
                 */
                
                //Check if a Boundary
                if(row == 0 || col == 0 || row == Height-1 || col == Width-1){
                    s[row][col] = 0; // Set the scalar value to show that it is a wall. 
                    colors[row][col] = Color.DARK_GRAY;
                }

            }
        }
        
        
    }    

    public void addForces(float dt){
        for(int x = 0; x < Height; x++){
            for(int y = 0; y < Width; y++){
        
                //Given the X position x, and the Y position y. 
                
                if(s[y][x] != 0 && s[y+1][x] != 0){ // Checking if the cell is not a wall or not right above a wall!

                    v[y][x] += GRAVITY; // Applying the Gravity
                    u[y][x] += GRAVITY; // Applying the Gravity

                }
            }
        }
    }
    
    public void solveCompressibility(float dt){
        
        for(int i = 0; i < PHYSICS_STEPS; i ++){
            for(int x = 0; x < Height; x++){
                for(int y = 0; y < Width; y++){
            
                    //Given the X position x, and the Y position y. 
                    
                    if(s[y][x] == 0){return;} // Return and don't calculate for walls!

                    int leftS = s[x-1][y];
                    int rightS = s[x+1][y];
                    int upS = s[x][y-1];
                    int downS = s[x][y+1];

                    int S = leftS + rightS + upS + downS;

                    if(S == 0){return;} //A single grid surrounded by obstacles should forgo all further calculations.

                    float divergence = u[x+1][y] - u[x-1][y];

                    float d = divergence * OVERRELAXTION_CONST / S;

                    u[x + 1][y] -= d * s[x+1][y]; //Right Box
                    u[x - 1][y] += d * s[x-1][y]; //Left Box
                    v[x][y + 1] += d * s[x][y+1]; //Down Box
                    v[x][y - 1] -= d * s[x][y-1]; //Up Box
                    

                }
            }    
        }
        
    }
    
    public void solveBorders(){

        for(int y = 0; y <Height; y++){ //Iterate from 0-Height y values
            
            u[y][0] = u[y][1];
            u[y][Width-1] = u[y][Width-2];
        }

        for(int x = 0; x <Width; x++){
            v[0][x] = v[1][x];
            v[Height-1][x] = v[Height -2][x];
        }

    }
    
    
    
    
    
    public void update(double deltaTime){
        
        float dt = (float) deltaTime;

        //1. Add Velocity Ect. 

        addForces(dt);

        //2. Make Incompressible  Ect.
        
        solveCompressibility(dt);
        solveBorders();

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

        return new Line2D.Double(StartingPointX, StartingPointY, StartingPointX + u[i][j] * VECTOR_LINE_SCALE, StartingPointY + v[i][j] * VECTOR_LINE_SCALE);
    }
}