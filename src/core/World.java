package core;

import tileengine.TETile;
import tileengine.Tileset;
import utils.FileUtils;

import java.util.*;

public class World {
    protected int width;
    protected int height;
    protected TETile[][] tiles;
    protected List<Room> roomList;
    protected long seed;
    protected Random random;
    protected Engine character;
    private TETile[][] cover;
    private boolean lightOff;
    protected int coins;
    private boolean encounter = false;
    protected int xf;
    protected int yf;
    protected int a;



    protected List<Enemy> enemies = new ArrayList<>();



    public World(TETile[][] tile) {

        this.tiles = tile;
    }
    public World(int width, int height, long seed) {
        this.width = width;
        this.height = height;

        this.tiles = new TETile[width][height];

        roomList = new ArrayList<Room>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tiles[i][j] = Tileset.NOTHING;
            }
        }
        this.seed = seed;
        random = new Random(seed);

        int n = random.nextInt(height + width) + height;
        for (int i = 0; i < n; i++) {
            randomRoom();
        }
        Collections.sort(roomList, new Comparator<Room>() {
            @Override
            public int compare(Room room1, Room room2) {
                return Integer.compare(room1.xSmall, room2.xSmall);

            }
        });

        connectRooms();
        character = new Engine(seed, this);
        character.putCharacter();

        a = 0;
        for (int i = 0; i < 2; i++) {
            Enemy e = new Enemy(seed+a, this);
            enemies.add(e);
            a++;
        }
        for (Enemy i: enemies) {
            i.putCharacter();
        }



        cover = new TETile[tiles.length][tiles[0].length];
        allBlack();

        lightOff = false;
    }

    public void addEnemy() {
        Enemy e = new Enemy(seed+a, this);
        enemies.add(e);
        a++;
    }
    protected class Room {
        int xSmall;
        int ySmall;
        int w;
        int h;

        protected Room(int xSmall, int ySmall, int w, int h) {
            this.xSmall = xSmall;
            this.ySmall = ySmall;
            this.w = w;
            this.h = h;
            if (w + xSmall >= width || h + ySmall >= height) {
                roomList.remove(this);
                return;
            }

            for (int i = xSmall; i < xSmall + w; i++) {
                if (i == xSmall) {
                    //left and right wall
                    for (int a = xSmall; a <= xSmall + w + 1; a += w + 1) {
                        for (int b = ySmall; b < ySmall + h; b++) {
                            tiles[a - 1][b] = Tileset.WALL;
                        }
                    }
                    //up and down wall
                    for (int c = ySmall; c <= ySmall + h + 1; c += h + 1) {
                        for (int x = xSmall - 1; x < xSmall + w + 1; x++) {
                            tiles[x][c - 1] = Tileset.WALL;
                        }
                    }
                }
                for (int j = ySmall; j < ySmall + h; j++) {
                    tiles[i][j] = Tileset.FLOOR;
                }
            }
        }

        protected int getX() {
            return xSmall;
        }

        protected int getY() {
            return ySmall;
        }

        protected int getWidth() {
            return w;
        }

        protected int getHeight() {
            return h;
        }

        protected int getCenterX() {
            return xSmall + w / 2;
        }

        protected int getCenterY() {
            return ySmall + h / 2;
        }
    }


    public void randomRoom() {
        //Random random2 = new Random();
        int w = random.nextInt((width) / 8 + 1) + 2;
        int h = random.nextInt((height) / 8 + 1) + 2;
        //Random random = new Random();
        int x = random.nextInt(width - w) + 1;
        int y = random.nextInt(height - h) + 1;
        //System.out.println("test "+x+", "+y+" "+w+" "+h);
        if (hasNoRoomNext(x, y, w, h) && w + x < width && h + y < height) {
            //System.out.println("true");
            roomList.add(new Room(x, y, w, h));
        }
    }


    private boolean hasNoRoomNext(int x, int y, int w, int h) {
        if (x > 0) {
            x = x - 1;
        }
        if (y > 0) {
            y = y - 1;
        }
        int wide = x + w;
        int high = y + h;
        if (wide < width - 1) {
            wide = x + w + 1;
        }
        if (high < height - 1) {
            high = y + h + 1;
        }
        for (int i = x; i < wide + 1; i++) {
            for (int j = y; j < high + 1; j++) {
                if (tiles[i][j] == Tileset.FLOOR) {
                    return false;
                }
            }
        }
        return true;
    }

    public void connectRooms() {
        for (int i = 0; i < roomList.size() - 1; i++) {
            connect(roomList.get(i), roomList.get(i + 1));
        }
    }

    private void connect(Room a, Room b) {
        //Random random = new Random();
        int ax = random.nextInt(a.w) + a.xSmall;
        int ay = random.nextInt(a.h) + a.ySmall;
        int bx = random.nextInt(b.w) + b.xSmall;
        int by = random.nextInt(b.h) + b.ySmall;
        int xlast = ax;
        //System.out.println("connect "+ax+", "+ay+" "+bx+", "+by);
        if (ax < bx) {
            for (int i = ax; i < bx + 1; i++) {
                tiles[i][ay] = Tileset.FLOOR;
                xlast = i;
            }
            if (ay < by) {
                for (int i = ay; i < by + 1; i++) {
                    tiles[xlast][i] = Tileset.FLOOR;
                }
            } else {
                for (int i = by; i < ay + 1; i++) {
                    tiles[xlast][i] = Tileset.FLOOR;
                }
            }
        } else {
            for (int i = bx; i < ax + 1; i++) {
                tiles[i][by] = Tileset.FLOOR;
                xlast = i;
            }
            if (ay < by) {
                for (int i = ay; i < by + 1; i++) {
                    tiles[xlast][i] = Tileset.FLOOR;
                }
            } else {
                for (int i = by; i < ay + 1; i++) {
                    tiles[xlast][i] = Tileset.FLOOR;
                }
            }
        }
        wall();
    }
    private void wall() {
        //wall
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (tiles[x][y] == Tileset.NOTHING
                        && (tiles[x][y + 1] == Tileset.FLOOR || tiles[x][y - 1] == Tileset.FLOOR
                        || tiles[x - 1][y] == Tileset.FLOOR || tiles[x + 1][y] == Tileset.FLOOR)) {
                    tiles[x][y] = Tileset.WALL;
                }
                //edge of the hall
                if (tiles[x - 1][y] == Tileset.WALL
                        && (tiles[x - 1][y - 1] == Tileset.FLOOR || tiles[x - 1][y + 1] == Tileset.FLOOR)
                        && tiles[x][y] == Tileset.NOTHING) {
                    tiles[x][y] = Tileset.WALL;
                }
            }
        }
        //edge of the world
        //bottom
        for (int x = 1; x < width - 1; x++) {
            //left corner
            if (tiles[0][1] == Tileset.WALL && tiles[1][1] == Tileset.FLOOR) {
                tiles[0][0] = Tileset.WALL;
            }
            if (tiles[x][0] == Tileset.NOTHING && (tiles[x][1] == Tileset.FLOOR
                    || (tiles[x - 1][0] == Tileset.WALL && tiles[x - 1][1] == Tileset.FLOOR))) {
                tiles[x][0] = Tileset.WALL;
            }
        }
        //top
        for (int x = 1; x < width - 1; x++) {
            if (tiles[x][height - 1] == Tileset.NOTHING && tiles[x][height - 2] == Tileset.FLOOR) {
                tiles[x][height - 1] = Tileset.WALL;
            }
            //fix edge
            if (tiles[x - 1][height - 1] == Tileset.WALL && tiles[x - 1][height - 2] == Tileset.FLOOR
                    && tiles[x][height - 1] == Tileset.NOTHING) {
                tiles[x][height - 1] = Tileset.WALL;
            }
        }
        //left
        for (int y = 1; y < height - 1; y++) {
            if (tiles[width - 2][y] == Tileset.FLOOR) {
                tiles[width - 1][y] = Tileset.WALL;
            }
        }
        //right
        for (int y = 1; y < height - 1; y++) {
            if (tiles[1][y] == Tileset.FLOOR) {
                tiles[0][y] = Tileset.WALL;
            }
        }
    }

    public TETile[][] getTiles() {
        return tiles;
    }

    public void export() {
        FileUtils.writeFile(Long.toString(seed), TETile.toString(tiles));
    }

    public static World fromTileToWorld(TETile[][] tiles) {
        World result = new World(tiles);
        result.fromInfoToWorld();
        result.fromEnemies();
        return result;
    }

    private void fromEnemies() {
        String input = FileUtils.readFile("previousEnemies.txt");
        String[] lines = input.split("\n");
        for (String i: lines) {
            String[] str = i.split(" ");
            enemies.add(new Enemy(Integer.parseInt(str[0]), Integer.parseInt(str[1]), this));
        }
    }

    private void fromInfoToWorld() {
        String input = FileUtils.readFile("previousInfo.txt");
        String[] lines = input.split("\n");
        this.width = Integer.parseInt(lines[0]);
        this.height = Integer.parseInt(lines[1]);
        this.seed = Long.parseLong(lines[2]);
        this.random = new Random(seed);
        this.character = new Engine(seed, this);
        this.coins = Integer.parseInt(lines[6]);
        character.fillInfo(lines);
        character.updateCoins(coins);

        this.lightOff = Boolean.parseBoolean(lines[5]);
        String coverIn = FileUtils.readFile("previousCover.txt");
        this.cover = convertStringToTile(coverIn);


    }

    public static TETile[][] convertStringToTile(String input) {
        // Split the input string into lines
        String[] lines = input.split("\n");

        // Determine the dimensions of the array
        int width = lines[0].length();
        int height = lines.length;

        // Create a 2D tileengine.Tileset array to store the result
        TETile[][] result = new TETile[width][height];

        // Iterate through each character in the string
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char character = lines[height - 1 - y].charAt(x);
                // Map '█' to LOCK_DOOR and '·' to FLOOR
                if (character == ' ') {
                    result[x][y] = Tileset.NOTHING;
                } else if (character == '#') {
                    result[x][y] = Tileset.WALL;
                } else if (character == '·') {
                    result[x][y] = Tileset.FLOOR;
                } else if (character == '@') {
                    result[x][y] = Tileset.AVATAR;
                } else if (character == '"') {
                    result[x][y] = Tileset.GRASS;
                } else if (character == '≈') {
                    result[x][y] = Tileset.WATER;
                } else if (character == '❀') {
                    result[x][y] = Tileset.FLOWER;
                } else if (character == '█') {
                    result[x][y] = Tileset.LOCKED_DOOR;
                } else if (character == '▢') {
                    result[x][y] = Tileset.UNLOCKED_DOOR;
                } else if (character == '▒') {
                    result[x][y] = Tileset.SAND;
                } else if (character == '▲') {
                    result[x][y] = Tileset.MOUNTAIN;
                } else if (character == '♠') {
                    result[x][y] = Tileset.TREE;
                } else {
                    result[x][y] = Tileset.NOTHING;
                }
            }
        }

        return result;
    }



    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Engine getCharacter() {
        return character;
    }

    public long getSeed() {
        return seed;
    }

    public Random getRandom() {
        return random;
    }

    public void covering() {
        int x = character.getX();
        int y = character.getY();
        int xMin = Math.max(0, x - 3);
        int xMax = Math.min(width - 1, x + 3);
        int yMin = Math.max(0, y - 3);
        int yMax = Math.min(height - 1, y + 3);
        for (int i = xMin; i <= xMax; i++) {
            for (int j = yMin; j <= yMax; j++) {
                if (isInsideRhombus(i, j, xMin, xMax, yMin, yMax)) {
                    cover[i][j] = tiles[i][j];
                }
            }
        }
    }

    private static boolean isInsideRhombus(int x, int y, int xMin, int xMax, int yMin, int yMax) {
        int centerX = (xMax + xMin) / 2;
        int centerY = (yMax + yMin) / 2;
        int radiusX = (xMax - xMin) / 2;
        int radiusY = (yMax - yMin) / 2;
        return Math.abs(x - centerX) * radiusY + Math.abs(y - centerY) * radiusX <= radiusX * radiusY;
    }
    public TETile[][] getCover() {
        lightOff = true;
        covering();
        return cover;
    }

    public void allBlack() {
        for (int i = 0; i < cover.length; i++) {
            for (int j = 0; j < cover[i].length; j++) {
                cover[i][j] = Tileset.NOTHING;
            }
        }
    }

    public boolean getLightOff() {
        return lightOff;
    }

    public void changeLightOff() {
        lightOff = !lightOff;
    }

    public TETile[][] getoutCover() {
        return this.cover;
    }

    public boolean getEncounter() {
        return encounter;
    }

    public int getCoins() {
        return coins;
    }

    public void changeCoins(int i) {
        coins = i;
        getCharacter().updateCoins(i);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void saveNew() {
        World current = new WorldOrigin(getWidth(), getHeight(), getSeed());
        FileUtils.writeFile("previousText.txt", TETile.toString(current.getTiles()));
        FileUtils.writeFile("previousInfo.txt", current.getWidth() + "\n"
                + current.getHeight() + "\n" + current.getSeed() + "\n"
                + current.getCharacter() + "\n" + current.getLightOff() + "\n" + current.getCoins());
        FileUtils.writeFile("previousCover.txt", TETile.toString(current.getoutCover()));
        String en = "";
        for (Enemy enemy: current.getEnemies()) {
            en += enemy.getX() + " " + enemy.getY() + "\n";
        }
        FileUtils.writeFile("previousEnemies.txt", en);
    }

}
