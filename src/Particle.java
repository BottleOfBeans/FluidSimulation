import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;

public class Particle {

    final int RADIUS = 2;
    final Vector2 GRAVITY = new Vector2(0,2);

    private Vector2 currentPos;
    private Vector2 prevPos;
    private int diameter;

    public Particle(Vector2 currentPos){
        this.currentPos = currentPos;
        this.prevPos = currentPos;
        diameter = RADIUS * RADIUS;
    }

    public void computePosition(){
        Vector2 velocity = currentPos.returnSubtract(prevPos);

        //Adding Gravity
        currentPos.addVector(GRAVITY);


        //Adding Velocity
        currentPos.addVector(velocity);

    }

    public Ellipse2D getParticle(){
        return new Ellipse2D.Double(currentPos.x - RADIUS, currentPos.y - RADIUS, diameter, diameter);
    }

}
