package main;

public interface Func<T> {
    public void call(QuadTree<T> quadTree, Node<T> node);
}
