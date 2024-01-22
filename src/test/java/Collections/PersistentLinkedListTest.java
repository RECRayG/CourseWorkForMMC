package Collections;

import API.PersistentCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersistentLinkedListTest {
    private PersistentLinkedList<Integer> persistentLinkedList;

    @BeforeEach
    void setUp() {
        persistentLinkedList = new PersistentLinkedList<>();
    }

    @Test
    void addAndGet() {
        // Добавление элемента и проверка его наличия
        persistentLinkedList.add(1);
        assertEquals(1, persistentLinkedList.size());
        assertTrue(persistentLinkedList.contains(1));

        // Добавление еще одного элемента и проверка их наличия
        persistentLinkedList.add(2);
        assertEquals(2, persistentLinkedList.size());
        assertTrue(persistentLinkedList.contains(1));
        assertTrue(persistentLinkedList.contains(2));

        // Получение элемента по индексу
        assertEquals(1, persistentLinkedList.get(0));
        assertEquals(2, persistentLinkedList.get(1));
    }

    @Test
    void update() {
        // Добавление элемента и его обновление
        persistentLinkedList.add(1);
        persistentLinkedList.update(0, 10);
        assertEquals(10, persistentLinkedList.get(0));

        // Попытка обновления с недопустимым индексом
        assertThrows(IndexOutOfBoundsException.class, () -> persistentLinkedList.update(1, 20));
    }

    @Test
    void remove() {
        // Добавление элемента и его удаление
        persistentLinkedList.add(1);
        assertEquals(1, persistentLinkedList.size());
        assertTrue(persistentLinkedList.contains(1));

        persistentLinkedList.remove(1);
        assertEquals(0, persistentLinkedList.size());
        assertFalse(persistentLinkedList.contains(1));

        // Попытка удаления несуществующего элемента
        persistentLinkedList.remove(2);
        assertEquals(0, persistentLinkedList.size());
    }

    @Test
    void undo() {
        // Добавление элемента, отмена операции и проверка
        persistentLinkedList.add(1);
        assertEquals(1, persistentLinkedList.size());

        persistentLinkedList.undo();
        assertEquals(0, persistentLinkedList.size());
    }

    @Test
    void redo() {
        // Добавление элемента, отмена и повтор операции
        persistentLinkedList.add(1);
        assertEquals(1, persistentLinkedList.size());

        persistentLinkedList.undo();
        assertEquals(0, persistentLinkedList.size());

        persistentLinkedList.redo();
        assertEquals(1, persistentLinkedList.size());
    }

    @Test
    void contains() {
        // Проверка наличия элемента
        assertFalse(persistentLinkedList.contains(1));

        persistentLinkedList.add(1);
        assertTrue(persistentLinkedList.contains(1));
    }

    @Test
    void size() {
        // Проверка размера после добавления и удаления элементов
        assertEquals(0, persistentLinkedList.size());

        persistentLinkedList.add(1);
        assertEquals(1, persistentLinkedList.size());

        persistentLinkedList.remove(1);
        assertEquals(0, persistentLinkedList.size());
    }

    @Test
    public void testDeepUndoAndRedo() {
        PersistentLinkedList<Integer> array = new PersistentLinkedList<>();

        // Добавляем элементы 1, 2, 3, 4, 5
        PersistentCollection<Integer> version1 = array.add(1).add(2).add(3).add(4).add(5);

        // Проверяем состояние после добавления
        assertEquals(5, version1.size());
        assertTrue(version1.contains(1));
        assertTrue(version1.contains(2));
        assertTrue(version1.contains(3));
        assertTrue(version1.contains(4));
        assertTrue(version1.contains(5));

        // Отменяем несколько операций (удаление 5, 4, 3)
        PersistentCollection<Integer> undo1 = version1.undo().undo().undo();
        assertEquals(2, undo1.size());
        assertTrue(undo1.contains(1));
        assertTrue(undo1.contains(2));
        assertFalse(undo1.contains(3));
        assertFalse(undo1.contains(4));
        assertFalse(undo1.contains(5));

        // Восстанавливаем отмененные операции (добавление 3, 4, 5)
        PersistentCollection<Integer> redo1 = undo1.redo().redo().redo();
        assertEquals(5, redo1.size());
        assertTrue(redo1.contains(1));
        assertTrue(redo1.contains(2));
        assertTrue(redo1.contains(3));
        assertTrue(redo1.contains(4));
        assertTrue(redo1.contains(5));

        // Отменяем все операции
        PersistentCollection<Integer> undo2 = redo1.undo().undo().undo().undo().undo();
        assertEquals(0, undo2.size());
        assertFalse(undo2.contains(1));
        assertFalse(undo2.contains(2));
        assertFalse(undo2.contains(3));
        assertFalse(undo2.contains(4));
        assertFalse(undo2.contains(5));
    }
}