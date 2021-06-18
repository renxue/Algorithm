package tree;

import java.util.Stack;

public class AVL<T extends Comparable<T>> {
    private Node root;
    private Node hot;//指向当前节点的父节点
    private int count;
    private boolean isLeftChild;//true表示当前节点是父节点的左孩子
    private class Node{
        private T val;
        private Node parent, left, right;
        private int height;
        public Node(T val, Node parent) {
            this.parent = parent;
            this.val = val;
            this.height = 0;
            this.left = null;
            this.right = null;
        }
    }

    public int size() {
        return count;
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
            Node currentNode = null;
            if (cmp < 0) {
                isLeftChild = true;
                currentNode = hot.left;
            } else if (cmp > 0) {
                isLeftChild = false;
                currentNode = hot.right;
            } else {
                return hot;
            }
            if (null == currentNode) 
                return currentNode;
            hot = currentNode;
        }
    }

    private Node tallerChild(Node x) {
        int lHeight = stature(x.left);
        int rHeight = stature(x.right);
        if (lHeight > rHeight) return x.left;
        else if (lHeight < rHeight) return x.right;
        else return isLeftChild(x) ? x.left : x.right;
    }

    public Node insert(T val) {
        Node node = search(val);
        if (node != null) return node;
        Node newNode = new Node(val, hot);
        count++;
        if (root == null) {
            root = newNode;
            return root;
        }
        if (hot != null) {
            if (isLeftChild)
                hot.left = newNode;
            else
                hot.right = newNode;
            //newNode的父亲hot若增高，则其祖父有可能失衡
            Node g = hot;
            while (g != null) {
                if (!avlIsBalance(g)) {
                    boolean isRoot = false;
                    boolean isLC = false;
                    Node gParent = null;
                    if (g.parent == null) isRoot = true;
                    else {
                        //标记当前旋转子树属于父节点的左子树还是右子树
                        if (isLeftChild(g))
                            isLC = true;
                        //记录当前旋转子树的父节点
                        gParent = g.parent;
                    }
                    g = rotateAt(tallerChild(tallerChild(g)));
                    //如果旋转操作触及到了根节点，就要替换根节点
                    if (isRoot) root = g;
                    else {
                        if (isLC) gParent.left = g;
                        else gParent.right = g;
                    }
                    break;
                } else {
                    updateHeight(g);
                }
                g = g.parent;
            }
        }
        return newNode;
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
     *                      g               g               g                g
     *                    ↙︎  ↘︎            ↙︎  ↘︎            ↙︎  ↘︎             ↙︎  ↘︎
     * @param v          p   c3         p    c3          c0   p           c0   p
     * @return         ↙︎  ↘︎           ↙︎  ↘︎                  ↙︎  ↘︎             ↙︎  ↘︎
     *                v    c2       c0    v                c1   v           v   c3
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

    public boolean remove(T val) {
        Node node = search(val);
        if (node == null) return false;
        removeAt(node, hot);
        Node g = hot;
        while (g != null) {
            if (!avlIsBalance(g)) {
                boolean isRoot = false;
                boolean isLC = false;
                Node gParent = null;
                if (g.parent == null) isRoot = true;
                else {
                    //标记当前旋转子树属于父节点的左子树还是右子树
                    if (isLeftChild(g))
                        isLC = true;
                    //记录当前旋转子树的父节点
                    gParent = g.parent;
                }
                g = rotateAt(tallerChild(tallerChild(g)));
                //如果旋转操作触及到了根节点，就要替换根节点
                if (isRoot) root = g;
                else {
                    if (isLC) gParent.left = g;
                    else gParent.right = g;
                }
            }
            updateHeight(g);
            g = g.parent;
        }
        count--;
        return true;
    }

    private void removeAt(Node x, Node hot) {
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
    }

    private boolean isLeftChild(Node x) {
        return x.parent.left == x;
    }

    private boolean isRightChild(Node x) {
        return x.parent.right == x;
    }

    private boolean hasLeftChild(Node x) {
        return (x.left == null) ? false : true;
    }

    private boolean hasRightChild(Node x) {
        return (x.right == null) ? false : true;
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
     * 这里只给出了中序遍历，目的是为了观察输出结果是否满足预期。
     * 因此，前序遍历和后序遍历没有实现。
     */
    public void inOrderTraversal() {
        Stack<Node> stack = new Stack();
        Node nodePosi = root;
        System.out.println("root : " + root.val);
        while (true) {
            if (nodePosi != null) {
                stack.push(nodePosi);
                nodePosi = nodePosi.left;
            } else if (!stack.isEmpty()) {
                StringBuilder print = new StringBuilder();
                Node x = stack.pop();
                print.append("val : ").append(x.val);
                print.append("    height: ").append(x.height);

                print.append("    lc : ");
                if (x.left != null)
                    print.append(x.left.val);
                else print.append("  ");

                print.append("    rc : ");
                if (x.right != null)
                    print.append(x.right.val);
                else print.append("  ");

                print.append("    p : ");
                if (x.parent != null)
                    print.append(x.parent.val);

                nodePosi = x.right;
                System.out.println(print);
            } else break;

        }
    }

    private void updateHeight(Node x) {
        x.height = 1 + Math.max(stature(x.left), stature(x.right));
    }

    private int stature(Node x) {
        return (x != null) ? x.height : -1;
    }

    private void updateHeightAbove(Node x) {
        while(x != null) {
            updateHeight(x);
            x = x.parent;
        }
    }
    /**
     * 理想平衡
     */
    private boolean balanced(Node x) {
        return stature(x.left) == stature(x.right);
    }

    /**
     * 平衡因子 
     */
    private int balanceFactory(Node x) {
        return stature(x.left) - stature(x.right);
    }

    /**
     * avl树的平衡条件
     */
    private boolean avlIsBalance(Node x) {
        return (-2 < balanceFactory(x)) && (balanceFactory(x) < 2);
    }

    public static void main(String args[]) {
        System.out.println("AVL API");
        AVL<Integer> avl = new AVL();
        avl.insert(15);
        avl.insert(13);
        avl.insert(12);
        avl.insert(16);
        avl.insert(17);
        avl.insert(18);
        avl.insert(19);
        avl.insert(20);
        avl.remove(18);
        avl.inOrderTraversal();
    }
}
