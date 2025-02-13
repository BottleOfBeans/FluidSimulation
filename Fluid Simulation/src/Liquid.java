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
     * 
     * CUSTOM PARAMETERS
     * 
     */

    static final float OVER_RELAXATION_CONST = 1.9f;
    static final float DENSITY = 1.0f;
    static final float WIND_TUNNEL_SPEED = 5.0f;
    static final float LINE_SCALE = 5.0f;



     //==============================


    static final int XCELLS = 8;
    static final int YCELLS = 8;

    static int xCells = XCELLS + 2;
    static int yCells = YCELLS + 2;

    float cellWidth = gameWidth/xCells;
    float cellHeight = gameHeight/yCells;

    static Rectangle2D[][] cells = new Rectangle2D.Float[yCells][xCells]; // Cells that can be visualized!

    float[][] u = new float[yCells][xCells]; // Horizontal Velocity Components
    float[][] v = new float[yCells][xCells]; // Vertical Velocity Components
    
    float[][] newU = new float[yCells][xCells]; // New Vertical Velocity Components
    float[][] newV = new float[yCells][xCells]; // New Vertical Velocity Components

    float[][] p = new float[yCells][xCells]; // Preassure Value for Each Cell
    int[][] s = new int[yCells][xCells]; // Scalar Value --> 0 represents a wall, 1 represents fluid
    

    public Liquid(){

        //Initialize the Liquid!
        for(int y = 0; y < yCells; y++){
            for(int x = 0; x < xCells; x++){
                
            
                //Zero out all the components!
                
                u[y][x] = 0f; //Zeroed Horizontal Velocity
                v[y][x] = 0f; //Zeroed Vertical Velocity
                newU[y][x] = 0f; //Zeroed New HV Value
                newV[y][x] = 0f; //Zeroed Old VV Value
                p[y][x] = 0f; //Zeroed Pressure
                s[y][x] = 1; //Zeroed Scalar Representation of Boundaries

                //Set up the Cells!
                cells[y][x] = new Rectangle2D.Float(x * cellWidth, y * cellHeight, cellWidth, cellHeight);

                if( x == 0 || x == xCells-1 || y == 0 || y == yCells-1){ //Test to see if it a boundary Cells
                    s[y][x] = 0; //Make the scalar 0;
                }


            }
        }
        
    }        
    

    /*
     * This function allows for the calculation of boundaries, forces and other various things before compression and advection calculations.
     */
    public void applyForces(double dt){
        
        //Iterate through every single cell!
        for(int y = 0; y < yCells; y++){
            for(int x = 0; x < xCells; x++){

                //Given a cell at position y,x
                
                //First establish no slip surfaces
                if(s[y][x] == 0){
                    u[y][x] = 0;
                    v[y][x] = 0;
                }

                //Establish Wind Tunnel Inflow (Left Boundary)
                //Wind tunnel will push in a constant speed of WIND_TUNNEL_SPEED
                if(s[y][x] == 0 && x == 0){
                    u[y][x] = WIND_TUNNEL_SPEED;
                }

                //Establish Wind Tunnel Outflow (Right Boundary)
                //By Copying the cell next to it, the wind tunnel will just simply absorb all of the pushed air
                if(s[y][x] == 0 && x == xCells-1){
                    u[y][x] = u[y][x-1];
                    v[y][x] = v[y][x-1];
                }



            }
        }
    }

    public void solveCompression(double dt){

        float preassureConst = (float) (DENSITY * (cellHeight + cellWidth)/2 * dt);

        for(int y = 0; y < yCells; y++){
            for(int x = 0; x < xCells; x++){
                //###CALC STARTS HERE
                
                //Given a cell of X,Y
                
                if(s[y][x] == 0){continue;} //If a wall then continue

                float sSum = s[y][x - 1] + s[y][x + 1] + s[y - 1][x] + s[y + 1][x]; //Scalar Calcaultion for Boundaries 
                
                if(sSum == 0){continue;} //If surrounded by walls, then continue

                float divergence = u[y][x + 1] - u[y][x]+ v[y - 1][x] - v[y][x]; //Calculating Divergence
                divergence = -divergence / sSum * OVER_RELAXATION_CONST;
                
                System.out.println( u[y][x + 1] - u[y][x]+ v[y - 1][x] - v[y][x]);

                p[y][x] = preassureConst * divergence; //Calcualte Preassure as a constant of density and the cell size

                u[y][x] -= s[y][x - 1] * divergence; 
                v[y][x] -= s[y + 1][x] * divergence;

                u[y][x + 1] += s[y][x+1] * divergence;
                v[y + 1][x] += s[y + 1][x] *divergence;

                //##### CALC ENDS HERE
                
            }
        }
    }

	public float avgU(int x, int y) {
        return (u[y][x] + u[y + 1][x] + u[y][x + 1] + u[y + 1][ x+ 1]) * 0.25f;            
    }
    public float avgV(int x, int y) {
        return (v[y][x] + v[y + 1][x] + v[y][x + 1] + v[y + 1][ x+ 1]) * 0.25f;            
    }

    public float[] sampleVelocity(float x, float y) {
        // Convert world position to grid index
        float gridX = x / cellWidth;
        float gridY = y / cellHeight;

        // Get integer grid indices
        int x0 = Math.max(0, Math.min(xCells - 2, (int) gridX));
        int x1 = x0 + 1;
        int y0 = Math.max(0, Math.min(yCells - 2, (int) gridY));
        int y1 = y0 + 1;

        // Get interpolation weights
        float tx = gridX - x0;
        float ty = gridY - y0;

        // Bilinear interpolation for horizontal velocity (u)
        float u00 = u[y0][x0]; // Bottom-left
        float u10 = u[y0][x1]; // Bottom-right
        float u01 = u[y1][x0]; // Top-left
        float u11 = u[y1][x1]; // Top-right
        float uInterp = (1 - tx) * (1 - ty) * u00 + tx * (1 - ty) * u10 + (1 - tx) * ty * u01 + tx * ty * u11;

        // Bilinear interpolation for vertical velocity (v)
        float v00 = v[y0][x0]; // Bottom-left
        float v10 = v[y0][x1]; // Bottom-right
        float v01 = v[y1][x0]; // Top-left
        float v11 = v[y1][x1]; // Top-right
        float vInterp = (1 - tx) * (1 - ty) * v00 + tx * (1 - ty) * v10 + (1 - tx) * ty * v01 + tx * ty * v11;

        return new float[]{uInterp, vInterp};
}

    public void solveAdvection(double dt){
        for(int y = 0; y < yCells; y++){
            for(int x = 0; x < xCells; x++){

                if(s[y][x] == 0){continue;}

                //U Component Advection               
                if(true){
                    float currentX = cellWidth * x;
                    float currentY = cellHeight * y + cellHeight/2;

                    float currentV = avgV(x, y);
                    currentX -= dt * u[y][x];
                    currentY -= dt * currentV;

                    newU[y][x] = (sampleVelocity(currentX, currentY))[0];
                }
                if(true){
                    float currentX = cellWidth * x + cellWidth/2;
                    float currentY = cellHeight * y;

                    float currentU = avgU(x, y);
                    currentX -= dt * currentU;
                    currentY -= dt * v[y][x];

                    newU[y][x] = (sampleVelocity(currentX, currentY))[1];
                }  
            }
        }
        
        u = newU;
        v = newV;
    }

    //Logic Code!

    public void updateCells(double dt){
        
        //1. Add Forces
        applyForces(dt);
        
        //3. Solve Compression
        solveCompression(dt);
        
        //4. Solve Advection
        solveAdvection(dt);
    }



    
    //Visualization Code!

    private Rectangle2D getCell(int i,int j){
        return cells[i][j];
    }
    private Color getColor(int i, int j){
        if(s[j][i] == 0){
            return Color.DARK_GRAY;
        }else{
            return Color.LIGHT_GRAY;
        }
    }
    public void drawVelocityLines(Graphics2D g){
        for(int x = 0; x < xCells; x++){
            for(int y = 0; y < yCells; y++){

                double startX = (x+0.5) * cellWidth;
                double startY = (y+0.5) * cellHeight;
                double endX = startX + u[y][x] * LINE_SCALE;
                double endY = startY + v[y][x] * LINE_SCALE;

                g.draw(new Line2D.Double(startX, startY, endX, endY));
            }
        }

    }
    
    public void updateCellDisplay(Graphics2D g){
        for(int y = 0; y < yCells; y++){
            for(int x = 0; x < xCells; x++){

                //Fill in the background color of the cell
                g.setColor(getColor(x,y));
                g.fill(getCell(x, y));

                //Fill in the outline of the cell
                g.setColor(Color.BLACK);
                g.draw(getCell(x, y));

                g.setColor(Color.RED);
            }
        }
    }
}