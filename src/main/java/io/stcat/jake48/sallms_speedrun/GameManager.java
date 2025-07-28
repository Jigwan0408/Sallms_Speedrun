package io.stcat.jake48.sallms_speedrun;

import io.stcat.jake48.sallms_speedrun.minigames.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.security.Key;
import java.util.*;

public class GameManager {

    private static final GameManager instance = new GameManager();

    private JavaPlugin plugin; // 플러그인 객체 필드

    private final Map<Integer, MiniGame> gameStages = new HashMap<>(); // 미니게임 단계 목록
    private MiniGame activeMiniGame; // 현재 활성화된 미니게임 객체 필드
    private int currentStage = 0; // 현재 단계를 추적하는 변수
    
    private GameState gameState = GameState.WAITING;
    private GameTimer gameTimer; // 타이머 객체 필드

    private final Set<UUID> players = new HashSet<>(); // Player UUID 저장
    private UUID selectedPlayerUUID; // 선택된 플레이어 UUID 객체 필드

    // 싱글톤 패턴
    private GameManager() {
    }

    public static GameManager getInstance() {
        return instance;
    }

    public void init(JavaPlugin plugin) {
        this.plugin = plugin;
        registerStages();
    }

    public GameState getGameState() {
        return gameState;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public boolean addPlayer(@NotNull Player player) {
        // Set.add()는 실제로 플레이어가 추가되었을 때만 true를 반환
        // 이미 존재한다면 false를 반환
        return players.add(player.getUniqueId());
    }

    public boolean removePlayer(@NotNull Player player) {
        // Set.remove()는 실제로 플레이어가 제거되었을 때만 true를 반환
        // 목록에 없었다면 false를 반환
        return players.remove(player.getUniqueId());
    }

    public UUID getSelectedPlayerUUID() {
        return this.selectedPlayerUUID;
    }

    public MiniGame getActiveMiniGame() {
        return this.activeMiniGame;
    }

    public int getCurrentStage() {
        return this.currentStage;
    }

    public Map<Integer, MiniGame> getGameStages() {
        return this.gameStages;
    }

    // 기본 startGame() 메서드. /sallms start 실행 시 호출
    // 랜덤 플레이어를 뽑아 1단계부터 시작
    public void startGame() {
        startGame(1);
    }

    // 특정 단계부터 시작하는 startGame() 메서드. /sallms start <단계> 실행 시 호출
    // 랜덤 플레이어를 뽑아 지정된 단계부터 시작
    public void startGame(int startStage) {
        if (gameState != GameState.WAITING) {
            Bukkit.broadcast(Component.text("게임이 이미 진행 중입니다.", NamedTextColor.RED));
            return;
        }
        if (players.isEmpty()) {
            Bukkit.broadcast(Component.text("참가자가 없어 게임을 시작할 수 없습니다.",  NamedTextColor.RED));
            return;
        }

        // 랜덤 플레이어 선택
        List<UUID> playerList = new ArrayList<>(players);
        Random random = new Random();
        int randomIndex = random.nextInt(playerList.size());
        selectedPlayerUUID = playerList.get(randomIndex);
        Player selectedPlayer = Bukkit.getPlayer(selectedPlayerUUID);

        if (selectedPlayer != null) {
            startGame(selectedPlayer, startStage);
        } else {
            Bukkit.broadcast(Component.text("선택된 플레이어가 오프라인 상태라 게임을 시작할 수 없습니다.", NamedTextColor.RED));
        }
    }

    // stargGame() 핵심 메서드
    // 특정 플레이어와 특정 단계로 게임을 시작
    public void startGame(Player selectedPlayer, int startStage) {
        // 게임 시작 가능 상태인지 확인
        if (gameState != GameState.WAITING) {
            Bukkit.broadcast(Component.text("게임이 이미 진행 중입니다.", NamedTextColor.RED));
            return;
        }
        if (players.isEmpty()) {
            Bukkit.broadcast(Component.text("참가자가 없어 게임을 시작할 수 없습니다.",  NamedTextColor.RED));
            return;
        }

        // 전달 받은 플레이어를 선택된 플레이어로 설정
        this.selectedPlayerUUID = selectedPlayer.getUniqueId();

        // 검증이 끝나면 게임 상태 변경
        this.gameState = GameState.RUNNING;

        // 플레이어를 서바이벌 모드로 변경
        selectedPlayer.setGameMode(GameMode.SURVIVAL);

        // 스코어보드 정리 및 메시지 전송
        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                ScoreboardManager.clearScoreboard(player);
            }
        }
        Bukkit.broadcast(Component.text("게임을 시작합니다! 선택된 플레이어: " + selectedPlayer.getName(), NamedTextColor.GREEN));

        // 선택된 플레이어를 n단계로 이동 (moveToStage의 성공 여부 확인, 기본값 1)
        boolean setupSuccess = moveToStage(selectedPlayer, startStage);

        // 만약 setup이 실패했다면, startGame 메서드 종료
        if (!setupSuccess) {
            return;
        }

        // 타이머 시작
        gameTimer = new GameTimer();
        gameTimer.runTaskTimer(plugin, 0L, 1L);
    }

    // 미니게임 등록 메서드
    private void registerStages() {
        // 1 스테이지 (블록 캐기)
        gameStages.put(1, new BlockBreak(this.plugin));

        // 2 스테이지 (농사 게임)
        gameStages.put(2, new FarmingGame(this.plugin));

         // 3 스테이지 (점프맵)
        gameStages.put(3, new JumpGame(this.plugin));

        // 4 스테이지 (건축)
        gameStages.put(4, new BuildGame(this.plugin));
    }

    // 다음 단계로 넘어가는 메서드
    public void advanceToNextStage(Player player) {
        // TODO: 마지막 단계인지 확인하는 로직

        moveToStage(player, (this.currentStage + 1));
    }

    // 특정 단계로 텔레포트 하는 메서드
    private boolean moveToStage(Player player, int stageNum) {
        // 다음 단께로 가기 전, 현재 진행 중인 미니게임이 있다면 정리
        if (activeMiniGame != null) {
            activeMiniGame.cleanup();
        }

        Sallms_Speedrun mainPlugin = (Sallms_Speedrun) plugin;
        Location stageLoc = mainPlugin.getStageTeleportLocation(stageNum);

        if (stageLoc == null) {
            player.sendMessage(Component.text(stageNum + "단계의 위치를 찾을 수 없습니다! 관리자에게 문의하세요.", NamedTextColor.RED));
            stopGame();
            return false;
        }

        this.currentStage = stageNum;
        player.sendMessage(Component.text(stageNum + "단계로 이동합니다!", NamedTextColor.GREEN));
        player.teleport(stageLoc);

        // 다음 단계의 MiniGame 객체 가져옴
        this.activeMiniGame = gameStages.get(stageNum);

        plugin.getLogger().info("[DEBUG] moveToStage for stage " + stageNum + ". activeMiniGame is: " + activeMiniGame);

        // 해당 객체가 존재하면 setup 메서드 호출
        if (activeMiniGame != null) {
            boolean success = activeMiniGame.setup(player, stageLoc);
            if (!success) {
                return false; // setup이 실패하면, 이 메서드도 '실패' 반환
            }
        }
        return true; // 성공
    }

    // 게임 종료
    public void stopGame() {
        // 게임이 이미 대기 상태면 아무것도 하지 않음
        if (gameState != GameState.RUNNING) {
            return;
        }

        // 게임 상태를 대기 중으로 변경
        this.gameState = GameState.WAITING;

        // 현재 진행 중인 미니게임 정리 메서드 호출
        if (activeMiniGame != null) {
            activeMiniGame.cleanup(); // 미니게임 복원
            activeMiniGame = null;    // 다음 게임을 위해 미니게임 객체 초기화
        }

        // 타이머 중지 및 최종 기록 계산
        String finalTime = "기록 없음";
        if (gameTimer != null) {
            gameTimer.cancel();
            finalTime = gameTimer.getFinalTime();
        }

        // 결과 메시지 및 스코어보드 표시
        Component message = Component.text("게임이 종료되었습니다. 최종 기록: " + finalTime, NamedTextColor.WHITE);
        Bukkit.broadcast(message);

        Sallms_Speedrun mainPlugin = (Sallms_Speedrun) this.plugin;
        Location spawnLoc = mainPlugin.getStageLocation(0); // 0번 스테이지(스폰)

        // 스폰 지점(0단계)로 모든 플레이어 텔레포트
        if (spawnLoc != null) {
            for (UUID playerUUID : players) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null) {
                    ScoreboardManager.displayFinalScore(player, finalTime);
                    player.teleport(spawnLoc); // 스폰 위치로 텔레포트
                }
            }
        } else {
            // 스폰 위치를 못 찾았을 때
            this.plugin.getLogger().severe("Failed to return the player because the spawn location (Stage 0) could not be found.");
        }

        // 선택된 플레이어 핫바 초기화
        if (selectedPlayerUUID != null) {
            Player player = Bukkit.getPlayer(selectedPlayerUUID);
            if (player != null) {
                for (int i = 0; i < 9; i++) {
                    player.getInventory().clear(i);
                }
                player.getInventory().setItemInOffHand(null);
            }
        }
        
        // 선택되었던 플레이어 정보 초기화
        this.selectedPlayerUUID = null;

    }


}
