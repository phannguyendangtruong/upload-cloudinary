package com.polysocial.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

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


	@PostMapping("/product")
	public List<String> add(@RequestParam(value = "file", required = false) List<MultipartFile> fi) throws IOException {
		List<String> urlPath = new ArrayList<String>();
		if(fi == null){
			return urlPath;
		}
		File folder = new File("Files");
		folder.mkdir();
		if(saveLocal(fi)){
			urlPath = upLoadServer(fi);
			folder.delete();
		}else{
			return urlPath;
		}
		return urlPath;
	}

	public Boolean saveLocal(List<MultipartFile> fi) throws IOException {
		for (int i = 0; i < fi.size(); i++) {
			files file = new files();
			file.setFile(fi.get(i));
			File f = new File(fi.get(i).getOriginalFilename());
			String type = fi.get(i).getContentType(); // check type luc up len server
			Path uploadPath = Paths.get("Files"); // trỏ toi folder
			String fName = f.getName(); // lay ra ten file
			try (InputStream inputStream = fi.get(i).getInputStream()) {
				Path filePath = uploadPath.resolve(fName);
				Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING); // luu file local
			} catch (IOException ioe) {
				return false;
			}
		}
		return true;
	}

	public List<String> upLoadServer(List<MultipartFile> fi) {
		List<String> urlPath = new ArrayList<>();
		String type = ""; // check type luc up len server
		int firtsIndex = 0;
		int lastIndex = 0;
		String url = "";
		for (int i = 0; i < fi.size(); i++) {
			type = fi.get(0).getContentType(); // check type luc up len server
			String fileName = fi.get(i).getOriginalFilename();
			try {
				if (type.equals("jpg") || type.equals("png") || type.equals("jpeg")) {
					String json = "" + this.cloudinary.uploader().upload("./Files/" + fileName,
							ObjectUtils.asMap("moderation", "aws_rek"));
				 	firtsIndex = json.indexOf("url=");
					lastIndex = json.indexOf("created_at");
					url = json.substring(firtsIndex + 4, lastIndex - 2);
					urlPath.add(url); // lay ra duong dan anh
					// cac loai anh bi cam
					String[] typeImage = { "Explicit Nudity", "Suggestive", "Violence", "Visually Disturbing",
							"Rude Gesture", "Drugs", "Tobacco", "Alcohol", "Gambling", "Hate Symbols" };
					for (int j = 0; j < type.length(); j++) {
						if (json.contains(typeImage[j])) {
							System.out.println("Anh khong hop le !!!!");
							return null;
						}
					}
				} else {
					String json = "" + this.cloudinary.uploader().upload("./Files/" + fileName,
							ObjectUtils.asMap("resource_type", "auto"));
					if (type.equals("video")) {
						firtsIndex = json.lastIndexOf("url=");
						lastIndex = json.indexOf("tags=");
						url = json.substring(firtsIndex + 4, lastIndex - 2);
					} else {
						firtsIndex = json.indexOf("url=");
						lastIndex = json.indexOf("created_at");
						url = json.substring(firtsIndex + 4, lastIndex - 2);
					}
					urlPath.add(url); // lay ra duong dan anh
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return urlPath;
	}

}
