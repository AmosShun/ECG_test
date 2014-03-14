package ECG_test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class test {
	public static final int N_buf = 20;
	private static Derivative derivative = new Derivative();
	private static Filter filter = new Filter();
	
	/*
	 * 从文件读数据
	 */
	public static void readfile(InputStream in, int[] buffer) throws IOException{
		for(int i=0; i<N_buf; i++){
			int temp_l = in.read();
			int temp_h = in.read();
			int temp = temp_h<<8;
			temp = temp + temp_l;
			buffer[i] = temp;
		}
	}
	
	/*
	 * 向文件写数据
	 */
	public static void writefile(OutputStream out, int[] buffer) throws IOException{
		for(int i=0; i<N_buf; i++){
			byte temp_l = (byte)buffer[i];
			byte temp_h = (byte)(buffer[i]>>8);
			out.write(temp_l);
			out.write(temp_h);
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		File file_in = new File("D:\\ecgdata");
		InputStream in = new FileInputStream(file_in);
		File file_out = new File("D:\\ecgdata2");
		OutputStream out = new FileOutputStream(file_out);
		int[] input_buffer = new int[N_buf];
		int[] output_buffer;
		
		for(int j=0; j<500; j++){
			//输入
			readfile(in, input_buffer);
			//差分法QRS
			derivative.process(input_buffer);
			//滤波
			output_buffer = filter.highpass(input_buffer);
			//输出
			writefile(out,output_buffer);
		}
	}
}
