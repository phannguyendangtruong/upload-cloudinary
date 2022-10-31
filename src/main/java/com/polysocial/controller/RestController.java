package com.polysocial.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.http.HttpStatus;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import com.polysocia.entity.files;

@org.springframework.web.bind.annotation.RestController
public class RestController {
	@Autowired
	private Cloudinary cloudinary;

	String ur = "";

	@PostMapping("/product")
	public String add(@RequestParam(value = "file", required = false) MultipartFile fi) throws IOException {
		files file = new files();
		file.setFile(fi);
		File f = new File(fi.getOriginalFilename());
		String type = checkType(file.getFile().getContentType()); // check type luc up len server
		Path uploadPath = Paths.get("Files"); // trỏ toi folder
		String fName = f.getName(); // lay ra ten file
		String fileName = fName.substring(fName.lastIndexOf("."));
		try (InputStream inputStream = fi.getInputStream()) {
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING); // luu file local
		} catch (IOException ioe) {
			throw new IOException("Could not save file: ");
		}

		try {
			if (type.equals("jpg")) {
				String demo = ""+this.cloudinary.uploader().upload("./Files/" + fileName, ObjectUtils.asMap("moderation", "aws_rek"));
				int firtsIndex = demo.indexOf("url=");
				int lastIndex = demo.indexOf("created_at");
				String url = demo.substring(firtsIndex + 4, lastIndex - 2);
				ur = url; // lay ra duong dan anh
				System.out.println(demo);
				// cac loai anh bi cam
				String[] typeImage = {"Explicit Nudity", "Suggestive", "Violence", "Visually Disturbing","Rude Gesture", "Drugs", "Tobacco", "Alcohol", "Gambling", "Hate Symbols"};
				for(int i = 0 ; i<type.length(); i++) {
					if(demo.contains(typeImage[i])) {
						System.out.println("Anh khong hop le !!!!");
					}
				}
				
				
			} else {
				String demo = "" + this.cloudinary.uploader().upload("./Files/" + fileName,
						ObjectUtils.asMap("resource_type", "auto"));
				System.out.println(demo);
				if (type.equals("video")) {
					int firtsIndex = demo.lastIndexOf("url=");
					int lastIndex = demo.indexOf("tags=");
					String url = demo.substring(firtsIndex + 4, lastIndex - 2);
					System.out.println(url);
					return url;
				} else {
					int firtsIndex = demo.indexOf("url=");
					int lastIndex = demo.indexOf("created_at");
					String url = demo.substring(firtsIndex + 4, lastIndex - 2);
					ur = url;
					System.out.println(url);

				}
				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return ur;
	}

	public String checkType(String type) {
		if (type.contains("pdf")) {
			type = "pdf";
		} else if (type.contains("document") || type.contains("msword")) {
			type = "docx";
		} else if (type.contains("zip")) {
			type = "zip";
		} else if (type.contains("jpg") || type.contains("jpeg")) {
			type = "jpg";
		} else if (type.contains("excel")) {
			type = "excel";
		} else if (type.contains("mp4") || type.contains("mp3")) {
			type = "video";
		} else {
			type = "N/A";
		}
		return type;
	}
}
