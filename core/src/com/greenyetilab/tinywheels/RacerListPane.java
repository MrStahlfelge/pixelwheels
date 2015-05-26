package com.greenyetilab.tinywheels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.XmlReader;
import com.greenyetilab.utils.UiBuilder;

/**
 * Display the high score table
 */
public class RacerListPane extends ScrollPane {
    public RacerListPane() {
        super(null);
    }

    public void init(Skin skin, Array<Racer> racers, Racer playerRacer) {
        Table table = new Table(skin);
        addRow(table, "highScore", "#", "Racer", "Best Lap", "Total");
        for (int idx = 0; idx < racers.size; ++idx) {
            Racer racer = racers.get(idx);
            StopWatchComponent stopWatchComponent = racer.getStopWatchComponent();
            String style = racer == playerRacer ? "newHighScore" : "highScore";
            addRow(table, style,
                    String.format("%d.", idx + 1),
                    racer.getVehicle().getName(),
                    StringUtils.formatRaceTime(stopWatchComponent.getBestLapTime()),
                    StringUtils.formatRaceTime(stopWatchComponent.getTotalTime())
            );
        }
        setWidget(table);
    }

    public static void register(UiBuilder builder) {
        builder.registerActorFactory("RacerListPane", new UiBuilder.ActorFactory() {
            @Override
            public Actor createActor(XmlReader.Element element) {
                return new RacerListPane();
            }
        });
    }

    public static void addRow(Table table, String style, String v1, String v2, String v3, String v4) {
        table.add(v1, style).right().padRight(24);
        table.add(v2, style).left().expandX();
        table.add(v3, style).right().padRight(24);
        table.add(v4, style).right();
        table.row();
    }
}