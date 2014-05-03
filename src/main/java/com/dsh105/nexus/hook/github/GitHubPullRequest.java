/*
 * This file is part of Nexus.
 *
 * Nexus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nexus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nexus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsh105.nexus.hook.github;

import com.google.gson.annotations.SerializedName;

public class GitHubPullRequest extends GitHubIssue {

    @SerializedName("merged")
    private boolean merged;

    @SerializedName("review_comments")
    private int reviewComments;

    @SerializedName("commits")
    private int commits;

    @SerializedName("additions")
    private int additions;

    @SerializedName("deletions")
    private int deletions;

    @SerializedName("changed_files")
    private int changedFiles;

    @SerializedName("merged_at")
    private String mergedAt;

    @SerializedName("merge_commit_sha")
    private String mergeCommit;

    public boolean isMerged() {
        return merged;
    }

    public int getReviewComments() {
        return reviewComments;
    }

    public int getCommits() {
        return commits;
    }

    public int getAdditions() {
        return additions;
    }

    public int getDeletions() {
        return deletions;
    }

    public int getChangedFiles() {
        return changedFiles;
    }

    public String getMergedAt() {
        return mergedAt;
    }

    public String getMergeCommit() {
        return mergeCommit;
    }
}