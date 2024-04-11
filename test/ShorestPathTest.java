package test;

import core.Movement;
import core.Pathfinding;
import core.Position;
import core.World;
import edu.princeton.cs.algs4.StdDraw;
import org.junit.jupiter.api.Test;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

import java.util.ArrayList;
import java.util.List;

public class ShorestPathTest {
    @Test
    public void shortest() {
        TERenderer ter = new TERenderer();
        TETile[][] grid = new TETile[4][4];
        for (int i = 0; i <4; i++) {
            for (int j = 0; j < 4; j++) {
                grid[i][j] = Tileset.FLOOR;
            }
        }
        grid[0][0] = Tileset.AVATAR;
        grid[1][3] = Tileset.TREE;
        ter.initialize(4,4);
        ter.renderFrame(grid);
        World world = new World(grid);
        Pathfinding find = new Pathfinding(world, new Position(0,0), new Position(1,3));
        List<Movement> a = find.findShortestPath();
        List<int[]> result = new ArrayList<>();
        for (Movement i: a) {
            System.out.print(i);
        }
        //StdDraw.pause(100000);

    }
}
