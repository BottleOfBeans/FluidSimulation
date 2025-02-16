import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.Random;

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
 *   
 *      -Horizontal Velocity is stored on the left middle
 *      -Vertical Velocity is stored on the bottom
 */




public class Liquid extends GameWindow{

    /*
     * CHANGEABLE PARAMETERS
     */
    final int XCELLS = 98;
    final int YCELLS = 98;

    final float VECTOR_LINE_SCALE = 1.9f;

    final float GRAVITY = -4.0f;
    final float OVER_RELAX_CONST = 1.9f;  //Set between 1 and 2.
    final float DENSITY = 20.0f;

    final boolean WIND_OR_GRAV = true; //TRUE FOR WIND, FALSE FOR GRAV

    final float WIND_TUNNEL_SPEED = 5.0f;

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
    
    float[][] p = new float[yCells][xCells]; // Pressure Value 

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
                
                //Add Container

                int radius = 10;

                Vector2 centerPos = new Vector2(xCells/2, yCells/2);
                Vector2 currentPos = new Vector2(x,y);
                if(currentPos.subtract(centerPos).magnitude() < radius){
                    s[y][x] = 0;
                }

                //Check if a Boundary
                if(y == 0 || x == 0 || y == yCells-1 || x == xCells-1){
                    s[y][x] = 0; // Set the scalar value to show that it is a wall. 
                    colors[y][x] = Color.DARK_GRAY;
                }

            }
        }
        
        
    }    

    /*
     * Starting from Scratch, Iteration 9
     */
    public void addForces(float dt){
        for(int x = 0; x < xCells; x++){
            for(int y = 0; y < yCells; y++){

                //Given a cell of X,Y

                if(s[y][x] == 0){continue;} // Move on if it is a wall, do NOT CALCULATE!

                if(WIND_OR_GRAV){
                    if(x == 1 && y % 3 == 0){
                        d[y][x] = 1;
                    }
                    if(x == 1 && s[y][x] != 0){ //INFLOW conditions
                        v[y][x] = 0;
                        u[y][x] = WIND_TUNNEL_SPEED;
                    }
                    if(x == xCells-2){ //OUTFLOW conditions
                        u[y][x] = u[y][x - 1]; // Zero-gradient condition for velocity
                        d[y][x] = d[y][x - 1]; // Zero-gradient condition for density
                    }
                }else{
                    //Apply Gravity
                    v[y][x] = GRAVITY * dt;
                }                
            }
        }
    }

    public void boundaryCheck(){
        for(int y = 0; y < yCells; y++){
            for(int x = 0; x < xCells; x++){

                if(s[y][x] == 0){                
                    v[y][x] = 0;
                    u[y][x] = 0;
                    d[y][x] = 0; // Ensure density is zero at solid boundaries
                }
            }
        }
    }

    public void solveCompression(){
        for (int y = 1; y < yCells - 1; y++) { // Avoid boundary cells
            for (int x = 1; x < xCells - 1; x++) {
        
                // Skip walls
                if (s[y][x] == 0) {
                    continue;
                }
        
                // Sum of neighboring cell states (1 = fluid, 0 = wall)
                int sSum = s[y - 1][x] + s[y + 1][x] + s[y][x - 1] + s[y][x + 1];
        
                // Skip if surrounded by walls
                if (sSum == 0) {
                    continue;
                }
        
                // Calculate divergence (staggered grid)
                float divergence = u[y][x] - u[y][x + 1] + v[y][x] - v[y + 1][x];
        
                // Apply over-relaxation and normalize by number of fluid neighbors
                divergence *= -OVER_RELAX_CONST;
                divergence /= sSum;
        
                // Update velocities, ensuring neighboring cells are not walls
                if (s[y][x - 1] != 0) {
                    newU[y][x] = u[y][x] + divergence * s[y][x - 1];
                }
                if (s[y][x + 1] != 0) {
                    newU[y][x + 1] = u[y][x + 1] - divergence * s[y][x + 1];
                }
                if (s[y - 1][x] != 0) {
                    newV[y][x] = v[y][x] + divergence * s[y - 1][x];
                }
                if (s[y + 1][x] != 0) {
                    newV[y + 1][x] = v[y + 1][x] - divergence * s[y + 1][x];
                }
            }
        }
        u = newU;
        v = newV;
    }

    public void solvePressure(float dt) {
        // Do not reinitialize p here; it should retain its values from the previous iteration.
    
        // Temporary array to store the new pressure values
        float[][] newP = new float[yCells][xCells];
    
        for (int y = 1; y < yCells - 1; y++) { // Avoid boundary cells
            for (int x = 1; x < xCells - 1; x++) {
    
                // Skip walls
                if (s[y][x] == 0) {continue;}
    
                // Sum of neighboring cell states (1 = fluid, 0 = wall)
                int sSum = s[y - 1][x] + s[y + 1][x] + s[y][x - 1] + s[y][x + 1];
    
                // Skip if surrounded by walls
                if (sSum == 0) {continue;}
    
                // Calculate divergence (staggered grid)
                float divergence = (u[y][x] - u[y][x + 1]) / cellWidth + (v[y][x] - v[y + 1][x]) / cellHeight;
    
                // Apply over-relaxation and normalize by number of fluid neighbors
                divergence *= OVER_RELAX_CONST;
    
                // Update pressure
                newP[y][x] = (s[y - 1][x] * p[y - 1][x] +
                              s[y + 1][x] * p[y + 1][x] +
                              s[y][x - 1] * p[y][x - 1] +
                              s[y][x + 1] * p[y][x + 1] -
                              divergence) / sSum;
            }
        }
    
        // Update the pressure field
        p = newP;
    }

    public void advectVelocity(float dt) {
        float xPosition, yPosition, u00, u01, u10, u11, e, t, w00, w10, w01, w11, avgV, avgU;
    
        for (int x = 0; x < xCells; x++) {
            for (int y = 0; y < yCells; y++) {
                if (s[y][x] == 0) continue;
    
                // Calculate the U (Horizontal) values
                avgV = 0.25f * (v[x - 1][y] + v[x - 1][y + 1] + v[x][y] + v[x][y + 1]);
                avgU = u[y][x];
    
                xPosition = x * cellWidth - dt * avgU;
                yPosition = (y + 0.5f) * cellHeight - dt * avgV;
    
                int xIndex = Math.max(0, Math.min(xCells - 1, (int) (xPosition / cellWidth)));
                int yIndex = Math.max(0, Math.min(yCells - 1, (int) (yPosition / cellHeight)));
    
                u00 = u[yIndex][xIndex];
                u01 = u[yIndex][xIndex + 1];
                u10 = u[yIndex + 1][xIndex];
                u11 = u[yIndex + 1][xIndex + 1];
    
                e = (xPosition / cellWidth) - xIndex;
                t = (yPosition / cellHeight) - yIndex;
    
                w00 = (1 - e) * (1 - t);
                w01 = e * (1 - t);
                w10 = (1 - e) * t;
                w11 = e * t;
    
                newU[y][x] = w00 * u00 + w01 * u01 + w10 * u10 + w11 * u11;
    
                // Calculate the V (Vertical) values
                avgU = 0.25f * (u[x - 1][y] + u[x - 1][y + 1] + u[x][y] + u[x][y + 1]);
                avgV = u[y][x];
    

                xPosition = x * cellWidth - dt * avgU;
                yPosition = (y + 0.5f) * cellHeight - dt * avgV;
    
                xIndex = Math.max(0, Math.min(xCells - 1, (int) (xPosition / cellWidth)));
                yIndex = Math.max(0, Math.min(yCells - 1, (int) (yPosition / cellHeight)));
    
                u00 = v[yIndex][xIndex];
                u01 = v[yIndex][xIndex + 1];
                u10 = v[yIndex + 1][xIndex];
                u11 = v[yIndex + 1][xIndex + 1];
    
                e = (xPosition / cellWidth) - xIndex;
                t = (yPosition / cellHeight) - yIndex;
    
                w00 = (1 - e) * (1 - t);
                w01 = e * (1 - t);
                w10 = (1 - e) * t;
                w11 = e * t;
    
                newV[y][x] = w00 * u00 + w01 * u01 + w10 * u10 + w11 * u11;
            }
        }
    
        // Update velocities
        u = newU;
        v = newV;
    }

    public void advectDensity(float dt) {
        float xPosition, yPosition, u00, u01, u10, u11, e, t, w00, w10, w01, w11, avgV, avgU;
    
        for (int y = 0; y < yCells; y++) {
            for (int x = 0; x < xCells; x++) {
                if (s[y][x] == 0) {
                    continue; // Skip solid cells
                }
    
                // Average velocities for backtracing
                avgV = 0.25f * (v[Math.max(0, x - 1)][y] + v[Math.max(0, x - 1)][Math.min(yCells - 1, y + 1)] +
                              v[x][y] + v[x][Math.min(yCells - 1, y + 1)]);
                avgU = 0.25f * (u[Math.max(0, x - 1)][y] + u[Math.max(0, x - 1)][Math.min(yCells - 1, y + 1)] +
                              u[x][y] + u[x][Math.min(yCells - 1, y + 1)]);
    
                // Backtrace the position
                xPosition = x * cellWidth - dt * avgU;
                yPosition = y * cellHeight - dt * avgV;
    
                // Clamp the position to grid boundaries
                xPosition = Math.max(0, Math.min((xCells - 1) * cellWidth, xPosition));
                yPosition = Math.max(0, Math.min((yCells - 1) * cellHeight, yPosition));
    
                // Calculate indices for interpolation
                int xIndex = (int) (xPosition / cellWidth);
                int yIndex = (int) (yPosition / cellHeight);
    
                // Clamp indices to avoid out-of-bounds errors
                xIndex = Math.max(0, Math.min(xCells - 2, xIndex)); // xCells - 2 to allow xIndex + 1
                yIndex = Math.max(0, Math.min(yCells - 2, yIndex)); // yCells - 2 to allow yIndex + 1
    
                // Get neighboring density values
                u00 = d[yIndex][xIndex];
                u01 = d[yIndex][xIndex + 1];
                u10 = d[yIndex + 1][xIndex];
                u11 = d[yIndex + 1][xIndex + 1];
    
                // Calculate fractional parts
                e = (xPosition / cellWidth) - xIndex;
                t = (yPosition / cellHeight) - yIndex;
    
                // Calculate interpolation weights
                w00 = (1 - e) * (1 - t);
                w01 = e * (1 - t);
                w10 = (1 - e) * t;
                w11 = e * t;
    
                // Interpolate density
                newD[y][x] = w00 * u00 + w01 * u01 + w10 * u10 + w11 * u11;
            }
        }
    
        // Update density field
        d = newD;
    }


    public void updateLiquid(double deltaTime){
        float dt = (float)(deltaTime);

        addForces(dt);        
        boundaryCheck();
        
        for(int i = 0; i < 20; i++){
            solveCompression();
            boundaryCheck();
        }
        

        solvePressure(dt);

        advectVelocity(dt);
        boundaryCheck();

        advectDensity(dt);

        pressureColorUpdate();
    }

    //Visualization Code!
    
    public void pressureColorUpdate(){
        // Assuming pMin and pMax are the minimum and maximum pressure values in the grid
        float pMin = Float.MAX_VALUE;
        float pMax = Float.MIN_VALUE;

        // First pass to find the min and max pressure values
        for (int y = 0; y < yCells; y++) {
            for (int x = 0; x < xCells; x++) {
                if (s[y][x] != 0) {
                    pMin = Math.min(pMin, p[y][x]);
                    pMax = Math.max(pMax, p[y][x]);
                }
            }
        }

        // Second pass to update colors based on normalized pressure
        for (int y = 0; y < yCells; y++) {
            for (int x = 0; x < xCells; x++) {
                if (s[y][x] == 0) {
                    continue; // Skip cells with no fluid
                }

                // Normalize pressure to [0, 1] range
                float normalizedPressure = (p[y][x] - pMin) / (pMax - pMin);

                // Map normalized pressure to color range [0, 255]
                int red = (int) (255 * normalizedPressure);
                int blue = 255 - red;
                int green = 0; // Or adjust based on your needs

                // Ensure color values are within [0, 255]
                red = Math.min(255, Math.max(0, red));
                blue = Math.min(255, Math.max(0, blue));
                green = Math.min(255, Math.max(0, green));

                colors[y][x] = new Color(red, green, blue);
            }
        }
    }
   
    public void densityColorUpdate(){
            // Assuming pMin and pMax are the minimum and maximum pressure values in the grid
            float dMin = Float.MAX_VALUE;
            float dMax = Float.MIN_VALUE;
    
            // First pass to find the min and max pressure values
            for (int y = 0; y < yCells; y++) {
                for (int x = 0; x < xCells; x++) {
                    if (s[y][x] != 0) {
                        dMin = Math.min(dMin, d[y][x]);
                        dMax = Math.max(dMax, d[y][x]);
                    }
                }
            }
    
            // Second pass to update colors based on normalized pressure
            for (int y = 0; y < yCells; y++) {
                for (int x = 0; x < xCells; x++) {
                    if (s[y][x] == 0) {
                        continue; // Skip cells with no fluid
                    }
    
                    // Normalize pressure to [0, 1] range
                    float normalizedPressure = (d[y][x] - dMin) / (dMax - dMin);
    
                    // Map normalized pressure to color range [0, 255]
                    int red = (int) (255 * normalizedPressure);
                    int blue = 255 - red;
                    int green = 0; // Or adjust based on your needs
    
                    // Ensure color values are within [0, 255]
                    red = Math.min(255, Math.max(0, red));
                    blue = Math.min(255, Math.max(0, blue));
                    green = Math.min(255, Math.max(0, green));
    
                    colors[y][x] = new Color(red, green, blue);
                }
            }
    
    }
    
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

    public Line2D getHorizontalLine(int i, int j) {
        float StartingPointX = (float) cells[i][j].getCenterX() - cellWidth/2;
        float StartingPointY = (float) cells[i][j].getCenterY();
        return new Line2D.Double(StartingPointX, StartingPointY, StartingPointX + u[i][j] * VECTOR_LINE_SCALE, StartingPointY);
    }
    public Line2D getVerticalLine(int i, int j) {
        float StartingPointX = (float) cells[i][j].getCenterX();
        float StartingPointY = (float) cells[i][j].getCenterY() - cellHeight/2;
        return new Line2D.Double(StartingPointX, StartingPointY, StartingPointX, StartingPointY + v[i][j] * VECTOR_LINE_SCALE);
    }
}