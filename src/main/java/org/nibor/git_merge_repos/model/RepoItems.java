package org.nibor.git_merge_repos.model;

import java.util.List;

public class RepoItems {
	private String repoName;
	private List<String> folders;

	public List<String> getFolders() {
		return folders;
	}

	public void setFolders(List<String> folders) {
		this.folders = folders;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}
}
