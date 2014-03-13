package ECG_test;

public class Derivative {
	private int sample_rate = 200;	//采样率
	private int d1,d2;//一阶、二阶差分值
	private int sum;//一阶二阶差分值相加
	private Derivator derivator1 = new Derivator();	//获得一阶微分器、平滑器实例
	private Smoother smoother1 = new Smoother();
	private Derivator derivator2 = new Derivator();	//获得二阶微分器、平滑器实例
	private Smoother smoother2 = new Smoother();
	private int THRESHOLD_UP = 400;	//迟滞比较器的一组阈值
	private int THRESHOLD_DOWN = 100;
	private int RR_counter;	//RR间期计数器
	private int RR_interval;	//RR间期
	private int QRS_counter;	//QRS间期计数器
	private int QRS_duration;	//QRS间期
	private int QRS_state;	//QRS状态机
	
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
			y0 = (x + 2*x1 + x2);
			x2 = x1;
			x1 = x;
			return y0;
		}
	}
	
	/*
	 * 显示信息
	 */
	private void display(){
		System.out.print("RR:"+RR_interval+"  QRS:"+QRS_duration+"\n");
	}
	
	/*
	 * 总处理函数
	 */
	public int[] process(int[] ecg){
		//输出数组
		int[] out = new int[ecg.length];
		//逐点操作
		for(int i=0; i<ecg.length; i++){
			//一阶差分，整流，平滑
			d1 = derivator1.get_derivative(ecg[i]);
			if(d1<0)
				d1 = -d1;
			d1 = smoother1.smooth(d1);
			//二阶差分，整流，平滑
			d2 = derivator2.get_derivative(d1);
			if(d2<0)
				d2 = -d2;
			d2 = smoother2.smooth(d2);
			//将一阶、二阶差分相加
			sum = (d1 + d2)*2;
			//阈值判断
			RR_counter++;
			if(QRS_state == 0){
				//上升沿阈值
				if(sum>THRESHOLD_UP){
					QRS_state = 1;
					//计算RR间期
					RR_interval = RR_counter*(1000/sample_rate);
					RR_counter = 0;
				}
			}
			else if(QRS_state == 1){
				QRS_counter++;
				//下降沿阈值
				if(sum<THRESHOLD_DOWN){
					QRS_state = 0;
					//计算QRS间期
					QRS_duration = QRS_counter*(1000/sample_rate);
					QRS_counter = 0;
					display();
				}
			}
			
			out[i] = sum;
		}
		return out;
	}
	
	

}