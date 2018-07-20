package de.bitbrain.scape.movement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import de.bitbrain.braingdx.behavior.BehaviorAdapter;
import de.bitbrain.braingdx.world.GameObject;
import de.bitbrain.scape.GameConfig;
import de.bitbrain.scape.model.Direction;

public class PlayerMovement extends BehaviorAdapter {

   private Vector2 velocity = new Vector2(GameConfig.PLAYER_START_SPEED, 0f);

   private boolean flipping = false;

   private boolean upAgain = false;

   private final CollisionDetector collisionDetector;

   public PlayerMovement(CollisionDetector collisionDetector) {
      this.collisionDetector = collisionDetector;
   }

   @Override
   public void update(GameObject source, float delta) {
      if (Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
         if (upAgain) {
            flip(source);
         }
         upAgain = false;
      } else {
         upAgain = true;
      }

      Direction direction = ((Direction)source.getAttribute(Direction.class));

      velocity.x = GameConfig.PLAYER_START_SPEED;
      velocity.y = GameConfig.PLAYER_START_SPEED * direction.getMultiplier();

      // 1. When moving up, get collision on the top
      //    When moving down, get collision on the bottom
      //    Get collision on the right
      // 2. when distance to horizontal collision > distance to vertical collision -> horizontal collision
      // 3. for horizontal collision -> set back right side to horizontal left point
      //    for vertical collision -> - if moving up, set up side to vertical down point
      //                              - if moving down, set down side to vertical up point
      //                              - set flipping to false

      source.setPosition(
            source.getLeft() + velocity.x * delta,
            source.getTop() + velocity.y * delta);
         Vector2 horizontalCollision = collisionDetector.getCollisionInFront(source);
         Vector2 verticalCollision = Direction.UP.equals(direction) ?
               collisionDetector.getCollisionAbove(source) :
               collisionDetector.getCollisionBelow(source);
         if (verticalCollision != null) {
            flipping  = false;
            source.setPosition(source.getLeft(), verticalCollision.y);
         } else if (horizontalCollision != null) {
            flipping = true;
            source.setPosition(horizontalCollision.x, source.getTop());
         } else {
            flipping = true;
         }

   }

   private void flip(GameObject source) {
      if (flipping) {
         return;
      }
      if (Direction.DOWN.equals(source.getAttribute(Direction.class))) {
         source.setAttribute(Direction.class, Direction.UP);
      } else {
         source.setAttribute(Direction.class, Direction.DOWN);
      }
      flipping = true;
   }
}
