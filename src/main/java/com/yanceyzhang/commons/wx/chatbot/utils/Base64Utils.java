package com.yanceyzhang.commons.wx.chatbot.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.yanceyzhang.commons.wx.chatbot.utils.dto.ImageBase64Md5;


public class Base64Utils {

	
	
	/**
	 * 本地图片转换
	 * @param imgFile 图片本地路径
	 * @return
	 *
	 * @author yanceyzhang
	 */
	public static ImageBase64Md5 ImageToBase64ByLocal(String imgFile) {

		InputStream in = null;
		byte[] data = null;
		try {
			in = new FileInputStream(imgFile);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String base64 = Base64.getEncoder().encodeToString(data);
		return new ImageBase64Md5(base64, DigestUtils.md5Hex(data));
	}
	/**
	 * 在线图片转换
	 * 
	 * @param imgURL 图片线上路径
	 * @return
	 *
	 * @author yanceyzhang
	 */
	public static ImageBase64Md5 ImageToBase64ByOnline(String imgURL) {
		try {
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			// 创建URL
			URL url = new URL(imgURL);
			byte[] by = new byte[1024];
			// 创建链接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(20000);
			InputStream is = conn.getInputStream();
			// 将内容读取内存中
			int len = -1;
			while ((len = is.read(by)) != -1) {
				data.write(by, 0, len);
			}
			byte[] b=data.toByteArray();
			String base64= Base64.getEncoder().encodeToString(b);
			String md5= DigestUtils.md5Hex(b);
			is.close();
			conn.disconnect();
			data.close();
			return new ImageBase64Md5(base64, md5);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * base64字符串转换成图片
	 * 
	 * @param imgStr      base64字符
	 * @param imgFilePath 图片存放路径
	 * @return
	 */
	public static boolean Base64ToImage(String imgStr, String imgFilePath) {
		if (StringUtils.isEmpty(imgStr)) {
			return false;
		}
		try {
			// Base64解码
			byte[] b = Base64.getDecoder().decode(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}

			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(b);
			out.flush();
			out.close();

			return true;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

}
