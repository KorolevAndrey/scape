package de.bitbrain.scape.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import de.bitbrain.braingdx.BrainGdxGame;
import de.bitbrain.braingdx.GameContext;
import de.bitbrain.braingdx.graphics.pipeline.layers.RenderPipeIds;
import de.bitbrain.braingdx.postprocessing.effects.Bloom;
import de.bitbrain.braingdx.screens.AbstractScreen;
import de.bitbrain.braingdx.util.DeltaTimer;
import de.bitbrain.scape.Colors;
import de.bitbrain.scape.GameConfig;
import de.bitbrain.scape.assets.Assets;
import de.bitbrain.scape.ui.TerminalUI;
import de.bitbrain.scape.ui.effects.TextGlitchRandomizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IntroScreen extends AbstractScreen<BrainGdxGame> {

   private GameContext context;
   private List<String> commands;

   private boolean bootSequence = false;
   private boolean exiting = false;

   private final DeltaTimer bootTimer = new DeltaTimer();
   private TextGlitchRandomizer randomizer;
   private TerminalUI ui;

   public IntroScreen(BrainGdxGame game) {
      super(game);
   }

   @Override
   protected void onCreate(GameContext context) {
      setBackgroundColor(Colors.BACKGROUND_VIOLET);
      commands = loadIntroCommands();
      this.context  = context;
      ui = new TerminalUI(commands);
      context.getStage().addActor(ui);
      randomizer = new TextGlitchRandomizer(ui);
      setupShaders(context);
   }

   @Override
   protected void onUpdate(float delta) {
      if (!exiting && Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
         exiting = true;
         context.getScreenTransitions().out(new LevelSelectionScreen(getGame(), true), 1f);
      } else if (!bootSequence && commands != null && commands.isEmpty() && Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
         bootSequence = true;
         ui.setPaused(true);
         randomizer.start();
      }
      if (bootSequence) {
         bootTimer.update(delta);
         randomizer.update(delta);
      }
      if (!exiting && bootTimer.reached(GameConfig.BOOT_SEQUENCE_DURATION)) {
         exiting = true;
         context.getScreenTransitions().out(new LevelSelectionScreen(getGame(), true), 1f);
      }
   }

   private void setupShaders(GameContext context) {
      Bloom bloom = new Bloom(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
      bloom.setBlurAmount(5f);
      bloom.setBloomIntesity(1.2f);
      bloom.setBlurPasses(50);
      bloom.setThreshold(0.3f);
      context.getRenderPipeline().getPipe(RenderPipeIds.UI).addEffects(bloom);
   }

   private List<String> loadIntroCommands() {
      List<String> commands = new ArrayList<String>();
      InputStream stream = null;
      try {
         stream = Gdx.files.internal("intro.txt").read();
         BufferedReader r = new BufferedReader(new InputStreamReader(stream));
         String line;
         while ((line = r.readLine()) != null) {
            commands.add(line + "\n");
         }
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (stream != null) {
            try {
               stream.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      return commands;
   }
}
