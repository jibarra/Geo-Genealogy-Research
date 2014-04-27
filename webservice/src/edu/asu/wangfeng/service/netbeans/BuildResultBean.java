/*
 * @author Fang Weng
 * © Arizona State University 2014
 * 
 * Class to store the return bean for services.
 * Stores the return filename(image) and url
 */

package edu.asu.wangfeng.service.netbeans;

public class BuildResultBean {
	private String filename;
	private String url;
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return "BuildResult [filename=" + filename + ", url=" + url + "]";
	}
	
}
