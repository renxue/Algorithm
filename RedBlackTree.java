package tree;

import java.util.Stack;

public class RedBlackTree<T extends Comparable<T>> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;
    private Node root;
    private Node hot;
    private int size;
    private class Node{
        private T val;
        private Node parent, left, right;
        private int height;
        private boolean color;//
        public Node(T val, Node parent, boolean color) {
            this.parent = parent;
            this.val = val;
            this.height = 0;
            this.color = color;
        }
    }

    public int size() {
        return size;
    }

    public Node search(T val) {
        return search(root, val);
    }

    private Node search(Node root, T val) {
        if (root == null || val == root.val) {
            hot = null;
            return root;
        }
        for (hot = root;;) {
            int cmp = val.compareTo(hot.val);
            Node currentNode = (cmp < 0) ? hot.left : hot.right;
            if (null == currentNode || val == currentNode.val)
                return currentNode;
            hot = currentNode;
        }
    }

    public Node insert(T val) {
        Node x = search(val);
        if (x != null) return x;
        x = new Node(val, hot, RED);
        if (root == null) {
            x.color = BLACK;
            root = x;
            return root;
        }
        if (hot != null) {
            int cmp = val.compareTo(hot.val);
            if (cmp < 0)
                hot.left = x;
            else
                hot.right = x;
        }
        size++;
        Node oldNode = x;
        solveDoubleRed(x);
        return oldNode;
    }

    private void solveDoubleRed(Node x) {
        if (x.parent == null) {
            x.color = BLACK; x.height += 1; return;
        }
        Node p = x.parent;
        if ( isBlack(p) ) return;
        Node g = p.parent;
        Node u = uncle(x);
        if ( isBlack(u) ) {//叔父节点是黑的情况
            if (isLeftChild(x) && isLeftChild(p))
                p.color = BLACK;
            else
                x.color = BLACK;
            Node gg = g.parent;
            Node r = rotateAt(x);
            r.parent = gg;
        } else {//叔父节点是红的情况
            g.color = BLACK; g.height++;
            u.color = BLACK; u.height++;
            if ( g.parent != null ) g.color = RED;
            solveDoubleRed(g);
        }
    }

    /**                           b
     * （3+4) - 重构            ↙    ↘︎
     *                       a       c
     *                     ↙︎  ↘︎     ↙︎ ↘︎
     *                    t0  t1  t2   t3
     */
    private Node connect34(Node a, Node b, Node c,
                               Node t0, Node t1, Node t2, Node t3 ) {
        a.left  = t0; if (t0 != null) t0.parent = a;
        a.right = t1; if (t1 != null) t1.parent = a;
        c.left  = t2; if (t2 != null) t2.parent = c;
        c.right = t3; if (t3 != null) t3.parent = c;
        b.left  = a; a.parent = b;
        b.right = c; c.parent = b;
        updateHeight(a); updateHeight(c); updateHeight(b);
        return b;
    }

    /**
     * 需要（3+4）旋转重构的场景包括以下四种：
     *                    场景一            场景二           场景三            场景四
     *                      G               G               G                G
     *                    ↙︎  ↘︎            ↙︎  ↘︎            ↙︎  ↘︎             ↙︎  ↘︎
     * @param v          P   c3         P    c3          c0   P           c0   P
     * @return         ↙︎  ↘︎           ↙︎  ↘︎                  ↙︎  ↘︎             ↙︎  ↘︎
     *                V    c2       c0    V                c1   V           V   c3
     *              ↙︎ ↘︎                 ↙︎  ↘︎                  ↙︎  ↘︎        ↙︎  ↘︎
     *            c0  c1              c1   c2               c2    c3    c1   c2
     */
    private Node rotateAt(Node v) {
        Node p = v.parent, g = p.parent;
        if (isLeftChild(p)) {
            if (isLeftChild(v)) {
                p.parent = g.parent;
                return connect34(v, p, g, v.left, v.right, p.right, g.right);
            } else {
                v.parent = g.parent;
                return connect34(p, v, g, p.left, v.left, v.right, g.right);
            }
        } else {
            if (isRightChild(v)) {
                p.parent = g.parent;
                return connect34(g, p, v, g.left, p.left, v.left, v.right);
            } else {
                v.parent = g.parent;
                return connect34(g, v, p, g.left, v.left, v.right, p.right);
            }
        }
    }

    /**
     * 用B树思想理解红黑树的删除，逻辑就会很清晰。红黑树就是（2， 4）B树。
     * @param val
     * @return
     */
    public boolean remove(T val) {
        Node node = search(val);
        if (node == null) return false;
        //r 为被删除的节点的替代则。
        Node r = removeAt(node, hot);
        size--;
        //删除后树为空 结束
        if ( size <= 0 )
            return true;

        //被删除的节点是根，则重新设置新根的颜色和更新树高。
        if ( hot == null ) {
            root.color = BLACK;
            updateHeight(root);
            return true;
        }
        //删除完成后,被删节点的父节点hot依然平衡，则整棵树的黑高度依然没有改变，则不调整。
        if ( blackHeightUpdated(hot) ) return true;
        if ( isRed(r) ) {
            r.color = BLACK;
            r.height++;
            return true;
        }
        solveDoubleBlack(r);
        return true;
    }

    private void solveDoubleBlack(Node r) {
        Node p = ( r != null ) ? r.parent : hot;//获取r的父亲
        if ( p == null ) return;
        Node s = (r == p.left) ? r.right : r.left;
        if ( isBlack(s) ) { //兄弟s为黑
            Node t = null;
            if ( isRed(s.right) ) t = s.right;
            if ( isRed(s.left) ) t = s.left;
            if ( t != null ) {//兄弟s有红孩子
                boolean oldColor = p.color;
                Node b = rotateAt(t);
                if ( hasLeftChild(b) ) b.left.color = BLACK; updateHeight(b.left);
                if ( hasRightChild(b) ) b.right.color = BLACK; updateHeight(b.right);
                b.color = oldColor; updateHeight(b);
            } else { //黑s无红孩子
                s.color = RED; s.height--;
                if ( isRed(p) ) p.color = BLACK;
                else {
                    p.height--;
                    solveDoubleBlack(p);
                }
            }
        } else { //兄弟s为红
            s.color = BLACK; p.color = RED;
            Node t = isLeftChild(s) ? s.left : s.right;
            hot = p;
            rotateAt(t);
            solveDoubleBlack(r);
        }
    }

    private Node uncle(Node x) {
        Node p = x.parent;
        if (isLeftChild(p)) return p.parent.right;
        return p.parent.left;
    }

    private Node removeAt(Node x, Node hot) {
        Node w = x; //实际被删除的节点
        Node succ = null; //实际被删除节点的接替者
        if (!hasLeftChild(x)) {
            succ = x.right;
        } else if (!hasRightChild(x)) {
            succ = x.left;
        } else {
            w = succ(x);
            Node tmp = x;
            x.val = w.val;
            w.val = tmp.val;
            Node u = w.parent;
            if (u == x) succ = w.right;
            else succ = w.right;
        }
        hot = w.parent;
        if (succ != null) succ.parent = hot;
        if (hot.left == w) hot.left = succ;
        else hot.right = succ;
        return succ;
    }

    private boolean hasLeftChild(Node x) {
        if (x.left == null) return false;
        return true;
    }

    private boolean hasRightChild(Node x) {
        if (x.right == null) return false;
        return true;
    }

    private boolean isLeftChild(Node x) {
        return x.parent.left == x;
    }

    private boolean isRightChild(Node x) {
        return x.parent.right == x;
    }

    private boolean isRed(Node x) {
        return x.color;
    }
    private boolean isBlack(Node x) {
        return x.color;
    }

    /**
     * 当前节点的直接后继节点
     */
    private Node succ(Node x) {
        Node s = null;
        if (hasRightChild(x)) {
            s = x.right;
            while (s.left != null) s = s.left;
        } else {
            s = x;
            while (s.parent.right == s) s = s.parent;
            s = s.parent;
        }
        return s;
    }

    /**
     * 中序遍历
     */
    public void inOrderTraversal() {
        Stack<RedBlackTree.Node> stack = new Stack();
        Node nodePosi = root;
        System.out.println(root.left.val);
        while (true) {
            if (nodePosi != null) {
                stack.push(nodePosi);
                nodePosi = nodePosi.left;
            } else if (!stack.isEmpty()) {
                Node x = stack.pop();
                System.out.print(x.val + "   ");
                nodePosi = x.right;
            } else break;
        }
        System.out.println();
    }

    private void updateHeight(Node x) {
        x.height = Math.max(stature(x.left), stature(x.right));
        x.height = isBlack(x) ? x.height++ : x.height;
    }

    private int stature(Node x) {
        return (x != null) ? x.height : 0;
    }

    private boolean blackHeightUpdated(Node x) {
        return stature(x.left) == stature(x.right) &&
        ((x).height == (isRed(x) ? stature(x.left) : stature(x.left) + 1));
    }


    public static void main(String args[]) {
        System.out.println("Red Black Tree API");
        RedBlackTree<Integer> bst = new RedBlackTree();
        bst.insert(15);
        bst.insert(9);
        bst.insert(10);
        bst.insert(13);
        bst.insert(18);
        bst.insert(17);
        bst.insert(20);
        bst.insert(19);
        bst.insert(50);
        //bst.inOrderTraversal();
        bst.remove(18);
        //bst.remove(17);
        //bst.inOrderTraversal();
        System.out.println(bst.search(20).parent.val);
        System.out.println(bst.search(20).left);
        System.out.println(bst.search(20).right.val);
    }

}
