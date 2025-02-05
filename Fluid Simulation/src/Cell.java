import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

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




public class Cell extends GameWindow{

    final Vector2 GRAVITY = new Vector2(0, .5f);
    final float VELOCITY_LINE_SCALE = .2f;
    final int MAX_VECTOR_LINE = 100;
    final float WIND_TUNNEL_SPEED = 10;

    Vector2 velocity = new Vector2(0,0);
    Vector2 pos;
    
    Rectangle2D cell;

    Vector2 top     = new Vector2(0,0);
    Vector2 left    = new Vector2(0,0);
    Vector2 right   = new Vector2(0,0);
    Vector2 bottom  = new Vector2(0,0);

    int s = 1; //Scalar Value Determining If Fluid or Not!
    boolean sourceBlock = false;

    Vector2 d;

    float v; 
    float p;
    float u;


    public Cell(float x, float y, float width, float height, int yPos, int xPos){
        cell = new Rectangle2D.Float(x, y, width, height);
        pos = new Vector2(xPos, yPos);
        if(pos.x == 0 || pos.y == 0 || pos.x == cells[0].length - 1 || pos.y == cells.length -1){
            s = 0;
        }
    }

    public void update(float dt){

        System.out.println(dt);

        //1. Apply Forces
        applyForces(dt);

        //2. Make Incompressible
        solveIncompressible();

        //3. Move the Velocity Field
        
    }


    public void applyForces(float dt){
        if(!sourceBlock){
            velocity = velocity.add(GRAVITY.scale(dt));
        }
    }

    public void solveIncompressible(){
        
        if(s==0){return;}

        // Top - Bottom + Right - Left
        d = top.subtract(bottom).add(right.subtract(left)); // Calculating Divergence as Vector2
        
        if(sourceBlock){
            top = top.subtract(d.scale(cells[(int) pos.x][(int) pos.y - 1].s / this.s));
            bottom = bottom.add(d.scale(cells[(int) pos.x][(int) pos.y + 1].s / this.s)); 
            right = right.subtract(d.scale(cells[(int) pos.x + 1][(int) pos.y].s / this.s));
            left = new Vector2(-WIND_TUNNEL_SPEED,0); // Adding Wind Tunnel
        }else{
            top = top.subtract(d.scale(cells[(int) pos.x][(int) pos.y - 1].s / this.s));
            bottom = bottom.add(d.scale(cells[(int) pos.x][(int) pos.y + 1].s / this.s)); 
            right = right.subtract(d.scale(cells[(int) pos.x + 1][(int) pos.y].s / this.s));
            left = left.subtract(d.scale(cells[(int) pos.x - 1][(int) pos.y].s / this.s)); 
        }

    }



    /*
     * Functions relating to displaying and graphics.
     */

    public Line2D getVelocityLine(){

        Vector2 length = velocity.normal().scale(VELOCITY_LINE_SCALE);

        float StartingX = (float) cell.getCenterX() - length.x/2;
        float StartingY = (float) cell.getCenterY() - length.y/2;

        return new Line2D.Float(StartingX, StartingY, StartingX + length.x, StartingY + length.y);
    }

    public Color getColor(){
        if(sourceBlock){
            return Color.BLUE;
        }
        if(s == 0){
            return Color.DARK_GRAY;
        }
        if(s ==1){
            return Color.GRAY;
        }else{
            return Color.BLACK;
        }
    }
    
    public void debugString(Graphics2D graphics){
        graphics.setFont(new Font("Arial", Font.BOLD, 10));
        graphics.setColor(Color.RED);
        graphics.drawString((String)("X: "+this.pos.x+", Y: "+this.pos.y), (int) cell.getMinX()+10 ,(int) cell.getMinY()+10);
        graphics.setColor(Color.ORANGE);
        graphics.drawString((String)("V_X: "+(int) this.velocity.x+", V_Y: "+(int) this.velocity.y), (int) cell.getMinX()+10 ,(int) cell.getMinY()+20);

    }

    public Rectangle2D getCell(){
        return cell;
    }
}
