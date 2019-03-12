package main;
import java.util.Arrays;
import java.util.Random;

public class Node<T> {

    private double x;
    private double y;
    private double w;
    private double h;
    private double ep;
    private Node<T> opt_parent;
    private Point<T>[] point;
    private NodeType nodetype = NodeType.EMPTY;
    private Node<T> nw;
    private Node<T> ne;
    private Node<T> sw;
    private Node<T> se;

    /**
     * Constructs a new quad tree node.
     *
     * @param {double} x X-coordiate of node.
     * @param {double} y Y-coordinate of node.
     * @param {double} w Width of node.
     * @param {double} h Height of node.
     * @param {Node}   opt_parent Optional parent node.
     * @constructor
     */
    public Node(double ep,double x, double y, double w, double h, Node<T> opt_parent) {
    	this.ep=ep;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.opt_parent = opt_parent;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }
    public double getEp(){
    	return this.ep;
    }
    public void setEp(double ep){
    	this.ep=ep;
    }
    public Node<T> getParent() {
        return opt_parent;
    }

    public void setParent(Node<T> opt_parent) {
        this.opt_parent = opt_parent;
    }

    public void setPoint(Point<T>[] point) {
        int str1Length = point.length;
        if(this.point==null){
        	this.point=point;
        }else{
            int str2length = this.point.length;
        	this.point = Arrays.copyOf(this.point, str1Length+str2length);//数组扩容
            System.arraycopy(point, 0, this.point, str2length, str1Length);
        }
    }
    public void deletePoint(double x, double y){
    	for(int i =0;i<this.point.length;i++){
    		if(this.point[i].getX()==x && this.point[i].getY()==y){
    			remove(this.point,point[i]);
    		}
    	}
    }
    private void remove(Point<T>[] arr, Point num) {
        Point<T>[] tmp = new Point[arr.length - 1];
        int idx = 0;
        boolean hasRemove = false;
        for (int i = 0; i < arr.length; i++) {
 
            if (!hasRemove && arr[i] == num) {
                hasRemove = true;
                continue;
            }
            tmp[idx++] = arr[i];
        }
     }

	public Point<T>[] getPoint() {
        return this.point;
    }

    public void setNodeType(NodeType nodetype) {
        this.nodetype = nodetype;
    }

    public NodeType getNodeType() {
        return this.nodetype;
    }


    public void setNw(Node<T> nw) {
        this.nw = nw;
    }

    public void setNe(Node<T> ne) {
        this.ne = ne;
    }

    public void setSw(Node<T> sw) {
        this.sw = sw;
    }

    public void setSe(Node<T> se) {
        this.se = se;
    }

    public Node<T> getNe() {
        return ne;
    }

    public Node<T> getNw() {
        return nw;
    }

    public Node<T> getSw() {
        return sw;
    }

    public Node<T> getSe() {
        return se;
    }
    public int getCount(){
    	if(this.getPoint()!=null){
    		return this.getPoint().length;
    	}
    	else{
    		return 0;
    	}
    }
	public <T> boolean even(Node<T> node,double lambda, QuadTree<T> tree, int[]dataset,double theta,double sensitive){
		double x = node.getX();
        double y = node.getY();
        double hw = node.getW() / 2;
        double hh = node.getH() / 2;
        
		Node<T> nw= new Node<T>(node.getEp()*lambda, x, y, hw, hh, node);
		Node<T> ne= new Node<T>(node.getEp()*lambda, x + hw, y, hw, hh, node);
        Node<T> sw= new Node<T>(node.getEp()*lambda, x, y + hh, hw, hh, node);
        Node<T> se= new Node<T>(node.getEp()*lambda, x + hw, y + hh, hw, hh, node);
        Node<T>[] children= new Node[]{nw,ne,sw,se};
        double to=0;
        double fp=0;
        for (int i=0;i<4;i++){
        	insert(tree, children[i], dataset);
        	to+=children[i].getPoint()==null?0:children[i].getPoint().length;
        }
        double ave= to/4+getNoise(sensitive/nw.getEp(),sensitive);
        for (int j=0;j<4;j++)
        {		
        	fp+=children[j].getPoint()==null?Math.abs(ave):Math.abs(children[j].getPoint().length-ave);
        }
        double total=node.getPoint()==null?0:node.getPoint().length;
        if(total==0){
        	return true;
        }
        if(fp/total<theta){
        	return true;
        }else{
        	return false;
        }
	}
	public <T> void insert(QuadTree<T> tree, Node<T> node, int[] dataset){
		for (int i=1;i<dataset.length-1;i+=2){
			int x=dataset[i];
			int y=dataset[i+1];
		if(x>=node.getX() && x<node.getW()+node.getX() && y>=node.getY() && y<node.getH()+node.getY()){
				Point<T> p=new Point<T>(x,y,node.getEp());
				tree.setPointForNode(node,p);
			}
		}
	}
	//mu，lambda
	public static double getNoise(double param,double sentive)	{
		Random random = new Random();
		double randomDouble = random.nextDouble()-0.5;
		double noise = param- sentive*Math.signum(randomDouble)*Math.log(1-2*Math.abs(randomDouble));
		return noise;	
	}
}
