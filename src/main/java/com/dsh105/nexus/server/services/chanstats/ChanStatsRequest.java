package com.dsh105.nexus.server.services.chanstats;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

public class ChanStatsRequest {

    private String channel;

    private int banned;
    private List<String> bannedUsers;

    private int ops;
    private List<String> chanOps;

    private int voiced;
    private List<String> voicedUsers;

    private int chanCount;

    private String channelTopic;

    public static class Builder {

        private String channel;

        private int banned;
        private List<String> bannedUsers;

        private int ops;
        private List<String> oppedUsers;

        private int voiced;
        private List<String> voicedUsers;

        private int chanCount;

        private String channelTopic;

        public Builder() {}

        public Builder setChannel(final String channel) {
            this.channel = channel;
            return this;
        }

        public Builder withBannedCount(final int banned) {
            this.banned = banned;
            return this;
        }

        public Builder withBannedUsers(final List<String> bans) {
            this.bannedUsers = bans;
            return this;
        }

        public Builder withOpCount(final int ops) {
            this.ops = ops;
            return this;
        }

        public Builder withOppedUsers(final List<String> ops) {
            this.oppedUsers = ops;
            return this;
        }

        public Builder withVoicedCount(final int voiced) {
            this.voiced = voiced;
            return this;
        }

        public Builder withVoicedUsers(final List<String> voiced) {
            this.voicedUsers = voiced;
            return this;
        }

        public Builder withChannelCount(final int chanCount) {
            this.chanCount = chanCount;
            return this;
        }

        public Builder withChannelTopic(final String topic) {
            this.channelTopic = topic;
            return this;
        }

        public ChanStatsRequest build() {
            return new ChanStatsRequest(this);
        }
    }

    private ChanStatsRequest(final Builder builder) {

    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ChanStatsRequest)
            return other.toString().equals(this.toString());
        return false;
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("channel", this.channel);

        json.put("banned_count", this.banned);
        JSONArray bannedUsers = new JSONArray();
        bannedUsers.addAll(this.bannedUsers);
        json.put("banned_users", bannedUsers.toJSONString());

        json.put("ops_count", this.ops);
        JSONArray oppedUsers = new JSONArray();
        oppedUsers.addAll(this.chanOps);
        json.put("opped_users", oppedUsers.toJSONString());

        json.put("voiced_count", this.voiced);
        JSONArray voicedUsers = new JSONArray();
        voicedUsers.addAll(this.voicedUsers);
        json.put("voiced_users", voicedUsers.toJSONString());

        json.put("chan_count", this.chanCount);

        json.put("channel_topic", this.channelTopic);

        return json.toJSONString();
    }
}
