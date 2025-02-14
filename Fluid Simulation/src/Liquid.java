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

    /*
     * CHANGEABLE PARAMETERS
     */
    final int XCELLS = 48;
    final int YCELLS = 48;

    final boolean IS_WIND_TUNNEL = true;
    final float WIND_TUNNEL_SPEED = 5.0f;
    

    final float VECTOR_LINE_SCALE = 5.0f;


    

    //DO NOT TOUCH!

    int xCells = XCELLS + 2;
    int yCells = YCELLS + 2;

    float cellWidth = gameWidth/xCells;
    float cellHeight = gameHeight/yCells;

    /*
     *  U --> Stored at the middle right of a cell 
     *  V --> Stored at the top of a cell
     *  D --> Stored at the middle of a cell 
     * 
     */

    Rectangle2D[][] cells = new Rectangle2D.Float[yCells][xCells]; // Cells that can be visualized!
    Color[][] colors = new Color[yCells][xCells];

    float[][] u = new float[yCells][xCells]; // Horizontal Velocity Components
    float[][] v = new float[yCells][xCells]; // Vertical Velocity Components
    
    float[][] newU = new float[yCells][xCells]; // New Vertical Velocity Components
    float[][] newV = new float[yCells][xCells]; // New Vertical Velocity Components

    float[][] d = new float[yCells][xCells]; // Density Value for Each Cell
    float[][] newD = new float[yCells][xCells]; //Temp new Density Values

    int[][] s = new int[yCells][xCells]; // Scalar Value --> 0 represents a wall, 1 represents fluid
    

    public Liquid(){

        //Initialize the Liquid!
        for(int y = 0; y < yCells; y++){
            for(int x = 0; x < xCells; x++){
                
            
                //Zero out all the components!
                u[y][x] = 0f;
                v[y][x] = 0f;
                newU[y][x] = 0f;
                newV[y][x] = 0f;
                d[y][x] = 0f;
                s[y][x] = 1;

                //Set up the Cells!
                cells[y][x] = new Rectangle2D.Float(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                colors[y][x] = Color.gray;

                /*
                 * Special Cases!
                 */
                
                //Container
                
                float radius = 20f;
                
                Vector2 currentCell = new Vector2(x, y);
                Vector2 center = new Vector2(xCells/2, yCells/2);
                
                if(currentCell.subtract(center).magnitude() <= radius){
                    d[y][x] = 225;
                }



                //Check if a Boundary
                if(y == 0 || x == 0 || y == yCells-1 || x == xCells-1){
                    s[y][x] = 0; // Set the scalar value to show that it is a wall. 
                    colors[y][x] = Color.DARK_GRAY;
                }

            }
        }
        
        
    }    

    public void addForces(float dt){
        for(int x = 0; x < xCells; x++){
            for(int y =0; y < yCells; y++){
                
                //Check if Boundary

                if(x == 0){ // Left Boundary
                    v[y][x] = 0;
                    if(IS_WIND_TUNNEL){
                        u[y][x] = WIND_TUNNEL_SPEED;
                    }else{
                        u[y][x] = u[y][x + 1];
                    }
                }
                if(x == xCells-2){ //Right Boundary
                    v[y][x] = 0;
                    u[y][x] = u[y][x - 1];
                }
                
                if(y == 0){ //Top Boundary
                    u[y][x] = 0;
                    v[y][x] = v[y + 1][x];
                }
                if(y == yCells - 2){
                    u[y][x] = 0;
                    v[y][x] = v[y - 1][x];
                }
            }
        }
    }


    public void diffuseDensity(float dt){

        newD = d;

        for(int x = 0; x < xCells; x++){
            for(int y = 0; y < yCells; y++){

                if(s[y][x] == 0){continue;}

                float deltaD = (d[y + 1][x] + d[y - 1][x] + d[y][x + 1] + d[y][x - 1]) * 0.25f; //Diffuse out the density
                newD[y][x] += dt * (deltaD - d[y][x]) / (1 + dt);
            }
        }

        d = newD;
    }
    public void diffuseU(float dt){

        newU = u;

        for(int x = 0; x < xCells; x++){
            for(int y = 0; y < yCells; y++){

                if(s[y][x] == 0){continue;}

                float deltaU = (u[y + 1][x] + u[y - 1][x] + u[y][x + 1] + u[y][x - 1]) * 0.25f; //Diffuse out the density
                newU[y][x] += dt * (deltaU - u[y][x]) / (1 + dt);
            }
        }

        u = newU;
    }
    public void diffuseV(float dt){

        newV = v;

        for(int x = 0; x < xCells; x++){
            for(int y = 0; y < yCells; y++){

                if(s[y][x] == 0){continue;}

                float deltaD = (v[y + 1][x] + v[y - 1][x] + v[y][x + 1] + v[y][x - 1]) * 0.25f; //Diffuse out the density
                newV[y][x] += dt * (deltaD - v[y][x]) / (1 + dt);
            }
        }

        v = newV;
    }

    void advect (float dt){
                
        int x0;
        int y0;
        

        for(int y = 0; y < yCells; y++){
            for(int x = 0; x < xCells; x++){

                x0 = (int) (x / cellWidth - dt*u[y][x] * cellWidth); //Original Coord X
                y0 = (int) (y / cellHeight - dt*v[y][x] * cellHeight); //Original Coord Y
                
                //Stay Within Bounds.

            }
        }
        
        // x = i-dt0*u[IX(i,j)]; y = j-dt0*v[IX(i,j)];
        // if (x<0.5) x=0.5; if (x>N+0.5) x=N+ 0.5; i0=(int)x; i1=i0+1;
        // if (y<0.5) y=0.5; if (y>N+0.5) y=N+ 0.5; j0=(int)y; j1=j0+1;
        // s1 = x-i0; s0 = 1-s1; t1 = y-j0; t0 = 1-t1;
        // d[IX(i,j)] = s0*(t0*d0[IX(i0,j0)]+t1*d 0[IX(i0,j1)])+
        // s1*(t0*d0[IX(i1,j0)]+t1*d0[IX(i1,j1)]);
    }
}
set_bnd ( N, b, d );
}




    public void updateLiquid(double deltaTime){
        float dt = (float) deltaTime;

        //Add Forces
        addForces(dt);

        //Diffuse
        diffuseDensity(dt);
        
        diffuseU(dt);
        diffuseV(dt);
        
        //Update the colors!
        updateColor();
    }


    public void updateColor(){
        for(int y = 0; y < yCells; y++){
            for(int x = 0; x < xCells; x++){

                //Given cell at X,Y

                if(s[y][x] == 0){continue;}

                float red = 225 - d[y][x];
                float blue = d[y][x] - 225;
                float green = 225 - 300;

                red = Math.min(225, Math.max(red, 0));
                blue = Math.min(225, Math.max(blue, 0));
                green = Math.min(225, Math.max(green, 0));

                colors[y][x] = new Color((int) red, (int) green, (int) blue);
            }
        }
    }


    //Visualization Code!
    public Rectangle2D getCell(int i,int j){
        return cells[i][j];
    }
    public Color getColor(int i, int j){
        return colors[i][j];
    }
    public int getHeight(){
        return yCells;
    }
    public int getWidth(){
        return xCells;
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