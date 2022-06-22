package Demo.Tool;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
public class GetTime {
	
	public String gettime(){
		
		//��ȡʱ��(String)
		Date now = new Date(); 
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//���Է�����޸����ڸ�ʽ
		String shijian = dateFormat.format(now); 
		return shijian;
	}
	
	public String Gettime(){
		Calendar c = Calendar.getInstance();//���Զ�ÿ��ʱ���򵥶��޸�
		String year = String.valueOf(c.get(Calendar.YEAR)); 
		String month = String.valueOf(c.get(Calendar.MONTH)+1); 
		String date = String.valueOf(c.get(Calendar.DATE)); 
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY)); 
		String minute = String.valueOf(c.get(Calendar.MINUTE)); 
		String second = String.valueOf(c.get(Calendar.SECOND));
		if(Integer.parseInt(month)<10)
			month = "0"+month;
		if(Integer.parseInt(date)<10)
			date = "0"+date;
		if(Integer.parseInt(hour)<10)
			hour = "0"+hour;
		if(Integer.parseInt(minute)<10)
			minute = "0"+minute;
		if(Integer.parseInt(second)<10)
			second = "0"+second;
		String shijian = year+month+date+hour+minute+second;
		return shijian;
	}
	
	public int getsinglevalue(String value){
		
		Calendar c = Calendar.getInstance();//���Զ�ÿ��ʱ���򵥶��޸�
		int year = c.get(Calendar.YEAR); 
		int month = c.get(Calendar.MONTH)+1; 
		int date = c.get(Calendar.DATE); 
		int hour = c.get(Calendar.HOUR_OF_DAY); 
		int minute = c.get(Calendar.MINUTE); 
		int second = c.get(Calendar.SECOND);
		
		switch (value) {
		case "year":
			return year;
		case "month":
			return month;
		case "day":
			return date;
		case "hour":
			return hour;
		case "minute":
			return minute;
		case "second":
			return second;
		default:
			return 404;
		}	
	}

	public long pasttime(Date a){
		long time = 0;

		if(a==null){
			System.out.println("a==null");
		}

		Date b = new Date();

		time = (b.getTime() - a.getTime())/1000;

		return time;
	}

//	public Date parseDate(String dateString){
//		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		Date date = null;
//		try{
//			date = format.parse(dateString);
//		}
//		catch (Exception e){
//			e.printStackTrace();
//		}
//		return date;
//	}

	public String getDateToString(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = sdf.format(date);
		return dateString;
	}

	public Date getStringToDate(String date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date stringDate = null;
		try{
			stringDate = sdf.parse(date);
		}
		catch (Exception e){
		    e.printStackTrace();
		}
		return stringDate;
	}

	public Date getNumberToDate(String number){
		String date = number.substring(0, 4)+"-";
		date += number.substring(4, 6)+"-";
		date += number.substring(6, 8)+" ";
		date += number.substring(8, 10)+":";
		date += number.substring(10, 12)+":";
		date += number.substring(12, 14);

		return getStringToDate(date);
	}
}
