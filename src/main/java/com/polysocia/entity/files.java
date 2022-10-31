package com.polysocia.entity;

import org.springframework.web.multipart.MultipartFile;

public class files {
	private MultipartFile fi;

	public MultipartFile getFile() {
		return fi;
	}

	public void setFile(MultipartFile fi) {
		this.fi = fi;
	}
}
