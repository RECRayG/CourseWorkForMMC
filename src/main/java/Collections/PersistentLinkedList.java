package Collections;

import API.PersistentCollection;
import Help.BitTree;

import java.util.Stack;

public class PersistentLinkedList<T> implements PersistentCollection<T> {
    /**
     * Структура двунаправленного списка.
     * @param <T> Тип хранимых объектов в коллекции.
     */
    private class Structure<T> extends BitTree<T> {
        /**
         * Ссылка на голову списка. Хранит смещение бит до нужного значения.
         */
        private int head = -1;
        /**
         * Ссылка на хвост списка. Хранит смещение бит до нужного значения.
         */
        private int tail = -1;

        /**
         * Базовый конструктор без параметров.
         */
        public Structure() {
            super(1, 5);
        }

        /**
         * Конструктор копирования.
         * @param structureToCopy Структура для копирования
         */
        public Structure(Structure<T> structureToCopy) {
            super(structureToCopy);
            this.head = structureToCopy.head;
            this.tail = structureToCopy.tail;
        }

        /**
         * Метод для проверки структуры на наличие элементов.
         * @return true, если структура пуста<br>false, если структура хранит элементы
         */
        public boolean isEmpty() {
            return this.getSize() <= 0;
        }

        public int getHead() {
            return head;
        }

        public void setHead(int head) {
            this.head = head;
        }

        public int getTail() {
            return tail;
        }

        public void setTail(int tail) {
            this.tail = tail;
        }
    }

    /**
     * Узел двунаправленного списка.
     * @param <T> Тип хранимых объектов в коллекции.
     */
    private class Node<T> {
        /**
         * Ссылка на следующий узел.
         */
        private int next;
        /**
         * Ссылка на предыдущий узел.
         */
        private int prev;
        /**
         * Значение.
         */
        private T value;

        /**
         * Базовый конструктор.
         * @param value Значение.
         * @param prev Ссылка на предыдущий узел.
         * @param next Ссылка на следующий узел.
         */
        public Node(T value, int prev, int next) {
            this.next = next;
            this.prev = prev;
            this.value = value;
        }

        /**
         * Конструктор копирования.
         * @param nodeToCopy Узел, который будет присвоен текущему
         */
        public Node(Node<T> nodeToCopy) {
            this.next = nodeToCopy.next;
            this.prev = nodeToCopy.prev;
            this.value = nodeToCopy.value;
        }

        public int getNext() {
            return next;
        }

        public void setNext(int next) {
            this.next = next;
        }

        public int getPrev() {
            return prev;
        }

        public void setPrev(int prev) {
            this.prev = prev;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    private final Stack<Structure<Node<T>>> undoHistory= new Stack<>();
    private final Stack<Structure<Node<T>>> redoHistory= new Stack<>();

    public PersistentLinkedList() {
        Structure<Node<T>> head = new Structure<>();
        undoHistory.push(head);
        redoHistory.clear();
    }

    public PersistentLinkedList(BitTree<T> bitTree) {
        Structure<Node<T>> head = new Structure<>();
        // Copy the structure of the BitTree's root node to the linked list's head
        head.setRoot(new BitTree.Node(bitTree.getRoot()));
        undoHistory.push(head);
        redoHistory.clear();
    }

    private Structure<Node<T>> getCurrentStructure() {
        return undoHistory.isEmpty() ? new Structure<>() : undoHistory.peek();
    }

    @Override
    public PersistentLinkedList<T> add(T element) {
        Structure<Node<T>> currentStructure = getCurrentStructure();
        Structure<Node<T>> newStructure = new Structure<>(currentStructure);

        int newNodeIndex = currentStructure.getSize();
        Node<T> newNode = new Node<>(element, newNodeIndex - 1, -1);

        if (!currentStructure.isEmpty()) {
            Node<T> lastNode = currentStructure.get(newNodeIndex - 1);
            lastNode.next = newNodeIndex;
        }

        newStructure.addList(newNode);

        // Обновляем head и tail в новой структуре
        if (newStructure.isEmpty()) {
            newStructure.head = -1;
            newStructure.tail = -1;
        } else {
            newStructure.head = 0;
            newStructure.tail = newStructure.getSize() - 1;
        }

        // Пушим новую структуру в историю
        undoHistory.push(newStructure);
        redoHistory.clear();

        return this;
    }

    @Override
    public PersistentCollection<T> update(int index, T element) {
        Structure<Node<T>> newStructure = new Structure<>(this.undoHistory.peek());
        Node<T> newNode = new Node<>(newStructure.get(index));

        newNode.setValue(element);

        int trueIndex = this.undoHistory.peek().getHead();
        for (int i = 0; i < index; i++) {
            trueIndex = this.undoHistory.peek().get(trueIndex).getNext();
        }
        newStructure.update(trueIndex, newNode);

        // Пушим новую структуру в историю
        undoHistory.push(newStructure);
        redoHistory.clear();

        return this;
    }

    @Override
    public PersistentLinkedList<T> remove(T element) {
        Structure<Node<T>> currentStructure = getCurrentStructure();
        Structure<Node<T>> newStructure = new Structure<>(currentStructure);

        int newSize = currentStructure.getSize() - 1;
        int currentIndex = -1;

        for (int i = 0; i < currentStructure.getSize(); i++) {
            Node<T> currentNode = currentStructure.get(i);
            if (currentNode.value.equals(element)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex != -1) {
            // Удаляем элемент из списка
            newStructure.removeList(currentIndex);

            // Обновляем ссылки на предыдущий и следующий элементы
            if (currentIndex > 0) {
                Node<T> prevNode = newStructure.get(currentIndex - 1);
                prevNode.next = currentIndex + 1;
            } else if (newSize > 0) {
                // Если удаляемый элемент был первым в списке, обновляем голову
                newStructure.head = currentIndex + 1;
            }

            if (currentIndex < newSize - 1) {
                Node<T> nextNode = newStructure.get(currentIndex + 1);
                nextNode.prev = currentIndex - 1;
            } else if (newSize > 0) {
                // Если удаляемый элемент был последним в списке, обновляем хвост
                newStructure.tail = newSize - 1;
            }

            // Обновляем head и tail в новой структуре
            if (newStructure.isEmpty()) {
                newStructure.head = -1;
                newStructure.tail = -1;
            }

            // Пушим новую структуру в историю
            undoHistory.push(newStructure);
            redoHistory.clear();
        }

        return this;
    }

    @Override
    public T get(Object param) {
        Integer index = (Integer) param;
        Structure<Node<T>> currentStructure = getCurrentStructure();
        if (index < 0 || index >= currentStructure.getSize()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + currentStructure.getSize());
        }

        return currentStructure.get(index).value;
    }

    @Override
    public boolean contains(T element) {
        Structure<Node<T>> currentStructure = getCurrentStructure();
        for (int i = 0; i < currentStructure.getSize(); i++) {
            Node<T> currentNode = currentStructure.get(i);
            if (currentNode.value.equals(element)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        Structure<Node<T>> currentStructure = getCurrentStructure();
        return currentStructure.getSize();
    }

    @Override
    public PersistentLinkedList<T> undo() {
        if (!undoHistory.isEmpty()) {
            redoHistory.push(undoHistory.pop());
        }
        return this;
    }

    @Override
    public PersistentLinkedList<T> redo() {
        if (!redoHistory.isEmpty()) {
            undoHistory.push(redoHistory.pop());
        }
        return this;
    }
}