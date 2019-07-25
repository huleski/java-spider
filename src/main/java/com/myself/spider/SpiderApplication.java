package com.myself.spider;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.IOException;

@SpringBootApplication
public class SpiderApplication  implements CommandLineRunner {
	private static final Logger logger = LoggerFactory.getLogger(SpiderApplication.class);

	private final static OkHttpClient client = new OkHttpClient();
	private final static Moshi moshi = new Moshi.Builder().build();
	private final static JsonAdapter<Result> resultAdapter = moshi.adapter(Result.class);
	private static String url = "";

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(SpiderApplication.class, args);
	}

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOrigin("*");
		config.setAllowCredentials(true);
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");

		UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
		configSource.registerCorsConfiguration("/**", config);
		return new CorsFilter(configSource);
	}

	@Override
	public void run(String... args) throws Exception {
//		synGetData();
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

		// 设置超时
//		client.newBuilder().connectTimeout(10, TimeUnit.SECONDS);
//		client.newBuilder().readTimeout(10,TimeUnit.SECONDS);
//		client.newBuilder().writeTimeout(10,TimeUnit.SECONDS);
}