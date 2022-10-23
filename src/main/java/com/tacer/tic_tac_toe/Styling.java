package com.tacer.tic_tac_toe;

import javafx.scene.image.*;
import javafx.scene.layout.*;

public class Styling {



    public enum Backgrounds {


        SERVER_BG ("Server.png"),
        SKY_BLUE ("SkyBlue.png"),

        LIGHT_GRAY ("LightGray.png");



        private Background background;


        Backgrounds(String image) {

            Background background = new Background(new BackgroundImage(
                    new Image(Styling.class.getResource("Assets/Backgrounds/"+image).toExternalForm(),600,400,false,true),
                    BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT));

            this.background = background;
        }

        public Background getBackground() {
            return background;
        }

    }


}
