package core;

import edu.princeton.cs.algs4.StdDraw;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.Substance;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;
import utils.FileUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class Menu {
    World previous;
    String input;
    long seed;
    World current;
    int width;
    int height;
    TERenderer ter;

    boolean lightOff;

    private long startTime;
    private static final int DISPLAY_DURATION_SECONDS = 40;

    private int mouseX = -1;
    private int mouseY = -1;


    public Menu(int coins) {
        returnToOrigin();
        this.width = current.getWidth();
        this.height = current.getHeight();
        this.ter = new TERenderer();
        System.out.println("coins");
        System.out.println(coins);
        current.changeCoins(coins);
        starting(current);
    }

    public Menu() {
        returnToOrigin();
        this.width = current.getWidth();
        this.height = current.getHeight();
        this.ter = new TERenderer();
        starting(current);
    }
    public Menu(World current) {
        this.current = current;
        this.width = current.getWidth();
        this.height = current.getHeight();
        this.ter = new TERenderer();
        starting(current);
    }

    public Menu(int width, int height) {
        this.width = width;
        this.height = height;
        this.ter = new TERenderer();
        if (FileUtils.fileExists("previousText.txt")
                && FileUtils.fileExists("previousInfo.txt")
                && FileUtils.fileExists("previousCover.txt")
            && FileUtils.fileExists("previousEnemies.txt")) {
            TETile[][] previousTile = World.convertStringToTile(FileUtils.readFile("previousText.txt"));
            previous = World.fromTileToWorld(previousTile);
        }
        begin();
    }

    private void starting(World world) {
        current = world;
        lightOff = current.getLightOff();

        ter.initialize(width, height + 2);
        if (lightOff) {
            ter.renderFrame(current.getCover());
        } else {
            ter.renderFrame(current.getTiles());
        }
        if (world instanceof EncounterWorld) {
            startTime = System.currentTimeMillis();
        }
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.filledRectangle(width / 2.0, height + 1.5, width / 2.0, 1);

        drawHUD();

        allowCharacter();
    }

    public void drawHUD() {
        StdDraw.setPenColor(StdDraw.WHITE);

        int c;

        if (current.getEncounter()) {
            c = current.getCharacter().getCoins();
        } else {
            c = current.getCoins();
        }

        StdDraw.textLeft(width - 10, height + 1, "Coins: " + c);

        String des = mouse((int) StdDraw.mouseX(), (int) StdDraw.mouseY());
        StdDraw.textLeft(2, height + 1, "Tile: " + des);


        StdDraw.show();
    }

    public void begin() {
        ter.initialize(width, height);
        StartPage start = new StartPage(width, height);
        start.start();

        char lastKeyPressed = '\0';
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                lastKeyPressed = StdDraw.nextKeyTyped();
                if (lastKeyPressed == 'E' || lastKeyPressed == 'e') {
                    explain();
                }
                if (lastKeyPressed == 'L' || lastKeyPressed == 'l') {
                    if (previous != null) {
                        System.out.println(TETile.toString(previous.getTiles()));
                        starting(previous);
                    }
                    return;
                } else if (lastKeyPressed == 'Q' || lastKeyPressed == 'q') {
                    end();
                    return;
                } else if (lastKeyPressed == 'N' || lastKeyPressed == 'n') {
                    seed = recordSeed();
                    starting(new WorldOrigin(80, 30, seed));
                }
            }
        }
    }

    private void explain() {
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.setPenColor(255, 255, 255);
        int x = width / 2;
        int y = height / 2 + 10;
        StdDraw.text(x, y, "Explanation");

        StdDraw.setFont(new Font("Arial", Font.PLAIN, 25));
        StdDraw.text(x, y - 5, "█: Locked Door, character cannot go through" +
                " but enemies can;");
        StdDraw.text(x, y-8, "when character unlocked a door, one more enemy will be added");
        StdDraw.text(x, y - 11, "@: Character");
        StdDraw.text(x, y - 14, "♠: Enemy");
        StdDraw.text(x, y - 17, "▒: Coin, can use to unlock doors");
        StdDraw.text(x, y - 20, "❀: Entrance to Encounter World, where character can " +
                "collect coins");

        StdDraw.show();
        char lastKeyPressed = '\0';
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                lastKeyPressed = StdDraw.nextKeyTyped();
                if (lastKeyPressed == 'Q' || lastKeyPressed == 'q') {
                    begin();
                    return;
                }
            }
        }
    }
    private long recordSeed() {
        input = "";
        int x = 5;
        int y = height - 1;
        char lastKeyPressed = '\0';
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                lastKeyPressed = StdDraw.nextKeyTyped();
                StdDraw.text(x, y, "" + lastKeyPressed);
                StdDraw.show();
                x++;
                if (lastKeyPressed == 'S' || lastKeyPressed == 's') {
                    if (input.equals("")) {
                        return 0;
                    } else {
                        return Long.parseLong(input);
                    }
                }
                input += lastKeyPressed;
            }
        }
    }

    public void end() {
        if (!(current instanceof EncounterWorld)) {
            FileUtils.writeFile("previousText.txt", TETile.toString(current.getTiles()));
            FileUtils.writeFile("previousInfo.txt", current.getWidth() + "\n"
                    + current.getHeight() + "\n" + current.getSeed() + "\n"
                    + current.getCharacter() + "\n" + current.getLightOff() + "\n" + current.getCoins());
            FileUtils.writeFile("previousCover.txt", TETile.toString(current.getoutCover()));
            String en = "";
            for (Enemy enemy : current.getEnemies()) {
                en += enemy.getX() + " " + enemy.getY() + "\n";
            }
            FileUtils.writeFile("previousEnemies.txt", en);
        }
        TETile[][] previousTile = World.convertStringToTile(FileUtils.readFile("previousText.txt"));
        previous = World.fromTileToWorld(previousTile);

        begin();
    }

    public void allowCharacter() {
        char lastKeyPressed = '\0';
        boolean readyToEnd = false;
        long lastMoveTime = System.currentTimeMillis();
        while (true) {
            long currentTime = System.currentTimeMillis();
            if (current instanceof EncounterWorld && System.currentTimeMillis() - startTime
                    > DISPLAY_DURATION_SECONDS * 1000) {
                displayGameBoard("TIME OUT!");

                TERenderer ter1 = new TERenderer();
                ter1.initialize(width, height);

                Menu menu = new Menu(current.getCharacter().getCoins());
                World current1 = menu.current;
                ter1.renderFrame(current1.getTiles());
                break;
            }
            if (readyToEnd) {
                if (lastKeyPressed == 'q' || lastKeyPressed == 'Q') {
                    end();
                }
            }
            if (lastKeyPressed == ':') {
                readyToEnd = true;
            } else {
                readyToEnd = false;
            }
            if (currentTime - lastMoveTime >= 500) {
                if (!current.getLightOff()) {
                    for (Enemy enemy : current.getEnemies()) {
                        enemy.move();
                        enemy.redoPath(new Position(current.getCharacter().getX(), current.getCharacter().getY()));
                        lastMoveTime = currentTime;
                        if (enemy.getX() == current.getCharacter().getX()
                                && enemy.getY() == current.getCharacter().getY()) {
                            gameOver();
                            return;
                        }
                    }
                }
            }
            if (StdDraw.hasNextKeyTyped()) {
                lastKeyPressed = StdDraw.nextKeyTyped();
                moveCharacter(lastKeyPressed);
                for (Enemy enemy: current.getEnemies()) {
                    if (enemy.getX() == current.getCharacter().getX()
                            && enemy.getY() == current.getCharacter().getY()) {
                        gameOver();
                        return;
                    }
                }
            }
            if (lightOff) {
                ter.renderFrame(current.getCover());
            } else {
                ter.renderFrame(current.getTiles());
            }

            drawHUD();
        }
    }

    private void gameOver() {
        System.out.println("over");
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("lose.wav");
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        displayGameBoard("Game Over!");
        current.saveNew();
        TETile[][] previousTile = World.convertStringToTile(FileUtils.readFile("previousText.txt"));
        previous = World.fromTileToWorld(previousTile);
        begin();
    }

    public void moveCharacter(char a) {
        StdDraw.enableDoubleBuffering();
        if (lightOff) {
            current.allBlack();
        }
        if (a == 'a' || a == 'A') {
            current.getCharacter().moveLeft();
        } else if (a == 'D' || a == 'd') {
            current.getCharacter().moveRight();
        } else if (a == 'W' || a == 'w') {
            current.getCharacter().moveUp();
        } else if (a == 'S' || a == 's') {
            current.getCharacter().moveDown();
        } else if (a == 'o' || a == 'O') {
            lightOff = !lightOff;
            current.changeLightOff();
        }
    }

    private static void displayGameBoard(String message) {
        StdDraw.setXscale(0, 800);
        StdDraw.setYscale(0, 600);
        StdDraw.clear(StdDraw.BLACK);


        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
        StdDraw.text(400, 300, message);


        StdDraw.show();

        StdDraw.pause(4000);

        StdDraw.clear();
        StdDraw.show();

    }

    public void returnToOrigin() {
        if (FileUtils.fileExists("previousText.txt")
                && FileUtils.fileExists("previousInfo.txt")
                && FileUtils.fileExists("previousCover.txt")) {
            TETile[][] previousTile = World.convertStringToTile(FileUtils.readFile("previousText.txt"));
            previous = World.fromTileToWorld(previousTile);
            current = previous;
        }
    }

    public String mouse(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            TETile tile = current.getTiles()[x][y];
            return tile.description();
        }
        return "";
    }
}
