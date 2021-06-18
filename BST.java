package tree;

import java.util.Stack;

public class BST<T extends Comparable<T>> {
    private Node root;
    private Node hot;
    private int count;
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
            Node currentNode = (cmp < 0) ? hot.left : hot.right;
            if (null == currentNode || val == currentNode.val) 
                return currentNode;
            hot = currentNode;
        }
    }

    public Node insert(T val) {
        Node node = search(val);
        if (node != null) return node;
        node = new Node(val, hot);
        if (root == null) root = node;
        if (hot != null) {
            int cmp = val.compareTo(hot.val);
            if (cmp < 0)
                hot.left = node;
            else
                hot.right = node;
        }
        count++;
        //Update the tree height
        updateHeightAbove(node);
        return node;
    }

    public boolean remove(T val) {
        Node node = search(val);
        if (node == null) return false;
        removeAt(node, hot);
        count--;
        updateHeightAbove(node);
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

    private boolean hasLeftChild(Node x) {
        if (x.left == null) return false;
        return true;
    }

    private boolean hasRightChild(Node x) {
        if (x.right == null) return false;
        return true;
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
        Stack<BST.Node> stack = new Stack();
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
    public static void main(String args[]) {
        System.out.println("BST API");
        BST<Integer> bst = new BST();
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
