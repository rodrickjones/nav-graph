package com.rodrickjones.navgraph.util;

import java.util.*;

/**
 * High performance contains checks with linked elements.
 * @param <T>
 */
public class Frontier<T> implements Queue<T>, Set<T> {
    private final Queue<T> queue;
    private final HashSet<T> set;

    public Frontier(Comparator<T> comparator) {
        queue = new PriorityQueue<>(comparator);
        set = new HashSet<>();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return queue.toArray(a);
    }

    @Override
    public boolean add(T t) {
        set.add(t);
        return queue.add(t);
    }

    @Override
    public boolean remove(Object o) {
        set.remove(o);
        return queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        set.addAll(c);
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        set.removeAll(c);
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        set.retainAll(c);
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        set.clear();
        queue.clear();
    }

    @Override
    public boolean offer(T t) {
        set.add(t);
        return queue.offer(t);
    }

    @Override
    public T remove() {
        T res = queue.remove();
        set.remove(res);
        return res;
    }

    @Override
    public T poll() {
        T res = queue.poll();
        set.remove(res);
        return res;
    }

    @Override
    public T element() {
        return queue.element();
    }

    @Override
    public T peek() {
        return queue.peek();
    }
}
