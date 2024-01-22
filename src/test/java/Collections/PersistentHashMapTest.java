package Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PersistentHashMapTest {
    private PersistentHashMap<String, Integer> hashMap;

    @BeforeEach
    void setUp() {
        hashMap = new PersistentHashMap<String, Integer>();
    }

    @Test
    void addAndGet() {
        // Добавление элемента и проверка его наличия
        hashMap.add("one", 1);
        assertEquals(1, hashMap.size());
        assertTrue(hashMap.contains("one", 1));

        // Повторное добавление элемента и проверка наличия только одного экземпляра
        hashMap.add("one", 1);
        assertEquals(1, hashMap.size());
        assertTrue(hashMap.contains("one", 1));

        // Добавление другого элемента и проверка его наличия
        hashMap.add("two", 2);
        assertEquals(2, hashMap.size());
        assertTrue(hashMap.contains("two", 2));

        // Получение элемента по ключу
        Map.Entry<String, Integer> entry = hashMap.get("one");
        assertNotNull(entry);
        assertEquals("one", entry.getKey());
        assertEquals(1, entry.getValue());
    }

    @Test
    void remove() {
        // Добавление элемента и его удаление
        hashMap.add("one", 1);
        assertEquals(1, hashMap.size());
        assertTrue(hashMap.contains("one", 1));

        hashMap.delete("one");
        assertEquals(0, hashMap.size());
        assertFalse(hashMap.contains("one", 1));

        // Попытка удаления несуществующего элемента
        assertNull(hashMap.delete("nonexistent"));
    }

    @Test
    void size() {
        // Проверка размера после добавления элементов
        assertEquals(0, hashMap.size());

        hashMap.add("one", 1);
        assertEquals(1, hashMap.size());

        hashMap.add("two", 2);
        assertEquals(2, hashMap.size());

        // Проверка размера после удаления элемента
        hashMap.delete("one");
        assertEquals(1, hashMap.size());
    }

    @Test
    void undo() {
        // Добавление элемента и отмена операции
        hashMap.add("one", 1);
        assertEquals(1, hashMap.size());

        hashMap.undo();
        assertEquals(0, hashMap.size());

        // Попытка отмены при отсутствии операций
        hashMap.undo();
        assertEquals(0, hashMap.size());
    }

    @Test
    void redo() {
        // Добавление элемента, отмена и повтор операции
        hashMap.add("one", 1);
        assertEquals(1, hashMap.size());

        hashMap.undo();
        assertEquals(0, hashMap.size());

        hashMap.redo();
        assertEquals(1, hashMap.size());

        // Попытка повтора при отсутствии отмененных операций
        hashMap.redo();
        assertEquals(1, hashMap.size());
    }

    @Test
    public void TestUndoTree() {
        PersistentHashMap<Integer, Integer> hashMap = new PersistentHashMap();

        hashMap.add(1,0).add(2,0).add(3,0);
        assertTrue(hashMap.contains(1,0));
        assertTrue(hashMap.contains(2,0));
        assertTrue(hashMap.contains(3,0));

        hashMap.delete(3).add(3,0).undo();
        assertTrue(hashMap.contains(1,0));
        assertTrue(hashMap.contains(2,0));
        assertFalse(hashMap.contains(3,0));

        hashMap.add(4,0);
        assertTrue(hashMap.contains(1,0));
        assertTrue(hashMap.contains(2,0));
        assertFalse(hashMap.contains(3,0));
        assertTrue(hashMap.contains(4,0));

        hashMap.undo();
        assertTrue(hashMap.contains(1,0));
        assertTrue(hashMap.contains(2,0));
        assertFalse(hashMap.contains(3,0));
        assertFalse(hashMap.contains(4,0));
    }
}