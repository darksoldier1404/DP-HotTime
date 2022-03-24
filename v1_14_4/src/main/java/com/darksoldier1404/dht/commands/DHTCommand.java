package com.darksoldier1404.dht.commands;

import com.darksoldier1404.dht.HotTime;
import com.darksoldier1404.dht.functions.DHTFunction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class DHTCommand implements CommandExecutor, TabCompleter {
    private final HotTime plugin = HotTime.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender.isOp()) {
                sender.sendMessage(plugin.prefix + "/핫타임 생성 <이름> <F/P> <시간>");
                sender.sendMessage("F = Fixed (고정 시간)");
                sender.sendMessage("P = Periodic (주기적인 시간, 생성된 또는 활성화한 시점부터)");
                sender.sendMessage("[ 시간 설정 - Fixed ]");
                sender.sendMessage("00:00 = 자정");
                sender.sendMessage("12:00 = 점심");
                sender.sendMessage("22:00 = 오후 10시");
                sender.sendMessage("[ 시간 설정 - Periodic ]");
                sender.sendMessage("03:00 = 3시간 마다");
                sender.sendMessage("12:00 = 12시간 마다");
                sender.sendMessage(plugin.prefix + "/핫타임 보상 <이름> - 보상을 설정합니다.");
                sender.sendMessage(plugin.prefix + "/핫타임 스위치 <이름> - 토글 형식입니다. (핫타임을 활성화/비활성화)");
                sender.sendMessage(plugin.prefix + "/핫타임 시간 <이름> <P/F> <시간> - 설정된 시간을 변경합니다.");
                sender.sendMessage(plugin.prefix + "/핫타임 보관함 <줄수> - 핫타임 보상 아이템을 보관할 보관함의 크기를 설정합니다.");
                sender.sendMessage(plugin.prefix + "/핫타임 삭제 <이름> - 해당 핫타임을 삭제합니다.");
                sender.sendMessage(plugin.prefix + "/핫타임 목록 - 핫타임 목록을 표시합니다.");
            }
            sender.sendMessage("/핫타임 보관함 - 보관함을 엽니다.");
            return false;
        }
        if (args[0].equals("생성")) {
            if (!sender.isOp()) {
                sender.sendMessage(plugin.prefix + "권한이 없습니다.");
                return false;
            }
            if (args.length == 1) {
                sender.sendMessage(plugin.prefix + "생성할 핫타임 이름을 입력해주세요.");
                return false;
            }
            if (args.length == 2) {
                sender.sendMessage(plugin.prefix + "핫타임의 타입을 입력해주세요.");
                return false;
            }
            if (args.length == 3) {
                sender.sendMessage(plugin.prefix + "핫타임의 시간을 입력해주세요.");
                return false;
            }
            if (args.length == 4) {
                DHTFunction.createHotTime(sender, args[1], args[2], args[3]);
                return false;
            }
        }
        if (args[0].equals("시간")) {
            if (!sender.isOp()) {
                sender.sendMessage(plugin.prefix + "권한이 없습니다.");
                return false;
            }
            if (args.length == 1) {
                sender.sendMessage(plugin.prefix + "수정할 핫타임 이름을 입력해주세요.");
                return false;
            }
            if (args.length == 2) {
                sender.sendMessage(plugin.prefix + "핫타임의 타입을 입력해주세요.");
                return false;
            }
            if (args.length == 3) {
                sender.sendMessage(plugin.prefix + "핫타임의 시간을 입력해주세요.");
                return false;
            }
            if (args.length == 4) {
                DHTFunction.changeTime(sender, args[1], args[2], args[3]);
                return false;
            }
        }
        if (args[0].equals("스위치")) {
            if (!sender.isOp()) {
                sender.sendMessage(plugin.prefix + "권한이 없습니다.");
                return false;
            }
            if (args.length == 1) {
                sender.sendMessage(plugin.prefix + "스위치할 핫타임 이름을 입력해주세요.");
                return false;
            }
            DHTFunction.switchHotTime(sender, args[1]);
            return false;
        }
        if (args[0].equals("보상")) {
            if (!sender.isOp()) {
                sender.sendMessage(plugin.prefix + "권한이 없습니다.");
                return false;
            }
            if (args.length == 1) {
                sender.sendMessage(plugin.prefix + "보상을 설정할 핫타임 이름을 입력해주세요.");
                return false;
            }
            if (args.length == 2) {
                DHTFunction.openRewardSettings(sender, args[1]);
                return false;
            }
        }
        if (args[0].equals("보관함")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.prefix + "인게임에서만 사용할 수 있습니다.");
                return false;
            }
            if (args.length == 1) {
                DHTFunction.openRewardStorage((Player) sender);
                return false;
            }
            if (args.length == 2) {
                if (sender.isOp()) {
                    DHTFunction.changeStorageLine(sender, args[1]);
                    return false;
                }else{
                    sender.sendMessage(plugin.prefix + "권한이 없습니다.");
                    return false;
                }
            }
        }
        if(args[0].equals("삭제")) {
            if(!sender.isOp()) {
                sender.sendMessage(plugin.prefix + "권한이 없습니다.");
                return false;
            }
            DHTFunction.deleteHotTime(sender, args[1]);
            return false;
        }
        if(args[0].equals("목록")) {
            if(!sender.isOp()) {
                sender.sendMessage(plugin.prefix + "권한이 없습니다.");
                return false;
            }
            DHTFunction.listHotTimes(sender);
            return false;
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender.isOp()) {
            if(args.length == 1) {
                return Arrays.asList("생성", "보상", "스위치", "시간", "보관함", "삭제", "목록");
            }
            if(!args[0].equals("보관함") || !args[0].equals("목록")) {
                return plugin.hottimes.keySet().stream().collect(Collectors.toList());
            }
        }else{
            if(args.length == 1) {
                return Arrays.asList("보관함");
            }
        }
        return null;
    }
}
