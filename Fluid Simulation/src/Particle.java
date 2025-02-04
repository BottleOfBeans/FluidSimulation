
import java.awt.geom.Ellipse2D;


public class Particle{

    //Things to have fun with!
    final Vector2 GRAVITY = new Vector2(0, 0.1f);
    final int RADIUS = 50;
    final float COLLISIONCONST = 0.5f;

    /*
     * DEBUG VALUES, COMMENT WHEN NOT USING
     */
    boolean insideContainer = false;

    //Basic Stufferoni
    Vector2 position;
    Vector2 velocity;
    

    public Particle(){
        position = new Vector2(GameWindow.gameWidth/2, GameWindow.gameHeight/2);
        velocity = new Vector2(0, 0);
    }


    public void updateParticle(float deltaTime){
        //ADD GRAVITY TO ALL OBJECTS!
        velocity = velocity.add(GRAVITY.scale(deltaTime));
        containerInteract();
    }

    public void updatePosition(){
        //ADD Position
        position = position.add(velocity);
    }

    public void containerInteract(){
        Ellipse2D circle = GameWindow.Circular_Container;

        Vector2 contCenter = new Vector2((float) circle.getCenterX(),(float) circle.getCenterY());

        Vector2 difference = position.subtract(contCenter);
        float dist = difference.magnitude();

        float speed = velocity.magnitude();
        

        if(dist >= circle.getHeight()){
            
            this.velocity = difference.normal().scale(speed * COLLISIONCONST);
            
            //FOR DEBUG ONLY!
            insideContainer = false;
        
        }else{
            //FOR DEBUG ONLY!
            insideContainer = true;
        }
    }





    public String debug(){
        String debugPos = "\n" +"( "+position.x + ", "+position.y+" )";
        String debugVelo = "\n"+"( "+velocity.x + ", "+velocity.y+" )";
        String debugCont = "\n" + "Inside Container?: " + insideContainer;

        return "\n" + debugPos + debugVelo + debugCont;
    }

    public Ellipse2D getParticle(){
        return new Ellipse2D.Double(position.x-this.RADIUS, position.y-this.RADIUS, this.RADIUS, this.RADIUS);
    }


}