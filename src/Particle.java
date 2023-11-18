import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public class Particle {

    //final double DAMPENING = 0.9;
    final int RADIUS = 20;
    final Vector2 GRAVITY = new Vector2(0,1);
    final Vector2 MAXVELOCITY = new Vector2(8,8);
    final int SUBSTEPS = 50;

    private Vector2 currentPos;
    private Vector2 prevPos;
    private Vector2 velocity;
    private int diameter;

    public Particle(Vector2 currentPos){
        
        Random rand = new Random();
        
        this.currentPos = currentPos;
        this.prevPos = currentPos;
        this.velocity = this.currentPos.returnSubtract(this.prevPos);
        diameter = RADIUS * 2;

        ;

    }

    public void computePosition(Rectangle2D boundary, double deltaTime){
        
        //Calculating velocity
        velocity = currentPos.returnSubtract(prevPos);

        //Applying forces
        velocity.addVector(GRAVITY);

        //Updating position
        prevPos = currentPos;
        currentPos.addVector(velocity);

        //Application of constraint post calculations allows for accurate acceleration calculations
        applyConstaint(boundary);
    }



    /*
     * Applies a rectangular constraint on all particles
     */
    public void applyConstaint(Rectangle2D constraint){ 
      
        if(currentPos.y + RADIUS > constraint.getMaxY()){ //Bottom Constraint
            double distanceFromContainer = currentPos.y + RADIUS - constraint.getMaxY();
            this.currentPos.y -= distanceFromContainer;
        }
        if(currentPos.y - RADIUS < constraint.getMinY()){ //Top Constaint
            double distanceFromContainer = currentPos.y - (RADIUS + constraint.getMinY());
            this.currentPos.y -= distanceFromContainer;
        }
        if(currentPos.x + RADIUS > constraint.getMaxX()){ //Right Constraint
            double distanceFromContainer = currentPos.x + RADIUS - constraint.getMaxX();
            this.currentPos.x -= distanceFromContainer;
        }
        if(currentPos.x - RADIUS < constraint.getMinX()){ //Left Constraint
            double distanceFromContainer = currentPos.x - (RADIUS + constraint.getMinX());
            this.currentPos.x -= distanceFromContainer;
        }

    }

    public void solveCollisions(){
        ;
    }

    
    public Vector2 getCurrentPos(){
        return currentPos;
    }
    public Ellipse2D getParticle(){
        return new Ellipse2D.Double(currentPos.x - RADIUS, currentPos.y - RADIUS, diameter, diameter);
    }

    public void setPreviousPos(Vector2 c){
        prevPos = c;
    }
    public void setCurrentPos(Vector2 c){
        currentPos = c;
    }
}
