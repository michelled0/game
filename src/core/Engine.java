package core;

import core.EncounterWorld;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;
import utils.FileUtils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.InputStream;
import java.util.Random;

public class Engine {
    private World world;
    private TETile[][] tiles;

    private Position pos;
    private int x;
    private int y;
    private long seed;
    private Random random;
    private TETile previous;
    private int coins;
    private boolean success = false;
    private int originCoins;
    private TETile previousp = Tileset.FLOOR;
    public Engine(long seed, World world) {
        this.world = world;
        this.seed = seed;
        this.random = new Random(seed);
        originCoins = world.getCoins();
        x = giveX();
        y = giveY();
        pos = new Position(x, y);
        tiles = world.getTiles();
        while (tiles[x][y] != Tileset.FLOOR) {
            x = giveX();
            y = giveY();
        }
        previous = tiles[x][y];
        if (world.getEncounter()) {
            coins = ((EncounterWorld) world).getCoins();
        } else {
            coins = world.getCoins();
        }
    }

    public void updateCoins(int coins) {
        this.coins = coins;
    }

    private int giveX() {
        return random.nextInt(world.getWidth());
    }

    private int giveY() {
        return random.nextInt(world.getHeight());
    }

    public void putCharacter() {
        tiles[x][y] = Tileset.AVATAR;
    }

    private void win() {
        if (tiles[x][y] == Tileset.WATER) {
            System.out.println("win");
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("win.wav");
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            displayGameBoard("CONGRATULAION! YOU WIN!!");
            world.saveNew();
            Menu menu = new Menu(80, 30);
        }
    }


    private void loss() {
        if (tiles[x][y] == Tileset.TREE) {
            displayGameBoard("Game Over!");
        }
    }

    private void move(int dx, int dy) {
        if (world.getEncounter()) {
            moveEncounter(dx, dy);
        } else {
            moveNormal(dx, dy);
        }
    }

    private void moveNormal(int dx, int dy) {
        int newX = x + dx;
        int newY = y + dy;
        if (isValidMove(newX, newY)) {
            TETile a = previous;
            TETile destinationTile = tiles[newX][newY];
            unlockIfLockedDoor(newX, newY);  // Unlock before updating previous
            if (destinationTile != Tileset.LOCKED_DOOR) {
                System.out.println(destinationTile.description());
                tiles[x][y] = previous;
                x = newX;
                y = newY;
                previous = tiles[x][y];
                previousp = a;
                encounter();
                win();
                tiles[x][y] = Tileset.AVATAR;
            }
        }
    }

    private void unlockIfLockedDoor(int newX, int newY) {
        if (tiles[newX][newY] == Tileset.LOCKED_DOOR) {
            if (coins > 0) {
                //previousp = previous;
                //previous = Tileset.UNLOCKED_DOOR;
                coins--;
                world.changeCoins(coins);
                if (world.getEnemies().size() < 10) {
                    world.addEnemy();
                }
                tiles[newX][newY] = Tileset.UNLOCKED_DOOR;
            }
        }
    }

    private void moveEncounter(int dx, int dy) {
        int newX = x + dx;
        int newY = y + dy;
        if (isValidMove(newX, newY) && (tiles[newX][newY] == Tileset.FLOOR || tiles[newX][newY] == Tileset.SAND)) {
            tiles[x][y] = previous;
            x = newX;
            y = newY;
            TETile last = tiles[x][y];
            tiles[x][y] = Tileset.AVATAR;
            if (last == Tileset.SAND) {
                try {
                    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("coins.wav");
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coins++;
                success();
            }
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < world.getWidth() && y >= 0 && y < world.getHeight() && tiles[x][y] != Tileset.WALL;
    }

    public void moveLeft() {
        move(-1, 0);
    }

    public void moveRight() {
        move(1, 0);
    }

    public void moveUp() {
        move(0, 1);
    }

    public void moveDown() {
        move(0, -1);
    }

    public String toString() {
        return x + "\n" + y;
    }

    public void fillInfo(String[] lines) {
        this.x = Integer.parseInt(lines[3]);
        this.y = Integer.parseInt(lines[4]);
        previous = Tileset.FLOOR;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public TETile getPrevious() {
        return previous;
    }

    public boolean checkEncounter() {
        return tiles[x][y] == Tileset.FLOWER;
    }

    public void encounter() {
        if (checkEncounter()) {
            tiles[x][y] = Tileset.AVATAR;
            FileUtils.writeFile("previousText.txt", TETile.toString(world.getTiles()));
            FileUtils.writeFile("previousInfo.txt", world.getWidth() + "\n"
                    + world.getHeight() + "\n" + world.getSeed() + "\n"
                    + world.getCharacter() + "\n" + world.getLightOff() + "\n" + world.getCoins());
            String en = "";
            for (Enemy enemy: world.getEnemies()) {
                en += enemy.getX() + " " + enemy.getY() + "\n";
            }
            FileUtils.writeFile("previousEnemies.txt", en);
            FileUtils.writeFile("previousCover.txt", TETile.toString(world.getoutCover()));
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("door.wav");
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            encounterOpen();

            TERenderer ter = new TERenderer();
            ter.initialize(world.getWidth(), world.getHeight());
            Menu menu = new Menu(new EncounterWorld(world.getWidth(), world.getHeight(), random.nextInt(), world.getCoins()));
            World current = menu.current;
            ter.renderFrame(current.getTiles());
        }
    }

    private void success() {
        if (coins == 10 + originCoins) {
            displayGameBoard("CONGRATULATIONS! You earn 10 golden sand");


            success = true;

            int width = 80;
            int height = 30;

            TERenderer ter = new TERenderer();
            ter.initialize(width, height);

            Menu menu = new Menu(coins);

        }
    }

    public boolean isSuccess() {
        return success;
    }

    private void encounterOpen() {
        displayGameBoard("Collect coins in 40 seconds");
    }

    private static void displayGameBoard(String message) {
        StdDraw.setXscale(0, 800);
        StdDraw.setYscale(0, 600);
        StdDraw.enableDoubleBuffering();


        StdDraw.clear(StdDraw.BLACK);


        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
        StdDraw.text(400, 300, message);


        StdDraw.show();
        StdDraw.pause(4000);
        StdDraw.clear();
        StdDraw.show();
    }

    public int getCoins() {
        return coins;
    }
}
