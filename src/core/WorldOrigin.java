package core;

import core.Engine;
import org.slf4j.helpers.Util;
import tileengine.TETile;
import tileengine.Tileset;
import utils.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldOrigin extends World {

    public WorldOrigin(TETile[][] tile) {
        super(tile);
    }
    public WorldOrigin(int width, int height, long seed) {
        super(width, height, seed);

        putFlower();
        putLock();
        putEnd();
        getCharacter().updateCoins(coins);
    }

    public int countRoomsBetweenNearestRooms(int x1, int y1, int x2, int y2) {
        Room nearestRoom1 = findNearestRoom(x1, y1);
        Room nearestRoom2 = findNearestRoom(x2, y2);

        if (nearestRoom1 == null || nearestRoom2 == null) {
            return -1; // Indicate that one or both nearest rooms are not found
        }

        // Find the minimum and maximum coordinates of the rectangle formed by the two nearest rooms
        int minX = Math.min(nearestRoom1.getX(), nearestRoom2.getX());
        int minY = Math.min(nearestRoom1.getY(), nearestRoom2.getY());
        int maxX = Math.max(nearestRoom1.getX() + nearestRoom1.getWidth(), nearestRoom2.getX() + nearestRoom2.getWidth());
        int maxY = Math.max(nearestRoom1.getY() + nearestRoom1.getHeight(), nearestRoom2.getY() + nearestRoom2.getHeight());

        int count = 0;

        // Iterate through the rooms and count those that lie within the rectangle
        for (Room room : roomList) {
            int roomX = room.getX();
            int roomY = room.getY();
            int roomMaxX = roomX + room.getWidth();
            int roomMaxY = roomY + room.getHeight();

            if (roomX >= minX && roomY >= minY && roomMaxX <= maxX && roomMaxY <= maxY) {
                count++;
            }
        }

        return count;
    }

    private Room findNearestRoom(int x, int y) {
        Room nearestRoom = null;
        int minDistance = Integer.MAX_VALUE;

        for (Room room : roomList) {
            int centerX = room.getCenterX();
            int centerY = room.getCenterY();
            int distance = Math.abs(centerX - x) + Math.abs(centerY - y);

            if (distance < minDistance) {
                minDistance = distance;
                nearestRoom = room;
            }
        }

        return nearestRoom;
    }

    private void putEnd() {
        xf = random.nextInt(width - 1);
        yf = random.nextInt(height - 1);
        while (tiles[xf][yf] != Tileset.FLOOR || distance(xf, yf, getCharacter().getX(), getCharacter().getY()) < 10) {
            xf = random.nextInt(width - 1);
            yf = random.nextInt(height - 1);
        }
        tiles[xf][yf] = Tileset.WATER;
    }

    private double distance(int x, int y, int xc, int yc) {
        return Math.sqrt(Math.pow(x - xc, 2) + Math.pow(y - yc, 2));
    }

    private void putFlower() {
        xf = random.nextInt(width - 1);
        yf = random.nextInt(height - 1);
        while (tiles[xf][yf] != Tileset.FLOOR) {
            xf = random.nextInt(width - 1);
            yf = random.nextInt(height - 1);
        }
        tiles[xf][yf] = Tileset.FLOWER;
        int c = 3 + countRoomsBetweenNearestRooms(getCharacter().getX(), getCharacter().getY(), xf, yf);
        changeCoins(c);
        System.out.println(coins);
    }

    private void putLock() {
        for (Room room: roomList) {
            for (int i = room.xSmall-1; i <= room.xSmall+room.w; i++) {
                if (i >= width || i < 0) {
                    break;
                }
                if (room.ySmall-1 < 0) {
                    break;
                }
                if ((tiles[i][room.ySmall-1] == Tileset.FLOOR)
                        && (tiles[i][room.ySmall-2] != Tileset.LOCKED_DOOR)
                        && (tiles[i][room.ySmall] != Tileset.LOCKED_DOOR)) {
                    tiles[i][room.ySmall-1] = Tileset.LOCKED_DOOR;
                }
                if (room.ySmall+room.h >= height) {
                    break;
                }
                if (tiles[i][room.ySmall+room.h] == Tileset.FLOOR
                        && tiles[i][room.ySmall+room.h-1] != Tileset.LOCKED_DOOR
                        && tiles[i][room.ySmall+room.h+1] != Tileset.LOCKED_DOOR) {
                    tiles[i][room.ySmall+room.h] = Tileset.LOCKED_DOOR;
                }
            }
        }
    }
}
