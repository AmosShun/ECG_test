package ECG_test;

import java.util.Arrays;

public class Derivative {
	private int sample_rate = 200;	//采样率
	private int N_buf;	//一个数据帧的长度
	private int d1,d2;//一阶、二阶差分值
	private int sum;//一阶二阶差分值相加
	private Derivator derivator1 = new Derivator();	//获得一阶微分器、平滑器实例
	private Smoother smoother1 = new Smoother();
	private Derivator derivator2 = new Derivator();	//获得二阶微分器、平滑器实例
	private Smoother smoother2 = new Smoother();
	private int THRESHOLD_UP;	//迟滞比较器的一组阈值
	private int THRESHOLD_DOWN;
	private int RR_counter;	//RR间期计数器
	private int RR_interval;	//RR间期
	private int QRS_counter;	//QRS间期计数器
	private int QRS_duration;	//QRS间期
	private int QRS_state;	//QRS状态机
	private int[] RR_intervals = new int[8];	//存放RR间期数组，用来计算心率
	private int heart_rate;	//心率
	private int RR_intervals_counter;	//RR间期数组的索引
	private boolean THRESHOLD_INITED;	//阈值初始化完成标志
	private boolean START;	//开始计算RR间期标志位
	
	
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
		System.out.print("RR:"+RR_interval+"  QRS:"+QRS_duration+"  ");
		System.out.print("HEART RATE:"+heart_rate+"\n");
	}
	
	/*
	 * 计算心率
	 */
	private void calculate_heart_rate(){
		RR_intervals[RR_intervals_counter] = RR_interval;
		if(++RR_intervals_counter == 8)
			RR_intervals_counter = 0;
		//如果存满8个RR间期，开始计算心率
		if(RR_intervals[7] != 0){
			int sum = 0;
			for(int i=0; i<8; i++){
				sum += RR_intervals[i];
			}
			sum /= 8;
			heart_rate = 60000/sum;
		}
	}
	
	/*
	 * 阈值初始化
	 */
	private int init_counter1;	//2秒计数器
	private int init_counter2;	//10组计数器
	private int max;
	private int[] maxes = new int[10];
	private void init_threshold(int data){
		if(data>max)
			max = data;
		//2秒到
		if(++init_counter1 == sample_rate*2){
			init_counter1 = 0;
			maxes[init_counter2] = max;	//将2秒内的最大值保存
			max = 0;	//最大值清零
			//10组到
			if(++init_counter2 == 10){
				init_counter2 = 0;
				Arrays.sort(maxes);	//对最大值排序
				//去首尾，求平均
				int sum = 0;
				for(int i=1; i<9; i++){
					sum += maxes[i];
				}
				sum /= 8;
				//设置阈值
				THRESHOLD_UP = sum/2;
				THRESHOLD_DOWN = sum/8;
				THRESHOLD_INITED = true;	//开启初始化完成标志位
				START = false; //防止直接计算第一个RR间期
			}
		}
	}
	
	/*
	 * 自适应调整阈值
	 */
	private int[] maxes_for_adjust = new int[4];
	private int maxes_counter;
	private void adjust_threshold(){
		maxes_for_adjust[maxes_counter] = max;
		max = 0;
		if(++maxes_counter == 4)
			maxes_counter = 0;
		//如果数组存满，计算平均值
		if(maxes_for_adjust[3] > 0){
			int sum = 0;
			for(int i=0; i<4; i++){
				sum += maxes_for_adjust[i];
			}
			sum /= 4;
			//更新阈值
			THRESHOLD_UP = sum/2;
			THRESHOLD_DOWN = sum/8;
		}
	}
	
	/*
	 * 总处理函数
	 */
	public int[] process(int[] ecg){
		N_buf = ecg.length;
		//输出数组
		int[] out = new int[N_buf];
		//逐点操作
		for(int i=0; i<N_buf; i++){
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
			if(THRESHOLD_INITED){
				//阈值判断
				RR_counter++;
				if(QRS_state == 0){
					//上升沿阈值
					if(sum>THRESHOLD_UP){
						QRS_state = 1;
						if(START){	//只有当START开启才计算RR间期，否则第一个RR间期计算不正确
							//计算RR间期
							RR_interval = RR_counter*(1000/sample_rate);
							//计算心率
							calculate_heart_rate();
						}
						RR_counter = 0;
						START = true;	//开始START标志位
					}
				}
				else if(QRS_state == 1){
					QRS_counter++;
					//获取输出最大值，用以调整阈值
					if(sum>max)
						max = sum;
					//下降沿阈值
					if(sum<THRESHOLD_DOWN){
						QRS_state = 0;
						//计算QRS间期
						QRS_duration = QRS_counter*(1000/sample_rate);
						QRS_counter = 0;
						display();
						//调整阈值
						adjust_threshold();
					}
				}
			}
			else{
				init_threshold(sum);
			}
			
			out[i] = sum;
		}
		return out;
	}
	
	

}