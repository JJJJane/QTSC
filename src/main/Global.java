package main;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
public class Global {
	private int[] dataset;
	private double theta;
	private double ep;
	private double xmin;
	private double ymin;
	private double w;
	private double h;
	private double lambda;
	private int treeH;
	private double sensitive;
	private double queryw;
	public Global(double xmin, double ymin, double w, double h, int treeH, double ep,double sensitive, double theta,double lambda, int[] dataset){
		this.xmin=xmin;
		this.ymin=ymin;
		this.w=w;
		this.h=h;
		this.treeH=treeH;
		this.ep=ep;
		this.sensitive=sensitive;
		this.theta=theta;
		this.lambda=lambda;
		this.dataset=dataset;
	}
	
	public double getQueryw(){
		return this.w/Math.pow(2,this.treeH);
	}
	public double getQueryh(){
		return this.h/Math.pow(2,this.treeH);
	}
	public double getXmin(){
		return this.xmin;
	}
	public double getYmin(){
		return this.ymin;
	}
	public double getW(){
		return this.w;
	}
	public double getH(){
		return this.h;
	}
	public double getEp(){
		return this.ep;
	}
	public double getSensitive(){
		return this.sensitive;
	}
	public double getTheta(){
		return this.theta;
	}
	public int[] getData(){
		return this.dataset;
	}
	public int getTreeH(){
		return this.treeH;
	}
	public double getLambda(){
		return this.lambda;
	}
	//查询结果，真实结果，参数，总数据集
	public double re(double noise,double row, double real, double count){
		double re=real>row*count?Math.abs(noise)/real :Math.abs(noise)/count*row;
		return re;
	}
	
}
