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

    /*
     * SCENE 1: A GRAVITY TANK, FOR PRESSURE VISUALIZATION AND GOOD FOR CHECKING COMPRESSION SOLVER
     * SCENE 2: A WIND TUNNEL, THE MAIN SCENE THAT I AM TRYING TO IMPLEMENT AS FLUID TRAVERSES AROUND A SPHERE
     * SCENE 3: VELOCITY INJECTOR, A VELOCITY STREAM INTRODUCED INTO A STATIC TANK
     * 
     * CONTAINER 1: SPHERE
     * CONTAINER 2: CUBE
     * CONTAINER 3: PEGS
     */

    final int SCENE = 1;
    final int CONTAINER = 1;

    final int XCELLS = 198;
    final int YCELLS = 198;

    final float VECTOR_LINE_SCALE = 0.5f;

    final float GRAVITY = -9.8f;
    final float OVER_RELAX_CONST = 1.9f;  //Set between 1 and 2.
    final float DENSITY = 100.0f;

    final float WIND_TUNNEL_SPEED = 500.0f;
    final float DENSITY_STREAM_SPEED = 15.0f;

    final int ITER = 200;

    //DO NOT TOUCH!

    int xCells = XCELLS;
    int yCells = YCELLS;

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
                colors[y][x] = Color.black;

                /*
                 * Special Cases!
                 */
                
                //Add Container

                if(CONTAINER == 0){ // No Container
                    ; 
                }
                else if (CONTAINER == 1){ // Container is a sphere
                    int radius = 20;
                    Vector2 centerPos = new Vector2(xCells/2 - 40, yCells/2);
                    Vector2 currentPos = new Vector2(x,y);
                    if(currentPos.subtract(centerPos).magnitude() < radius){
                        s[y][x] = 0;
                        colors[y][x] = Color.GRAY;
                    }
    
                    //Check if a Boundary
                    if(y == 0 || x == 0 || y == yCells-1 || x == xCells-1){
                        s[y][x] = 0; // Set the scalar value to show that it is a wall. 
                        colors[y][x] = Color.DARK_GRAY;
                    }
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
                
                if(SCENE == 0){ //GRAVITY TANK
                    if(s[y][x] == 1 && s[y - 1][x] != 0){
                        v[y][x] += GRAVITY;
                    }
                    if(y == yCells-2 && x < 80 && x > 20){
                        d[y][x] += 0.5f;
                    }
                } 
                else if (SCENE == 1){ // WIND TUNNEL
                    if(x == 1 && y != 0 && y != yCells-1){ // Inflow
                        v[y][x] = 0;
                        u[y][x] = WIND_TUNNEL_SPEED;
                        

                        Random rand = new Random();
                        int offset = rand.nextInt(5) - 3;

                        //Single Big Stream
                        // if(y == (int) (yCells/2 - (1 + offset)) &&  x == 1){
                        //     for(int i = 0; i < 4; i ++){
                        //         d[y - offset + i][x + 2] += DENSITY_STREAM_SPEED;
                        //     }
                        // }

                        if(y % 20 == 0){
                            d[y][x + 2] += DENSITY_STREAM_SPEED * 5;
                        }

                    }
                }
                else if (SCENE == 2){//VELOCITY INJECTOR
                    Random rand = new Random();
                    int offset = rand.nextInt(6) - 3;
                    if(y == (int) (yCells/2 - offset) && x == 1){
                        d[y + offset][x] = DENSITY_STREAM_SPEED;
                        u[y + offset][x] += WIND_TUNNEL_SPEED;
                    }             
                }
                
            }
        }
    }
    
    public void solveCompression(float dt){

        float pc = DENSITY * cellHeight / dt;

        for(int i = 0; i < ITER; i ++){
            for(int x = 1; x < xCells - 1; x++){
                for(int y = 1; y < yCells - 1; y++){

                    if(s[y][x] == 0){continue;} // Skip Walls!

                    float sSum = s[y][x - 1] + s[y][x + 1] + s[y - 1][x] + s[y + 1][x];

                    if(sSum == 0){continue;} // If Surrounded on all sides then don't calculate;

                    float divergence = u[y][x + 1] - u[y][x] + v[y + 1][x] - v[y][x];

                    float pressure = -divergence / sSum;
                    pressure = pressure * OVER_RELAX_CONST;
                    p[y][x] += pc * pressure;

                    u[y][x]     -=      s[y][x - 1]     * pressure;
                    u[y][x + 1] +=      s[y][x + 1]     * pressure;
                    v[y][x]     -=      s[y - 1][x]     * pressure;
                    v[y + 1][x] +=      s[y + 1][x]     * pressure;

                }
            }
        }

    }

    public void boundaryFix(float dt) {
        // Horizontal velocity (u) boundaries
        for (int y = 0; y < yCells; y++) {
            u[y][0] = 0 ; // Left boundary
            u[y][xCells - 1] = 0; // Right boundary            

            if(SCENE == 1){ //If wind tunnel then outflow conditions apply!
                u[y][xCells - 2] = u[y][xCells - 3];
                d[y][xCells - 2] = d[y][xCells - 3];
                p[y][xCells - 2] = d[y][xCells - 3];
            }
        }
    
        // Vertical velocity (v) boundaries
        for (int x = 0; x < xCells; x++) {
            v[0][x] = 0; // Top boundary
            v[yCells - 1][x] = 0; // Bottom boundary
        }
    }

    public float sampleField(float x, float y, String type) {
        // Lock down the boundaries
        x = Math.max(0, Math.min(x, (xCells - 1) * cellWidth));
        y = Math.max(0, Math.min(y, (yCells - 1) * cellHeight));
    
        float dx, dy;
        float[][] sampleField;
    
        switch (type) {
            case "UFIELD":
                // U values are stored at the middle of vertical edges (staggered in x)
                dx = 0.0f; // Offset in x-direction
                dy = 0.5f; // No offset in y-direction
                sampleField = u;
                break;
    
            case "VFIELD":
                // V values are stored at the middle of horizontal edges (staggered in y)
                dx = 0.5f; // No offset in x-direction
                dy = 0.0f; // Offset in y-direction
                sampleField = v;
                break;
            case "DFIELD":
                // V values are stored at the middle of horizontal edges (staggered in y)
                dx = 0.5f; // No offset in x-direction
                dy = 0.5f; // Offset in y-direction
                sampleField = d;
            break;

            default:
                System.out.println("NOT A CORRECT FIELD VALUE");
                return 0.0f;
        }
    
        // Adjust the position for staggered grid
        float staggeredX = x - dx * cellWidth;
        float staggeredY = y - dy * cellHeight;
    
        // Calculate the grid cell indices
        int xPos = (int) (staggeredX / cellWidth);
        int yPos = (int) (staggeredY / cellHeight);
    
        // Clamp the indices to stay within the grid boundaries
        xPos = Math.max(1, Math.min(xPos, xCells - 2));
        yPos = Math.max(1, Math.min(yPos, yCells - 2));
    
        // Calculate the fractional part within the cell
        float i = (staggeredX % cellWidth) / cellWidth;
        float j = (staggeredY % cellHeight) / cellHeight;
    
        // Perform bilinear interpolation
        float q11 = sampleField[yPos][xPos];
        float q12 = sampleField[yPos + 1][xPos];
        float q21 = sampleField[yPos][xPos + 1];
        float q22 = sampleField[yPos + 1][xPos + 1];

        float r1 = q11 * (1 - i) + q21 * i; // Interpolate along x-axis
        float r2 = q12 * (1 - i) + q22 * i; // Interpolate along x-axis
        float result = r1 * (1 - j) + r2 * j; // Interpolate along y-axis
    
        return result;
    }
    
    public float averageU(int x, int y){
        return (u[y - 1][x] + u[y][x] + u[y - 1][x + 1] + u[y][x + 1]) * 0.25f;
    }
    public float averageV(int x, int y){
        return (v[y][x - 1] + v[y][x] + v[y + 1][x - 1] + v[y + 1][x]) * 0.25f;
    }

    public void advectVelocities(float dt) {
        for (int y = 1; y < yCells - 1; y++) {
            for (int x = 1; x < xCells - 1; x++) {
                if (s[y][x] == 0) continue; // Skip walls
    
                if(s[y][x - 1] != 0){
                    // Advect horizontal velocity (u)
                    float xPosU = x * cellWidth;
                    float yPosU = (y + 0.5f) * cellHeight;
        
                    float uVel = u[y][x];
                    float vVel = averageV(x, y);
        
                    xPosU -= uVel * dt;
                    yPosU -= vVel * dt;

                    newU[y][x] = sampleField(xPosU, yPosU, "UFIELD");
                }
    
    
                if(s[y - 1][x] != 0){
                    // Advect vertical velocity (v)
                    float xPosV = (x + 0.5f) * cellWidth;
                    float yPosV = y * cellHeight;
        
                    float uVel = averageU(x, y);
                    float vVel = v[y][x];
        
                    xPosV -= uVel * dt;
                    yPosV -= vVel * dt;
        
                    newV[y][x] = sampleField(xPosV, yPosV, "VFIELD");
                }

            }
        }
    
        // Update velocities
        u = newU.clone();
        v = newV.clone();

    }        
    
    public void advectDensity(float dt) {
        // Temporary array to store the new density values
        newD = new float[yCells][xCells];
    
        // Half cell width and height for staggered grid
        float h2x = cellWidth * 0.5f;
        float h2y = cellHeight * 0.5f;
    
        for (int y = 1; y < yCells - 1; y++) {
            for (int x = 1; x < xCells - 1; x++) {
                if (s[y][x] == 0) continue; // Skip walls
    
                // Calculate the velocity at the cell center
                float uVel = (u[y][x] + u[y][x + 1]); // Average horizontal velocity
                float vVel = (v[y][x] + v[y + 1][x]); // Average vertical velocity
    
                // Calculate the previous position using backward tracing
                float xPos = (x + 0.5f) * cellWidth - uVel * dt;
                float yPos = (y + 0.5f) * cellHeight - vVel * dt;
    
                // Sample the density at the previous position
                newD[y][x] = sampleField(xPos, yPos, "DFIELD");
            }
        }
    
        // Update the density field
        d = newD.clone();
    }

    public float adjustDt(float dt) {
        // Find the maximum velocity magnitude in the grid
        float maxU = 0.0f;
        float maxV = 0.0f;
    
        for (int y = 1; y < yCells - 1; y++) {
            for (int x = 1; x < xCells - 1; x++) {
                if (s[y][x] == 0) continue; // Skip walls
    
                maxU = Math.max(maxU, Math.abs(u[y][x]));
                maxV = Math.max(maxV, Math.abs(v[y][x]));
            }
        }
    
        // Calculate the CFL number
        float cflX = (maxU * dt) / cellWidth;
        float cflY = (maxV * dt) / cellHeight;
        float cfl = cflX + cflY;
    
        // Define the maximum allowed CFL number
        float maxCFL = 1.0f; // Can be relaxed for Semi-Lagrangian, but 1 is a safe default
    
        // Adjust dt if the CFL condition is violated
        if (cfl > maxCFL) {
            dt = dt * (maxCFL / cfl); // Scale dt to satisfy the CFL condition
            //System.out.println("CFL condition violated! Adjusted dt to: " + dt);
        }
    
        return dt;
    }
    
    public void updateLiquid(double deltaTime){
        float dt = (float) deltaTime;
        dt = adjustDt(dt);
        addForces(dt);

        solveCompression(dt); //More or less works in a grav tank.
        boundaryFix(dt);

        advectVelocities(dt);
        advectDensity(dt);

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
            float dMin = 0.0f;
            float dMax = 200.0f;
            
            // Second pass to update colors based on normalized pressure
            for (int y = 0; y < yCells; y++) {
                for (int x = 0; x < xCells; x++) {
                    if (s[y][x] == 0) {
                        continue; // Skip cells with no fluid
                    }
    
                    // Normalize pressure to [0, 1] range
                    float normalizedPressure = (d[y][x] - dMin) / (dMax - dMin);
    
                    // Map normalized pressure to color range [0, 255]
                    // int red = (int) (255 * normalizedPressure);
                    // int blue = 255 - red;
                    // int green = (int) (red/3); // Or adjust based on your needs
    
                    //GRAYSCALE
                    int red = (int) (255 * normalizedPressure);
                    int blue = red;
                    int green = red; // Or adjust based on your needs

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
        
        int streamAmount = 100;
        int streamLength = 10;
    
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
