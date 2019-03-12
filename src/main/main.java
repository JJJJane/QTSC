package main;
import java.io.*;
import java.lang.String;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
public class main {
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 创建窗口对象
                MyFrame frame = new MyFrame();
                // 显示窗口
                frame.setVisible(true);
            }
        });
	}
	public static class MyFrame extends JFrame {

        public static final String TITLE = "查询误差";

        public static final int WIDTH = 250;
        public static final int HEIGHT = 300;

        public MyFrame() {
            super();
            initFrame();
        }

        private void initFrame() {
            // 设置 窗口标题 和 窗口大小
            setTitle(TITLE);
            setSize(WIDTH, HEIGHT);

            // 设置窗口关闭按钮的默认操作(点击关闭时退出进程)
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // 把窗口位置设置到屏幕的中心
            setLocationRelativeTo(null);

            // 设置窗口的内容面板
            MyPanel panel = new MyPanel(this);
            setContentPane(panel);
        }

    }
	public static class MyPanel extends JPanel {

        private MyFrame frame;

        public MyPanel(MyFrame frame) {
            super();
            this.frame = frame;
        }

        /**
         * 绘制面板的内容: 创建 JPanel 后会调用一次该方法绘制内容,
         * 之后如果数据改变需要重新绘制, 可调用 updateUI() 方法触发
         * 系统再次调用该方法绘制更新 JPanel 的内容。
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
          //load data
    		File file = new File("/Users/taohuadao/Documents/WFE/QuadTree1/eastwe.txt");
    		String[] split=txt2String(file).split("\t");
    		int[] coordinate=new int[split.length];
            for(int i=1;i<split.length;i++){
            	coordinate[i]=Integer.valueOf(split[i],10);
            }
            //xmin,ymin,w,h,treeh,ep,sensitive,theta,dataset
    		Global global=new Global(520000,170000,20480,20480,2,1,1,0.5,2,coordinate);
    		double ep=global.getEp();
    		double sensitive=global.getSensitive();
    		double theta=global.getTheta();
    		double xmin=global.getXmin();
    		double ymin=global.getYmin();
    		double w=global.getW();
    		double h=global.getH();
    		double lambda=global.getLambda();
    		double perw=global.getQueryw();
    		double perh=global.getQueryh();
    		int treeH=global.getTreeH();
    		int[] dataset=global.getData();
    		//构建树
    		QuadTree<String> tree=new QuadTree<String>(ep,xmin,ymin,w,h);
    		tree.QTSC(tree,tree.getRootNode(),treeH,lambda,dataset,theta,sensitive);
    		double[] error={0,0,0,0,0};
    		//在不同的查询范围中循环
    		for(int k=0;k<error.length;k++){
    			double queryw=(k+1)*perw;
        		double queryh=(k+1)*perh;
        		//生成查询区域
        		List<double[]> recs=tree.query(10,xmin,ymin,queryw,queryh,w,h);
        		List<double[]> count=tree.search(recs, queryw, queryh,sensitive);
	    		double[] re={0,0,0,0,0,0,0,0,0,0};
	    		for(int j=0;j<10;j++){
		    		//查询结果，参数，真实结果，总值
	    			double RE=global.re(count.get(j)[1],100,count.get(j)[0],2000);
	    			re[j]=RE;
	    		}
	    		double sum=0;
	    		for(int i=0;i<re.length;i++){
	    			sum+=re[i];
	    		}
	    		sum/=re.length;
	    		error[k]=sum;   
    		}
    		drawLine(g,error);
        }
        private void drawLine(Graphics g, double[] res) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 设置画笔颜色
            g2d.setColor(Color.RED);
            
            for(int i=0;i<res.length;i++){
            	int a=i*10;
            	int b=(int)(res[i]*300);
            	System.out.println(b);
            	g2d.fillRect(a,10,8,b);
            	}
        }
        }
	 public static String txt2String(File file){
	        StringBuilder result = new StringBuilder();
	        try{
	            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
	            String s = null;
	            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
	                result.append(System.lineSeparator()+s);
	            }
	            br.close();    
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	        return result.toString();
	    }
}
