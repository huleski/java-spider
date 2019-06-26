package com.myself.spider;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class SpiderApplication  implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(SpiderApplication.class);

	private final static OkHttpClient client = new OkHttpClient();
	private final static Moshi moshi = new Moshi.Builder().build();
	private final static JsonAdapter<Result> resultAdapter = moshi.adapter(Result.class);

	@Autowired
	private PictureService pictureService;

	private static String url = "https://api.pixivic.com/ranks?page=0&date=2019-06-22&mode=day";

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SpiderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		synGetData();

//		Picture picture = new Picture(6755775, 75268748, 1, "Neoartcore",
//				"https://i.pximg.net/user-profile/img/2015/01/06/18/05/08/8812179_3f0edae6e9ee2e5248ce8c72c8c5e6ff_170.jpg",
//				"Nessa", "https://upload.cc/i1/2019/06/20/ZlNW9b.jpg", "https://upload.cc/i1/2019/06/20/ZlNW9b.jpg",
//				"2019-06-16");
//		pictureService.insert(picture);
	}

	/**
	 * Get同步请求
	 */
	public void synGetData() throws Exception {
		Request request = new Request.Builder().url(url) .build();
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
			Headers responseHeaders = response.headers();
			for (int i = 0; i < responseHeaders.size(); i++) {
				System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
			}
			Result result = resultAdapter.fromJson(response.body().source());
			System.out.println(result);
		}
	}

	/**
	 * Get异步请求
	 */
	public void asynGetData(){
		Request request = new Request.Builder().url(url).build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				logger.error("----------------------------请求失败------------------------");
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				try (ResponseBody responseBody = response.body()) {
					if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
					Result result = resultAdapter.fromJson(responseBody.source());
					System.out.println(responseBody.string());
				}
			}
		});
	};

	/**
	 * 文件下载
	 */
	public void download() {
		String url = "http://www.0551fangchan.com/images/keupload/20120917171535_49309.jpg";
		//构建request对象
		Request request = new Request.Builder().url(url).build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				InputStream inputStream = response.body().byteStream();
				FileOutputStream fileOutputStream = new FileOutputStream(new File("/sdcard/logo.jpg"));
				byte[] buffer = new byte[2048];
				int len = 0;
				while ((len = inputStream.read(buffer)) != -1) {
					fileOutputStream.write(buffer, 0, len);
				}
				fileOutputStream.close();
				logger.info("文件下载成功...");
			}
		});

		// 设置超时
//		client.newBuilder().connectTimeout(10, TimeUnit.SECONDS);
//		client.newBuilder().readTimeout(10,TimeUnit.SECONDS);
//		client.newBuilder().writeTimeout(10,TimeUnit.SECONDS);
	}
}
