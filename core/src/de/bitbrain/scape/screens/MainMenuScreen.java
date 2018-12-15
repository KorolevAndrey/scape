package de.bitbrain.scape.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import de.bitbrain.braingdx.GameContext;
import de.bitbrain.braingdx.assets.SharedAssetManager;
import de.bitbrain.braingdx.behavior.movement.Orientation;
import de.bitbrain.braingdx.graphics.animation.AnimationConfig;
import de.bitbrain.braingdx.graphics.animation.AnimationFrames;
import de.bitbrain.braingdx.graphics.animation.AnimationRenderer;
import de.bitbrain.braingdx.graphics.animation.AnimationSpriteSheet;
import de.bitbrain.braingdx.graphics.pipeline.layers.RenderPipeIds;
import de.bitbrain.braingdx.graphics.postprocessing.AutoReloadPostProcessorEffect;
import de.bitbrain.braingdx.graphics.postprocessing.effects.Bloom;
import de.bitbrain.braingdx.graphics.postprocessing.effects.Vignette;
import de.bitbrain.braingdx.screens.AbstractScreen;
import de.bitbrain.braingdx.world.GameObject;
import de.bitbrain.scape.Colors;
import de.bitbrain.scape.ScapeGame;
import de.bitbrain.scape.assets.Assets;
import de.bitbrain.scape.graphics.CharacterType;
import de.bitbrain.scape.preferences.PlayerProgress;
import de.bitbrain.scape.ui.Styles;

import static de.bitbrain.scape.GameConfig.DEFAULT_BLOOM_CONFIG;

public class MainMenuScreen extends AbstractScreen<ScapeGame> {

   public MainMenuScreen(ScapeGame game) {
      super(game);
   }

   private GameContext context;

   private boolean exiting = false;

   @Override
   protected void onCreate(GameContext context) {
      this.context = context;
      context.getScreenTransitions().in(0.3f);
      setBackgroundColor(Colors.BACKGROUND_VIOLET);
      setupLogo(context);
      setupUI(context);
      setupShaders();
   }

   @Override
   protected void onUpdate(float delta) {
      super.onUpdate(delta);
      if (!exiting && Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
         exiting = true;
         Gdx.app.exit();
      }
   }

   private void setupShaders() {
      AutoReloadPostProcessorEffect<Bloom> bloomEffect = context.getShaderManager().createBloomEffect();
      AutoReloadPostProcessorEffect<Vignette> vignetteEffect = context.getShaderManager().createVignetteEffect();
      bloomEffect.mutate(DEFAULT_BLOOM_CONFIG);
      context.getRenderPipeline().getPipe(RenderPipeIds.UI).addEffects(vignetteEffect, bloomEffect);
   }

   private void setupLogo(GameContext context) {
      final Texture playerTexture = SharedAssetManager.getInstance().get(Assets.Textures.MENU_LOGO);
      AnimationSpriteSheet sheet = new AnimationSpriteSheet(playerTexture, 5);

      context.getRenderManager().register("LOGO", new AnimationRenderer(sheet, AnimationConfig.builder()
            .registerFrames("LOGO", AnimationFrames.builder()
                  .frames(14)
                  .origin(0, 0)
                  .duration(0.07f)
                  .playMode(Animation.PlayMode.LOOP)
            .build())
            .build()));



      GameObject logo = context.getGameWorld().addObject();
      logo.setDimensions(25f, 5f);
      logo.setType("LOGO");
      logo.getScale().y = -1f;
      logo.setAttribute(Orientation.class, Orientation.RIGHT);
      context.getGameCamera().setTrackingTarget(logo, true);
      context.getGameCamera().setStickToWorldBounds(false);
      context.getGameCamera().setZoomScalingFactor(0f);
      context.getGameCamera().setDefaultZoomFactor(0.05f);
   }

   private void setupUI(final GameContext context) {
      Table layout = new Table();
      layout.setFillParent(true);
      Table buttons = new Table();
      addMenuButton("Continue", new ClickListener() {
         @Override
         public void clicked(InputEvent event, float x, float y) {
            context.getScreenTransitions().out(new LevelSelectionScreen(getGame(), true), 1f);
         }
      }, buttons).padRight(40f);
      addMenuButton("New Game", new ClickListener() {
         @Override
         public void clicked(InputEvent event, float x, float y) {
            PlayerProgress progress = new PlayerProgress(null);
            progress.reset();
            context.getScreenTransitions().out(new IntroScreen(getGame()), 1f);
         }
      }, buttons);
      addMenuButton("Leave", new ClickListener() {
         @Override
         public void clicked(InputEvent event, float x, float y) {
            Gdx.app.exit();
         }
      }, buttons).padLeft(40f);
      layout.padTop(400f).add(buttons).padBottom(100f).row();
      Label credits = new Label("a game by bitbrain\n© 2018", Styles.LABEL_CREDITS);
      credits.setAlignment(Align.center);
      credits.getColor().a = 0.3f;
      layout.add(credits);
      context.getStage().addActor(layout);
   }

   private Cell addMenuButton(String text, ClickListener listener, Table buttons) {
      TextButton button = new TextButton(text, Styles.BUTTON_MENU);
      button.addListener(listener);
      button.setChecked(false);
      return buttons
            .add(button)
            .width(Gdx.graphics.getWidth() / 5f)
            .height(Gdx.graphics.getHeight() / 8f);
   }
}
