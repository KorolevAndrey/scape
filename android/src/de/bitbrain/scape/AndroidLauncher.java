package de.bitbrain.scape;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import de.bitbrain.scape.ScapeGame;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new ScapeGame(), config);
	}
}
