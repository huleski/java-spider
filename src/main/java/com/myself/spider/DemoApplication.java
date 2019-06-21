package com.myself.spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class, args);
		Picture picture = new Picture(6755775, 75268748, 1, "Neoartcore",
				"https://i.pximg.net/user-profile/img/2015/01/06/18/05/08/8812179_3f0edae6e9ee2e5248ce8c72c8c5e6ff_170.jpg",
				"Nessa", "https://upload.cc/i1/2019/06/20/ZlNW9b.jpg", "https://upload.cc/i1/2019/06/20/ZlNW9b.jpg",
				"2019-06-16");
		PictureService pictureService = context.getBean(PictureService.class);
		pictureService.insert(picture);
	}

}
