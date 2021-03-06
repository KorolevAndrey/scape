package de.bitbrain.scape.ui.levelselection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import de.bitbrain.braingdx.assets.SharedAssetManager;
import de.bitbrain.braingdx.graphics.GraphicsFactory;
import de.bitbrain.scape.Colors;
import de.bitbrain.scape.assets.Assets;
import de.bitbrain.scape.i18n.Bundle;
import de.bitbrain.scape.i18n.Messages;
import de.bitbrain.scape.level.LevelMetaData;
import de.bitbrain.scape.progress.PlayerProgress;
import de.bitbrain.scape.ui.Styles;


public class LevelProgressUI extends Actor {

   private static final int PADDING = 10;
   private static final int LABEL_PADDING = 2;

   private final Label descriptionLabel;
   private final Label progressLabel;
   private final Label completeLabel;
   private final Texture texture;
   private final NinePatch background;
   private final float totalProgress;

   public LevelProgressUI(LevelMetaData metaData, PlayerProgress playerProgress) {
      setWidth(260f);
      setHeight(40f);
      totalProgress = (float)playerProgress.getPointRecord() / metaData.getNumberOfBytes();
      texture = GraphicsFactory.createTexture(2, 2, Color.WHITE);
      Texture ninePatch = SharedAssetManager.getInstance().get(Assets.Textures.MENU_FOCUS_DEFAULT_NINEPATCH, Texture.class);
      background = GraphicsFactory.createNinePatch(ninePatch, 15);
      descriptionLabel = new Label(Bundle.get(Messages.MENU_SELECTION_PROGRESS), Styles.LABEL_SELECTION_PROGRESS_DESCRIPTION);
      if (totalProgress == 1f) {
         progressLabel = new Label(playerProgress.getPointRecord() + "/" + metaData.getNumberOfBytes(), Styles.LABEL_SELECTION_PROGRESS_COMPLETE);
         completeLabel = new Label(Bundle.get(Messages.MENU_SELECTION_COMPLETE), Styles.LABEL_SELECTION_COMPLETE);
         completeLabel.setFontScale(1.1f);
      } else {
         completeLabel = null;
         progressLabel = new Label(playerProgress.getPointRecord() + "/" + metaData.getNumberOfBytes(), Styles.LABEL_SELECTION_PROGRESS);
      }
   }

   @Override
   public void draw(Batch batch, float parentAlpha) {
      descriptionLabel.setPosition(getX() + getWidth() / 2f - descriptionLabel.getPrefWidth() / 2f, getY() + getHeight() - LABEL_PADDING);
      descriptionLabel.draw(batch, parentAlpha);
      progressLabel.setPosition(getX() + getWidth() / 2f - progressLabel.getPrefWidth() / 2f, getY() - progressLabel.getPrefHeight() + LABEL_PADDING);
      progressLabel.draw(batch, parentAlpha);
      background.draw(batch, getX(), getY(), getWidth(), getHeight());
      Color color = totalProgress == 1f ? Colors.PRIMARY_BLUE.cpy() : Colors.PRIMARY_RED.cpy();
      color.a = getColor().a * parentAlpha;
      batch.setColor(color);
      if (totalProgress > 0f) {
         batch.draw(texture, getX() + PADDING, getY() + PADDING, totalProgress * getWidth() - PADDING * 2f, getHeight() - PADDING * 2f);
      }
      if (totalProgress == 1f) {
         completeLabel.setPosition(
               getX() + getWidth() / 2f - completeLabel.getPrefWidth() / 2f,
               getY() + getHeight() / 2f - completeLabel.getPrefHeight() / 2f
         );
         completeLabel.draw(batch, parentAlpha);
      }
      batch.setColor(Color.WHITE);
   }
}
