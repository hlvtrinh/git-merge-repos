package org.nibor.git_merge_repos;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.nibor.git_merge_repos.model.MergeModel;
import org.nibor.git_merge_repos.model.RepoItems;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main class for merging repositories via command-line.
 */
public class Main {

	public static void main(String[] args) throws IOException, GitAPIException, URISyntaxException {
		if (ArrayUtils.isEmpty(args)) {
			exitInvalidUsage("JSON input file is missing");
		}

		File input = new File(args[0]);
		if (!input.exists()) {
			exitInvalidUsage("JSON input file is not available");
		}

		List<MergeModel> mergeItems = new ObjectMapper().readValue(FileUtils.openInputStream(input),
				new TypeReference<List<MergeModel>>() {
				});
		if (CollectionUtils.isEmpty(mergeItems)) {
			exitInvalidUsage("Unable to read file or no item to merge");
		}

		mergeItems.parallelStream().forEach(mergeItem -> {
			try {
				mergeReposFolders(mergeItem);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
		});
	}

	private static void mergeReposFolders(MergeModel mergeItem)
			throws IOException, GitAPIException {
		List<RepoItems> repos;
		if (mergeItem == null || CollectionUtils.isEmpty(repos = mergeItem.getRepoItems())
				|| StringUtils.isBlank(mergeItem.getTargetRepo())) {
			System.err.println("Invalid data");
			return;
		}
		List<SubtreeConfig> subtreeConfigs = Collections.synchronizedList(new ArrayList<>());
		repos.parallelStream().forEach(repo -> {
			collectSubTree(mergeItem, subtreeConfigs, repo);
		});

		if (CollectionUtils.isEmpty(subtreeConfigs)) {
			printOut("[%s] Nothing to merge", mergeItem.getTargetRepo());
			return;
		}

		File outputDirectory = new File(mergeItem.getTargetRepo());
		String outputPath = outputDirectory.getAbsolutePath();
		printOut("[%s]Started merging %s repositories into one, output directory: %s",
				mergeItem.getTargetRepo(), String.valueOf(subtreeConfigs.size()), outputPath);

		long start = System.currentTimeMillis();
		RepoMerger merger = new RepoMerger(outputPath, subtreeConfigs);
		List<MergedRef> mergedRefs = merger.run();
		long end = System.currentTimeMillis();

		long timeMs = (end - start);
		printIncompleteRefs(mergedRefs);
		printOut("[%s]Merged repository: '%s' has done, took %sms",
				mergeItem.getTargetRepo(), outputPath, String.valueOf(timeMs));
	}

	private static void collectSubTree(MergeModel mergeItem, List<SubtreeConfig> subtreeConfigs,
			RepoItems repo) {
		List<String> folders = repo.getFolders();
		if (CollectionUtils.isEmpty(folders)) {
			System.err.println(String.format("[%s]Repo %s has no folder",
					mergeItem.getTargetRepo(), repo.getRepoName()));
			return;
		} else {
			repo.getFolders().parallelStream().forEach(folder -> {
				SubtreeConfig config;
				try {
					config = new SubtreeConfig(folder,
							new URIish(new File(repo.getRepoName()).toURI().toURL()));
					subtreeConfigs.add(config);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private static void printIncompleteRefs(List<MergedRef> mergedRefs) {
		for (MergedRef mergedRef : mergedRefs) {
			if (!mergedRef.getConfigsWithoutRef().isEmpty()) {
				printOut("[%s]%s '%s' was not in: %s", mergedRef.getRefType(),
						mergedRef.getRefName(), join(mergedRef.getConfigsWithoutRef()));
			}
		}
	}

	private static String join(Collection<SubtreeConfig> configs) {
		StringBuilder sb = new StringBuilder();
		for (SubtreeConfig config : configs) {
			if (sb.length() != 0) {
				sb.append(", ");
			}
			sb.append(config.getRemoteName());
		}
		return sb.toString();
	}

	private static void exitInvalidUsage(String message) {
		System.err.println(message);
		System.exit(64);
	}

	private static void printOut(String format, Object... args) {
		System.out.println(String.format(format, args));
	}
}
