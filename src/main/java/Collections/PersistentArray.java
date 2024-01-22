package Collections;

import API.PersistentCollection;
import Help.BitTree;

import java.util.Stack;

public class PersistentArray<T> implements PersistentCollection<T> {
    private final Stack<BitTree<T>> undoHistory = new Stack<>();
    private final Stack<BitTree<T>> redoHistory = new Stack<>();
    public static final int INITIAL_PERSISTENT_ARRAY_SIZE = 16;

    public PersistentArray() {
        BitTree<T> bitTree = new BitTree<>(INITIAL_PERSISTENT_ARRAY_SIZE);
        this.undoHistory.push(bitTree);
        this.redoHistory.clear();
    }

    public PersistentArray(BitTree<T> bitTree) {
        this.undoHistory.push(new BitTree<>(bitTree));
        this.redoHistory.clear();
    }

    /**
     * Метод для вставки элемента в коллекцию.
     * @param element Элемент для добавления.
     * @return Изменённая коллекция с добавленным элементом.
     */
    @Override
    public PersistentArray<T> add(T element) {
        BitTree<T> bitTree = new BitTree<>(this.undoHistory.peek());
        this.undoHistory.push(bitTree);
        this.redoHistory.clear();
        bitTree.addArray(element);
        return this;
    }

    @Override
    public PersistentCollection<T> update(int index, T element) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        BitTree<T> bitTree = new BitTree<>(this.undoHistory.peek());
        bitTree.update(index, element);

        this.undoHistory.push(bitTree);
        this.redoHistory.clear();

        return this;
    }

    /**
     * Метод для удаления элемента из коллекции.
     * @param element Элемент для удаления.
     * @return Изменённая коллекция без удалённого элемента.
     */
    @Override
    public PersistentArray<T> remove(T element) {
        BitTree<T> bitTree = new BitTree<>(this.undoHistory.peek());
        this.undoHistory.push(bitTree);
        this.redoHistory.clear();
        bitTree.removeArray(element);
        return this;
    }

    /**
     * Получить последнюю сохранённую версию коллекции (до выполнения любой операции).
     */
    @Override
    public PersistentArray<T> undo() {
        if (!undoHistory.isEmpty()) {
            redoHistory.push(undoHistory.pop());
        }
        return this;
    }

    /**
     * Повторить последнее изменение с коллекцией после undo().
     */
    @Override
    public PersistentArray<T> redo() {
        if (!redoHistory.isEmpty()) {
            undoHistory.push(redoHistory.pop());
        }
        return this;
    }

    /**
     * Получить элемент коллекции по индексу.
     * @param param Индекс элемента.
     * @return Значение элемента коллекции.
     */
    @Override
    public T get(Object param) {
        return this.undoHistory.peek().get((Integer) param);
    }

    /**
     * Метод для получения информации о наличии элемента в коллекции.
     * @param element Элемент для проверки.
     * @return true, если элемент присутствует в коллекции,<br> false, если элемент отсутствует в коллекции.
     */
    @Override
    public boolean contains(T element) {
        BitTree<T> bitTree = new BitTree<>(this.undoHistory.peek());
        return bitTree.contains(element);
    }

    /**
     * Метод для получения текущей размерности коллекции.
     * @return Размерность коллекции.
     */
    @Override
    public int size() {
        return this.undoHistory.peek().getSize();
    }
}
