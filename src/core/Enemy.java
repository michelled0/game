package core;

import test.ShorestPathTest;
import tileengine.TETile;
import tileengine.Tileset;

import java.util.*;
import java.util.Random;
public class Enemy {
    private World world;
    private TETile[][] tiles;
    private int x;
    private int y;
    private long seed;
    private Random random;

    private TETile previous;
    private TETile previousp = Tileset.FLOOR;


    private List<Movement> path;

    public Enemy(int x, int y, World world) {
        this.x = x;
        this.y = y;
        this.world = world;
        tiles = world.getTiles();
        previous = Tileset.FLOOR;

        calculateShortestPath();
    }
    public Enemy(long seed, World world) {
        previous = Tileset.FLOOR;
        this.world = world;
        this.seed = seed;
        this.random = new Random(seed);
        x = giveX();
        y = giveY();
        tiles = world.getTiles();
        while (tiles[x][y] != Tileset.FLOOR || distance(x, y, world.getCharacter().getX(), world.getCharacter().getY()) < 8) {
            x = giveX();
            y = giveY();
        }
        calculateShortestPath();
    }

    private static double distance(double x1, double y1, double x2, double y2) {
        double distance = Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
        return distance;
    }

    private int giveX() {
        return random.nextInt(world.getWidth());
    }

    private int giveY() {
        return random.nextInt(world.getHeight());
    }

    public void redoPath(Position pos) {
        calculateShortestPath();
    }
    public List<Movement> getPath() {
        return path;
    }

    private void calculateShortestPath() {
        Position enemyPos = new Position(x, y);
        Engine character = world.getCharacter();

        Position characterPos = new Position(character.getX(), character.getY());
        Pathfinding pathFind = new Pathfinding(world, enemyPos, characterPos);
        path = pathFind.findShortestPath();
    }

    public void move() {
        if (!path.isEmpty()) {
            Movement nextMove = path.remove(0);
            moveInDirection(nextMove);
        }
    }

    private void moveInDirection(Movement direction) {
        switch (direction) {
            case LEFT:
                moveLeft();
                break;
            case RIGHT:
                moveRight();
                break;
            case UP:
                moveUp();
                break;
            case DOWN:
                moveDown();
                break;
        }
    }


    public void putCharacter() {
        tiles[x][y] = Tileset.TREE;
    }

    public void moveLeft() {
        if (x - 1 < 0) {
            return;
        } else if (tiles[x - 1][y] == Tileset.WALL) {
            return;
        } else {
            tiles[x][y] = previous;
            previousp = previous;
            x = x - 1;
            previous = tiles[x][y];
            if (previous == Tileset.TREE) {
                previous = previousp;
            }
            tiles[x][y] = Tileset.TREE;
        }

    }

    public void moveRight() {
        if (x + 1 >= world.getWidth()) {
            return;
        } else if (tiles[x + 1][y] == Tileset.WALL) {
            return;
        } else {
            tiles[x][y] = previous;
            x = x + 1;
            previous = tiles[x][y];
            if (previous == Tileset.TREE) {
                previous = previousp;
            }
            tiles[x][y] = Tileset.TREE;
        }
    }

    public void moveUp() {
        if (y + 1 >= world.getHeight()) {
            return;
        } else if (tiles[x][y + 1] == Tileset.WALL) {
            return;
        } else {
            tiles[x][y] = previous;
            y = y + 1;
            previous = tiles[x][y];
            if (previous == Tileset.TREE) {
                previous = previousp;
            }
            tiles[x][y] = Tileset.TREE;
        }
    }

    public void moveDown() {
        if (y - 1 < 0) {
            return;
        } else if (tiles[x][y - 1] == Tileset.WALL) {
            return;
        } else {
            tiles[x][y] = previous;
            y = y - 1;
            previous = tiles[x][y];
            if (previous == Tileset.TREE) {
                previous = previousp;
            }
            tiles[x][y] = Tileset.TREE;
        }
    }
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
