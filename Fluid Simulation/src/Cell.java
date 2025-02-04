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
 * 
 */




public class Cell {

    Rectangle2D cell;
    
    boolean borderCell = false;

    Vector2 leftV;
    Vector2 rightV;
    Vector2 upV;
    Vector2 downV;

    public Cell(float upperLeft, float upperRight, float width, float height){
        cell = new Rectangle2D.Float(upperLeft, upperRight, width, height);
    }



    public Rectangle2D getCell(){
        return cell;
    }
}
