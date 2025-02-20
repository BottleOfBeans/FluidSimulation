import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
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

    final int SCENE = 1;

    final int XCELLS = 248;
    final int YCELLS = 248;

    final float VECTOR_LINE_SCALE = 0.5f;

    final float GRAVITY = -9.8f;
    final float OVER_RELAX_CONST = 1.9f;  //Set between 1 and 2.
    final float DENSITY = 100.0f;

    final float WIND_TUNNEL_SPEED = 100.0f;

    final int ITER = 10;

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
                u[y][x] = 0.0f;
                v[y][x] = 0.0f;
                newU[y][x] = 0.0f;
                newV[y][x] = 0.0f;
                d[y][x] = 0.0f;
                s[y][x] = 1;

                //Set up the Cells!
                cells[y][x] = new Rectangle2D.Float(x * cellWidth, y * cellHeight, cellWidth, cellHeight);
                colors[y][x] = Color.gray;

                /*
                 * Special Cases!
                 */
                
                //Add Container

                int radius = 30;

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
     * Starting from Scratch, Iteration 21
     */

    @SuppressWarnings("unused")
    public void addForces(float dt){

        Random random = new Random();


        for(int y = 0; y < yCells; y ++){
            for(int x = 0; x < xCells; x++){
                
                if(s[y][x] == 0){ //Zero Out Boundries
                    u[y][x] = 0;
                    v[y][x] = 0;
                    p[y][x] = 0;
                    d[y][x] = 0;
                }

                if(SCENE == 0){ //GRAVITY TANK
                    if(s[y][x] == 1 && s[y - 1][x] != 0){
                        v[y][x] += GRAVITY;
                    }
                } 
                else if (SCENE == 1){ // WIND TUNNEL
                    if(x == 1 && y != 0 && y != yCells-1){ // Inflow
                        v[y][x] = 0;
                        u[y][x] = WIND_TUNNEL_SPEED;
                        
                        //Single Big Stream
                        if(y % 20 == 0){
                            for(int i = 0; i < 3; i++){
                                d[y + i][x] += 5.0f;
                            }
                        }

                    }
                    if(x == xCells - 1){ // Outflow
                        u[y][x] = u[y][x - 1]; // Zero Gradient for Velocity
                        d[y][x] = d[y][x - 1]; // Zero Gradient for Density
                    }

                }
                
            }
        }
    }
    
    public void solveCompression(float dt){

        float pc = DENSITY * cellHeight / dt;

        for(int i = 0; i < ITER; i ++){
            for(int x = 0; x < xCells; x++){
                for(int y = 0; y < yCells; y++){

                    if(s[y][x] == 0){continue;} // Skip Walls!

                    float sSum = s[y][x - 1] + s[y][x + 1] + s[y - 1][x] + s[y + 1][x];

                    if(sSum == 0){continue;} // If Surrounded on all sides then don't calculate;

                    float divergence = u[y][x + 1] - u[y][x] + v[y + 1][x] - v[y][x];

                    float pressure = -divergence / sSum;
                    
                    p[y][x] = pc * pressure;

                    u[y][x]     -=      s[y][x - 1]     * pressure;
                    u[y][x + 1] +=      s[y][x + 1]     * pressure;
                    v[y][x]     -=      s[y - 1][x]     * pressure;
                    v[y + 1][x] +=      s[y + 1][x]     * pressure;

                }
            }
        }

    }

    public float sampleD(float x, float y) {
        float gridX = x / cellWidth;
        float gridY = y / cellHeight;
    
        // Adjust to prevent interpolation errors
        gridX = Math.max(1, Math.min(xCells - 2, gridX));
        gridY = Math.max(1, Math.min(yCells - 2, gridY));
    
        int x0 = (int) gridX;
        int y0 = (int) gridY;
        int x1 = Math.min(x0 + 1, xCells - 2);
        int y1 = Math.min(y0 + 1, yCells - 2);
    
        float tx = gridX - x0;
        float ty = gridY - y0;
    
        float d00 = d[y0][x0];
        float d01 = d[y0][x1];
        float d10 = d[y1][x0];
        float d11 = d[y1][x1];
    
        float d0 = d00 * (1 - tx) + d01 * tx;
        float d1 = d10 * (1 - tx) + d11 * tx;
    
        return d0 * (1 - ty) + d1 * ty;
    }
    public float sampleU(float x, float y) {
        float gridX = x / cellWidth;
        float gridY = y / cellHeight;
    
        gridX = Math.max(1, Math.min(xCells - 2, gridX));
        gridY = Math.max(1, Math.min(yCells - 2, gridY));
    
        int x0 = (int) gridX;
        int y0 = (int) gridY;
        int x1 = Math.min(x0 + 1, xCells - 2);
        int y1 = Math.min(y0 + 1, yCells - 2);
    
        float tx = gridX - x0;
        float ty = gridY - y0;
    
        float u00 = u[y0][x0];
        float u01 = u[y0][x1];
        float u10 = u[y1][x0];
        float u11 = u[y1][x1];
    
        float u0 = u00 * (1 - tx) + u01 * tx;
        float u1 = u10 * (1 - tx) + u11 * tx;
    
        return u0 * (1 - ty) + u1 * ty;
    }
    public float sampleV(float x, float y) {
        float gridX = x / cellWidth;
        float gridY = y / cellHeight;
    
        gridX = Math.max(1, Math.min(xCells - 2, gridX));
        gridY = Math.max(1, Math.min(yCells - 2, gridY));
    
        int x0 = (int) gridX;
        int y0 = (int) gridY;
        int x1 = Math.min(x0 + 1, xCells - 2);
        int y1 = Math.min(y0 + 1, yCells - 2);
    
        float tx = gridX - x0;
        float ty = gridY - y0;
    
        float v00 = v[y0][x0];
        float v01 = v[y0][x1];
        float v10 = v[y1][x0];
        float v11 = v[y1][x1];
    
        float v0 = v00 * (1 - tx) + v01 * tx;
        float v1 = v10 * (1 - tx) + v11 * tx;
    
        return v0 * (1 - ty) + v1 * ty;
    }
    


    public void advect(float dt) {
        float prevX, prevY;
        
        for (int y = 1; y < yCells - 1; y++) { // Skip boundary cells
            for (int x = 1; x < xCells - 1; x++) { // Skip boundary cells
    
                if (s[y][x] == 0) {
                    continue; // Skip walls
                }
    
                // Advect horizontal velocity (u)
                prevX = x * cellWidth - dt * u[y][x];
                prevY = (y + 0.5f) * cellHeight - dt * sampleV(x * cellWidth, (y + 0.5f) * cellHeight);
                newU[y][x] = sampleU(prevX, prevY);
    
                // Advect vertical velocity (v)
                prevX = (x + 0.5f) * cellWidth - dt * sampleU((x + 0.5f) * cellWidth, y * cellHeight);
                prevY = y * cellHeight - dt * v[y][x];
                newV[y][x] = sampleV(prevX, prevY);
    
                // Advect density (d)
                prevX = x * cellWidth - dt * u[y][x];
                prevY = y * cellHeight - dt * v[y][x];
                newD[y][x] = sampleD(prevX, prevY);
            }
        }
    
        // Update the fields with the new values
        u = newU.clone();
        v = newV.clone();
        d = newD.clone();
    }


    public void updateLiquid(double deltaTime){
        float dt = (float) deltaTime;

        addForces(dt);

        solveCompression(dt); //More or less works in a grav tank.

        advect(dt);

        densityColorUpdate();
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
                    int green = (int) (red/3); // Or adjust based on your needs
    
                    // Ensure color values are within [0, 255]
                    red = Math.min(255, Math.max(0, red));
                    blue = Math.min(255, Math.max(0, blue));
                    green = Math.min(255, Math.max(0, green));
    
                    colors[y][x] = new Color(red, green, blue);
                }
            }
    
    }
    
    public void combinedColorUpdate(){
        float dMin = Float.MAX_VALUE;
        float dMax = Float.MIN_VALUE;
        float pMin = Float.MAX_VALUE;
        float pMax = Float.MIN_VALUE;


        // First pass to find the min and max pressure values
        for (int y = 0; y < yCells; y++) {
            for (int x = 0; x < xCells; x++) {
                if (s[y][x] != 0) {
                    dMin = Math.min(dMin, d[y][x]);
                    dMax = Math.max(dMax, d[y][x]);
                    pMin = Math.min(dMin, d[y][x]);
                    pMax = Math.max(dMax, d[y][x]);

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

                float normalizedDensity = (p[y][x] - pMin) / (pMax - pMin);

                // Map normalized pressure to color range [0, 255]
                red += (int) (255 * normalizedDensity);
                blue += 255 - red;
                green += 0; // Or adjust based on your needs

                red /= 2;
                blue /=2;
                green /=2;

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

    public void drawStreamlines(Graphics2D g) {
        
        int streamAmount = 10;
        int streamLength = 40;
    
        // Precompute step sizes
        int xStep = GameWindow.gameWidth / streamAmount;
        int yStep = GameWindow.gameHeight / streamAmount;
    
        // Precompute bounds
        int maxXCell = xCells - 1;
        int maxYCell = yCells - 1;
    
        // Temporary variables to avoid object creation in the loop
        float sx, sy;
        int xPos, yPos;
    
        for (int x = 0; x < GameWindow.gameWidth; x += xStep) {
            for (int y = 0; y < GameWindow.gameHeight; y += yStep) {
                sx = x;
                sy = y;
    
                for (int i = 0; i < streamLength; i++) {
                    xPos = (int) (sx / cellWidth);
                    yPos = (int) (sy / cellHeight);
    
                    // Clamp xPos and yPos within bounds
                    xPos = Math.min(maxXCell, Math.max(0, xPos));
                    yPos = Math.min(maxYCell, Math.max(0, yPos));
    
                    // Draw the streamline segment
                    g.drawLine((int) sx, (int) sy, (int) (sx + u[yPos][xPos]), (int) (sy + v[yPos][xPos]));
    
                    // Update the position
                    sx += u[yPos][xPos];
                    sy += v[yPos][xPos];
                }
            }
        }
    }
}
