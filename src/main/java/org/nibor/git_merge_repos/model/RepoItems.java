package org.nibor.git_merge_repos.model;

import java.util.List;

public class RepoItems {
	private String repo;
	private List<String> folders;

	public List<String> getFolders() {
		return folders;
	}

	public void setFolders(List<String> folders) {
		this.folders = folders;
	}

	public String getRepo() {
		return repo;
	}

	public void setRepo(String repo) {
		this.repo = repo;
	}
}
