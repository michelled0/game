package core;

import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;

public class StartPage {
    int width;
    int height;
    public StartPage(int width, int height) {
        this.width = width;
        this.height = height;
    }
    public void start() {
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.setPenColor(255, 255, 255);
        int x = width / 2;
        int y = height / 2 + 10;
        StdDraw.text(x, y, "Game");

        StdDraw.setFont(new Font("Arial", Font.PLAIN, 25));
        StdDraw.text(x, y - 5, "New Game (N)");
        StdDraw.text(x, y - 10, "Load Game (L)");
        StdDraw.text(x, y - 15, "Quit(Q)");
        StdDraw.text(x, y-20, "Explain(E)");


        StdDraw.setFont(new Font("Arial", Font.PLAIN, 20));
        int x1 = 3;
        int y1 = height - 1;
        StdDraw.text(x1, y1, "Seed: ");

        StdDraw.show();
    }
}
