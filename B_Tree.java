package tree;

import array.Vector;


/**
 * B树插入，删除，查询。
 * 根节点常驻内存，其他节点用到时再从外存加载到内存。
 * 因为内存访问速度比外存快，最少也要快10的5次方倍，为了平衡两种存储设置之间的差异，
 * 达到系统的最优访问，所以每一次外存读取都是以页为单位读取。
 * 机械磁盘的最小读写单位是扇区，一般大小为 512 字节。
 * 而固态磁盘的最小读写单位是页，通常大小是 4KB、8KB 等。
 * @param <T>
 */
public class B_Tree<T extends Comparable<T>> {
    private int size = 0;
    private int order;
    private Node<T> root = null;
    private Node<T> hot = null;

    public B_Tree() {
        this.root = new Node();
        this.order = 3;
    }

    public Node search(T e) {
        Node v = root; hot = null;
        while (v != null) {
            int index = v.key.search(e);
            if ( (0 <= index) && (compare(e, (T) v.key.get(index)) == 0) ) {
                return v;
            }
            hot = v;
            v = (Node) v.child.get(index + 1);
        }
        return null;
    }

    private int compare(T a, T b) {
        return a.compareTo(b);
    }

    public Node insert(T e) {
        Node v = search(e);
        if ( v != null) return v;
        int index = hot.key.search(e);
        hot.key.insert(index+1, e);
        hot.child.insert(index + 2, null);
        size++;
        //解决上溢缺陷
        solveOverflow(hot);
        return root;
    }

    public boolean remove(T e){
        Node v = search(e);
        if (v == null) return false;
        int r = v.key.search(e);
        if (v.child.get(0) != null) {
            Node<T> u = (Node<T>) v.child.get(r + 1);
            while (u != null) u = u.child.get(0);//找出e的后继
            v.key.insert(r, u.key.get(0));//交换v和u
            v = u; r = 0;
        }
        v.key.remove(r);
        v.child.remove(r + 1);
        size--;
        solveUnderflow(v);//通过旋转和合并处理下溢问题
        return true;
    }

    /**
     * 通过合并解决下溢
     * @param v
     */
    private void solveUnderflow(Node v) {
        if ( (order + 1) / 2 <= v.child.getSize() ) return;//节点未发生下溢
        Node p = v.parent;
        if ( p == null ) {
            Node CNode = (Node) v.child.get(0);
            if ( (v.key.getSize() == 0) && (CNode != null) ) {
                root = CNode;
                root.parent = null;
            }
            CNode = null;
            return;
        }
        //确定v是p的第几个孩子
        int r = 0; while (p.child.get(r) != v) r++;
        //1, 右旋
        if ( r > 0 ) {
            Node ls = (Node) p.child.get(r-1);
            if ( (order + 1) / 2 < ls.child.getSize() ) {
                v.key.insert(0, p.key.remove(r-1));
                p.key.insert(r -1, ls.key.remove(ls.key.getSize() - 1));
                v.child.insert(0, ls.child.remove(ls.child.getSize() - 1));
                if ( v.child.get(0) != null )
                    ((Node)v.child.get(0)).parent = v;
            }
        }
        //2, 左旋
        if (p.child.getSize() - 1 > r) {
            Node rs = (Node)p.child.get(r + 1);
            if ( (order + 2) / 2 < rs.child.getSize() ) {
                v.key.insert(v.key.getSize(), p.key.get(r));
                p.key.insert(r, rs.key.remove(0));
                v.child.insert(v.child.getSize(), rs.child.remove(0));
                if (v.child.get(v.child.getSize()-1) != null)
                    ((Node)v.child.get(v.child.getSize())).parent = v;
            }
        }
        //3, 与左兄弟合并
        if ( r > 0 ) {
            Node ls = (Node) p.child.get(r-1);
            ls.key.insert(ls.key.getSize(), p.key.remove(r - 1));
            p.child.remove(r);
            ls.child.insert(ls.child.getSize(), v.child.remove(0));
            if ( ls.child.get(ls.child.getSize()) != null )
                ((Node)ls.child.get(ls.child.getSize())).parent = ls;
            if ( v.key.getSize() > 0 ) {
                ls.key.insert(ls.key.getSize(), v.key.remove(0));
                ls.child.insert(ls.child.getSize(), v.child.remove(0));
                if (ls.child.get(ls.child.getSize() -1) != null)
                    ((Node)ls.child.get(ls.child.getSize() - 1)).parent = v;
            }
        }
        //4, 与右兄弟合并
        else {
            Node rs = (Node) p.child.get(r + 1);
            rs.key.insert(0, p.key.remove(r));
            p.child.remove(r);
            rs.child.insert(0, v.child.remove(v.child.getSize()-1));
            if (rs.child.get(0) != null) ((Node)rs.child.get(0)).parent = rs;
            while ( v.key.getSize() > 0 ) {
                rs.key.insert(0, v.key.remove(v.key.getSize()-1));
                rs.child.insert(0, v.child.remove(v.child.getSize()-1));
                if ( rs.child.get(0) != null ) ((Node)rs.child.get(0)).parent = rs;
            }
        }
        solveUnderflow(p);
    }

    //通过分裂解决上溢
    private void solveOverflow(Node v) {
        if ( order >= v.child.getSize()) return;//当前节点未溢出
        int s = order >> 1;
        Node rightNode = new Node();
        for ( int j = 0; j < order-s-1; j++ ) {
            rightNode.child.remove(j);
            rightNode.child.insert(j, v.child.remove(s+1));
            rightNode.key.insert(j, v.key.remove(s+1));
        }
        rightNode.child.insert(order-s-1, v.child.remove(s+1));
        if (rightNode.child.get(0) != null) {
            for (int j = 0; j < order-s; j++) {
                ((Node)rightNode.child.get(j)).parent = rightNode;
            }
        }
        Node p = v.parent;
        if ( p == null ) {
            p = new Node<T>();
            root = p;
            p.child.remove(0);
            p.child.insert(0, v);
            v.parent = p;
        }
        int r = 1 + p.key.search(v.key.get(0));
        p.key.insert(r, v.key.remove(s));
        p.child.insert(r+1, rightNode);
        rightNode.parent = p;
        solveOverflow(p);
    }

    private class Node<T extends Comparable<T>> implements Comparable<Node<T>> {
        Node parent = null;
        Vector<T> key = null;
        Vector<Node<T>> child = null;
        {key = new Vector<>();
         child = new Vector<>();}

        /**
         * 构建一个空节点
         */
        public Node() {
            this.parent = null;
            child.insert(0, null);
        }

        /**
         * 创建一个节点
         * @param e
         * @param lc
         * @param rc
         */
        public Node(T e, Node lc, Node rc) {
            key.insert(0, e);
            child.insert(0, lc);
            child.insert(1, rc);
            if ( lc != null ) lc.parent = this;
            if ( rc != null ) rc.parent = this;
        }

        @Override
        public int compareTo(Node<T> o) {
            return 0;
        }
    }

    public static void main(String args[]) {
        System.out.println("B-Tree API");
        B_Tree<Integer> btree = new B_Tree();
        btree.insert(1);
        btree.insert(2);
        btree.insert(3);
        btree.remove(3);
    }
}
