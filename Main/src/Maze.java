import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maze
 * tobrien - 9/14/12
 */

public class Maze {
    final private Cube[] cubes;
    final private int width;
    final private int height;
    final private int depth;
    private int start;
    private int end;
    private int length;

    /**
     * Maze Constructor
     *
     * @param width     number of spaces wide (excluding walls)
     * @param height    number of spaces tall (excluding walls)
     * @param depth     number of layers (excluding walls)
     */
    public Maze(int width, int height, int depth) {
        this.width = 2*width - 1;
        this.height = 2*height - 1;
        this.depth = 2*depth - 1;

        int size = this.width*this.height*this.depth;

        cubes = new Cube[size];
        DisjointSets sets = new DisjointSets(size);
        List<Integer> east = new ArrayList<Integer>(size/2);
        List<Integer> south = new ArrayList<Integer>(size/2);
        List<Integer> back = new ArrayList<Integer>(size/2);

        initializeLists(east, south, back);

        do {
            tryEast( sets, east.remove(east.size()-1));
            trySouth( sets, south.remove(south.size()-1));
            tryBack( sets, back.remove(back.size()-1));

        } while (!east.isEmpty());

        setStart();
        setEnd();
    }

    /*
     * Public methods
     */
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public int at(int row, int col, int depth) {
        return cubes[flatten(row, col, depth)].type;
    }

    public void print() {
        char W = '0';
        char O = ' ';
        char B = 'X';
        char U = '/';
        char D = '\\';
        char S = 'S';
        char E = 'E';

        for (int dep = 0; dep < depth; dep += 2) {
            System.out.println("DEPTH "+dep/2);

            for (int col = 0; col <= width + 1; ++col)
                System.out.print(W);
            System.out.println();

            for (int row = 0; row < height; ++row) {
                System.out.print(W);

                for (int col = 0; col < width; ++col) {
                    switch (cubes[flatten(row, col, dep)].type) {
                        case Cube.OPEN:
                            if (!isWall(row, col, dep-1) && !isWall(row, col, dep+1)) {
                                System.out.print(B);
                            }
                            else if (!isWall(row, col, dep+1)) {
                                System.out.print(D);
                            }
                            else if (!isWall(row, col, dep-1)) {
                                System.out.print(U);
                            }
                            else {
                                System.out.print(O);
                            }
                            break;
                        case Cube.START:
                            System.out.print(S);
                            break;
                        case Cube.END:
                            System.out.print(E);
                            break;
                        default:
                            System.out.print(W);
                    }
                }
                System.out.println(W);
            }

            for (int col = 0; col <= width + 1; ++col)
                System.out.print(W);
            System.out.println();
            System.out.println();
        }
    }

    /*
     * Private maze building methods
     */
    private void initializeLists(List<Integer> east, List<Integer> south, List<Integer> back) {
        int index;
        for (int dep = 0; dep < depth; ++dep) {
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    index = flatten(row, col, dep);
                    if (even(row) && even(col) && even(dep)) {
                        cubes[index] = new Cube(Cube.OPEN, row, col, dep);

                        east.add(index);
                        south.add(index);
                        back.add(index);
                    }
                    else {
                        cubes[index] = new Cube(Cube.WALL, row, col, dep);
                    }
                }
            }
        }

        Collections.shuffle(east);
        Collections.shuffle(south);
        Collections.shuffle(back);
    }

    private void tryWall(DisjointSets sets, int index, int step) {
        if (sets.findRoot(index) == sets.findRoot(index + step*2))
            return;

        cubes[index + step].type = Cube.OPEN;
        sets.setUnion(index, index + step);
        sets.setUnion(index, index + step * 2);
    }

    private void tryEast(DisjointSets sets, int index) {
        if (index % width >= width - 2 || width == 1)
            return;
        tryWall(sets, index, 1);
    }

    private void trySouth(DisjointSets sets, int index) {
        if ( (index % (width*height)) / width >= height - 2 || height == 1)
            return;
        tryWall(sets, index, width);
    }

    private void tryBack(DisjointSets sets, int index) {
        if (index / (width*height) >= depth - 2 || depth == 1)
            return;
        tryWall(sets, index, width*height);
    }

    private void setStart() {
        start = 0;
        cubes[start].type = Cube.START;
    }

    private void setEnd() {
        solve();
        cubes[end].type = Cube.END;
    }

    /*
     * Private Maze Solver methods
     */
    private void solve() {
        length = 0;
        assignNeighbors();
        DFS(cubes[0], 0);
    }

    private void DFS(Cube cube, int pathLength) {
        cube.visited = true;

        for (int neighborIndex : cube.neighborIndices) {
            if (!cubes[neighborIndex].visited)
                DFS(cubes[neighborIndex], pathLength+1);
        }

        if (checkLength(cube, pathLength)) {
            end = flatten(cube.row, cube.col, cube.dep);
            length = pathLength;
        }

        cube.visited = false;
    }

    private boolean checkLength(Cube cube, int pathLength) {
        if (pathLength > length) {
            return true;
        }
        else if (pathLength == length) {
            if (cube.dep > cubes[end].dep) {
                return true;
            }
            else if (cube.dep == cubes[end].dep) {
                if (cube.row > cubes[end].row) {
                    return true;
                }
                else if (cube.row == cubes[end].row) {
                    if (cube.col > cubes[end].col) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void assignNeighbors() {
        for (int dep = 0; dep < depth; ++dep) {
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    if (!even(row) || !even(col) || !even(dep))
                        continue;

                    for (int dir : Direction.DIRECTIONS) {
                        if (checkDirection(row, col, dep, dir))
                            cubes[flatten(row, col, dep)].neighborIndices.add(getNeighbor(row, col, dep, dir));

                    }
                }
            }
        }
    }

    private int getNeighbor(int row, int col, int dep, int dir) {
        IndexOutOfBoundsException outOfBounds = new IndexOutOfBoundsException("Direction: "+dir+" Position: ("+row+","+col+","+dep+")");

        switch (dir) {
            case Direction.NORTH:
                if (row <= 1) throw outOfBounds;
                return flatten(row-2, col, dep);
            case Direction.SOUTH:
                if (row >= height - 1) throw outOfBounds;
                return flatten(row+2, col, dep);
            case Direction.EAST:
                if (col >= width - 1) throw outOfBounds;
                return flatten(row, col+2, dep);
            case Direction.WEST:
                if (col <= 1) throw outOfBounds;
                return flatten(row, col-2, dep);
            case Direction.UP:
                if (dep <= 1) throw outOfBounds;
                return flatten(row, col, dep-2);
            case Direction.DOWN:
                if (dep >= depth - 1) throw outOfBounds;
                return flatten(row, col, dep+2);
        }
        throw outOfBounds;
    }

    private boolean checkDirection(int row, int col, int dep, int dir) {
        switch (dir) {
            case Direction.NORTH:
                return !isWall(row-1, col, dep);
            case Direction.SOUTH:
                return !isWall(row+1, col, dep);
            case Direction.EAST:
                return !isWall(row, col+1, dep);
            case Direction.WEST:
                return !isWall(row, col-1, dep);
            case Direction.UP:
                return !isWall(row, col, dep-1);
            case Direction.DOWN:
                return !isWall(row, col, dep+1);
        }

        return false;
    }

    /*
     * Private util methods
     */
    private int flatten(int row, int col, int depth) {
        return col + row*this.width + depth*this.width*this.height;
    }

    private boolean isWall(int row, int col, int depth) {
        return row < 0 || row >= height || col < 0 || col >= width || depth < 0 || depth >= this.depth || cubes[flatten(row, col, depth)].type == Cube.WALL;
    }

    /*
     * Private static util functions
     */
    private static boolean even(int i) {
        return i % 2 == 0;
    }

    /*
     * Inner classes
     */
    public static class Cube {
        final static public int OPEN = 0;
        final static public int WALL = 1;
        final static public int START = 2;
        final static public int END = 3;

        public int type;
        final public int row;
        final public int col;
        final public int dep;
        public boolean visited;
        public List<Integer> neighborIndices;

        public Cube(int type, int row, int col, int depth) {
            this.type = type;
            this.row = row;
            this.col = col;
            this.dep = depth;
            this.visited = false;
            this.neighborIndices = new ArrayList<Integer>(Direction.DIRECTIONS.length);
        }
    }

    public static class Direction {
        public static final int NORTH = 0;
        public static final int SOUTH = 1;
        public static final int EAST = 2;
        public static final int WEST = 3;
        public static final int UP = 4;
        public static final int DOWN = 5;

        public static final int[] DIRECTIONS = {NORTH, SOUTH, EAST, WEST, UP, DOWN};
    }
}
