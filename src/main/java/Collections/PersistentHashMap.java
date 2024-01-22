package Collections;
import API.PersistentCollection;

import java.util.*;
import java.util.Map.Entry;

public class PersistentHashMap<K, V> implements PersistentCollection<Entry<K, V>> {
    private static class UndoRedoAction<K, V> {
        private final int index;
        private final Entry<K, V> entry;

        public UndoRedoAction(int index, Entry<K, V> entry) {
            this.index = index;
            this.entry = entry;
        }

        public int getIndex() {
            return index;
        }

        public Entry<K, V> getEntry() {
            return entry;
        }
    }
    private final PersistentArray<PersistentLinkedList<Entry<K, V>>> hashMap = new PersistentArray<>();
    private final Stack<UndoRedoAction<K, V>> undoHistory = new Stack<>();
    private final Stack<UndoRedoAction<K, V>> redoHistory = new Stack<>();


    public PersistentHashMap() {
        for (int i = 0; i < PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE; i++) {
            hashMap.add(new PersistentLinkedList<>());
        }
    }

    public PersistentHashMap<K, V> add(K key, V value) {
        return add(new AbstractMap.SimpleEntry<>(key, value));
    }

    @Override
    public PersistentHashMap<K, V> add(Entry<K, V> element) {
        int index = hashcodeIndex(Objects.hashCode(element.getKey()));
        for (int i = 0; i < hashMap.get(index).size(); i++) {
            Entry<K, V> entry = hashMap.get(index).get(i);
            if (entry.getKey().equals(element.getKey())) {
                hashMap.get(index).update(i, element);
                undoHistory.push(new UndoRedoAction<>(index, entry));
                redoHistory.clear();
                return this;
            }
        }
        hashMap.get(index).add(element);
        undoHistory.push(new UndoRedoAction<>(index, null));
        redoHistory.clear();
        return this;
    }

    @Override
    public PersistentCollection<Entry<K, V>> update(int index, Entry<K, V> element) {
        throw new UnsupportedOperationException("Method 'update' is not supported for PersistentHashMap");
    }

    public PersistentHashMap<K, V> delete(K key) {
        return remove(new AbstractMap.SimpleEntry<>(key, null));
    }

    @Override
    public PersistentHashMap<K, V> remove(Entry<K, V> element) {
        int index = hashcodeIndex(Objects.hashCode(element.getKey()));
        for (int i = 0; i < hashMap.get(index).size(); i++) {
            Entry<K, V> entry = hashMap.get(index).get(i);
            if (entry.getKey().equals(element.getKey())) {
                hashMap.get(index).remove(entry);
                undoHistory.push(new UndoRedoAction<>(index, entry));
                redoHistory.clear();
                return this;
            }
        }
        return null;
    }

    @Override
    public Entry<K, V> get(Object key) {
        int index = hashcodeIndex(Objects.hashCode(key));
        PersistentLinkedList<Entry<K, V>> get = hashMap.get(index);
        int size = get.size();
        for (int i = 0; i < size; i++) {
            Entry<K, V> entry = get.get(i);
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    private int hashcodeIndex(int hashCode) {
        return Math.abs(hashCode) % hashMap.size();
    }

    public boolean contains(K key, V value) {
        return contains(new AbstractMap.SimpleEntry<>(key, value));
    }

    @Override
    public boolean contains(Entry<K, V> element) {
        int hashCode = Objects.hashCode(element.getKey());
        int index = Math.abs(hashCode) % hashMap.size();
        return hashMap.get(index).contains(element);
    }

    @Override
    public int size() {
        int size = 0;
        for (int i = 0; i < hashMap.size(); i++) {
            size += hashMap.get(i).size();
        }
        return size;
    }

    @Override
    public PersistentHashMap<K, V> undo() {
        if (!undoHistory.isEmpty()) {
            UndoRedoAction<K, V> action = undoHistory.pop();
            int index = action.getIndex();
            Entry<K, V> entry = action.getEntry();

            if (entry != null) {
                hashMap.get(index).remove(entry);
            } else if (index == PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE) {
                for (int i = 0; i < PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE; i++) {
                    undoHistory.pop();
                    redoHistory.push(new UndoRedoAction<>(PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE, null));
                }
            } else if (index > PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE) {
                int count = index - PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE;
                for (int i = 0; i < count; i++) {
                    hashMap.get(undoHistory.peek().getIndex()).undo();
                    redoHistory.push(undoHistory.pop());
                }
                redoHistory.push(new UndoRedoAction<>(index, null));
            } else {
                hashMap.get(index).undo();
                redoHistory.push(new UndoRedoAction<>(index, null));
            }
        }

        return this;
    }

    @Override
    public PersistentHashMap<K, V> redo() {
        if (!redoHistory.isEmpty()) {
            UndoRedoAction<K, V> action = redoHistory.pop();
            int index = action.getIndex();
            Entry<K, V> entry = action.getEntry();
            if (entry != null) {
                hashMap.get(index).add(entry);
            } else if (index == PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE) {
                for (int i = 0; i < PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE; i++) {
                    redoHistory.pop();
                    undoHistory.push(new UndoRedoAction<>(PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE, null));
                }
            } else if (index > PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE) {
                int count = index - PersistentArray.INITIAL_PERSISTENT_ARRAY_SIZE;
                for (int i = 0; i < count; i++) {
                    hashMap.get(redoHistory.peek().getIndex()).redo();
                    undoHistory.push(redoHistory.pop());
                }
                undoHistory.push(new UndoRedoAction<>(index, null));
            } else {
                hashMap.get(index).redo();
                undoHistory.push(new UndoRedoAction<>(index, null));
            }
        }

        return this;
    }
}