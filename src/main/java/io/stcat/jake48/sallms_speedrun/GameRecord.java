package io.stcat.jake48.sallms_speedrun;

import java.util.UUID;

public record GameRecord(String playerName, UUID playerUUID, long timeMillis) {
}
