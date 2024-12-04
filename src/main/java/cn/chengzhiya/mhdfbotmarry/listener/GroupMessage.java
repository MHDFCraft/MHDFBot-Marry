package cn.chengzhiya.mhdfbotmarry.listener;

import cn.chengzhiya.mhdfbot.api.MHDFBot;
import cn.chengzhiya.mhdfbot.api.builder.MessageBuilder;
import cn.chengzhiya.mhdfbot.api.entity.user.Member;
import cn.chengzhiya.mhdfbot.api.event.message.GroupMessageEvent;
import cn.chengzhiya.mhdfbot.api.listener.EventHandler;
import cn.chengzhiya.mhdfbot.api.listener.Listener;
import cn.chengzhiya.mhdfbotmarry.entity.Marry;
import cn.chengzhiya.mhdfbotmarry.main;
import cn.chengzhiya.mhdfbotmarry.util.DatabaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.chengzhiya.mhdfbotmarry.util.LangUtil.i18n;

public final class GroupMessage implements Listener {
    private boolean chance(double chance) {
        return Math.random() * 100.0 < chance;
    }

    /**
     * 获取指定群聊的群成员用户列表
     *
     * @param group 目标群号
     * @return 群成员用户列表
     */
    private List<Long> getMemberList(Long group) {
        List<Long> memberList = new ArrayList<>();
        for (Member member : MHDFBot.getGroupMemberList(group, false)) {
            memberList.add(member.getId());
        }
        return memberList;
    }

    @EventHandler
    public void onGroupMessage(GroupMessageEvent event) {
        if (!main.instance.getConfig().getStringList("allowUseGroup").contains(String.valueOf(event.getGroupId()))) {
            return;
        }
        if (!event.getMessage().startsWith("#")) {
            return;
        }

        String[] args = event.getMessage().split(" ");
        MessageBuilder messageBuilder = MessageBuilder.builder();

        if (main.instance.getConfig().getBoolean("messageSettings.reply")) {
            messageBuilder.reply(event.getMessageId());
        }
        if (main.instance.getConfig().getBoolean("messageSettings.at")) {
            messageBuilder.at(event.getSender().getUserId()).text("\n");
        }

        switch (args[0]) {
            case "#群老婆" -> {
                Marry marry = DatabaseUtil.getMarry(event.getGroupId(), event.getSender().getUserId());

                if (marry != null) {
                    if (Objects.equals(marry.getMr(), event.getSender().getUserId())) {
                        messageBuilder.text(i18n("marry.isMr")
                                .replace("{atMrs}", MessageBuilder.builder().at(marry.getMrs()).build()));
                    } else {
                        messageBuilder.text(i18n("marry.isMrs")
                                .replace("{atMr}", MessageBuilder.builder().at(marry.getMr()).build()));
                    }
                    break;
                }

                if (main.instance.getConfig().getBoolean("marrySettings.dog") && chance(main.instance.getConfig().getDouble("marrySettings.dogChange"))) {
                    DatabaseUtil.setRole(event.getSender().getUserId(), "Dog");
                }
                if (main.instance.getConfig().getBoolean("marrySettings.optionalWife") && chance(main.instance.getConfig().getDouble("marrySettings.optionalWifeChange"))) {
                    DatabaseUtil.setRole(event.getSender().getUserId(), "OptionalWife");
                }

                String role = DatabaseUtil.getRole(event.getSender().getUserId());
                if (role != null) {
                    if (role.equals("Dog")) {
                        messageBuilder.text(i18n("marry.isDog"));
                        break;
                    }
                    if (role.equals("OptionalWife")) {
                        messageBuilder.text(i18n("marry.canOptionalWife"));
                        break;
                    }
                }

                List<Long> allowMarryList = getMemberList(event.getGroupId());
                allowMarryList.removeAll(DatabaseUtil.getMarryList(event.getGroupId()));

                Random random = new Random();
                Long mrs = allowMarryList.get(random.nextInt(allowMarryList.size()));

                DatabaseUtil.setMarry(event.getGroupId(), new Marry(event.getSender().getUserId(), mrs));
                messageBuilder.text(i18n("marry.isMr").replaceAll("\\{atMrs}", MessageBuilder.builder().at(mrs).build()));
            }
            case "#选老婆" -> {
                if (!main.instance.getConfig().getBoolean("marrySettings.optionalWife")) {
                    return;
                }

                String role = DatabaseUtil.getRole(event.getSender().getUserId());
                Marry marry = DatabaseUtil.getMarry(event.getGroupId(), event.getSender().getUserId());

                if (role == null) {
                    messageBuilder.text(i18n("marry.cantOptionalWife"));
                    break;
                }

                if (!role.equals("OptionalWife")) {
                    messageBuilder.text(i18n("marry.cantOptionalWife"));
                    break;
                }

                if (marry != null) {
                    messageBuilder.text(i18n("marry.havaWife"));
                    break;
                }

                if (args.length == 1) {
                    messageBuilder.text(i18n("marry.optionalWife"));
                    break;
                }

                Pattern pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)(?:,name=[^]]+)?]");
                Matcher matcher = pattern.matcher(event.getMessage());

                if (!matcher.find()) {
                    messageBuilder.text(i18n("marry.optionalWife"));
                    break;
                }

                Long mrs = Long.parseLong(matcher.group(1));
                if (DatabaseUtil.getMarry(event.getGroupId(), mrs) != null) {
                    messageBuilder.text(i18n("marry.isMarry"));
                }

                DatabaseUtil.setMarry(event.getGroupId(), new Marry(event.getSender().getUserId(), mrs));
                messageBuilder.text(i18n("marry.isMr").replaceAll("\\{atMrs}", MessageBuilder.builder().at(mrs).build()));
            }
            case "#换老婆" -> {
                if (!main.instance.getConfig().getBoolean("marrySettings.changeWife")) {
                    return;
                }

                Marry marry = DatabaseUtil.getMarry(event.getGroupId(), event.getSender().getUserId());
                if (marry == null) {
                    messageBuilder.text(i18n("marry.notHaveWife"));
                    break;
                }

                if (!Objects.equals(marry.getMr(), event.getSender().getUserId())) {
                    messageBuilder.text(i18n("marry.notHaveWife"));
                    break;
                }

                if (DatabaseUtil.getChangeWifeTimes(event.getGroupId(), event.getSender().getUserId()) >= main.instance.getConfig().getInt("marrySettings.maxChangeTimes")) {
                    messageBuilder.text(i18n("marry.changeWifeMax"));
                    break;
                }

                List<Long> allowMarryList = getMemberList(event.getGroupId());
                allowMarryList.removeAll(DatabaseUtil.getMarryList(event.getGroupId()));

                Random random = new Random();
                Long mrs = allowMarryList.get(random.nextInt(allowMarryList.size()));

                marry.setMrs(mrs);
                DatabaseUtil.changeMarry(event.getGroupId(), marry);
                DatabaseUtil.addChangeWifeTimes(event.getGroupId(), event.getSender().getUserId());
                messageBuilder.text(i18n("marry.isMr").replaceAll("\\{atMrs}", MessageBuilder.builder().at(mrs).build()));
            }
            default -> {
                return;
            }
        }
        MHDFBot.sendGroupMsg(event.getGroupId(), messageBuilder.build());
    }
}
