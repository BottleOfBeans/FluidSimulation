import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
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
    final int CONTAINER = 2;

    final int XCELLS = 500;
    final int YCELLS = 500;

    final float VECTOR_LINE_SCALE = 0.5f;

    final float GRAVITY = -9.8f;
    final float OVER_RELAX_CONST = 1.9f;  //Set between 1 and 2.
    final float DENSITY = 100.0f;

    final float WIND_TUNNEL_SPEED = 50.0f;
    final float DENSITY_STREAM_SPEED = 5.0f;

    final int ITER = 10;
    int CTER = 0;

    //DO NOT TOUCH!

    int xCells = XCELLS;
    int yCells = YCELLS;

    float cellWidth = gameWidth/xCells;
    float cellHeight = gameHeight/yCells;

    float relativeWindSpeed = WIND_TUNNEL_SPEED;

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

    @SuppressWarnings("unused")
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
                    * Setup Boundaries
                    */
                if(SCENE == 0 || SCENE == 2){
                    if(y == 0 || x == 0 || y == yCells-1 || x == xCells-1){
                        s[y][x] = 0; // Set the scalar value to show that it is a wall. 
                        colors[y][x] = Color.DARK_GRAY;
                    }
                }
                if(SCENE == 1){ // Given a wind tunnel!
                    if(y == 0 || y == yCells-1 || x == 0){
                        s[y][x]  = 0;
                        colors[y][x] = Color.DARK_GRAY;
                    }else{
                        s[y][x] = 1;
                    }
                    if(x == 1){
                        u[y][x] = 5.0f;
                    }
                }
        
                /*
                    * Special Cases!
                    */
                
                //Add Container

                if(CONTAINER == 0){ // No Container
                    ; 
                }
                else if (CONTAINER == 1){ // Container is a sphere
                    int radius = 20;
                    Vector2 centerPos = new Vector2(xCells/2 - 20, yCells/2);
                    Vector2 currentPos = new Vector2(x,y);
                    if(currentPos.subtract(centerPos).magnitude() <= radius+ 3){
                        s[y][x] = 0;
                        colors[y][x] = Color.DARK_GRAY;
                    }
    
                }
                else if (CONTAINER == 2){ // Container is an airfoil
                    int length = 250;
                    Vector2 leadingEdge = new Vector2(xCells/2 - 100, yCells/2); // Approximate center
                
                    Vector2 currentPos = new Vector2(x, y);
                    double xRel = currentPos.x - leadingEdge.x;
                    double yRel = currentPos.y - leadingEdge.y;
                
                    // NACA-like symmetric airfoil shape equation (approximation)
                    double thickness = 0.12; // Adjust for different airfoil shapes
                    double chord = length;
                    double maxHeight = thickness * chord; 
                
                    double yUpper = (maxHeight / 0.2) * (0.2969 * Math.sqrt(xRel / chord) 
                                    - 0.126 * (xRel / chord) 
                                    - 0.3516 * Math.pow(xRel / chord, 2) 
                                    + 0.2843 * Math.pow(xRel / chord, 3) 
                                    - 0.1015 * Math.pow(xRel / chord, 4));
                
                    double yLower = -yUpper; // Symmetric airfoil
                
                    if (xRel >= 0 && xRel <= length && yRel >= yLower && yRel <= yUpper) {
                        s[y][x] = 0;
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
    public void addForces(float dt) {
        for (int y = 0; y < yCells; y++) {
            for (int x = 0; x < xCells; x++) {
                if (SCENE == 0) { // GRAVITY TANK
                    // ADDED POST RELEASE FIX: guard y>0 and scale by dt
                    if (y > 0 && s[y][x] == 1 && s[y - 1][x] != 0) {
                        v[y][x] += GRAVITY * dt;
                    }
                    if (y == yCells - 2 && x < 80 && x > 20) {
                        d[y][x] += 0.5f;
                    }
                } else if (SCENE == 1) { // WIND TUNNEL
                    if (x == 1 && y != 0 && y != yCells - 1) {
                        v[y][x] = 0;
                        u[y][x] = relativeWindSpeed; // keep inflow at x=1
                        d[y][1] = 0.0f;
                    }
                } else if (SCENE == 2) {
                    if (y == (int)(yCells / 2) && x == (int)(xCells / 2)) {
                        u[y][x] = 5.0f;
                        d[y][x] = 5.0f;
                    }
                }
            }
        }
    }
    
    public void solveCompression(float dt){

        float pc = DENSITY * cellHeight * dt;

        for(int i = 0; i < ITER; i ++){
            for(int x = 0; x < xCells - 1; x++){
                for(int y = 0; y < yCells - 1; y++){

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
            // POST RELEASE FIX: left = inflow (if wind tunnel), otherwise copy interior
            if (SCENE == 1) {
                u[y][0] = relativeWindSpeed;
                v[y][0] = 0f;
                d[y][0] = d[y][1];
                p[y][0] = p[y][1];
            } else {
                u[y][0] = u[y][1];
            }
    
            // POST RELEASE FIX: right = outflow (zero-gradient)
            u[y][xCells - 1] = u[y][xCells - 2];
            v[y][xCells - 1] = v[y][xCells - 2];
            d[y][xCells - 1] = d[y][xCells - 2];
            p[y][xCells - 1] = p[y][xCells - 2];
        }
    
        // Vertical velocity (v) + top/bottom no-slip
        for (int x = 0; x < xCells; x++) {
            v[0][x] = 0;           // top boundary
            v[yCells - 1][x] = 0;  // bottom boundary
            u[0][x] = 0;
            u[yCells - 1][x] = 0;
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
        // Make stable copies to sample from
        float[][] u0 = new float[yCells][xCells];
        float[][] v0 = new float[yCells][xCells];
        for (int y = 0; y < yCells; y++) {
            System.arraycopy(u[y], 0, u0[y], 0, xCells);
            System.arraycopy(v[y], 0, v0[y], 0, xCells);
        }

        // FIX: iterate only interior (avoid x-1, y-1 OOB access)
        for (int y = 1; y < yCells - 1; y++) {
            for (int x = 1; x < xCells - 1; x++) {
                if (s[y][x] == 0) { newU[y][x] = 0; newV[y][x] = 0; continue; }

                // Advect u at vertical face (centered at (x, y+0.5))
                float xPosU = x * cellWidth;
                float yPosU = (y + 0.5f) * cellHeight;
                float uVel = u0[y][x];
                float vVel = 0.25f * (v0[y][x - 1] + v0[y][x] + v0[y + 1][x - 1] + v0[y + 1][x]);
                xPosU -= uVel * dt;
                yPosU -= vVel * dt;
                newU[y][x] = sampleField(xPosU, yPosU, "UFIELD"); // samples old field

                // Advect v at horizontal face (centered at (x+0.5, y))
                float xPosV = (x + 0.5f) * cellWidth;
                float yPosV = y * cellHeight;
                float uBar = 0.25f * (u0[y - 1][x] + u0[y][x] + u0[y - 1][x + 1] + u0[y][x + 1]);
                float vBar = v0[y][x];
                xPosV -= uBar * dt;
                yPosV -= vBar * dt;
                newV[y][x] = sampleField(xPosV, yPosV, "VFIELD"); // samples old field
            }
        }

        for (int y = 1; y < yCells - 1; y++) {
            System.arraycopy(newU[y], 1, u[y], 1, xCells - 2);
            System.arraycopy(newV[y], 1, v[y], 1, xCells - 2);
            Arrays.fill(newU[y], 1, xCells - 1, 0f);
            Arrays.fill(newV[y], 1, xCells - 1, 0f);
        }
}
    
    public void advectDensity(float dt) {
        float[][] tempD = new float[yCells][xCells];

        for (int y = 1; y < yCells - 1; y++) {
            for (int x = 1; x < xCells - 1; x++) {
                if (s[y][x] == 0) continue; // Skip walls

                float uVel = 0.5f * (u[y][x] + u[y][x + 1]);
                float vVel = 0.5f * (v[y][x] + v[y + 1][x]);

                float xPos = (x + 0.5f) * cellWidth  - uVel * dt; 
                float yPos = (y + 0.5f) * cellHeight - vVel * dt;

                xPos = Math.max(cellWidth  * 0.5f, Math.min(xPos, (xCells - 1.5f) * cellWidth));
                yPos = Math.max(cellHeight * 0.5f, Math.min(yPos, (yCells - 1.5f) * cellHeight));

                tempD[y][x] = sampleField(xPos, yPos, "DFIELD");
            }
        }

        // --- 2) Min/Max for normalization ---
        float dMin = Float.MAX_VALUE, dMax = Float.MIN_VALUE;
        float pMin = Float.MAX_VALUE, pMax = Float.MIN_VALUE;
        for (int y = 1; y < yCells - 1; y++) {
            for (int x = 1; x < xCells - 1; x++) {
                if (s[y][x] == 0) continue;
                float dv = tempD[y][x];
                dMin = Math.min(dMin, dv);
                dMax = Math.max(dMax, dv);
                float pv = p[y][x];
                pMin = Math.min(pMin, pv);
                pMax = Math.max(pMax, pv);
            }
        }
        float eps = 1e-6f;
        float dRange = Math.max(eps, dMax - dMin);
        float pRange = Math.max(eps, pMax - pMin);

        // --- 3) Color mapping: pressure as faint background, dye bright in front ---
        // Tuning knobs 
        final float PRESSURE_BLEND = 0.50f;   // how visible pressure is 
        final float PRESSURE_SAT   = 1.00f;   // pressure tint saturation 
        final float PRESSURE_VAL   = 0.25f;   // pressure tint brightness 
        final float DYE_FLOOR      = 0.08f; 
        final float DYE_GAMMA      = 0.70f;  

        for (int y = 1; y < yCells - 1; y++) {
            for (int x = 1; x < xCells - 1; x++) {
                if (s[y][x] == 0) {
                    colors[y][x] = Color.DARK_GRAY; // obstacle remains clearly visible
                    continue;
                }

                // Normalize fields
                float dv = tempD[y][x];
                float dyeNorm = (dv - dMin) / dRange;           // 0..1
                dyeNorm = Math.max(0f, Math.min(1f, dyeNorm));
                // "Lit" dye brightness: boosted mid-tones, with a small floor
                float dyeVal = DYE_FLOOR + (1f - DYE_FLOOR) * (float)Math.pow(dyeNorm, DYE_GAMMA);

                float presNorm = (p[y][x] - pMin) / pRange;     // 0..1
                presNorm = Math.max(0f, Math.min(1f, presNorm));

                // Pressure hue: low → blue (240°), high → red (0°)
                float hue = (1.0f - presNorm) * (240f / 360f);  // map to H in [0..1]

                // Compute pressure tint (dim & desaturated)
                int presRGB = Color.HSBtoRGB(hue, PRESSURE_SAT, PRESSURE_VAL);
                int pr = (presRGB >> 16) & 0xFF;
                int pg = (presRGB >> 8)  & 0xFF;
                int pb = (presRGB)       & 0xFF;

                // Compute dye color (bright grayscale “in front”)
                int dyeRGB = Color.HSBtoRGB(0f, 0f, dyeVal);
                int dr = (dyeRGB >> 16) & 0xFF;
                int dg = (dyeRGB >> 8)  & 0xFF;
                int db = (dyeRGB)       & 0xFF;

                // Pre-blend: final = mix(dye, pressureTint, PRESSURE_BLEND) but with dye dominant
                float w = 1f - PRESSURE_BLEND; // ~0.88 dye, ~0.12 pressure
                int r = Math.min(255, Math.max(0, Math.round(w * dr + (1f - w) * pr)));
                int g = Math.min(255, Math.max(0, Math.round(w * dg + (1f - w) * pg)));
                int b = Math.min(255, Math.max(0, Math.round(w * db + (1f - w) * pb)));

                colors[y][x] = new Color(r, g, b);
            }
        }

        // --- 4) Commit dye field ---
        d = tempD;
    }

    public void densityHandler(int count){
        if(SCENE == 1){

            int streamSize = 15;

            // System.out.println(CTER);

            // if(CTER % 10 == 0){
            //     for(int y = 0; y < yCells; y++){
            //         d[y][2] = DENSITY_STREAM_SPEED;
            //     }
            // }

            
            for(int y = 0; y < yCells; y++){
                if(y == (int) (yCells/2 + 5)){
                    for(int i = 0; i < streamSize; i++){
                        d[y + i][3] += DENSITY_STREAM_SPEED;
                    }
                }
                if(y == (int) (yCells/2 - 5)){
                    for(int i = 0; i < streamSize; i++){
                        d[y - i][3] += DENSITY_STREAM_SPEED;
                    }
                }
            }
        }

    }

    public float adjustDt(float dt) {
        float maxU = 0.0f, maxV = 0.0f;
        for (int y = 1; y < yCells - 1; y++) {
            for (int x = 1; x < xCells - 1; x++) {
                if (s[y][x] == 0) continue;
                maxU = Math.max(maxU, Math.abs(u[y][x]));
                maxV = Math.max(maxV, Math.abs(v[y][x]));
            }
        }
        float cflX = (maxU * dt) / cellWidth;
        float cflY = (maxV * dt) / cellHeight;
                
        float cfl = Math.max(cflX, cflY);  

        //AFTER PATCH FIX: let semi-Lagrangian run “looser”
        float maxCFL = 3.0f;                // was 1.0f

        if (cfl > maxCFL) {
            dt = dt * (maxCFL / cfl);
        }
        return dt;

    }
    
    public void updateLiquid(double deltaTime) {
        CTER++;
        float dt = (float) deltaTime;
        dt = adjustDt(dt);
    
        addForces(dt);
    
        // Clear pressure
        for (int i = 0; i < xCells; i++) {
            for (int j = 0; j < yCells; j++) {
                p[j][i] = 0.0f;
            }
        }
    
        // First projection 
        solveCompression(dt);
        boundaryFix(dt);
    
        // Advect and then project once more to remove new divergence
        advectVelocities(dt);
        boundaryFix(dt);
        solveCompression(dt);     // FIX: extra projection
        boundaryFix(dt);
    
        advectDensity(dt);
        densityHandler(CTER);
        if (CTER >= 100) CTER = 0;
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
        // Parameters from JS code
        float segLen = cellHeight * 0.5f; // Equivalent to f.h * 0.2
        int numSegs = 100; // Number of segments per streamline
    
        // Set stroke color (equivalent to c.strokeStyle = "#000000")
        g.setColor(Color.WHITE);
    
        // Loop through the grid (equivalent to JS loops)
        for (int i = 1; i < xCells - 1; i += segLen) {
            for (int j = 1; j < yCells - 1; j += segLen) {
                // Starting point of the streamline (equivalent to (i + 0.5) * f.h)
                float x = (i + 0.5f) * cellWidth;
                float y = (j + 0.5f) * cellHeight;
    
                // Begin a new path (equivalent to c.beginPath())
                GeneralPath path = new GeneralPath();
                path.moveTo(x, y); // Equivalent to c.moveTo(cX(x), cY(y))
    
                // Draw the streamline segments
                for (int n = 0; n < numSegs; n++) {
                    // Sample the velocity field (equivalent to f.sampleField(x, y, U_FIELD))
                    float uVel = sampleField(x, y, "UFIELD");
                    float vVel = sampleField(x, y, "VFIELD");
    
                    // Update the position (equivalent to x += u * 0.01; y += v * 0.01)
                    x += uVel * 0.1;
                    y += vVel * 0.1f;
    
                    // Break if the streamline goes out of bounds (equivalent to if (x > f.numX * f.h))
                    if (x > xCells * cellWidth) {
                        break;
                    }
    
                    // Add a line segment to the path (equivalent to c.lineTo(cX(x), cY(y)))
                    path.lineTo(x, y);
                }
    
                // Draw the streamline (equivalent to c.stroke())
                g.draw(path);
            }
        }
    }
}

