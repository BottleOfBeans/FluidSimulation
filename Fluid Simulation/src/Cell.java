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




public class Cell extends GameWindow{

    final Vector2 GRAVITY = new Vector2(0f, 0f);
    final float VELOCITY_LINE_SCALE = 100f;
    final int MAX_VECTOR_LINE = 100;
    final float WIND_TUNNEL_SPEED = 5;
    final int COLOR_SCALE = 1;
    final float DENSITY = 1;

    Vector2 velocity = new Vector2(0,0);
    Vector2 pos;
    
    Rectangle2D cell;

    Vector2 top     = new Vector2(0,0);
    Vector2 left    = new Vector2(0,0);
    Vector2 right   = new Vector2(0,0);
    Vector2 bottom  = new Vector2(0,0);

    int s = 1; //Scalar Value Determining If Fluid or Not!
    boolean sourceBlock = false;

    Vector2 d = new Vector2(0,0);
    Vector2 p = new Vector2(0,0);

    float v; 
    
    float u;


    public Cell(float x, float y, float width, float height, int yPos, int xPos){
        cell = new Rectangle2D.Float(x, y, width, height);
        pos = new Vector2(xPos, yPos);
        if(pos.x == 0 || pos.y == 0 || pos.x == cells[0].length - 1 || pos.y == cells.length -1){
            s = 0;
        }
        if(pos.x == 1 && s == 1){
            sourceBlock = true;
        }
    }

    public void update(float dt){

        //1. Apply Forces
        applyForces(dt);
        
        //2. Make Incompressible
        solveIncompressible(dt);
        solvePressure(dt);

        //3. Move the Velocity Field
        solveVelocity(dt);
    }

    public void applyForces(float dt){
        if(!sourceBlock){
            velocity = velocity.add(GRAVITY.scale(dt));
        }
    }

    public void solveIncompressible(float dt) throws NullPointerException{
        if(s == 0){return;}
        
        Cell upperCell = cells[(int) pos.y - 1][(int) pos.x];
        Cell lowerCell = cells[(int) pos.y + 1][(int) pos.x];
        Cell rightCell = cells[(int) pos.y][(int) pos.x + 1];
        Cell leftCell = cells[(int) pos.y][(int) pos.x - 1];

        if(upperCell == null || lowerCell == null || rightCell == null || leftCell == null){return;}

        d = this.right.subtract(this.left).add(this.top.subtract(this.bottom));
        s = upperCell.s + lowerCell.s + rightCell.s + leftCell.s;

        //DEBUG TO SEE WHAT RESULTS IN NULL!
        //System.out.println("X: " + pos.x + ", Y: "+pos.y);
        
        this.right = this.left.subtract(d.scale(rightCell.s / this.s));
        this.left = this.left.add(d.scale(leftCell.s / this.s));
        this.top = this.left.subtract(d.scale(upperCell.s / this.s));
        this.bottom = this.left.add(d.scale(lowerCell.s / this.s));        
    
        if(sourceBlock){
            this.right = new Vector2(-WIND_TUNNEL_SPEED * dt, 0);
            this.left = new Vector2(0, 0);
            this.top = new Vector2(0, 0);
            this.bottom = new Vector2(0, 0);
        }

    }

    public void solveVelocity(float dt){
        
        if(s==0){return;}
        
        velocity = this.top.add(this.bottom.add(this.left).add(this.right));

        Cell upperCell = cells[(int) pos.y - 1][(int) pos.x];
        Cell lowerCell = cells[(int) pos.y + 1][(int) pos.x];
        Cell rightCell = cells[(int) pos.y][(int) pos.x + 1];
        Cell leftCell = cells[(int) pos.y][(int) pos.x - 1];

        if(upperCell == null || lowerCell == null || rightCell == null || leftCell == null){return;}

        top = (upperCell.bottom.add(this.top)).scale(0.5f);
        bottom = (lowerCell.top.add(this.bottom)).scale(0.5f);
        right = (rightCell.left.add(this.right)).scale(0.5f);
        left = (leftCell.right.add(this.right)).scale(0.5f);

        v = velocity.magnitude();
    }

    public void solvePressure(float dt){
        if(s == 0){return;}
        p = p.add(d.scale((1/s) * DENSITY / dt));
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

            int red = (int) (255 - p.magnitude() * COLOR_SCALE);
            int blue = (int) p.magnitude() * COLOR_SCALE;
            int green = 0;

            red = Math.min(Math.max(0, red), 255);
            blue = Math.min(Math.max(0, blue), 255);
            green = Math.min(Math.max(0, green), 255);

            return new Color(red,blue,green);
        }else{
            return Color.BLACK;
        }
    }
    
    public void debugString(Graphics2D graphics){
        graphics.setFont(new Font("Arial", Font.BOLD, 2));
        graphics.setColor(Color.RED);
        graphics.drawString((String)("X: "+this.pos.x+", Y: "+this.pos.y), (int) cell.getMinX()+10 ,(int) cell.getMinY()+10);
        graphics.setColor(Color.ORANGE);
        graphics.drawString((String)("V_X: "+(int) this.velocity.x+", V_Y: "+(int) this.velocity.y), (int) cell.getMinX()+10 ,(int) cell.getMinY()+20);
        graphics.drawString((String)("P "+p.magnitude()), (int) cell.getMinX()+10 ,(int) cell.getMinY()+30);

    }

    public Rectangle2D getCell(){
        return cell;
    }
}
