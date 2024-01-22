package Help;

import Collections.PersistentArray;
import Collections.PersistentLinkedList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class BitTree<T> {
    /**
     * Узел бинарного битового дерева, содержащий ссылку на список значений и связанный список потомков (таких же узлов).
     *
     * @param <T> Любой тип данных, используемый в коллекции.
     */
    public static class Node<T> {
        private List<T> value;
        private List<Node<T>> child;

        public Node() {
            this.child = null;
            this.value = null;
        }

        public Node(Node<T> other) {
            if (other != null) {
                if (other.child != null) {
                    child = new ArrayList<>();
                    child.addAll(other.child);
                }

                if (other.value != null) {
                    value = new ArrayList<>();
                    value.addAll(other.value);
                }
            }
        }

        public Node(Node<T> other, int endIndex) {
            if (other.child != null) {
                child = new ArrayList<>();
                for (int i = 0; i <= endIndex; i++) {
                    child.add(other.child.get(i));
                }
            }

            if (other.value != null) {
                value = new ArrayList<>();
                for (int i = 0; i <= endIndex; i++) {
                    value.add(other.value.get(i));
                }
            }
        }

        public List<T> getValue() {
            return value;
        }

        public void setValue(List<T> value) {
            this.value = value;
        }

        public List<Node<T>> getChild() {
            return child;
        }

        public void setChild(List<Node<T>> child) {
            this.child = child;
        }
    }

    /**
     * Корень дерева.
     */
    private Node<T> root;

    /**
     * Параметры битового двоичного дерева на основе документации.
     *
     * @see "https://hypirion.com/musings/understanding-persistent-vector-pt-2"
     */
    private int
            bits = 5,
            width = 1 << bits,
            mask = width - 1,
            depth,
            maxSize,
            size;

    public BitTree(int depth, int bits) {
        create(depth, bits);
        this.root = new Node<>();
    }

    public BitTree(int size) {
        create((size - 1) >>> (32 - Integer.numberOfLeadingZeros(size - 1) + this.bits - 1),
                this.bits);
        this.root = new Node<>();
    }

    /**
     * Конструктор копирования.
     *
     * @param toCopy Бинарное битовое дерево, которое будет скопировано в текущее.
     */
    public BitTree(BitTree<T> toCopy) {
        create(toCopy.depth, toCopy.bits);
        this.root = new Node<>(toCopy.root);
        this.size = toCopy.size;
    }

    private void create(int depth, int bits) {
        this.bits = bits;
        this.depth = depth > 0 ? depth : 1;
        // Перерасчёт других параметров, зависящих от глубины и кол-ва бит
        this.width = 1 << this.bits;
        this.mask = this.width - 1;
        this.maxSize = 1 << (this.bits * this.depth);
    }

    public boolean contains(T element) {
        return containsInNode(root, element);
    }

    private boolean containsInNode(Node<T> node, T element) {
        if (node == null) {
            return false;
        }

        List<T> values = node.getValue();
        if (values != null) {
            for (T value : values) {
                if (value.equals(element)) {
                    return true;
                }
            }
        }

        List<Node<T>> children = node.getChild();
        if (children != null) {
            for (Node<T> child : children) {
                if (containsInNode(child, element)) {
                    return true;
                }
            }
        }

        return false;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> node = root;

        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = (index >> level) & mask;
            node = node.getChild().get(widthIndex);
        }

        return node.getValue().get(index & mask);
    }

    public PersistentArray<T> removeArray(T element) {
        if (size == 0) {
            root = new Node<>();
            return new PersistentArray<>(this);
        }

        Node<T> node = root;
        Stack<Node<T>> path = new Stack<>();
        path.push(node);

        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = ((size - 1) >> level) & mask;

            Node<T> childNode = node.getChild().get(widthIndex);
            path.push(childNode);

            node = childNode;
        }

        List<T> values = node.getValue();

        int index = values.indexOf(element);
        if (index != -1) {
            values.remove(index);
        }

        while (!path.isEmpty()) {
            Node<T> currentNode = path.pop();
            if (currentNode.getChild() == null || currentNode.getChild().isEmpty()) {
                if (currentNode.getValue() == null || currentNode.getValue().isEmpty()) {
                    // Если нода не имеет потомков и значений - затираем
                    continue;
                } else {
                    // Если нода имеет значения, но не имеет потомков - останова
                    break;
                }
            } else {
                // Если нода имеет потомков, проверяем если это один потомок
                if (currentNode.getChild().size() == 1) {
                    Node<T> onlyChild = currentNode.getChild().get(0);
                    if (onlyChild.getValue() == null || onlyChild.getValue().isEmpty()) {
                        // Если один потомок не имеет значений, то заменяем текущую ноду этой
                        currentNode.setValue(onlyChild.getValue());
                        currentNode.setChild(onlyChild.getChild());
                    }
                }
            }
        }

        size--;

        return new PersistentArray<>(this);
    }

    public PersistentLinkedList<T> removeList(int index/*T element*/) {
        remove(index);
        return new PersistentLinkedList<>(this);
    }

    private void remove(int index) {
        Node<T> node = root;

        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = (index >> level) & mask;
            int widthIndexNext = (index >> (level - bits)) & mask;

            Node<T> childNode = node.getChild().get(widthIndex);
            Node<T> newNode = new Node<>(childNode, widthIndexNext);
            node.getChild().set(widthIndex, newNode);
            node = newNode;
        }

        node.getValue().remove(index & mask);

        size--;
    }

    public PersistentArray<T> addArray(T element) {
        add(element);
        return new PersistentArray<>(this);
    }

    public PersistentLinkedList<T> addList(T element) {
        add(element);
        return new PersistentLinkedList<>(this);
    }

    public void add(T element) {
        size++;

        if (size > maxSize) {
            calculateDeep();
        }

        Node<T> node = root;

        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = ((size - 1) >> level) & mask;
            Node<T> newNode;

            if (node.getChild() == null) {
                node.setChild(new LinkedList<>());
                newNode = new Node<>();
                node.getChild().add(newNode);
            } else {
                if (widthIndex == node.getChild().size()) {
                    newNode = new Node<>();
                    node.getChild().add(newNode);
                } else {
                    Node<T> childNode = node.getChild().get(widthIndex);
                    newNode = new Node<>(childNode);
                    node.getChild().set(widthIndex, newNode);
                }
            }

            node = newNode;
        }

        if (node.getValue() == null) {
            node.setValue(new LinkedList<>());
        }

        node.getValue().add(element);
    }

    private void calculateDeep() {
        Node<T> newNode = new Node<>();
        if (root.getValue() == null) {
            newNode.setChild(root.getChild());
        } else {
            newNode.setValue(root.getValue());
            root.setValue(null);
        }
        root.setChild(new LinkedList<>());
        root.getChild().add(newNode);
        depth++;

        // Перерасчёт других параметров, зависящих от глубины и кол-ва бит
        this.width = 1 << this.bits;
        this.mask = this.width - 1;
        this.maxSize = 1 << (this.bits * this.depth);
    }

    public void update(int index, T element) {
        Node<T> node = root;
        for (int level = bits * (depth - 1); level > 0; level -= bits) {
            int widthIndex = (index >> level) & mask;
            Node<T> childNode = node.getChild().get(widthIndex);
            Node<T> newNode = new Node<>(childNode);
            node.getChild().set(widthIndex, newNode);
            node = newNode;
        }
        node.getValue().set(index & mask, element);
    }

    public Node<T> getRoot() {
        return root;
    }

    public void setRoot(Node<T> root) {
        this.root = root;
    }

    public int getBits() {
        return bits;
    }

    public void setBits(int bits) {
        this.bits = bits;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}