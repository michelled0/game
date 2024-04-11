package core;

import tileengine.Tileset;

import java.util.ArrayList;
import java.util.List;

public class EncounterWorld extends World {
    private boolean encounter;
    private int coins;

    public EncounterWorld(int width, int height, long seed, int coin) {
        super(width, height, seed);
        tiles[character.getX()][character.getY()] = character.getPrevious();
        encounter = true;
        coins = coin;
        putCoins();
        character = new Engine(seed, this);
        character.putCharacter();
        tiles[xf][yf] = Tileset.FLOOR;
    }

    public boolean getEncounter() {
        return true;
    }

    public void putCoins() {
        int n = 10; //getRandom().nextInt(20)+5;
        for (int i = 0; i < n; i++) {
            putCoin();
        }
    }

    private void putCoin() {
        int x = giveX();
        int y = giveY();
        while (tiles[x][y] != Tileset.FLOOR) {
            x = giveX();
            y = giveY();
        }
        tiles[x][y] = Tileset.SAND;
    }

    private int giveX() {
        return getRandom().nextInt(getWidth());
    }

    private int giveY() {
        return getRandom().nextInt(getHeight());
    }

    public int getCoins() {
        return coins;
    }
}
