package io.stcat.jake48.sallms_speedrun.minigames;

import org.bukkit.Material;

// 건축물을 구성하는 각 블록의 상대 좌표와 타입을 저장하는 데이터 객체
public record RelativeBlock(int x, int y, int z, Material type) {
}
