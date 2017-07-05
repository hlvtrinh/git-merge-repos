package org.nibor.git_merge_repos.model;

import java.util.List;

public class MergeModel {
	private String targetRepo;
	private List<RepoItems> repoItems;

	public String getTargetRepo() {
		return targetRepo;
	}

	public void setTargetRepo(String targetRepo) {
		this.targetRepo = targetRepo;
	}

	public List<RepoItems> getRepoItems() {
		return repoItems;
	}

	public void setRepoItems(List<RepoItems> repoItems) {
		this.repoItems = repoItems;
	}
}
