import java.util.ArrayList;
import java.util.List;

/**
 * DisjointSets
 * tobrien - 9/14/12
 */

public class DisjointSets {
    final protected List<Integer> sets;

    public DisjointSets() {
        sets = new ArrayList<Integer>();
    }

    public DisjointSets(int size) {
        sets = new ArrayList<Integer>(size);
        addElements(size);
    }

    public void addElements(int count) {
        for (int i = 0; i < count; ++i)
            sets.add(-1);
    }

    public int findRoot(int element) {
        int max = sets.size() - 1;
        int min = 0;

        if (element > max || element < min) {
            return -1;
        }

        if (sets.get(element) < 0) {
            return element;
        }

        sets.set(element, findRoot(sets.get(element)));

        return sets.get(element);
    }

    public void setUnion(int elementA, int elementB) {
        int rootA = findRoot(elementA);
        int rootB = findRoot(elementB);

        if (rootA == -1 || rootB == -1 || rootA == rootB)
            return;

        if (sets.get(rootA) <= sets.get(rootB)) {
            sets.set(rootA, sets.get(rootA) + sets.get(rootB));
            sets.set(rootB, rootA);
        }
        else {
            sets.set(rootB, sets.get(rootB) + sets.get(rootA));
            sets.set(rootA, rootB);
        }
    }
}
