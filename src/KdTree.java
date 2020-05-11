import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.StdOut;

import java.util.ArrayList;
import java.util.List;

public class KdTree {
    private enum Division {
        HORIZONTAL,
        VERTICAL;
    }

    private class Node {
        Point2D point2D;
        Node left;
        Node right;
        Division division;
    }

    private Node root;
    private int n;

    public KdTree()                               // construct an empty set of points
    {
        n = 0;

    }

    public boolean isEmpty()                      // is the set empty?
    {
        return n == 0;
    }

    public int size()                         // number of points in the set
    {
        return n;
    }

    public void insert(Point2D p)              // add the point to the set (if it is not already in the set)
    {
        if (p == null)
            throw new IllegalArgumentException();
        root = insert(p, root, Division.VERTICAL);
    }

    private Node insert(Point2D p, Node current, Division division) {
        if (current == null) {
            Node node = new Node();
            node.point2D = p;
            node.left = null;
            node.right = null;
            node.division = division;
            n++;
            return node;
        }
        if (current.point2D.equals(p)) {
            return current;
        }
        if (division == Division.HORIZONTAL) {
            if (p.y() <= current.point2D.y()) {
                current.left = insert(p, current.left, Division.VERTICAL);
            } else {
                current.right = insert(p, current.right, Division.VERTICAL);
            }
        } else {
            if (p.x() <= current.point2D.x()) {
                current.left = insert(p, current.left, Division.HORIZONTAL);
            } else {
                current.right = insert(p, current.right, Division.HORIZONTAL);
            }
        }
        return current;
    }

    public boolean contains(Point2D p)            // does the set contain point p?
    {
        if (p == null)
            throw new IllegalArgumentException();
        return contains(p, root);
    }

    private boolean contains(Point2D p, Node current) {
        if (current == null)
            return false;
        if (current.point2D.equals(p))
            return true;
        else {
            if (current.division == Division.VERTICAL) {
                if (p.x() <= current.point2D.x()) {
                    return contains(p, current.left);
                } else {
                    return contains(p, current.right);
                }
            } else {
                if (p.y() <= current.point2D.y())
                    return contains(p, current.left);
                else
                    return contains(p, current.right);
            }
        }
    }

    public void draw()                         // draw all points to standard draw
    {
        draw(root);
    }

    private void draw(Node current) {
        if (current == null)
            return;
        draw(current.left);
        draw(current.right);
        current.point2D.draw();
    }

    public Iterable<Point2D> range(RectHV rect)             // all points that are inside the rectangle (or on the boundary)
    {
        List<Point2D> list = new ArrayList<>();
        range(root, rect, list);
        return list;
    }

    private void range(Node current, RectHV rectHV, List<Point2D> contained) {
        if (current == null)
            return;
        if (current.division == Division.VERTICAL) {
            if (current.point2D.x() > rectHV.xmax()) {
                range(current.left, rectHV, contained);
            } else if (current.point2D.x() < rectHV.xmin()) {
                range(current.right, rectHV, contained);
            } else {
                range(current.left, rectHV, contained);
                range(current.right, rectHV, contained);
            }
        } else {
            if (current.point2D.y() > rectHV.ymax()) {
                range(current.left, rectHV, contained);
            } else if (current.point2D.y() < rectHV.ymin()) {
                range(current.right, rectHV, contained);
            } else {
                range(current.left, rectHV, contained);
                range(current.right, rectHV, contained);
            }
        }
        if (current.point2D.x() >= rectHV.xmin() && current.point2D.x() <= rectHV.xmax() && current.point2D.y() >= rectHV.ymin() && current.point2D.y() <= rectHV.ymax()) {
            contained.add(current.point2D);
        }
    }

    public Point2D nearest(Point2D p)             // a nearest neighbor in the set to point p; null if the set is empty
    {
        if (p == null)
            throw new IllegalArgumentException();
        return nearest(root, p, Double.POSITIVE_INFINITY);
    }

    private Point2D nearest(Node current, Point2D p, double minSoFar) {
        if (current == null)
            return null;
        Point2D result = null;
        if (current.point2D.distanceSquaredTo(p) < minSoFar) {
            minSoFar = current.point2D.distanceSquaredTo(p);
            result = current.point2D;
        }

        if (current.division == Division.VERTICAL) {
            Point2D tempResult = null;
            if (p.x() <= current.point2D.x()) {
                tempResult = nearest(current.left, p, minSoFar);
            }
            if (tempResult == null) {
                tempResult = nearest(current.right, p, minSoFar);
            }
            if (tempResult != null) {
                result = tempResult;
            }
        } else {
            Point2D tempResult = null;
            if (p.y() <= current.point2D.y()) {
                tempResult = nearest(current.left, p, minSoFar);
            }
            if (tempResult == null) {
                tempResult = nearest(current.right, p, minSoFar);
            }
            if (tempResult != null) {
                result = tempResult;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        KdTree tree = new KdTree();
        tree.insert(new Point2D(0.4, 0.2));
        tree.insert(new Point2D(0.2, 0.1));
        tree.insert(new Point2D(0.7, 0.3));
        tree.insert(new Point2D(0.3, 0.6));

        StdOut.println("Find existing point: " + tree.contains(new Point2D(0.7, 0.3)));
        StdOut.println("Find non-existant p: " + !tree.contains(new Point2D(0.2, 0.6)));
        StdOut.println("Count of nodes = 4 : " + tree.size());

        tree.insert(new Point2D(0.3, 0.6));

        StdOut.println("Cannot insert same : " + tree.size());

        StdDraw.setPenRadius(0.01);
        tree.draw();

        RectHV rect = new RectHV(0.3, 0.1, 0.9, 0.9);
        StdDraw.setPenColor(StdDraw.BLUE);
        StdDraw.setPenRadius(0.002);
        rect.draw();

        for (Point2D point : tree.range(rect)) {
            StdOut.println(point.toString());
        }

        StdOut.println();

        StdDraw.setPenColor(StdDraw.RED);
        StdDraw.setPenRadius(0.01);
        Point2D comp = new Point2D(0.4, 0.5);
        comp.draw();

        StdOut.println(tree.nearest(comp).toString());
    }
}
