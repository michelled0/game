package core;

import tileengine.TETile;
import tileengine.Tileset;

import java.util.*;

public class Pathfinding {
    private Position start;
    private Position end;
    private TETile[][] grid;

    private class TreeNode {
        Position position;
        TreeNode parent;
        int g; // cost from start to current node
        double h; // heuristic (estimated cost from current node to goal)

        public TreeNode(Position position, TreeNode parent, int g, double h) {
            this.position = position;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        public double getF() {
            return g + h;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TreeNode treeNode = (TreeNode) obj;
            return Objects.equals(position, treeNode.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(position);
        }
    }

    public Pathfinding(World world, Position start, Position end) {
        this.start = start;
        this.end = end;
        grid = world.getTiles();
    }

    public List<Movement> findShortestPath() {
        if (start.equals(end)) {
            return new LinkedList<>();
        }

        Set<TreeNode> openSet = new HashSet<>();
        Set<Position> closedSet = new HashSet<>();
        PriorityQueue<TreeNode> openQueue = new PriorityQueue<>(Comparator.comparingDouble(TreeNode::getF));

        TreeNode root = new TreeNode(start, null, 0, calculateHeuristic(start, end));
        openSet.add(root);
        openQueue.add(root);

        while (!openQueue.isEmpty()) {
            TreeNode current = openQueue.poll();
            Position currentPosition = current.position;

            if (currentPosition.equals(end)) {
                return reconstructPath(current);
            }

            closedSet.add(currentPosition);

            for (Movement movement : Movement.values()) {
                Position nextPosition = getNextPosition(currentPosition, movement);
                if (isValidMove(grid, nextPosition) && !closedSet.contains(nextPosition)) {
                    int g = current.g + 1;
                    double h = calculateHeuristic(nextPosition, end);
                    TreeNode nextNode = new TreeNode(nextPosition, current, g, h);

                    TreeNode existingNode = getNodeWithEqualPosition(openSet, nextNode);
                    if (existingNode == null || g < existingNode.g) {
                        openSet.add(nextNode);
                        openQueue.add(nextNode);
                    }
                }
            }
        }

        return new LinkedList<>(); // No path found
    }

    private static TreeNode getNodeWithEqualPosition(Set<TreeNode> nodes, TreeNode targetNode) {
        for (TreeNode node : nodes) {
            if (node.position.equals(targetNode.position)) {
                return node;
            }
        }
        return null;
    }

    private static List<Movement> reconstructPath(TreeNode endNode) {
        List<Movement> path = new LinkedList<>();
        TreeNode current = endNode;

        while (current.parent != null) {
            Position currentPos = current.position;
            Position parentPos = current.parent.position;
            if (currentPos.getX() > parentPos.getX()) {
                path.add(Movement.RIGHT);
            } else if (currentPos.getX() < parentPos.getX()) {
                path.add(Movement.LEFT);
            } else if (currentPos.getY() < parentPos.getY()) {
                path.add(Movement.DOWN);
            } else if (currentPos.getY() > parentPos.getY()) {
                path.add(Movement.UP);
            }

            current = current.parent;
        }

        Collections.reverse(path);
        return path;
    }

    private static Position getNextPosition(Position current, Movement movement) {
        int x = current.getX();
        int y = current.getY();

        switch (movement) {
            case LEFT:
                x--;
                break;
            case RIGHT:
                x++;
                break;
            case UP:
                y--;
                break;
            case DOWN:
                y++;
                break;
        }

        return new Position(x, y);
    }

    private static boolean isValidMove(TETile[][] grid, Position position) {
        int rows = grid.length;
        int cols = grid[0].length;

        return position.getX() >= 0 && position.getX() < rows &&
                position.getY() >= 0 && position.getY() < cols &&
                (!grid[position.getX()][position.getY()].equals(Tileset.WALL));
    }

    private static double calculateHeuristic(Position start, Position end) {
        return Math.sqrt(Math.pow(start.getX() - end.getX(), 2) + Math.pow(start.getY() - end.getY(), 2));
    }
}


