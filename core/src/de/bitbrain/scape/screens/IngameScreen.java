package de.bitbrain.scape.screens;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import de.bitbrain.braingdx.BrainGdxGame;
import de.bitbrain.braingdx.GameContext;
import de.bitbrain.braingdx.assets.SharedAssetManager;
import de.bitbrain.braingdx.behavior.movement.Movement;
import de.bitbrain.braingdx.behavior.movement.Orientation;
import de.bitbrain.braingdx.graphics.animation.SpriteSheet;
import de.bitbrain.braingdx.graphics.animation.types.AnimationTypes;
import de.bitbrain.braingdx.graphics.pipeline.layers.RenderPipeIds;
import de.bitbrain.braingdx.postprocessing.effects.Bloom;
import de.bitbrain.braingdx.screens.AbstractScreen;
import de.bitbrain.braingdx.tmx.TiledMapType;
import de.bitbrain.braingdx.tweens.ActorTween;
import de.bitbrain.braingdx.tweens.SharedTweenManager;
import de.bitbrain.braingdx.world.GameObject;
import de.bitbrain.scape.Colors;
import de.bitbrain.scape.PlayerContext;
import de.bitbrain.scape.assets.Assets;
import de.bitbrain.scape.camera.OutOfBoundsManager;
import de.bitbrain.scape.event.*;
import de.bitbrain.scape.camera.LevelScrollingBounds;
import de.bitbrain.scape.graphics.CharacterType;
import de.bitbrain.scape.graphics.PlayerParticleSpawner;
import de.bitbrain.scape.movement.CollisionDetector;
import de.bitbrain.scape.movement.PlayerAdjustment;
import de.bitbrain.scape.movement.PlayerMovement;
import de.bitbrain.scape.ui.LevelDescriptionUI;
import de.bitbrain.scape.ui.PointsLabel;

import static de.bitbrain.scape.graphics.CharacterInitializer.createAnimations;

public class IngameScreen extends AbstractScreen<BrainGdxGame> {

   private final String tiledMapPath;

   private PlayerContext playerContext;

   private Vector2 resetPosition = new Vector2();
   private GameObject player;
   private LevelScrollingBounds levelScroller;
   private OutOfBoundsManager outOfBoundsManager;
   private GameContext context;

   private boolean anyKeyPressedToStartlevel = false;

   private LevelDescriptionUI descriptionUI;

   public IngameScreen(BrainGdxGame game, String tiledMapPath) {
      super(game);
      this.tiledMapPath = tiledMapPath;
   }

   @Override
   protected void onCreate(final GameContext context) {
      playerContext = new PlayerContext();
      setBackgroundColor(Colors.BACKGROUND_VIOLET);
      context.getTiledMapManager().getAPI().setEventFactory(new ScopeEventFactory());
      context.getTiledMapManager().getAPI().setDebug(false);

      setupEvents(context);

      context.getTiledMapManager().load(
            SharedAssetManager.getInstance().get(tiledMapPath, TiledMap.class),
            context.getGameCamera().getInternalCamera(),
            TiledMapType.ORTHOGONAL
      );


      setupWorld(context);
      setupUI(context);
      setupShaders(context);
   }

   @Override
   protected void onUpdate(float delta) {
      if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
         Gdx.app.exit();
         return;
      }
      if (!anyKeyPressedToStartlevel && Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
         anyKeyPressedToStartlevel = true;
         descriptionUI.hide(2f);
         Tween.to(descriptionUI, ActorTween.ALPHA, 1f).delay(0.5f)
               .target(0f)
               .setCallbackTriggers(TweenCallback.COMPLETE)
               .setCallback(new TweenCallback() {
                  @Override
                  public void onEvent(int type, BaseTween<?> source) {
                     context.getStage().getActors().removeValue(descriptionUI, false);
                  }
               })
               .start(SharedTweenManager.getInstance());
         setupPlayer();
      }
      if (anyKeyPressedToStartlevel) {
         super.onUpdate(delta);
         levelScroller.update(delta);

      }
   }

   public void resetLevel() {
      player.setPosition(resetPosition.x, resetPosition.y);
      levelScroller.reset();
      PlayerAdjustment.adjust(player, context);
   }

   private void setupEvents(GameContext context) {
      context.getEventManager().register(
            new GameOverEventListener(this),
            GameOverEvent.class
      );
      context.getEventManager().register(
            new LevelCompleteEventListener(getGame(), context),
            LevelCompleteEvent.class
      );
      context.getEventManager().register(
            new ByteCollector(context.getGameWorld(), context.getParticleManager(), playerContext),
            ByteCollectedEvent.class
      );
   }

   private void setupWorld(GameContext context) {
      this.context = context;
      final Texture playerTexture = SharedAssetManager.getInstance().get(Assets.Textures.PLAYER);
      SpriteSheet sheet = new SpriteSheet(playerTexture, 8, 2);
      createAnimations(context, sheet, CharacterType.PLAYER, AnimationTypes.FORWARD)
            .origin(0, 0)
            .frames(8)
            .interval(0.05f);
      createAnimations(context, sheet, CharacterType.BYTE, AnimationTypes.FORWARD)
            .origin(0, 1)
            .frames(8)
            .interval(0.05f);
      for (GameObject o : context.getGameWorld()) {
         if ("PLAYER".equals(o.getType())) {
            o.setDimensions(8f, 8f);
            float correctX = (float) (Math.floor(o.getLeft() / context.getTiledMapManager().getAPI().getCellWidth()) * context.getTiledMapManager().getAPI().getCellWidth());
            float correctY = (float) (Math.floor(o.getTop() / context.getTiledMapManager().getAPI().getCellHeight()) * context.getTiledMapManager().getAPI().getCellHeight());
            o.setPosition(correctX, correctY);
            context.getGameCamera().setStickToWorldBounds(true);
            context.getGameCamera().setDefaultZoomFactor(0.15f);
            context.getGameCamera().setTrackingTarget(o);
            context.getGameCamera().setTargetTrackingSpeed(0.05f);
            player = o;
            this.resetPosition.x = player.getLeft();
            this.resetPosition.y = player.getTop();
         }
         if ("BYTE".equals(o.getType())) {
            o.setDimensions(8f, 8f);
            context.getParticleManager().attachEffect(Assets.Particles.BYTE, o, 4f, 4f);
            float correctX = (float) (Math.floor(o.getLeft() / context.getTiledMapManager().getAPI().getCellWidth()) * context.getTiledMapManager().getAPI().getCellWidth());
            float correctY = (float) (Math.floor(o.getTop() / context.getTiledMapManager().getAPI().getCellHeight()) * context.getTiledMapManager().getAPI().getCellHeight());
            o.setPosition(correctX, correctY);
         }
      }
      levelScroller = new LevelScrollingBounds(context.getTiledMapManager().getAPI());
      context.getGameWorld().setBounds(levelScroller);
      outOfBoundsManager = new OutOfBoundsManager(context.getEventManager(), levelScroller, player);
   }

   private void setupUI(GameContext context) {
      Table layout = new Table();
      layout.setFillParent(true);
      layout.right().bottom().padRight(90).padBottom(50).add(new PointsLabel(playerContext));
      context.getStage().addActor(layout);

      descriptionUI = new LevelDescriptionUI(extractLevelNameFromFileName(), Integer.valueOf(extractLevelNumberFromFileName()));
      context.getStage().addActor(descriptionUI);
      descriptionUI.show(2f);
   }

   private String extractLevelNameFromFileName() {
      String numberString = extractLevelNumberFromFileName();
      String name = tiledMapPath
            .replaceAll(".*" + numberString, "")
            .replace(".tmx", "");
      return name.substring(1).replace("_", " ");
   }

   private String extractLevelNumberFromFileName() {
      return tiledMapPath.replaceAll("\\D+","");
   }

   private void setupShaders(GameContext context) {
      Bloom bloom = new Bloom(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      bloom.setBlurAmount(5f);
      bloom.setBloomIntesity(1.2f);
      bloom.setBlurPasses(50);
      bloom.setThreshold(0.3f);
      context.getRenderPipeline().getPipe(RenderPipeIds.UI).addEffects(bloom);
   }

   private void setupPlayer() {
      CollisionDetector collisionDetector = new CollisionDetector(context);
      PlayerMovement movement = new PlayerMovement(collisionDetector);
      context.getBehaviorManager().apply(movement, player);
      player.setAttribute(Movement.class, movement);
      player.setAttribute(Orientation.class, Orientation.RIGHT);
      context.getBehaviorManager().apply(new PlayerParticleSpawner(context.getParticleManager(), movement), player);
      PlayerAdjustment.adjust(player, context);
   }
}