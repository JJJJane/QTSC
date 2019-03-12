package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Datastructure: A point Quad Tree for representing 2D data. Each
 * region has the same ratio as the bounds for the tree.
 * <p/>
 * The implementation currently requires pre-determined bounds for data as it
 * can not rebalance itself to that degree.
 */
public class QuadTree<T> {
    private Node<T> root_;
    private int count_ = 0;

    /**
     * Constructs a new quad tree.
     *
     * @param {double} minX Minimum x-value that can be held in tree.
     * @param {double} minY Minimum y-value that can be held in tree.
     * @param {double} maxX Maximum x-value that can be held in tree.
     * @param {double} maxY Maximum y-value that can be held in tree.
     */
    public QuadTree(double ep, double minX, double minY, double w, double h) {
        this.root_ = new Node<T>(ep, minX, minY, w, h, null);
    }

    /**
     * Returns a reference to the tree's root node.  Callers shouldn't modify nodes,
     * directly.  This is a convenience for visualization and debugging purposes.
     *
     * @return {Node} The root node.
     */
    public Node<T> getRootNode() {
        return this.root_;
    }

    /**
     * Sets the value of an (x, y) point within the quad-tree.
     *
     * @param {double} x The x-coordinate.
     * @param {double} y The y-coordinate.
     * @param {T} value The value associated with the point.
     */
    public void set(double x, double y, double value) {

        Node<T> root = this.root_;
        if (x < root.getX() || y < root.getY() || x > root.getX() + root.getW() || y > root.getY() + root.getH()) {
            throw new QuadTreeException("Out of bounds : (" + x + ", " + y + ")");
        }
        if (this.insert(root, new Point<T>(x, y, value))) {
            this.count_++;
        }
    }

    /**
     * Gets the value of the point at (x, y) or null if the point is empty.
     *
     * @param {double} x The x-coordinate.
     * @param {double} y The y-coordinate.
     * @param {T} opt_default The default value to return if the node doesn't
     *                 exist.
     * @return {*} The value of the node, the default value if the node
     *         doesn't exist, or undefined if the node doesn't exist and no default
     *         has been provided.
     */
    public double get(double x, double y, double opt_default) {
        Node<T> node = this.find(this.root_, x, y);
        return node != null ? node.getPoint()[0].getValue() : opt_default;
    }

    /**
     * Removes a point from (x, y) if it exists.
     *
     * @param {double} x The x-coordinate.
     * @param {double} y The y-coordinate.
     * @return {T} The value of the node that was removed, or null if the
     *         node doesn't exist.
     */
    public String remove(double x, double y) {
        Node<T> node = this.find(this.root_, x, y);
        if (node != null) {
            node.deletePoint(x,y);
            this.balance(node);
            this.count_--;
            return "removed";
        } else {
            return null;
        }
    }

    /**
     * Returns true if the point at (x, y) exists in the tree.
     *
     * @param {double} x The x-coordinate.
     * @param {double} y The y-coordinate.
     * @return {boolean} Whether the tree contains a point at (x, y).
     */
//    public boolean contains(double x, double y) {
//        return this.get(x, y, null) != null;
//    }

    /**
     * @return {boolean} Whether the tree is empty.
     */
    public boolean isEmpty() {
        return this.root_.getNodeType() == NodeType.EMPTY;
    }

    /**
     * @return {number} The number of items in the tree.
     */
    public int getCount() {
        return this.count_;
    }

    /**
     * Removes all items from the tree.
     */
    public void clear() {
        this.root_.setNw(null);
        this.root_.setNe(null);
        this.root_.setSw(null);
        this.root_.setSe(null);
        this.root_.setNodeType(NodeType.EMPTY);
        this.root_.setPoint(null);
        this.count_ = 0;
    }

    /**
     * Returns an array containing the coordinates of each point stored in the tree.
     * @return {Array.<Point>} Array of coordinates.
     */
    public Point<T>[] getKeys() {
        final List<Point<T>> arr = new ArrayList<Point<T>>();
        this.traverse(this.root_, new Func<T>() {
            @Override
            public void call(QuadTree<T> quadTree, Node<T> node) {
            	for (int i=0 ; i<node.getPoint().length;i++){
            		arr.add(node.getPoint()[i]);
            	}
            }
        });
        return arr.toArray((Point<T>[]) new Point[arr.size()]);
    }

    /**
     * Returns a list containing all values stored within the tree.
     * @return {List<T>} The values stored within the tree.
     */
//    public List<T> getValues() {
//        final List<T> arr = new ArrayList<T>();
//        this.traverse(this.root_, new Func<T>() {
//            @Override
//            public void call(QuadTree<T> quadTree, Node<T> node) {
//            	for (int i=0 ; i<node.getPoint().length;i++){
//	                arr.add(node.getPoint()[i].getValue());
//	            }
//            }
//        });
//
//        return arr;
//    }

    public Point<T>[] searchIntersect(final double xmin, final double ymin, final double xmax, final double ymax) {
        final List<Point<T>[]> arr = new ArrayList<Point<T>[]>();
        this.navigate(this.root_, new Func<T>() {
            @Override
            public void call(QuadTree<T> quadTree, Node<T> node) {
                Point<T>[] pt = node.getPoint();
                for (int i=0 ; i<node.getPoint().length;i++){
                if (pt[i].getX() < xmin || pt[i].getX() > xmax || pt[i].getY() < ymin || pt[i].getY() > ymax) {
                    // Definitely not within the polygon!
                } else {
                    arr.add(node.getPoint());
                }
                }

            }
        }, xmin, ymin, xmax, ymax);
        return arr.toArray((Point<T>[]) new Point[arr.size()]);
    }

    public Point<T>[] searchWithin(final double xmin, final double ymin, final double xmax, final double ymax) {
        final List<Point<T>[]> arr = new ArrayList<Point<T>[]>();
        this.navigate(this.root_, new Func<T>() {
            @Override
            public void call(QuadTree<T> quadTree, Node<T> node) {
                Point<T>[] pt = node.getPoint();
                for (int i=0 ; i<node.getPoint().length;i++){
	                if (pt[i].getX() > xmin && pt[i].getX() < xmax && pt[i].getY() > ymin && pt[i].getY() < ymax) {
	                    arr.add(node.getPoint());
	                }
                }
            }
        }, xmin, ymin, xmax, ymax);
        return arr.toArray((Point<T>[]) new Point[arr.size()]);
    }

    public void navigate(Node<T> node, Func<T> func, double xmin, double ymin, double xmax, double ymax) {
        switch (node.getNodeType()) {
            case LEAF:
                func.call(this, node);
                break;

            case POINTER:
                if (intersects(xmin, ymax, xmax, ymin, node.getNe()))
                    this.navigate(node.getNe(), func, xmin, ymin, xmax, ymax);
                if (intersects(xmin, ymax, xmax, ymin, node.getSe()))
                    this.navigate(node.getSe(), func, xmin, ymin, xmax, ymax);
                if (intersects(xmin, ymax, xmax, ymin, node.getSw()))
                    this.navigate(node.getSw(), func, xmin, ymin, xmax, ymax);
                if (intersects(xmin, ymax, xmax, ymin, node.getNw()))
                    this.navigate(node.getNw(), func, xmin, ymin, xmax, ymax);
                break;
        }
    }

    private boolean intersects(double left, double bottom, double right, double top, Node<T> node) {
        return !(node.getX() > right ||
                (node.getX() + node.getW()) < left ||
                node.getY() > bottom ||
                (node.getY() + node.getH()) < top);
    }
    /**
     * Clones the quad-tree and returns the new instance.
     * @return {QuadTree} A clone of the tree.
     */
    public QuadTree<T> clone() {
        double x1 = this.root_.getX();
        double y1 = this.root_.getY();
        double x2 = x1 + this.root_.getW();
        double y2 = y1 + this.root_.getH();
        double ep=1;
        final QuadTree<T> clone = new QuadTree<T>(ep, x1, y1, x2, y2);
        // This is inefficient as the clone needs to recalculate the structure of the
        // tree, even though we know it already.  But this is easier and can be
        // optimized when/if needed.
        this.traverse(this.root_, new Func<T>() {
            @Override
            public void call(QuadTree<T> quadTree, Node<T> node) {
            	for (int i=0 ; i<node.getPoint().length;i++){
            		clone.set(node.getPoint()[i].getX(), node.getPoint()[i].getY(), node.getPoint()[i].getValue());
            	}
            }
        });


        return clone;
    }

    /**
     * Traverses the tree depth-first, with quadrants being traversed in clockwise
     * order (NE, SE, SW, NW).  The provided function will be called for each
     * leaf node that is encountered.
     * @param {QuadTree.Node} node The current node.
     * @param {function(QuadTree.Node)} fn The function to call
     *     for each leaf node. This function takes the node as an argument, and its
     *     return value is irrelevant.
     * @private
     */
    public void traverse(Node<T> node, Func<T> func) {
        switch (node.getNodeType()) {
            case LEAF:
                func.call(this, node);
                break;

            case POINTER:
                this.traverse(node.getNe(), func);
                this.traverse(node.getSe(), func);
                this.traverse(node.getSw(), func);
                this.traverse(node.getNw(), func);
                break;
        }
    }

    /**
     * Finds a leaf node with the same (x, y) coordinates as the target point, or
     * null if no point exists.
     * @param {QuadTree.Node} node The node to search in.
     * @param {number} x The x-coordinate of the point to search for.
     * @param {number} y The y-coordinate of the point to search for.
     * @return {QuadTree.Node} The leaf node that matches the target,
     *     or null if it doesn't exist.
     * @private
     */
    public Node<T> find(Node<T> node, double x, double y) {
        Node<T> resposne = null;
        switch (node.getNodeType()) {
            case EMPTY:
                break;

            case LEAF:
            	for (int i=0 ; i<node.getPoint().length;i++){
            		resposne = node.getPoint()[i].getX() == x && node.getPoint()[i].getY() == y ? node : null;
            	}
                break;

            case POINTER:
                resposne = this.find(this.getQuadrantForPoint(node, x, y), x, y);
                break;

            default:
                throw new QuadTreeException("Invalid nodeType");
        }
        return resposne;
    }

    /**
     * Inserts a point into the tree, updating the tree's structure if necessary.
     * @param {.QuadTree.Node} parent The parent to insert the point
     *     into.
     * @param {QuadTree.Point} point The point to insert.
     * @return {boolean} True if a new node was added to the tree; False if a node
     *     already existed with the correpsonding coordinates and had its value
     *     reset.
     * @private
     */
    private boolean insert(Node<T> parent, Point<T> point) {
        Boolean result = false;
        switch (parent.getNodeType()) {
            case EMPTY:
            	for (int i=0 ; i<parent.getPoint().length;i++){
                this.setPointForNode(parent, point);
            	}
                result = true;
                break;
            case LEAF:
            	for (int i =0;i<parent.getPoint().length ;i++){
	                if (parent.getPoint()[i].getX() == point.getX() && parent.getPoint()[i].getY() == point.getY()) {
	                    this.setPointForNode(parent, point);
	                    result = false;
	                } else {
	                    this.split(parent);
	                    result = this.insert(parent, point);
	                }
            	}
                break;
            case POINTER:
                result = this.insert(
                        this.getQuadrantForPoint(parent, point.getX(), point.getY()), point);
                break;

            default:
                throw new QuadTreeException("Invalid nodeType in parent");
        }
        return result;
    }

    /**
     * Converts a leaf node to a pointer node and reinserts the node's point into
     * the correct child.
     * @param {QuadTree.Node} node The node to split.
     * @private
     */
    private void split(Node<T> node) {
        Point<T>[] oldPoint = node.getPoint();
        node.setPoint(null);

        node.setNodeType(NodeType.POINTER);

        double x = node.getX();
        double y = node.getY();
        double hw = node.getW() / 2;
        double hh = node.getH() / 2;
        double ep=1;
        node.setNw(new Node<T>(ep,x, y, hw, hh, node));
        node.setNe(new Node<T>(ep,x + hw, y, hw, hh, node));
        node.setSw(new Node<T>(ep,x, y + hh, hw, hh, node));
        node.setSe(new Node<T>(ep,x + hw, y + hh, hw, hh, node));
        for (int i=0 ; i<node.getPoint().length;i++){
        this.insert(node, oldPoint[i]);
        }
    }

    /**
     * Attempts to balance a node. A node will need balancing if all its children
     * are empty or it contains just one leaf.
     * @param {QuadTree.Node} node The node to balance.
     * @private
     */
    private void balance(Node<T> node) {
        switch (node.getNodeType()) {
            case EMPTY:
            case LEAF:
                if (node.getParent() != null) {
                    this.balance(node.getParent());
                }
                break;

            case POINTER: {
                Node<T> nw = node.getNw();
                Node<T> ne = node.getNe();
                Node<T> sw = node.getSw();
                Node<T> se = node.getSe();
                Node<T> firstLeaf = null;

                // Look for the first non-empty child, if there is more than one then we
                // break as this node can't be balanced.
                if (nw.getNodeType() != NodeType.EMPTY) {
                    firstLeaf = nw;
                }
                if (ne.getNodeType() != NodeType.EMPTY) {
                    if (firstLeaf != null) {
                        break;
                    }
                    firstLeaf = ne;
                }
                if (sw.getNodeType() != NodeType.EMPTY) {
                    if (firstLeaf != null) {
                        break;
                    }
                    firstLeaf = sw;
                }
                if (se.getNodeType() != NodeType.EMPTY) {
                    if (firstLeaf != null) {
                        break;
                    }
                    firstLeaf = se;
                }

                if (firstLeaf == null) {
                    // All child nodes are empty: so make this node empty.
                    node.setNodeType(NodeType.EMPTY);
                    node.setNw(null);
                    node.setNe(null);
                    node.setSw(null);
                    node.setSe(null);

                } else if (firstLeaf.getNodeType() == NodeType.POINTER) {
                    // Only child was a pointer, therefore we can't rebalance.
                    break;

                } else {
                    // Only child was a leaf: so update node's point and make it a leaf.
                    node.setNodeType(NodeType.LEAF);
                    node.setNw(null);
                    node.setNe(null);
                    node.setSw(null);
                    node.setSe(null);
                    node.setPoint(firstLeaf.getPoint());
                }

                // Try and balance the parent as well.
                if (node.getParent() != null) {
                    this.balance(node.getParent());
                }
            }
            break;
        }
    }

    /**
     * Returns the child quadrant within a node that contains the given (x, y)
     * coordinate.
     * @param {QuadTree.Node} parent The node.
     * @param {number} x The x-coordinate to look for.
     * @param {number} y The y-coordinate to look for.
     * @return {QuadTree.Node} The child quadrant that contains the
     *     point.
     * @private
     */
    private Node<T> getQuadrantForPoint(Node<T> parent, double x, double y) {
        double mx = parent.getX() + parent.getW() / 2;
        double my = parent.getY() + parent.getH() / 2;
        if (x < mx) {
            return y < my ? parent.getNw() : parent.getSw();
        } else {
            return y < my ? parent.getNe() : parent.getSe();
        }
    }

    /**
     * Sets the point for a node, as long as the node is a leaf or empty.
     * @param {QuadTree.Node} node The node to set the point for.
     * @param {QuadTree.Point} point The point to set.
     * @private
     */
    public void setPointForNode(Node<T> node, Point<T> point) {
    	Point<T>[] p = new Point[]{point};
        node.setPoint(p);
    }
    //构建差分隐私树
    public <T> Node<T> QTSC(QuadTree<T> tree, Node<T> node, int h,double lambda, int[] dataset,double theta,double sensitive){
		node.insert(tree, node, dataset);
		if(h==0){
			return node;
		}else if(node.even(node,lambda,tree,dataset,theta,sensitive)){
			return node;
		}else{
			double x = node.getX();
	        double y = node.getY();
	        double hw = node.getW() / 2;
	        double hh = node.getH() / 2;
	        node.setNodeType(NodeType.POINTER);
	        node.setNw(new Node<T>(node.getEp()*lambda, x, y, hw, hh, node));
	        node.setNe(new Node<T>(node.getEp()*lambda, x + hw, y, hw, hh, node));
	        node.setSw(new Node<T>(node.getEp()*lambda, x, y + hh, hw, hh, node));
	        node.setSe(new Node<T>(node.getEp()*lambda, x + hw, y + hh, hw, hh, node));
	        QTSC(tree, node.getNe(),h-1,lambda, dataset,theta,sensitive);
	        QTSC(tree, node.getNw(),h-1,lambda, dataset,theta,sensitive);
	        QTSC(tree, node.getSe(),h-1,lambda, dataset,theta,sensitive);
	        QTSC(tree, node.getSw(),h-1,lambda, dataset,theta,sensitive);
		}
		return node;
	}
    
    public List<double[]> search(List<double[]> recs, double w, double h,double sensitive){
    	List<double[]> result=new ArrayList<double[]>();
    	for(int i=0;i<recs.size();i++){
	    	List<Node<T>> leaf=new ArrayList<Node<T>>();
	    	leaf=searchLeaf(this.root_,recs.get(i),w,h);
	    	double count=0;
	    	double noise=0;
	    	for (int j=0;j<leaf.size();j++){	
    			count+=leaf.get(j).getCount();
    			double x=(recs.get(i)[0]+w-leaf.get(j).getX())*(recs.get(i)[1]+h-leaf.get(j).getH())/(leaf.get(j).getW()*leaf.get(j).getH());
    			System.out.println(x);
    			noise+=getNoise(sensitive/leaf.get(j).getEp(),sensitive);	
	    	}
	    	double[] temp={count,noise};
			result.add(temp);
    	}
    	return result;
    }
    
    //获取噪声
    public double getNoise(double param,double sentive)	{
		Random random = new Random();
		double randomDouble = random.nextDouble()-0.5;
		double noise = param- sentive*Math.signum(randomDouble)*Math.log(1-2*Math.abs(randomDouble));
		return noise;	
	}
   
    //确定在范围内的叶子结点
    public List<Node<T>> searchLeaf(Node<T> node,double[] rec, double w,double h){
    	List<Node<T>> leaf=new ArrayList<Node<T>>();
    	if(node.getNodeType()==NodeType.EMPTY){
    		if(nodeSearch(node,rec,w,h)){
    			leaf.add(node);
    		}
    	}else{
        	List<Node<T>> child=new ArrayList<Node<T>>();
    		child.addAll(searchLeaf(node.getNe(),rec,w,h));
    		child.addAll(searchLeaf(node.getNw(),rec,w,h));
    		child.addAll(searchLeaf(node.getSe(),rec,w,h));
    		child.addAll(searchLeaf(node.getSw(),rec,w,h));
    		if (child.size()>0){			
    			leaf.addAll(child);}
    	}
    	return leaf;
    }
  //确定该叶子结点是否在范围内
    public boolean nodeSearch(Node<T> node,double[] rec, double w,double h){
    	double xmin=rec[0];
    	double xmax=rec[0]+w;
    	double ymin=rec[1];
    	double ymax=rec[1]+h;
    	double nxmin=node.getX();
    	double nymin=node.getY();
    	double nxmax=node.getW()+nxmin;
    	double nymax=node.getH()+nymin;
    	if((((ymax>=nymin)&&(ymax<=nymax))||((ymin<=nymax)&&(ymin>=nymin)))&&(((xmax>=nxmin)&&(xmax<=nxmax))||((xmin<=nxmax)&&(xmin>=nxmin)))){
			return true;
		}else{
			return false;
		}
    }
  //获取查询范围，自变量为x开始的坐标，一开始的坐标，查询区域的宽度，总宽度，总高度
  	public List<double[]> query(int num, double minx, double miny,double perw,double perh,double w,double h){
  		List<double[]> recs=new ArrayList<double[]>();
  		int n=(int) (w/perw)-1;
  		int m=(int)(h/perh)-1;
  		for(int a=0;a<num;a++){
	  		Random rand=new Random();
	  	     int i=(int)(Math.random()*n);       //  生成0-n的随机数
	  	     int j=(int)(Math.random()*m);
	  	     double xstart=minx+i*perw;
	  	     double ystart=miny+j*perh;
	  	     double[] query={xstart,ystart};
	  	     recs.add(query);
  	     }
  	     return recs;
  	}

}
