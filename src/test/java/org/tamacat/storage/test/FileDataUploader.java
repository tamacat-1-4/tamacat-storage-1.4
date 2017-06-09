/*
 * Copyright (c) 2015 Tamacat.org
 * All rights reserved.
 */
package org.tamacat.storage.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.tamacat.dao.DaoFactory;
import org.tamacat.dao.exception.DaoException;
import org.tamacat.log.Log;
import org.tamacat.log.LogFactory;
import org.tamacat.storage.StorageEngine;
import org.tamacat.storage.StorageFactory;
import org.tamacat.storage.UploadProgressListener;
import org.tamacat.util.IOUtils;
import org.tamacat.util.StringUtils;
import org.tamacat.util.UniqueCodeGenerator;

public class FileDataUploader {

	static final Log LOG = LogFactory.getLog(FileDataUploader.class);
	protected StorageEngine engine;
	protected String secretKey;
	protected long uploadSizeLimit = 100 * 1024 * 1024;
	
	public FileDataUploader(String appId) {
		engine = StorageFactory.getInstance(appId);
	}
	
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
	public void setUploadSizeLimit(long uploadSizeLimit) {
		this.uploadSizeLimit = uploadSizeLimit;
	}
	
	public Collection<FileData> uploadFiles(HttpServletRequest req, HttpServletResponse resp, String cid)
			throws FileUploadException, IOException {
		UploadProgressListener listener = new UploadProgressListener(resp.getWriter());
		
		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(uploadSizeLimit);
		
		upload.setProgressListener(listener);
		//String path = req.getParameter("path");
		String path = req.getHeader("path");

		Collection<FileData> attachments = new ArrayList<>();
		Map<String, List<FileItem>> fileItems = upload.parseParameterMap(req);
		LOG.debug("path="+path);
		if (StringUtils.isEmpty(path)) {
			throw new FileUploadException("Please select a path.");
		}
		
		if (fileItems == null || fileItems.size() == 0 || fileItems.get("file") == null) {
			throw new FileUploadException("Please upload files.");
		}
		for (FileItem item : fileItems.get("file")) {
			if (item.isFormField()==false) {
				String itemName = item.getName();
				if (StringUtils.isNotEmpty(itemName)) {
					File file = null;
					String id = getFileItemValue(fileItems, "id");
					if (StringUtils.isNotEmpty(id)) {
						String name = getFileItemValue(fileItems, "filename");
						if (StringUtils.isNotEmpty(name)) {
							file = new File(name);
						}
					} else {
						id = UniqueCodeGenerator.generate();
					}
					String fileName = file.getName();
					LOG.debug(id+"="+fileName);
					
					FileData attachment = new FileData();
					if (StringUtils.isNotEmpty(secretKey)) {
						attachment.setSecretKey(secretKey);
						attachment.setEncrypted(true);
					}
					attachment.setFilePath(file.toPath());
					
					attachment.val(FileData.ID, id);
					attachment.val(FileData.CID, cid);
					attachment.val(FileData.FOLDER_ID, path);
					attachment.val(FileData.NAME, fileName);
					attachment.val(FileData.SIZE, item.getSize());
					attachment.val(FileData.CONTENT_TYPE, item.getContentType());
					InputStream stream = item.getInputStream();
					attachment.setInputStream(stream);
					attachment.val(FileData.PATH, id);
					attachment.val(FileData.TRASH, "0");
					attachment.setNew(true);
					attachments.add(upload(attachment));
				}
			} else {
				LOG.debug(item.getFieldName()+"="+item.getString());
			}
		}
		return attachments;
	}

	String getFileItemValue(Map<String, List<FileItem>> fileItems, String key) {
		List<FileItem> filenames = fileItems.get(key);
		if (filenames != null && filenames.size()>0) {
			FileItem name = filenames.get(0);
			if (name != null && name.isFormField()) {
				try {
					return name.getString("UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	public FileData upload(FileData data) {
		FileOutputStream out = null;
		FileDataDao dao = DaoFactory.getDao(FileDataDao.class);
		try {
			dao.createWithFile(data);
		} catch (Exception e) {
			throw new DaoException(e);
		} finally {
			IOUtils.close(out);
			if (dao != null) dao.release();
		}
		return data;
	}

}
