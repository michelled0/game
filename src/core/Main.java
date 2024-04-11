package core;

//import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        renderGame();
    }

    private static void renderGame() {
        int width = 80;
        int height = 30;
        TERenderer ter = new TERenderer();
        ter.initialize(width, height);
        Menu menu = new Menu(width, height);
        World current = menu.current;
        ter.renderFrame(current.getTiles());

    }
}



