package com.tacer.tic_tac_toe;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public enum FontPresets {

    REGULAR(Font.font("Times new Roman", FontWeight.NORMAL, FontPosture.REGULAR, 17)),
    REGULAR_LARGE(Font.font("Times new Roman", FontWeight.BOLD, FontPosture.REGULAR, 100)),
    FULL_FONT(Font.font("Times new Roman", FontWeight.BOLD, FontPosture.REGULAR, 60));

    private final Font font;

    FontPresets(Font font) {
        this.font = font;
    }

    public Font getFont() {
        return this.font;
    }
}