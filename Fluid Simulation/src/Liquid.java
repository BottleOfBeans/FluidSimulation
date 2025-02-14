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

    final boolean  IF_GRAVITY = false;
    final boolean  IF_WIND_TUNNEL = true;
    final float WIND_TUNNEL_SPEED = 5.0f;
    final float DENSITY = 25.5f;

    static final int XCELLS = 50;
    static final int YCELLS = 50;

    static final float OVERRELAXTION_CONST = 1.0f;
    static final int PHYSICS_STEPS = 1;     
    static final float GRAVITY = 9.8f;

    static final float VECTOR_LINE_SCALE = 5.0f;
    static final float PRESSURE_SCALE_FACTOR = 5;

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
                
                if(s[y][x] == 0){
                    u[y][x] = 0;
                    v[y][x] = 0;
                }

                if(IF_GRAVITY){ //If Gravity is enabled then apply!
                    if(s[y][x] != 0 && s[y+1][x] != 0){ // Checking if the cell is not a wall or not right above a wall!

                        v[y][x] += GRAVITY; // Applying the Gravity to the Vertical
                        //u[y][x] += GRAVITY; // Applying the Gravity to the Horizontal
    
                    }
                }
            
                if(IF_WIND_TUNNEL){
                    if(x == 1 && y != 0 && y != cellHeight-1){
                        u[y][x] = WIND_TUNNEL_SPEED;
                        v[y][x] = 0;
                    }
                    else if(x == Width-1 &&  y != 0 && y != cellHeight-1){
                        u[y][x] = u[y][x-1];
                    }
                }
            }
        }
    }
    
    public void solveCompressibility(float dt){
        
        float densityConst = DENSITY * cellHeight / dt;

        for(int i = 0; i <= PHYSICS_STEPS; i ++){

            for(int x = 0; x < Height; x++){
                for(int y = 0; y < Width; y++){
                    //Given the X position x, and the Y position y. 
                    
                    if(s[y][x] == 0){continue;} //If is a wall then don't calc
                    
                                //Up            Down            Right       Left
                    int sSum = s[y - 1][x] + s[y + 1][x] + s[y][x - 1] + s[y][x + 1];

                    if(sSum == 0){continue;} //If surrounded completley by walls then don't calc

                                        //Right  -   Left  +   Down  -   Up
                    float divergence = u[y][x+1] - u[y][x] + v[y][x] - v[y - 1][x];

                    divergence *= OVERRELAXTION_CONST;

                    p[y][x] = -densityConst * divergence;

                    //Left  += d * s(Left) / Sum of S
                    u[y][x] += divergence * s[y][x - 1] / sSum;
                    
                    //Down  += d * s(Down) / Sum of S
                    v[y][x] -= divergence * s[y + 1][x] / sSum;

                    //Right     -= d          *  s(Right) / Sum of S
                    u[y][x + 1] -= divergence * s[y][x + 1] / sSum;

                    //Up        -= d          *  s(Up)   / Sum of S
                    v[y - 1][x] += divergence * s[y - 1][x] / sSum;

                    if(x  == 2){
                        System.out.println("====");
                        System.out.println(String.format("X: %d,  Y: %d,  Divergence: %.3f,   Pressure: %.3f", x, y, divergence,p[y][x]));
                    }

                }
            }    
        }
    }
   
    public float sampleU(float gx, float gy){
        
        int x = (int) (gx/cellWidth); // Cell Location X 
        int y = (int) (gy/cellHeight); //Cell Location Y

        float u00 = u[y][x]; //Top Left
        float u01 = u[y][x+1]; //Top Right
        float u10 = u[y + 1][x]; //Bottom Left
        float u11 = u[y + 1][x + 1]; //Bottom Right

        float xLeft = Math.abs(gx - (x * cellWidth));
        float xRight = Math.abs(gx - ((x + 1) * cellWidth));

        float yUp = Math.abs(gy - (y * cellHeight));
        float yDown = Math.abs(gy - ((y +1 ) * cellWidth));
        
        float upU = (u00 * xLeft + u01 * xRight) / (xLeft + xRight);
        float downU = (u10 * xLeft + u11 * xRight) / (xLeft + xRight);

        return (upU * yUp + downU * yDown)/(yUp + yDown);

    }   
    public float sampleV(float gx, float gy){
        
        int x = (int) (gx/cellWidth); // Cell Location X 
        int y = (int) (gy/cellHeight); //Cell Location Y

        float u00 = v[y][x]; //Top Left
        float u01 = v[y][x+1]; //Top Right
        float u10 = v[y + 1][x]; //Bottom Left
        float u11 = v[y + 1][x + 1]; //Bottom Right

        float xLeft = Math.abs(gx - (x * cellWidth));
        float xRight = Math.abs(gx - ((x + 1) * cellWidth));

        float yUp = Math.abs(gy - (y * cellHeight));
        float yDown = Math.abs(gy - ((y +1 ) * cellWidth));
        
        float upU = (u00 * xLeft + u01 * xRight) / (xLeft + xRight);
        float downU = (u10 * xLeft + u11 * xRight) / (xLeft + xRight);

        return (upU * yUp + downU * yDown)/(yUp + yDown);    
    }

    public void advectVelocity(float dt){
    
        for(int x = 0; x < Height; x++){
            for(int y = 0; y < Width; y++){
         
                //Given the indicies y, x for all the value tables!

                if(s[y][x] == 0){continue;} //Silly sussy walls should not be calculated at all
            
                //From here, it is given that it is not a wall, now how should one calculate?

                float newX = cellWidth * x - (u[y][x] * dt);
                float newY = cellHeight * y - (v[y][x] * dt);

                newU[y][x] = sampleU(newX, newY);
                newV[y][x] = sampleU(newX, newY);
        
            }
        }

        u = newU;
        v = newV;

    }
    
    public void updateColor(){

        for(int x = 0; x < Width - 2; x++){
            for(int y = 0; y < Height - 1; y++){
                
                if(s[y][x] != 0){
            
                    int red = (int) (p[y][x] * PRESSURE_SCALE_FACTOR - 255);
                    int blue = (int) (255 - p[y][x] * PRESSURE_SCALE_FACTOR);
                    int green = (int) (255 - p[y][x] * PRESSURE_SCALE_FACTOR * 0.5);

                    red = Math.min(225, Math.max(0, red));
                    blue = Math.min(225, Math.max(0, blue));
                    green = Math.min(255, Math.max(0,green));

                    colors[y][x] = new Color(red, green, blue);
                }
            }
       }
    }
    
    public void updateLiquid(double deltaTime){
        
        float dt = (float) deltaTime;

        //1. Add Velocity Ect. 

        addForces(dt);

        //2. Make Incompressible  Ect.
        
        solveCompressibility(dt);

        //3. Advect Velocity
        
        //advectVelocity(dt);
        
        updateColor();
        //4. Repeat!
        
    
    }


    
    //Visualization Code!
    public Rectangle2D getCell(int i,int j){
        return cells[i][j];
    }
    public Color getColor(int i, int j){
        return colors[i][j];
    }
    public Line2D getHorizontalLine(int i, int j){
        
        float StartingPointX = (float) cells[i][j].getCenterX()  - cellWidth/2;
        float StartingPointY = (float) cells[i][j].getCenterY();

        return new Line2D.Double(StartingPointX, StartingPointY, StartingPointX + u[i][j] * VECTOR_LINE_SCALE, StartingPointY);
    }
    public Line2D getVerticalLine(int i, int j){
        
        float StartingPointX = (float) cells[i][j].getCenterX(); 
        float StartingPointY = (float) cells[i][j].getCenterY() - cellHeight/2;

        return new Line2D.Double(StartingPointX, StartingPointY, StartingPointX, StartingPointY + v[i][j]);
    }
}