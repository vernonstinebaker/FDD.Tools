package net.sourceforge.fddtools.ui.fx;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TreeMoveHelperTest {
    @Test
    void appendOnNegativeIndex() {
        var oldList = new ArrayList<>(List.of("A","B","C"));
        var newList = new ArrayList<String>();
        int idx = TreeMoveHelper.move(oldList, newList, "B", -1);
        assertEquals(0, idx);
        assertEquals(List.of("A","C"), oldList);
        assertEquals(List.of("B"), newList);
    }

    @Test
    void clampBeyondSizeToAppend() {
        var list = new ArrayList<>(List.of(1,2,3));
        int idx = TreeMoveHelper.move(list, list, 2, 99); // move within same list
        // move removed original '2' then appended at end due to >size after removal (size now 2 so index->append at 2)
        assertEquals(2, idx);
        assertEquals(List.of(1,3,2), list);
    }

    @Test
    void insertWithinBoundsSameParent() {
        var list = new ArrayList<>(List.of('x','y','z'));
        int idx = TreeMoveHelper.move(list, list, 'z', 1);
        assertEquals(1, idx);
        assertEquals(List.of('x','z','y'), list);
    }

    @Test
    void crossParentInsertMiddle() {
        var oldList = new ArrayList<>(List.of("one","two","three"));
        var newList = new ArrayList<>(List.of("alpha","omega"));
        int idx = TreeMoveHelper.move(oldList, newList, "two", 1); // middle insertion
        assertEquals(1, idx);
        assertEquals(List.of("one","three"), oldList); // removed
        assertEquals(List.of("alpha","two","omega"), newList);
    }

    @Test
    void appendWhenIndexEqualsSize() {
        var list = new ArrayList<>(List.of(10,20,30));
        int size = list.size();
        int idx = TreeMoveHelper.move(list, list, 20, size); // requested index == size after removal => append
        assertEquals(size-1, idx); // after removal size shrinks by 1 then append at end -> new index last position
        assertEquals(List.of(10,30,20), list);
    }

    @Test
    void insertAtFrontIndexZero() {
        var oldList = new ArrayList<>(List.of('a','b','c'));
        var newList = new ArrayList<>(List.of('x','y'));
        int idx = TreeMoveHelper.move(oldList, newList, 'b', 0);
        assertEquals(0, idx);
        assertEquals(List.of('a','c'), oldList);
        assertEquals(List.of('b','x','y'), newList);
    }
}
