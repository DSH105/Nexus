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

package com.dsh105.nexus.hook.travis;

import com.google.gson.annotations.SerializedName;

public class TravisRepo {

    @SerializedName("id")
    private int id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("description")
    private String description;

    @SerializedName("last_build_id")
    private int lastBuildId;

    @SerializedName("last_build_number")
    private int lastBuildNumber;

    @SerializedName("last_build_state")
    private String last_build_state;

    @SerializedName("last_build_duration")
    private int lastBuildDuration;

    @SerializedName("last_build_started_at")
    private String dateLastBuildStartedAt;

    @SerializedName("last_build_finished_at")
    private String dateLastBuildFinishedAt;
}