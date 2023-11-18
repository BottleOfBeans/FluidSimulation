import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.*;

public class MouseHandler implements MouseListener{

   private static double mouseX = 0;
   private static double mouseY = 0;
   private static Vector2 mouseVector = new Vector2(mouseX,mouseY);
   private static Particle selected;

   @Override
   public void mouseClicked(MouseEvent e) {
      ;
   }

   @Override
   public void mousePressed(MouseEvent e) {
      Point clickLocation = e.getPoint();
      if(e.getButton() == MouseEvent.BUTTON1 && MouseEvent.MOUSE_DRAGGED == e.MOUSE_DRAGGED){
         for(Particle p : GameWindow.particles){
            if(p.getParticle().contains(clickLocation) && p != null){
               selected = p;
            }
         }
     }
   }

   @Override
   public void mouseReleased(MouseEvent e) {      
      if(this.selected != null){
         this.selected = null;
      }
   }

   @Override
   public void mouseEntered(MouseEvent e) {
      ;
   }

   @Override
   public void mouseExited(MouseEvent e) {
      ;
   }

   public static Particle getSelected(){
      return selected;
   }
   public static Vector2 getMouseVector(){
      mouseX = MouseInfo.getPointerInfo().getLocation().getX();
      mouseY = MouseInfo.getPointerInfo().getLocation().getY();

      mouseVector = new Vector2(mouseX, mouseY);

      return mouseVector;
   }
}
