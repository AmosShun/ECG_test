package ECG_test;

public class Derivative {
	private int d1,d2;//一阶、二阶差分值
	private int sum;//一阶二阶差分值相加
	//获得一阶微分器、平滑器实例
	Derivator derivator1 = new Derivator();
	Smoother smoother1 = new Smoother();
	//获得二阶微分器、平滑器实例
	Derivator derivator2 = new Derivator();
	Smoother smoother2 = new Smoother();
	
	
	/*
	 * 微分器
	 */
	private class Derivator{
		private int x,x1,x2;
		private int get_derivative(int data){
			int y0;
			x = data;
			y0 = x - x2;
			x2 = x1;
			x1 = x;
			return y0;
		}
	}
	
	/*
	 * 平滑器
	 */
	private class Smoother{
		private int x, x1, x2;
		private int smooth(int data){
			int y0;
			x = data;
			y0 = (x + 2*x1 + x2)/4;
			x2 = x1;
			x1 = x;
			return y0;
		}
	}
	
	/*
	 * 总处理函数
	 */
	public int[] process(int[] ecg){
		//输出数组
		int[] out = new int[ecg.length];
		//逐点操作
		for(int i=0; i<ecg.length; i++){
			d1 = derivator1.get_derivative(ecg[i]);
			if(d1<0)
				d1 = -d1;
			d1 = smoother1.smooth(d1);
			d2 = derivator2.get_derivative(d1);
			if(d2<0)
				d2 = -d2;
			d2 = smoother2.smooth(d2);
			sum = (d1 + d2)*2;
			out[i] = sum;
		}
		return out;
	}
	

}