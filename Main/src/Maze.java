import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maze
 * tobrien - 9/14/12
 */

public class Maze {
    final private int[] cubes;
    final private int width;
    final private int height;
    final private int depth;

    public Maze(int width, int height, int depth) {
        this.width = 2*width - 1;
        this.height = 2*height - 1;
        this.depth = 2*depth - 1;

        int size = this.width*this.height*this.depth;

        cubes = new int[size];
        DisjointSets sets = new DisjointSets(size);
        List<Integer> east = new ArrayList<Integer>(size/2);
        List<Integer> south = new ArrayList<Integer>(size/2);
        List<Integer> back = new ArrayList<Integer>(size/2);

        int index;
        for (int dep = 0; dep < this.depth; ++dep) {
            for (int row = 0; row < this.height; ++row) {
                for (int col = 0; col < this.width; ++col) {
                    index = flatten(row, col, dep);
                    if (even(row) && even(col) && even(dep)) {
                        cubes[index] = Cube.OPEN;

                        east.add(index);
                        south.add(index);
                        back.add(index);
                    }
                    else {
                        cubes[index] = Cube.WALL;
                    }
                }
            }
        }

        Collections.shuffle(east);
        Collections.shuffle(south);
        Collections.shuffle(back);

        do {
            tryEast( sets, east.remove(east.size()-1));
            trySouth( sets, south.remove(south.size()-1));
            tryBack( sets, back.remove(back.size()-1));

        } while (!east.isEmpty());

        setStart();
        setEnd();
    }

    public int at(int row, int col, int depth) {
        return cubes[flatten(row, col, depth)];
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
                    switch (cubes[flatten(row, col, dep)]) {
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

    private void tryWall(DisjointSets sets, int index, int step) {
        if (sets.findRoot(index) == sets.findRoot(index + step*2))
            return;

        cubes[index + step] = Cube.OPEN;
        sets.setUnion(index, index + step);
        sets.setUnion(index, index + step*2);
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
        cubes[0] = Cube.START;
    }

    private void setEnd() {

    }

    private int flatten(int row, int col, int depth) {
        return col + row*this.width + depth*this.width*this.height;
    }

    private boolean isWall(int row, int col, int depth) {
        return row < 0 || row >= height || col < 0 || col >= width || depth < 0 || depth >= this.depth || cubes[flatten(row, col, depth)] == Cube.WALL;
    }

    private static boolean even(int i) {
        return i % 2 == 0;
    }
}
