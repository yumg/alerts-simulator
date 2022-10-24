package util;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpKit {
	public static String get(String path, String[] params) {
		if (params.length > 0) {
			String paramStr = String.join("&", params);
			path = path + "?" + paramStr;
		}
		try {
			String message = "";
			// 创建远程url连接对象
			URL url = new URL(path);
			// 通过远程url连接对象打开一个连接，强转成httpURLConnection类
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// 设置连接方式：get
			connection.setRequestMethod("GET");
			// 设置连接主机服务器的超时时间：15000毫秒
			connection.setConnectTimeout(15000);
			// 设置读取远程返回的数据时间：60000毫秒
			connection.setReadTimeout(60000);
			// 发送请求
			connection.connect();
			// 通过connection连接，获取输入流
			if (connection.getResponseCode() == 200) {
				InputStream inputStream = connection.getInputStream();
				byte[] data = new byte[1024];
				StringBuffer sb1 = new StringBuffer();
				int length = 0;
				while ((length = inputStream.read(data)) != -1) {
					String s = new String(data, 0, length);
					sb1.append(s);
				}
				message = sb1.toString();
				inputStream.close();
				// 关闭连接
				connection.disconnect();
				return message;
			} else
				throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String post(String path, String[] params, String body) {
		if (params.length > 0) {
			String paramStr = String.join("&", params);
			path = path + "?" + paramStr;
		}
		try {
			String message = "";
			// 创建远程url连接对象
			URL url = new URL(path);
			// 通过远程url连接对象打开一个连接，强转成httpURLConnection类
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// 设置连接方式：post
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			// 设置连接主机服务器的超时时间：15000毫秒
			connection.setConnectTimeout(15000);
			// 设置读取远程返回的数据时间：60000毫秒
			connection.setReadTimeout(60000);
			// 发送请求
			connection.connect();

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
			writer.write(body);
			writer.close();
			
			// 通过connection连接，获取输入流
			if (connection.getResponseCode() == 200) {
				InputStream inputStream = connection.getInputStream();
				byte[] data = new byte[1024];
				StringBuffer sb1 = new StringBuffer();
				int length = 0;
				while ((length = inputStream.read(data)) != -1) {
					String s = new String(data, 0, length);
					sb1.append(s);
				}
				message = sb1.toString();
				inputStream.close();
				// 关闭连接
				connection.disconnect();
				return message;
			} else
				throw new Exception();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
